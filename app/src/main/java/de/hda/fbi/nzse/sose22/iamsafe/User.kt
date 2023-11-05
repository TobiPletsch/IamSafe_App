package de.hda.fbi.nzse.sose22.iamsafe

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.*


class User(
    tID: String, tName: String, tUpdatedAt: Timestamp?, tLocation: GeoPoint,
    tNotificationBlacklistUserIds: Vector<String>, tLocationBlockedUserIds: Vector<String>)
{
    var id: String = tID
    var name: String = tName
    var lastStatusSetAt: Timestamp? = tUpdatedAt
    var lastStatusLocation: GeoPoint = tLocation

    var notificationBlacklistUserIds: Vector<String> = tNotificationBlacklistUserIds
    var locationBlockedUserIds: Vector<String> = tLocationBlockedUserIds
}

