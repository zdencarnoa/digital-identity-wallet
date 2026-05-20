package fer.zavrsni.wallet.presentation

import android.util.Base64
import android.util.Log
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import fer.zavrsni.wallet.crypto.KeystoreManager
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.json.JsonObject
import java.security.interfaces.ECPrivateKey


object SdJwtParser {

    private const val TAG = "SdJwtParser"

    fun testKeystoreSigning(keystoreManager: KeystoreManager): Boolean {
        return try{
            Log.d(TAG, "Test potpisivanja kroz keystore")

            val signer = KeystoreSigner(keystoreManager)

            val header = JWSHeader.Builder(JWSAlgorithm.ES256).type(JOSEObjectType("kb+jwt")).build()

            val claims = JWTClaimsSet.Builder().audience("https://test-verifier.fer.hr").claim("nonce", "test-nonce-12345").issueTime(java.util.Date()).build()

            val signedJwt = SignedJWT(header, claims)
            signedJwt.sign(signer)

            Log.d(TAG, "Potpis uspjesno generiran")
            Log.d(TAG, "Potpisani JWT: ${signedJwt.serialize()}")
            Log.d(TAG, "Keystore potpisivanje radi")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Neuspjesno potpisivanje: ${e.message}", e)
            false
        }
    }

    fun verifyAndLog(sdJwtString: String, issuerPublicJwk: JsonObject): Boolean {
        return try{
            val parts = sdJwtString.split("~")
            val jwtPart = parts[0]
            val disclosures = parts.drop(1).filter { it.isNotEmpty() }

            Log.d(TAG, "SD-JWT")
            Log.d(TAG, "JWT dio duljina: ${jwtPart.length}")
            Log.d(TAG, "Broj disclosure-a: ${disclosures.size}")

            Log.d(TAG, "JWT dio prije parsiranja: $jwtPart")
            Log.d(TAG, "Broj tocaka u JWT-u: ${jwtPart.count { it == '.' }}")

            val signedJwt = SignedJWT.parse(jwtPart)

            val ecKey = ECKey.parse(issuerPublicJwk.toString())
            val verifier = ECDSAVerifier(ecKey)

            val signatureValid = signedJwt.verify(verifier)

            Log.d(TAG, "Potpis issuera valjan $signatureValid")

            if(!signatureValid){
                Log.e(TAG, "neuspjesna verifikacija, nevaljan potpis")
                return false
            }

            val claims = signedJwt.jwtClaimsSet
            Log.d(TAG, "JWT claims")

            claims.claims.forEach { (key, value) ->
                Log.d(TAG, "  $key = $value")
            }

            Log.d(TAG, "Selective disclosure atributi")

            disclosures.forEach { disclosure ->
                val decoded = decodeDisclosure(disclosure)
                Log.d(TAG, "  $decoded")
            }

            true
        } catch (e: Exception){
            Log.e(TAG, "Neuspjeh ${e.message}")
            false
        }
    }

    private fun decodeDisclosure(disclosure: String): String {
        val decoded = Base64.decode(
            disclosure,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )

        return String(decoded, Charsets.UTF_8)
    }
}