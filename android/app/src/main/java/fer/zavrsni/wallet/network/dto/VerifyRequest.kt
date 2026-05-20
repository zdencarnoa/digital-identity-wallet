package fer.zavrsni.wallet.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyRequest(
    @SerialName("session_id")
    val sessionId: String,
    val presentation: String
) {
}