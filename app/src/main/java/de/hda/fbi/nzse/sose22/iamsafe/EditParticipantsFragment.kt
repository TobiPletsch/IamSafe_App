package de.hda.fbi.nzse.sose22.iamsafe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*


class EditParticipantsFragment : Fragment(R.layout.fragment_edit_contacts) {

    private val args: EditParticipantsFragmentArgs by navArgs()

    private lateinit var currentEvent: GroupEvent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        runBlocking {
            DataStore.tempCurrentParticipants = Vector()
            DataStore.tempUpdateEventParticipantIds = Vector()
            val answer = async {
                var eventId = ""
                var count = -1
                for ((itemIndex, item) in DataStore.events.withIndex()) {
                    if (args.isActiveEvent && isDatePast(item.endDate.toDate())) {
                        // do nothing
                    } else if (!args.isActiveEvent && !isDatePast(item.endDate.toDate())) {
                        // do nothing
                    } else {
                        count++
                    }

                    if (count == args.pos) {
                        eventId = DataStore.events[itemIndex].id
                        break
                    }
                }

                Fetcher.getAllEventParticipants(eventId)
                Fetcher.getContacts()
            }
            answer.await()
        }


        DataStore.tempCurrentParticipants.forEach { el -> DataStore.tempUpdateEventParticipantIds.add(el.id) }

        // Make my contacts, which are not currently selected, available for selection
        DataStore.contacts.forEach {
            if (DataStore.tempCurrentParticipants.indexOfFirst { part -> part.id == it.id } == -1) {
                DataStore.tempCurrentParticipants.add(it)
            }
        }

        // Don't include myself in participant list
        DataStore.tempCurrentParticipants.removeAt(DataStore.tempCurrentParticipants.indexOfFirst { it.id == DataStore.userId })

        val view: View = inflater.inflate(R.layout.fragment_edit_contacts, container, false)
        val recyclerViewEditContacts: RecyclerView = view.findViewById(R.id.recyclerview_viewcontacts_contactlist)

        currentEvent = if (args.isActiveEvent) {
            DataStore.events.filter { !isDatePast(it.endDate.toDate()) }[args.pos]
        } else {
            DataStore.events.filter { isDatePast(it.endDate.toDate()) }[args.pos]
        }

        recyclerViewEditContacts.adapter =
            EditParticipantsRecyclerViewAdapter(currentEvent) { position ->
                onListItemClick(position)
            }

        return view
    }

    private fun onListItemClick(position: Int) {
        Log.d("EditContacts", DataStore.tempCurrentParticipants[position].name)
    }


}