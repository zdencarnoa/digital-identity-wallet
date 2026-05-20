package fer.zavrsni.wallet.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class VerifyResponse(
    @SerialName("verified_claims")
    val verifiedClaims: Map<String, JsonElement>
) {
}