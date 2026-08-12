package com.example.votingsystem

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<Unit>

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") pass: String
    ): Response<AuthResponse>

    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: OtpRequest): Response<OtpResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): Response<OtpVerifyResponse>

    @POST("elections/create")
    suspend fun createElection(
        @Header("Authorization") token: String,
        @Body request: ElectionCreate
    ): Response<ApiElection>

    @GET("elections/my-elections")
    suspend fun getMyElections(@Header("Authorization") token: String): Response<List<ApiElection>>

    @POST("elections/join")
    suspend fun joinElection(
        @Header("Authorization") token: String,
        @Body request: JoinElectionRequest
    ): Response<Unit>

    @GET("elections/joined")
    suspend fun getJoinedElections(@Header("Authorization") token: String): Response<List<ApiElection>>

    @PATCH("elections/{election_id}/schedule")
    suspend fun updateElectionSchedule(
        @Header("Authorization") token: String,
        @Path("election_id") electionId: String,
        @Body request: ElectionTimeUpdate
    ): Response<ApiElection>

    @GET("candidates")
    suspend fun getCandidates(@Query("election_id") electionId: String? = null): Response<List<ApiCandidate>>

    @POST("votes/cast")
    suspend fun castVote(
        @Header("Authorization") token: String,
        @Body request: VoteRequest
    ): Response<Unit>

    @GET("elections/{election_id}/participants")
    suspend fun getParticipants(
        @Header("Authorization") token: String,
        @Path("election_id") electionId: String
    ): Response<List<ParticipantResponse>>

    @PATCH("elections/participants/{participant_id}/status")
    suspend fun updateParticipantStatus(
        @Header("Authorization") token: String,
        @Path("participant_id") participantId: String,
        @Body request: ParticipantStatusUpdate
    ): Response<ParticipantResponse>

    @GET("votes/results/{election_id}")
    suspend fun getResults(
        @Path("election_id") electionId: String
    ): Response<List<CandidateResult>>
}
