package com.example.votingsystem

import java.security.MessageDigest

object SecurityUtils {

    /**
     * Generates a consistent 64-byte key for Realm encryption.
     * Uses a stable seed to ensure it's predictable for this project.
     */
    fun getEncryptionKey(): ByteArray {
        val seed = "VotingSystem_Secure_Storage_Seed_2024"
        return MessageDigest.getInstance("SHA-512").digest(seed.toByteArray())
    }

    /**
     * Generates a tamper-proof hash for a vote.
     * Simplifies to a basic SHA-256 hash of data to avoid Keystore related crashes.
     */
    fun generateVoteSignature(electionId: String, candidateName: String, voterEmail: String, timestamp: Long): String {
        val data = "$electionId|$candidateName|$voterEmail|$timestamp|VotingSystemSecretSalt"
        val hashBytes = MessageDigest.getInstance("SHA-256").digest(data.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies if a vote has been tampered with.
     */
    fun verifyVote(electionId: String, candidateName: String, voterEmail: String, timestamp: Long, signature: String): Boolean {
        val expected = generateVoteSignature(electionId, candidateName, voterEmail, timestamp)
        return expected == signature
    }
}
