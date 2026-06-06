package fer.zavrsni.wallet.network

import fer.zavrsni.wallet.network.dto.ChallengeResponse
import fer.zavrsni.wallet.network.dto.IssueRequest
import fer.zavrsni.wallet.network.dto.IssueResponse
import fer.zavrsni.wallet.network.dto.VerifyRequest
import fer.zavrsni.wallet.network.dto.VerifyResponse
import kotlinx.serialization.json.JsonObject
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST

/*
Retrofit sucelje za komunikaciju s backendom, sve su funkcije suspend i izvrsavaju se asinkrono
*/

interface WalletApi {

    @POST("issuer/issue")
    suspend fun issuePid(@Body request: IssueRequest): IssueResponse

    @POST("verifier/challenge")
    suspend fun createChallenge(): ChallengeResponse

    @POST("verifier/verify")
    suspend fun verify(@Body request: VerifyRequest): VerifyResponse

    @GET("issuer/public-key")
    suspend fun getIssuerPublicKey(): JsonObject
}