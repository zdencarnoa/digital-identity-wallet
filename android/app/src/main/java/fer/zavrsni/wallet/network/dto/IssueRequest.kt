package fer.zavrsni.wallet.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IssueRequest(
    val oib: String,
    @SerialName("holder_public_jwk")
    val holderPublicJwk: Map<String, String>
)