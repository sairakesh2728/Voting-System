package com.example.votingsystem

import android.app.Application
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VotingApp : Application() {
    
    companion object {
        var realm: Realm? = null
        
        // Demo user state
        var demoUserEmail: String? = null
        var demoUserName: String? = null
        var authToken: String? = null

        // Temporary storage for election creation to avoid TransactionTooLargeException
        val tempCandidateList = mutableListOf<Candidate>()

        fun logoutAndClear() {
            demoUserEmail = null
            demoUserName = null
            authToken = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Local Realm Database
        val config = RealmConfiguration.Builder(
            schema = setOf(Candidate::class, Election::class, Participant::class, UserData::class, Vote::class)
        )
        .compactOnLaunch()
        .deleteRealmIfMigrationNeeded()
        .build()
        
        realm = Realm.open(config)

        // seedDemoData() // Optional: Can be commented out if relying solely on live backend
    }

    private fun seedDemoData() {
        realm?.writeBlocking {
            val existing = query<Election>().find()
            if (existing.isEmpty()) {
                // Add Sample Election 1
                copyToRealm(Election()).apply {
                    electionId = "demo_1"
                    name = "2024 Presidential Election"
                    creatorEmail = "admin@voting.com"
                    date = "10/11/2024"
                    time = "09:00"
                    electionCode = "123456"
                    candidates.add(copyToRealm(Candidate("John Doe", "", "")))
                    candidates.add(copyToRealm(Candidate("Jane Smith", "", "")))
                }
            }
        }
    }
}
