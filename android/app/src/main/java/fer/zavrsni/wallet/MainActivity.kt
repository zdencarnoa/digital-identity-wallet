package fer.zavrsni.wallet

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import fer.zavrsni.wallet.crypto.KeystoreManager
import fer.zavrsni.wallet.network.ApiClient
import fer.zavrsni.wallet.network.dto.IssueRequest
import fer.zavrsni.wallet.presentation.SdJwtParser
import fer.zavrsni.wallet.storage.PidStorage
import fer.zavrsni.wallet.ui.theme.WalletTheme
import fer.zavrsni.wallet.network.dto.VerifyRequest
import fer.zavrsni.wallet.presentation.BiometricAuth
import fer.zavrsni.wallet.presentation.PresentationBuilder
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    companion object {
        private const val TAG = "WalletTest"
        private const val TEST_OIB = "12345678901"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            runFullFlow()
        }

        setContent {
            WalletTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private suspend fun runFullFlow() {
        try {
            // Stvaranje keymanagera za keystore i generiranje kljuceva
            val keystore = KeystoreManager()

            if (keystore.keyExists()) {
                Log.d(TAG, "Kljuc vec postoji...")
                keystore.deleteKey()
            }

            Log.d(TAG, "Generiranje para kljuceva")
            val holderJwk = keystore.generateKeyPair()
            Log.d(TAG, "JWK: $holderJwk")

            // Izdavanje PID-a
            Log.d(TAG, "POST /issuer/issue za OIB $TEST_OIB...")
            val issueResponse = ApiClient.walletApi.issuePid(
                IssueRequest(oib = TEST_OIB, holderPublicJwk = holderJwk)
            )
            Log.d(TAG, "Dobiven SD-JWT VC (duljina ${issueResponse.sdJwtVc.length})")

            // Pohrana PID-a u sustav
            val storage = PidStorage(this@MainActivity)
            storage.savePid(issueResponse.sdJwtVc)
            Log.d(TAG, "PID pohranjen")

            // Provjera s javnim kljucem issuera
            Log.d(TAG, "Dohvacam issuer javni kljuc...")
            val issuerJwk = ApiClient.walletApi.getIssuerPublicKey()
            val check = SdJwtParser.verifyAndLog(issueResponse.sdJwtVc, issuerJwk)
            if (!check) {
                Log.e(TAG, "Fail 1 - issuer javni kljuc")
                return
            }

            Log.d(TAG, "Trazim biometrijsku autentikaciju")
            try {
                BiometricAuth.authenticate(
                    activity = this@MainActivity,
                    title = "Prezentacija identiteta",
                    subtitle = "Potvrdite za potpisivanje prezentacije"
                )
                Log.d(TAG, "Autentikacija uspjesna - mogu potpisati")
            } catch (e: BiometricAuth.BiometricCancelledException) {
                Log.w(TAG, "Korisnik otkazao, prekidam")
                return
            } catch (e: BiometricAuth.BiometricFailedException) {
                Log.e(TAG, "Autentikacija pala: ${e.message}")
                return
            }

            // Provjera potpisa kroz keystore
            val signingOk = SdJwtParser.testKeystoreSigning(keystore)
            if (!signingOk) {
                Log.e(TAG, "Fail 2 - keystore potpis pao")
                return
            }

            // Challenge verifiera
            Log.d(TAG, "POST /verifier/challenge...")
            val challenge = ApiClient.walletApi.createChallenge()
            Log.d(TAG, "Challenge: session=${challenge.sessionId}, nonce=${challenge.nonce}")

            //Autentikacija
            Log.d(TAG, "Trazim biometrijsku autentikaciju...")
            try {
                BiometricAuth.authenticate(
                    activity = this@MainActivity,
                    title = "Prezentacija identiteta",
                    subtitle = "Potvrdite za potpisivanje prezentacije"
                )
                Log.d(TAG, "Autentikacija uspjesna - mogu potpisati")
            } catch (e: BiometricAuth.BiometricCancelledException) {
                Log.w(TAG, "Korisnik otkazao - prekidam flow")
                return
            } catch (e: BiometricAuth.BiometricFailedException) {
                Log.e(TAG, "Autentikacija pala: ${e.message}")
                return
            }

            // Kreiranje prezentacije za verifikaciju
            Log.d(TAG, "Slazem prezentaciju (samo birth_date)...")
            val storedPid = storage.loadPid() ?: error("Fail 3 - PID nije pohranjen")
            val presentation = PresentationBuilder(keystore).build(
                storedSdJwt = storedPid,
                attributesToDisclose = setOf("birth_date"),
                verifierAudience = challenge.audience,
                nonce = challenge.nonce,
            )

            // Slanje prezentacije na verifikaciju
            Log.d(TAG, "POST /verifier/verify...")
            val verifyResponse = ApiClient.walletApi.verify(
                VerifyRequest(
                    sessionId = challenge.sessionId,
                    presentation = presentation,
                )
            )

            Log.d(TAG, "PRIHVACENA PREZENTACIJA")
            Log.d(TAG, "Otkriveni atributi:")
            verifyResponse.verifiedClaims.forEach { (key, value) ->
                Log.d(TAG, "  $key = $value")
            }

            Log.d(TAG, "RADI!")
        } catch (e: Exception) {
            Log.e(TAG, "Greska: ${e.message}", e)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WalletTheme {
        Greeting("Android")
    }
}