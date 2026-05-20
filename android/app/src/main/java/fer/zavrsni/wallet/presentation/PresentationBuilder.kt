package fer.zavrsni.wallet.presentation

import android.util.Base64
import android.util.Log
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import fer.zavrsni.wallet.crypto.KeystoreManager
import java.security.MessageDigest
import java.util.Date

/*
Zadaca ove klase je izgradnja prezentacije u tocnom formatu u kojem ce ju verifier na backendu moci verificirati.
To znaci da iz pohranjenog PID-a mora sloziti prezentaciju oblika issuerJwt~atribut1~atribut2~...~ i od toga napraviti sazetak.
Na kraj sazetka dodaje kb-jwt potpisan privatnim kljucem koji se nalazi unutar keystore sustava. Takva prezentacija salje se verifieru na provjeru.
*/

class PresentationBuilder(private val keystoreManager: KeystoreManager){

    companion object{
        private const val TAG = "PresentationBuilder"
    }

    fun build(
        storedSdJwt: String,
        attributesToDisclose: Set<String>,
        verifierAudience: String,
        nonce: String
    ): String {
        Log.d(TAG, "Izgradnja prezentacije")
        Log.d(TAG, "Atributi za otkriti $attributesToDisclose")

        val parts = storedSdJwt.split("~")
        val issuerJwt = parts[0]
        val allDisclosures = parts.drop(1).filter { it.isNotEmpty() }

        Log.d(TAG, "Ukupno atributa ${allDisclosures.size}")

        val selectedDisclosures = allDisclosures.filter { disclosure ->
            val parsed = parseDisclosure(disclosure)
            val name = parsed[1] as String
            name in attributesToDisclose
        }

        Log.d(TAG, "Broj atributa za prikazati ${selectedDisclosures.size}")

        val presentation = buildString {
            append(issuerJwt)
            append("~")
            selectedDisclosures.forEach { disclosure ->
                append(disclosure)
                append("~")
            }
        }

        val sdHash = computeSdHash(presentation)
        Log.d(TAG, "sd_hash duljina ${sdHash.length}")

        val kbJwt = buildKbJwt(audience = verifierAudience, nonce = nonce, sdHash = sdHash)

        val fullPresentation = presentation + kbJwt
        Log.d(TAG, "Slozena prezentacija duljine ${fullPresentation.length}")

        return fullPresentation
    }

    private fun parseDisclosure(disclosure: String): List<Any> {
        val decoded = Base64.decode(
            disclosure,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )

        val jsonString = String(decoded, Charsets.UTF_8)

        val jsonArray = org.json.JSONArray(jsonString)
        return List(jsonArray.length()){jsonArray.get(it)}
    }

    /*Prema sluzbenoj dokumentaciji, sazetak se obavlja nad ascii bajtovima
    BEZ kb-jwt dijela pa ova funkcija djeluje samo nad jednim dijelom ukupne prezentacije
    */

    private fun computeSdHash(partialPresentation: String): String {
        val ascii = partialPresentation.toByteArray(Charsets.US_ASCII)
        val digest = MessageDigest.getInstance("SHA-256").digest(ascii)

        return Base64.encodeToString(
            digest,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

    private fun buildKbJwt(audience: String, nonce: String, sdHash: String): String {
        val header = JWSHeader.Builder(JWSAlgorithm.ES256).type(JOSEObjectType("kb+jwt")).build()

        val claims = JWTClaimsSet.Builder().issueTime(Date()).audience(audience).claim("nonce", nonce).claim("sd_hash", sdHash).build()

        val signedJwt = SignedJWT(header, claims)
        signedJwt.sign(KeystoreSigner(keystoreManager))

        return signedJwt.serialize()
    }
}