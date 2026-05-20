package fer.zavrsni.wallet.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChallengeResponse(
    @SerialName("session_id")
    val sessionId: String,
    val nonce: String,
    val audience: String
) {
}