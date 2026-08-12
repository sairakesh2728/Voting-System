package com.example.votingsystem

import android.os.Parcelable
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.parcelize.Parcelize
import org.mongodb.kbson.ObjectId

@Parcelize
class Candidate(
    var name: String = "",
    var photoUrl: String = "",
    var symbolUrl: String = ""
) : RealmObject, Parcelable {
    // Empty constructor for Realm
    constructor() : this("", "", "")
}

class Election() : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var electionId: String = ""
    var name: String = ""
    var creatorEmail: String = ""
    var candidates: RealmList<Candidate> = realmListOf()
    var date: String = ""
    var time: String = ""
    var electionCode: String = ""
}

class Participant() : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var fullName: String = ""
    var idNumber: String = ""
    var electionCode: String = ""
    var electionId: String = ""
    var electionName: String = ""
    var userUid: String = ""
    var status: String = "pending"
    var timestamp: Long = 0
}

class UserData() : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var name: String = ""
    var email: String = ""
    var createdAt: Long = 0
}

class Vote() : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var electionId: String = ""
    var candidateName: String = ""
    var voterEmail: String = ""
    var timestamp: Long = 0
    var signature: String = ""
}
