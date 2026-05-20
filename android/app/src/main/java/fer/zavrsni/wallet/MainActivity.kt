package fer.zavrsni.wallet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
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
import fer.zavrsni.wallet.storage.PidStorage
import fer.zavrsni.wallet.ui.theme.WalletTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

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
            val keystore = KeystoreManager()

            if (keystore.keyExists()) {
                Log.d(TAG, "Kljuc vec postoji")
                keystore.deleteKey()
            }

            Log.d(TAG, "Generiram novi par kljuceva")
            val holderJwk = keystore.generateKeyPair()
            Log.d(TAG, "JWK: $holderJwk")

            Log.d(TAG, "POST /issuer/issue za OIB $TEST_OIB...")

            val issueResponse = ApiClient.walletApi.issuePid(
                IssueRequest(
                    oib = TEST_OIB,
                    holderPublicJwk = holderJwk
                )
            )

            Log.d(TAG, "Dobiven SD-JWT VC (duljina ${issueResponse.sdJwtVc.length})")
            Log.d(TAG, "Prvih 80 znakova: ${issueResponse.sdJwtVc}...")

            val storage = PidStorage(this@MainActivity)

            storage.savePid(issueResponse.sdJwtVc)
            Log.d(TAG, "PID pohranjen u EncryptedSharedPreferences")

            val loadedPid = storage.loadPid()
            val isto = loadedPid == issueResponse.sdJwtVc
            Log.d(TAG, "Procitano isto sto je pohranjeno: $isto")

            Log.d(TAG, "RADI")

        } catch (e: Exception) {
            Log.e(TAG, "Greska u flow-u: ${e.message}", e)
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