package fer.zavrsni.wallet.presentation

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.impl.ECDSA
import com.nimbusds.jose.jca.JCAContext
import com.nimbusds.jose.util.Base64
import com.nimbusds.jose.util.Base64URL
import fer.zavrsni.wallet.crypto.KeystoreManager

/*
Primarna zadaca ove klase je omoguciti da se potpisuje pomocu kljuca unutar android keystore sustava.
Razlog za to je sto JWSSigner ocekuje pristup kljucu za potpisivanje, a android keystore daje samo referencu na kljuc.
Ova klasa funkcionira kao wrapper nad JWSSigner i koristi moju funkciju sign data kako bi potpisao dane podatke pomocu reference iz android keystore sustava.
Osim toga, postoji razlika izmedu formata potpisa koji ocekuje JWSSigner i onog kojeg daje Signature.getInstance("SHA256withECDSA").run, ali ovaj problem
rjesen je nimbus funkcijom transcodeSignatureToConcat.
*/


class KeystoreSigner(private val keystoreManager: KeystoreManager) : JWSSigner{

    private val jcaContext = JCAContext()

    override fun supportedJWSAlgorithms(): MutableSet<JWSAlgorithm?>? {
        return mutableSetOf(JWSAlgorithm.ES256)
    }

    override fun getJCAContext(): JCAContext=jcaContext

    override fun sign(header: JWSHeader, signingInput: ByteArray): Base64URL? {
        val algorithm = header.algorithm
        if(algorithm != JWSAlgorithm.ES256){
            throw JOSEException("nepodrzan algoritam $algorithm")
        }

        val signatureDer = keystoreManager.signData(signingInput)

        val expectedLength = ECDSA.getSignatureByteArrayLength(JWSAlgorithm.ES256)
        val jwsSignature = ECDSA.transcodeSignatureToConcat(signatureDer, expectedLength)

        return Base64URL.encode(jwsSignature)
    }
}