package de.hda.fbi.nzse.sose22.iamsafe

import java.util.*

class DataStore {
    companion object GlobalData {
        lateinit var userId: String
        lateinit var user: User
        var contacts = Vector<User>()
        var events = Vector<GroupEvent>()

        var addContactIdOnLaunch: String? = null

        lateinit var tmpCreateEvent : GroupEvent

        var tempCurrentParticipants = Vector<User>()
        var tempCreateEventParticipants = Vector<String>()
        var tempUpdateEventParticipantIds = Vector<String>()


    }
}