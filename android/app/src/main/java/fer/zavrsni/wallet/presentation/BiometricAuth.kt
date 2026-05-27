package fer.zavrsni.wallet.presentation

import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object BiometricAuth {

    private const val TAG = "BiometricAuth"

    suspend fun authenticate(
        activity: FragmentActivity,
        title: String = "Potrebna autentikacija",
        subtitle: String = "Potvrdite identitet za nastavak",
    ): Boolean = suspendCancellableCoroutine { continuation ->

        val executor = Executors.newSingleThreadExecutor()

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Log.d(TAG, "Autentikacija uspjesna!")
                if (continuation.isActive) continuation.resume(true)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Log.e(TAG, "Greska pri autentikaciji ($errorCode), $errString")
                if (continuation.isActive) {
                    when(errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON ->
                            continuation.resumeWithException(BiometricCancelledException("Korisnik je otkazao"))
                        else ->
                            continuation.resumeWithException(BiometricCancelledException("$errString (kod $errorCode)"))
                    }
                }
            }

            override fun onAuthenticationFailed() {
                Log.d(TAG, "Autentikacija neuspjesna, pokusajte ponovno")
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder().
            setTitle(title).setSubtitle(subtitle).
            setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
            androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        continuation.invokeOnCancellation {
            prompt.cancelAuthentication()
        }

        prompt.authenticate(promptInfo)
    }

    class BiometricCancelledException(message: String) : Exception(message)
    class BiometricFailedException(message: String) : Exception(message)
}