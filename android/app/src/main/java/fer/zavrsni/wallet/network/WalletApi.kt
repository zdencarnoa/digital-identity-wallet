package fer.zavrsni.wallet.network

import fer.zavrsni.wallet.network.dto.ChallengeResponse
import fer.zavrsni.wallet.network.dto.IssueRequest
import fer.zavrsni.wallet.network.dto.IssueResponse
import fer.zavrsni.wallet.network.dto.VerifyRequest
import fer.zavrsni.wallet.network.dto.VerifyResponse
import retrofit2.http.Body
import retrofit2.http.POST

/*
Interface za povezivanje na backend, funkcije su asinkrone, dozvoljavaju paralelno izvodenje
*/

interface WalletApi {

    @POST("issuer/issue")
    suspend fun issuePid(@Body request: IssueRequest): IssueResponse

    @POST("verifier/challenge")
    suspend fun createChallenge(): ChallengeResponse

    @POST("verifier/verify")
    suspend fun verify(@Body request: VerifyRequest): VerifyResponse
}