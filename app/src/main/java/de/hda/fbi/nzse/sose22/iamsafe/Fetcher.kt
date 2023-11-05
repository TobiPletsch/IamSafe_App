package de.hda.fbi.nzse.sose22.iamsafe

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Fetcher {
    companion object Fetch {
        @Suppress("UNCHECKED_CAST")
        fun userFromDoc(document: DocumentSnapshot): User {
            @Suppress("UNCHECKED_CAST") // Causes problem error if fixed according to code inspection
            return User(
                document.id,
                document.get("name").toString(),
                document.get("lastStatusSetAt") as Timestamp,
                document.get("lastStatusLocation") as GeoPoint,
                Vector<String>(document.get("notificationBlacklistUserIds") as ArrayList<String>),
                Vector<String>(document.get("locationBlockedUserIds") as ArrayList<String>),
            )
        }

        private fun eventFromDoc(document: DocumentSnapshot): GroupEvent {
            @Suppress("UNCHECKED_CAST") // Causes problem error if fixed according to code inspection
            return GroupEvent(document.id, document.get("name").toString(), document.get("participantsIds") as List<String>, document.get("ownerId").toString(), document.get("description").toString(), document.get("startDate") as Timestamp, document.get("endDate") as Timestamp)
        }

        suspend fun getMe() {
            val userId = DataStore.userId
            if(userId.isEmpty()) {
                Log.e("getMe", "UserID is not set")
                return
            }

            val firestore = FirebaseFirestore.getInstance()
            val me = firestore.collection("users").document(userId).get().await()
            if(me.exists()) {
                DataStore.user = userFromDoc(me)

                try {
                    val token = FirebaseMessaging.getInstance().token.await()
                    Log.d("TOKEN", token)

                    firestore.collection("users").document(userId).update(hashMapOf("fcmToken" to token) as Map<String, Any>).await()
                } catch (e: Exception) {
                    Log.d("Notifications", "No push notifications allowed")
                }
            }
        }

        suspend fun getContacts() {
            val userId = DataStore.userId
            if (userId.isEmpty()) {
                Log.e("getContacts", "UserID is not set")
                return
            }

            val firestore = FirebaseFirestore.getInstance()

            DataStore.contacts = Vector()

            val contactsDocument = firestore.collection("users").document(userId).get().await()
            if(contactsDocument.exists()) {
                @Suppress("UNCHECKED_CAST") // causes plethora of new issues if fixed according to code inspection
                for (contactId in contactsDocument.get("subscribedUsersIds") as List<String>) {
                    if(contactId.isNotBlank()) {
                        val contactUser =
                            firestore.collection("users").document(contactId).get().await()

                        Log.d("getContact", "got contact")
                        if (contactUser.exists()) {
                            DataStore.contacts.add(userFromDoc(contactUser))
                        }
                    }
                }
            }
        }

        suspend fun getEvents() {
            val userId = DataStore.userId
            if (userId.isEmpty()) {
                Log.e("getContacts", "UserID is not set")
                return
            }

            val firestore = FirebaseFirestore.getInstance()

            DataStore.events = Vector()

            val events = firestore.collection("events").whereArrayContains("participantsIds", userId).get().await()
            events.documents.forEach {
                DataStore.events.add(eventFromDoc(it))
            }
        }

        suspend fun getAllEventParticipants (eventId: String) {
            val userId = DataStore.userId
            if (userId.isEmpty()) {
                Log.e("getContacts", "UserID is not set")
                return
            }

            val firestore = FirebaseFirestore.getInstance()

            DataStore.contacts = Vector()

            val eventDocument = firestore.collection("events").document(eventId).get().await()
            for (participantId in eventDocument.get("participantsIds") as List<*>) {
                val contactUser =
                    firestore.collection("users").document(participantId as String).get().await()

                Log.d("participant", contactUser.id)

                Log.d("getContact", "got contact")
                if(contactUser.exists()) {
                    DataStore.tempCurrentParticipants.add(userFromDoc(contactUser))
                }
            }
        }

        suspend fun createEvent (event: GroupEvent) {
            val firestore = FirebaseFirestore.getInstance()

            val addEventObj = HashMap<String, Any>()
            addEventObj["name"] = event.name
            addEventObj["participantsIds"] = event.participantsIds
            addEventObj["startDate"] = event.startDate
            addEventObj["endDate"] = event.endDate
            addEventObj["ownerId"] = event.ownerId
            addEventObj["description"] = event.description

            firestore.collection("events").add(addEventObj).await()
        }

        suspend fun updateGroupEvent (event: GroupEvent) {
            val firestore = FirebaseFirestore.getInstance()

            val updateEventObj = HashMap<String, Any>()
            updateEventObj["name"] = event.name
            updateEventObj["participantsIds"] = event.participantsIds
            updateEventObj["startDate"] = event.startDate
            updateEventObj["endDate"] = event.endDate
            updateEventObj["ownerId"] = event.ownerId
            updateEventObj["description"] = event.description

            firestore.collection("events").document(event.id).update(updateEventObj).await()
        }

        suspend fun removeContact (contactId: String) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users").document(DataStore.userId).update("subscribedUsersIds", DataStore.contacts.filter { item -> item.id != contactId }.map { item -> item.id }).await()
        }

        suspend fun deleteGroupEvent (eventId: String) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("events").document(eventId).delete().await()
        }

        fun markMeAsSafe(eventId: String/*, tLocation: GeoPoint*/) {
            val functions = FirebaseFunctions.getInstance("europe-west1")

            //val callData = hashMapOf ("location" to hashMapOf("lat" to tLocation.latitude, "lon" to tLocation.longitude), "eventId" to eventId)
            val callData = hashMapOf ("location" to hashMapOf("lat" to 36.1162, "lon" to 115.1745), "eventId" to eventId)

            functions.getHttpsCallable("markAsSafe").call(callData).addOnSuccessListener { Log.d("FIREBASE", "SUCCESS") }.addOnFailureListener { Log.d("ERROR FIREBASE", it.message.toString()) }
        }

        fun addContact(contactId: String, context: Context) {
            val functions = FirebaseFunctions.getInstance("europe-west1")

            val callData = hashMapOf ("contactId" to contactId)

            functions.getHttpsCallable("addContact").call(callData)
                .addOnSuccessListener { Toast.makeText(context, "Added ${(it.data as HashMap<*, *>)["username"]} as contact!", Toast.LENGTH_LONG).show()}
                .addOnFailureListener { e -> Log.e("AddContact", e.message.toString())}

        }

        suspend fun saveUserOptions(){
            val firestore = FirebaseFirestore.getInstance()

            val updateObject = HashMap<String, Any>()

            updateObject["notificationBlacklistUserIds"] = DataStore.user.notificationBlacklistUserIds
            updateObject["locationBlockedUserIds"] = DataStore.user.locationBlockedUserIds

            firestore.collection("users").document(DataStore.userId).update(updateObject).await()
        }

        suspend fun getAddMeContactDynamicLink(): String {
            return try {
                val dynamicLink = Firebase.dynamicLinks.shortLinkAsync {
                    link = Uri.parse("https://iamsafe.info/addContact/${DataStore.userId}")
                    domainUriPrefix = "https://iamsafe.info"

                    androidParameters {}
                }.await()


                dynamicLink.shortLink.toString()
            } catch (e: Exception) {
                "ERROR"
            }
        }

        fun leaveEvent(eventId: String, navController: NavController, context: Context) {
            val functions = FirebaseFunctions.getInstance("europe-west1")

            val callData = hashMapOf ("eventId" to eventId)

            functions.getHttpsCallable("leaveEvent").call(callData).addOnSuccessListener {
                Toast.makeText(context, "Left event successfully", Toast.LENGTH_SHORT).show()

                val action = EventDetailsViewOnlyFragmentDirections.actionEventDetailsViewNotOwnerFragmentToEventsFragment()
                navController.navigate(action)
            }
        }
    }
}