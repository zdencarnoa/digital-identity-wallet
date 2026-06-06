package fer.zavrsni.wallet

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import fer.zavrsni.wallet.crypto.KeystoreManager
import fer.zavrsni.wallet.network.ApiClient
import fer.zavrsni.wallet.network.dto.IssueRequest
import fer.zavrsni.wallet.network.dto.VerifyRequest
import fer.zavrsni.wallet.presentation.BiometricAuth
import fer.zavrsni.wallet.presentation.PresentationBuilder
import fer.zavrsni.wallet.storage.PidStorage
import fer.zavrsni.wallet.ui.theme.WalletTheme
import kotlinx.coroutines.launch

/**
  Glavni Activity demo wallet-a.
  Automatski se pokrece pri pokretanju aplikacije
 */
class MainActivity : FragmentActivity() {

    companion object {
        private const val TAG = "Wallet"
        private const val DEMO_OIB = "12345678901"
        private const val DISCLOSE_FOR_DEMO = "birth_date"
    }

    private lateinit var keystore: KeystoreManager
    private lateinit var storage: PidStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        keystore = KeystoreManager()
        storage = PidStorage(this)

        lifecycleScope.launch {
            runDemo()
        }

        setContent {
            WalletTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(name = "Wallet", modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    /**
       1. Ako wallet jos nema PID, dohvati ga
       2. Pripremi prezentaciju za demo verifier sa selektivnim otkrivanjem
          i posalji ju na verifikaciju

     Pri svakom pokusaju pristupa privatnom kljucu zahtjeva se biometrijska autentikacija ili PIN
     */
    private suspend fun runDemo() {
        try {
            if (!storage.hasPid()) {
                requestPid()
            } else {
                Log.d(TAG, "PID vec postoji u walletu, preskacem zahtjev za PID-om")
            }

            presentToVerifier(disclose = setOf(DISCLOSE_FOR_DEMO))

        } catch (e: Exception) {
            Log.e(TAG, "Greska u demonstraciji: ${e.message}", e)
        }
    }

    /**
     Zahtjev za PID-om generira novi kljucni par u Keystore-u, salje javni dio
     issuer-u i prima SD-JWT VC koji se pohranjuje lokalno
     **/
    private suspend fun requestPid() {
        Log.d(TAG, "Pokrecem zahtjev za PID-om")

        // Ako se u storageu nalazi kljuc bez PID-a, obrisi ga
        if (keystore.keyExists()) {
            keystore.deleteKey()
        }

        val holderJwk = keystore.generateKeyPair()
        Log.d(TAG, "Generiran novi par kljuceva u Keystore-u")

        val response = ApiClient.walletApi.issuePid(IssueRequest(oib = DEMO_OIB, holderPublicJwk = holderJwk))

        storage.savePid(response.sdJwtVc)
        Log.d(TAG, "PID dohvacen i pohranjen (${response.sdJwtVc.length} znakova)")
    }

    /**
     Prezentacija PID-a verifier-u
     1. Zatrazi nonce od verifier-a
     2. Trazi biometrijsku autentikaciju korisnika
     3. Slaze SD-JWT prezentaciju s odabranim atributima i KB-JWT-om
     4. Salje prezentaciju verifier-u i prikazuje verificirane atribute
     */
    private suspend fun presentToVerifier(disclose: Set<String>) {
        Log.d(TAG, "Pokrecem prezentaciju za atribute: $disclose")

        val storedPid = storage.loadPid()
            ?: error("PID nije pohranjen, prvo treba zatraziti PID")

        // 1. Challenge
        val challenge = ApiClient.walletApi.createChallenge()
        Log.d(TAG, "Primljen challenge od verifier-a")

        // 2. Biometrijska autentikacija prije koristenja kljuca
        if (!authenticate()) {
            return
        }

        // 3. Slaganje prezentacije (KB-JWT potpis se obavlja preko Keystore-a, nema direktnog pristupa kljucu)
        val presentation = PresentationBuilder(keystore).build(
            storedSdJwt = storedPid,
            attributesToDisclose = disclose,
            verifierAudience = challenge.audience,
            nonce = challenge.nonce,
        )

        // 4. Slanje prezentacije na verifikaciju
        val response = ApiClient.walletApi.verify(
            VerifyRequest(sessionId = challenge.sessionId, presentation = presentation)
        )

        Log.d(TAG, "Verifier prihvatio prezentaciju. Selektivno otkriveni atributi:")
        response.verifiedClaims.forEach { (key, value) ->
            Log.d(TAG, "  $key = $value")
        }
    }

    /**
     Trazi od korisnika biometrijsku ili PIN autentikaciju.
     Vraca true ako je uspjesna, false ako je korisnik otkazao ili ako je autentikacija neuspjesna.
     */
    private suspend fun authenticate(): Boolean {
        return try {
            BiometricAuth.authenticate(
                activity = this,
                title = "Prezentacija identiteta",
                subtitle = "Potvrdite za potpisivanje prezentacije"
            )
            true
        } catch (e: BiometricAuth.BiometricCancelledException) {
            Log.w(TAG, "Korisnik je otkazao autentikaciju")
            false
        } catch (e: BiometricAuth.BiometricFailedException) {
            Log.e(TAG, "Autentikacija neuspjesna: ${e.message}")
            false
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WalletTheme {
        Greeting("Android")
    }
}