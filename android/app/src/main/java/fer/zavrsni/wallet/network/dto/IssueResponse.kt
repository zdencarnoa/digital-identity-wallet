package fer.zavrsni.wallet.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IssueResponse(
    @SerialName("sd_jwt_vc")
    val sdJwtVc: String
) {
}