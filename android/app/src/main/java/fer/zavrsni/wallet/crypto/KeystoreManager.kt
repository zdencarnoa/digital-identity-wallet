package fer.zavrsni.wallet.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

/*
Upravljanje korisnikovim privatnim kljucem unutar Android Keystore sustava

Sve operacije s privatnim kljucem odvijaju se unutar trusted execution environment-a (TEE), odnosno
aplikacija nikad nema direktan pristup kljucu. Pristup kljucu zahtjeva biometrijsku ili PIN autentikaciju korisnika.

*/


class KeystoreManager {

    // Identifikator aplikacije u keystoreu, jedinstveni alias unutar aplikacije, poveznica s privatnim kljucem
    companion object {

        private const val KEY_ALIAS = "wallet_holder_key"

        private const val ANDROID_KEYSTORE = "AndroidKeyStore"

        // Krivulja P-256 propisana EUDI ARF-om
        private const val EC_CURVE = "secp256r1"
    }

    // Provjerava postoji li kljuc s danim alias-om
    fun keyExists(): Boolean {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return keyStore.containsAlias(KEY_ALIAS)
    }

    // Generira novi par kljuceva i veze privatni kljuc uz alias
    fun generateKeyPair(): Map<String, String> {

        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE)).
        setDigests(KeyProperties.DIGEST_SHA256).setUserAuthenticationRequired(true).
        setUserAuthenticationParameters(5, KeyProperties.AUTH_BIOMETRIC_STRONG
        or KeyProperties.AUTH_DEVICE_CREDENTIAL).
        build()

        generator.initialize(spec)
        val keyPair: KeyPair = generator.generateKeyPair()

        return publicKeyToJwk(keyPair.public as ECPublicKey)
    }

    // Potpisuje dane podatke s privatnim kljucem, ali mu NE pristupa
    fun signData(data: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val privateKey = keyStore.getKey(KEY_ALIAS, null) as? PrivateKey
            ?: error("Privatni kljuc s aliasom '$KEY_ALIAS' ne postoji")

        return Signature.getInstance("SHA256withECDSA").run {
            initSign(privateKey)
            update(data)
            sign()
        }
    }

    // Brisanje kljuca iz walleta
    fun deleteKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        if (keyStore.containsAlias(KEY_ALIAS)){
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }

    /*
    Sljedece funkcije potrebne su radi kompatibilnosti s python libraryem sd-jwt-python na backendu
    */

    // Pretvara Java ECPublicKey u JWK format kompatibilan s sd-jwt-python bibliotekom na backendu
    private fun publicKeyToJwk(publicKey: ECPublicKey): Map<String, String> {
        val point = publicKey.w
        val fieldSize = 32 // jer P-256 ima koordinate od 32 bajta

        val x = padTo(point.affineX.toByteArray(), fieldSize)
        val y = padTo(point.affineY.toByteArray(), fieldSize)

        return mapOf(
            "kty" to "EC",
            "crv" to "P-256",
            "x" to base64UrlEncode(x),
            "y" to base64UrlEncode(y)
        )
    }

    // Ova funkcija sluzi da makne vodece nule ako ima vise od 32 bajta, ili doda vodece nule ako je manje od 32
    private fun padTo(bytes: ByteArray, targetSize: Int): ByteArray {
        if (bytes.size == targetSize){
            return bytes
        }

        if (bytes.size == targetSize + 1 && bytes[0] == 0.toByte()){
            return bytes.copyOfRange(1, bytes.size)
        }

        if (bytes.size < targetSize){
            val padded = ByteArray(targetSize - bytes.size) + bytes
            return padded
        }

        error("Neispravna velicina bajtova ${bytes.size}, ocekujem $targetSize")
    }

    /*
    Ova funkcija koristi poseban nacin base64 kodiranja zbog JWK specifikacije, mora
    biti url safe, ne smije dodavati oznake novog reda ni padding kako bi podatak ostao nepromijenjen
    */
    private fun base64UrlEncode(bytes: ByteArray): String {
        return Base64.encodeToString(
            bytes,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

}