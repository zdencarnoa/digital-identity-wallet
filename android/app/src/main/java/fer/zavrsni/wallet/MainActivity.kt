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
import fer.zavrsni.wallet.crypto.KeystoreManager
import fer.zavrsni.wallet.ui.theme.WalletTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "WalletTest"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        testKeystore()

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

    private fun testKeystore() {
        val keystore = KeystoreManager()

        if (keystore.keyExists()){
            Log.d(TAG, "Kljuc vec postoji, obrisi")
            keystore.deleteKey()
        }

        Log.d(TAG, "Generiram novi par kljuceva")
        val jwk = keystore.generateKeyPair()

        Log.d(TAG, "JWK generiran:")
        Log.d(TAG, "  kty = ${jwk["kty"]}")
        Log.d(TAG, "  crv = ${jwk["crv"]}")
        Log.d(TAG, "  x   = ${jwk["x"]}")
        Log.d(TAG, "  y   = ${jwk["y"]}")

        val testData = "podatci koji se potpisuju".toByteArray()
        val signature = keystore.signData(testData)
        Log.d(TAG, "Potpis dobiven, duljina je ${signature.size} bajtova")

        val jwkPonovo = keystore.publicKeyAsJwk()
        val isto = jwkPonovo["x"] == jwk["x"] && jwkPonovo["y"] == jwk["y"]
        Log.d(TAG, "Ponovno citanje vraca isti kljuc: $isto")
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