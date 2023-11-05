package de.hda.fbi.nzse.sose22.iamsafe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*


class EventDetailsEditableFragment : Fragment(R.layout.fragment_event_detail_view) {

    private val args: EventDetailsEditableFragmentArgs by navArgs()

    private lateinit var btnEditContacts : Button
    private lateinit var btnSaveEvent : Button
    private lateinit var btnDeleteEvent : Button
    private lateinit var tvEventDateRange : TextView

    private lateinit var currentEvent: GroupEvent


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_event_detail_view, container, false)

        // Buttons
        btnEditContacts = view.findViewById(R.id.button_eventdetailview_editparticipants)!!
        btnEditContacts.setOnClickListener { handleEditContactsEvent() }

        btnSaveEvent = view.findViewById(R.id.button_eventdetailview_saveevent)!!
        btnSaveEvent.setOnClickListener { handleSaveEvent() }

        btnDeleteEvent = view.findViewById(R.id.button_eventdetailview_deleteevent)!!
        btnDeleteEvent.setOnClickListener { handleDeleteEvent() }

        tvEventDateRange = view.findViewById(R.id.textview_eventdetailview_showdaterange)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentEvent = if(args.isActiveEvent){
            DataStore.events.filter { !isDatePast(it.endDate.toDate())}[args.index]
        } else {
            DataStore.events.filter { isDatePast(it.endDate.toDate())}[args.index]
        }

        setLayoutText()

        val startDate = currentEvent.startDate.seconds * makeMilSec
        val endDate = currentEvent.endDate.seconds * makeMilSec

        val eventDateRangeString = timeInSecondsToDateString(currentEvent.startDate.seconds) +
                " - " + timeInSecondsToDateString(currentEvent.endDate.seconds)

        tvEventDateRange.text = eventDateRangeString

        //Date Range Picker section start
        val datePicker = requireView().findViewById<TextView>(R.id.textview_eventdetailview_showdaterange)

        val builder =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select start and end date")
                .setTheme(R.style.MaterialCalendarTheme)
                .setSelection(
                    androidx.core.util.Pair(
                        startDate,
                        endDate
                    )
                )

        val dateRangePicker = builder.build()


        datePicker.setOnClickListener{
            dateRangePicker.show(activity?.supportFragmentManager!!, dateRangePicker.toString())
        }

        dateRangePicker.addOnCancelListener{

        }

        dateRangePicker.addOnPositiveButtonClickListener {
                datePicked ->
            val eventStartDate = datePicked.first
            val eventEndDate = datePicked.second

            currentEvent.startDate = convertLongToTimestamp(eventStartDate)
            currentEvent.endDate = convertLongToTimestamp(eventEndDate)

            val eventDateRangeString = timeInSecondsToDateString(currentEvent.startDate.seconds) +
                    " - " + timeInSecondsToDateString(currentEvent.endDate.seconds)

            tvEventDateRange.text = eventDateRangeString
        }

        //Date Range Picker section end
    }

    private fun handleEditContactsEvent(){
        val eventIndexCorrespondingInVector = args.index
        val isInEventVector = args.isActiveEvent    // else is in expiredEvents Vector (DataStore)
        findNavController().navigate(EventDetailsEditableFragmentDirections.actionEventDetailViewFragmentToEditContacts(eventIndexCorrespondingInVector, isInEventVector))
    }

    private fun handleSaveEvent(){
        val nameInput: TextInputLayout = requireView().findViewById(R.id.textinputlayout_eventdetailview_eventname)
        val descriptionInput: TextInputLayout = requireView().findViewById(R.id.textinputlayout_eventdetailview_eventdescription)

        currentEvent.name = nameInput.editText?.text.toString()
        currentEvent.description = descriptionInput.editText?.text.toString()
        if(DataStore.tempUpdateEventParticipantIds.size > 0) {
            currentEvent.participantsIds = DataStore.tempUpdateEventParticipantIds
        }

        // action can be found in navigation_bottom.xml -> is an edge in the graph
        runBlocking {
            val answer = async { Fetcher.updateGroupEvent(DataStore.events.find{el -> el.id == currentEvent.id}!!) }
            answer.await()
        }

        DataStore.tempUpdateEventParticipantIds = Vector<String>()


        findNavController().navigate(EventDetailsEditableFragmentDirections.actionEventDetailViewFragmentToEventsFragment())
    }

    private fun handleDeleteEvent() {
        runBlocking {
            val answer = async { Fetcher.deleteGroupEvent(currentEvent.id) }
            answer.await()
        }

        // action can be found in navigation_bottom.xml -> is an edge in the graph
        findNavController().navigate(EventDetailsEditableFragmentDirections.actionEventDetailViewFragmentToEventsFragment())
    }

    private fun setLayoutText(){
        val tilEventName : TextInputLayout = view?.findViewById(R.id.textinputlayout_eventdetailview_eventname) as TextInputLayout
        val tilEventDesc : TextInputLayout = view?.findViewById(R.id.textinputlayout_eventdetailview_eventdescription) as TextInputLayout

        tilEventName.editText?.setText(currentEvent.name)
        tilEventDesc.editText?.setText(currentEvent.description)

    }
}
