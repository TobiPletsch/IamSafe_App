package de.hda.fbi.nzse.sose22.iamsafe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*


class EventsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        runBlocking {
            val answer = async { Fetcher.getEvents() }
            answer.await()
        }


        val view : View = inflater.inflate(R.layout.fragment_events, container, false)
        val recyclerViewActiveEvents : RecyclerView =  view.findViewById(R.id.recyclerview_events_activeeventslist)
        val recyclerViewExpiredEvents: RecyclerView = view.findViewById(R.id.recyclerview_events_expiredeventslist)

        val activeEventsAdapter = ActiveEventsRecyclerViewAdapter { position ->
            onListItemClick(position, true)
        }
        val expiredEventsAdapter = ExpiredEventsRecyclerViewAdapter { position ->
            onListItemClick(position, false)
        }

        recyclerViewActiveEvents.adapter = activeEventsAdapter
        recyclerViewExpiredEvents.adapter = expiredEventsAdapter

        // loadDebugData()

        return view
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)

        //CREATE EVENT
        //
        val buttonCreateEvent: FloatingActionButton = requireView().findViewById(R.id.floatingactionbutton_events_addevent)
        buttonCreateEvent.setOnClickListener {
            // empty tmpEvent (used for saving when clicking on Add Participants in Create Events)
            DataStore.tmpCreateEvent = GroupEvent(
                "", "", emptyList(), "", "",
                convertLongToTimestamp(MaterialDatePicker.thisMonthInUtcMilliseconds()),
                convertLongToTimestamp(MaterialDatePicker.todayInUtcMilliseconds())
            )
            DataStore.tempCreateEventParticipants = Vector()
            val action = EventsFragmentDirections.actionEventsFragmentToCreateEventFragment()
            findNavController().navigate(action)
        }

    }

    private fun onListItemClick(position: Int, isActiveEvent: Boolean) {

        // get correct event from Datastore.events (recycler views do not correspond to order in vector)
        val currentEvent: GroupEvent = if (isActiveEvent) {
            DataStore.events.filter { !isDatePast(it.endDate.toDate()) }[position]    // is not expired event
        } else {
            DataStore.events.filter { isDatePast(it.endDate.toDate()) }[position]     // is expired event
        }

        //navigate to correct detail view, depending if owner of event or not
        if (currentEvent.ownerId == DataStore.userId) {
            findNavController().navigate(
                EventsFragmentDirections.actionEventsFragmentToEventDetailViewFragment(position, isActiveEvent) )
        } else {
            findNavController().navigate(
                EventsFragmentDirections.actionEventsFragmentToEventDetailsViewNotOwnerFragment( position, isActiveEvent ))
        }

    }

}