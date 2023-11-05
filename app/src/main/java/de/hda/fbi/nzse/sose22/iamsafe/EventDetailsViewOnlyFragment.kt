package de.hda.fbi.nzse.sose22.iamsafe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*


class EventDetailsViewOnlyFragment : Fragment(R.layout.fragment_event_detail_view_not_owner) {


    private val args: EventDetailsViewOnlyFragmentArgs by navArgs()

    private lateinit var currentEvent: GroupEvent

    private lateinit var tvEventName : TextView
    private lateinit var tvEventDesc : TextView
    private lateinit var tvEventDateRange : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        runBlocking {
            DataStore.tempCurrentParticipants = Vector()
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

                    if (count == args.index) {
                        eventId = DataStore.events[itemIndex].id
                        break
                    }
                }

                Fetcher.getAllEventParticipants(eventId)
            }
            answer.await()
        }

        // Make my contacts, which are not currently selected, available for selection
        DataStore.contacts.forEach {
            if (DataStore.tempCurrentParticipants.indexOfFirst { part -> part.id == it.id } == -1) {
                DataStore.tempCurrentParticipants.add(it)
            }
        }

        // Don't include myself in participant list
        DataStore.tempCurrentParticipants.removeAt(DataStore.tempCurrentParticipants.indexOfFirst { it.id == DataStore.userId })

        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_event_detail_view_not_owner, container, false)
        tvEventName = view.findViewById(R.id.textview_eventdetailviewnotowner_eventname)
        tvEventDesc = view.findViewById(R.id.textview_eventdetailviewnotowner_eventdescription)
        tvEventDateRange = view.findViewById(R.id.textview_eventdetailviewnotowner_eventdaterange)
        val recyclerViewEventNotOwner : RecyclerView = view.findViewById(R.id.recyclerview_eventdetailviewnotowner_participantlist)

        currentEvent = if (args.isActiveEvent) {
            DataStore.events.filter { !isDatePast(it.endDate.toDate()) }[args.index]
        } else {
            DataStore.events.filter { isDatePast(it.endDate.toDate()) }[args.index]
        }

        recyclerViewEventNotOwner.adapter = EventDetailsViewOnlyRecyclerViewAdapter{
            onListItemClick() // Does not server a purpose -> leave in case of wanting to add contact detail view on click
        }

        val leaveButton: Button = view.findViewById(R.id.button_eventdetailviewnotowner_leaveevent)
        leaveButton.setOnClickListener {
            handleLeaveEvent()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvEventName.text = currentEvent.name
        tvEventDesc.text = currentEvent.description

        val eventDateRangeString = timeInSecondsToDateString(currentEvent.startDate.seconds) +
                " - " + timeInSecondsToDateString(currentEvent.endDate.seconds)
        tvEventDateRange.text = eventDateRangeString
    }

    private fun onListItemClick() {}

    private fun handleLeaveEvent() {
        Toast.makeText(requireContext(), "Loading...", Toast.LENGTH_SHORT).show()
        Fetcher.leaveEvent(currentEvent.id, findNavController(), requireContext())
    }
}