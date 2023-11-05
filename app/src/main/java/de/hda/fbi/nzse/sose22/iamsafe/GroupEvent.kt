package de.hda.fbi.nzse.sose22.iamsafe

import com.google.firebase.Timestamp

class GroupEvent (
    tID: String, tName : String, tParticipantsIds : List<String>, tOwnerId: String,
    tDescription : String, tStartDate : Timestamp, tEndDate : Timestamp) {
    val id : String = tID
    var name : String = tName
    var participantsIds: List<String> = tParticipantsIds
    val ownerId: String = tOwnerId
    var description : String = tDescription
    var startDate : Timestamp = tStartDate
    var endDate : Timestamp = tEndDate
}