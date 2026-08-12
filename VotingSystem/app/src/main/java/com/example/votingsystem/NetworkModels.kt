package com.example.votingsystem

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val user: UserProfileResponse?
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)

data class UserProfileResponse(
    val id: String?,
    val name: String,
    val email: String
)

data class ElectionCreate(
    val name: String,
    val date: String,
    val candidates: List<ApiCandidate>
)

data class ElectionTimeUpdate(
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String
)

data class ApiElection(
    val id: String?,
    val electionId: String,
    val name: String,
    @SerializedName("creator_email")
    val creatorEmail: String,
    val date: String,
    @SerializedName("start_time")
    val startTime: String?,
    @SerializedName("end_time")
    val endTime: String?,
    @SerializedName("election_code")
    val electionCode: String,
    val candidates: List<ApiCandidate>
)

data class ApiCandidate(
    val name: String,
    @SerializedName("photo_url")
    val photoUrl: String?,
    @SerializedName("symbol_url")
    val symbolUrl: String?
)

data class JoinElectionRequest(
    @SerializedName("election_code")
    val electionCode: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("id_number")
    val idNumber: String
)

data class OtpRequest(
    val email: String
)

data class OtpResponse(
    val success: Boolean,
    val message: String
)

data class OtpVerifyRequest(
    val email: String,
    val otp: String
)

data class OtpVerifyResponse(
    val success: Boolean,
    val message: String
)

data class VoteRequest(
    @SerializedName("election_id")
    val electionId: String,
    @SerializedName("candidate_name")
    val candidateName: String,
    @SerializedName("voter_email")
    val voterEmail: String,
    val timestamp: Long,
    val signature: String
)

data class CandidateResult(
    val candidate: String,
    val votes: Int,
    @SerializedName("symbol_url")
    val symbolUrl: String?
)

data class ParticipantResponse(
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("election_id")
    val electionId: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("id_number")
    val idNumber: String,
    @SerializedName("election_code")
    val electionCode: String,
    val status: String,
    val timestamp: String
)

data class ParticipantStatusUpdate(
    val status: String
)
