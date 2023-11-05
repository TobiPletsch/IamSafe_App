package de.hda.fbi.nzse.sose22.iamsafe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class CreateEventFragment : Fragment() {

    private lateinit var tilEventName : TextInputLayout
    private lateinit var tilEventDesc : TextInputLayout
    private lateinit var tvEventDateRange : TextView

    private var chosenEventStartTime : Long = MaterialDatePicker.todayInUtcMilliseconds()
    private var chosenEventEndTime : Long = MaterialDatePicker.todayInUtcMilliseconds()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        val view : View = inflater.inflate(R.layout.fragment_create_event, container, false)
        tilEventName = view.findViewById(R.id.textinputlayout_createevent_eventname)
        tilEventDesc = view.findViewById(R.id.textinputlayout_createevent_eventdescription)
        tvEventDateRange = view.findViewById(R.id.textview_createevent_showdaterange)

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // load previously done changes
        tilEventName.editText?.setText(DataStore.tmpCreateEvent.name)
        tilEventDesc.editText?.setText(DataStore.tmpCreateEvent.description)

        val eventDateRangeString = timeInSecondsToDateString(DataStore.tmpCreateEvent.startDate.seconds) +
                " - " + timeInSecondsToDateString(DataStore.tmpCreateEvent.endDate.seconds)

        tvEventDateRange.text = eventDateRangeString

        chosenEventStartTime = DataStore.tmpCreateEvent.startDate.seconds * makeMilSec
        chosenEventEndTime = DataStore.tmpCreateEvent.endDate.seconds * makeMilSec



        // Date Range Picker section start
        val datePicker = requireView().findViewById<TextView>(R.id.textview_createevent_showdaterange)

        val builder =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select start and end date")
                .setTheme(R.style.MaterialCalendarTheme)
                .setSelection(
                    androidx.core.util.Pair(
                        chosenEventStartTime,
                        chosenEventEndTime
                    )
                )

        val dateRangePicker = builder.build()

        datePicker.setOnClickListener{
            dateRangePicker.show(activity?.supportFragmentManager!!, dateRangePicker.toString())
        }

        dateRangePicker.addOnCancelListener{
            // do nothing
        }

        dateRangePicker.addOnPositiveButtonClickListener {
            datePicked ->
            val startDate = datePicked.first
            val endDate = datePicked.second

            chosenEventStartTime = startDate
            chosenEventEndTime = endDate

            DataStore.tmpCreateEvent.startDate = convertLongToTimestamp(startDate)
            DataStore.tmpCreateEvent.endDate = convertLongToTimestamp(endDate)

            val eventDateRangeString = timeInSecondsToDateString(DataStore.tmpCreateEvent.startDate.seconds) +
                    " - " + timeInSecondsToDateString(DataStore.tmpCreateEvent.endDate.seconds)

            tvEventDateRange.text = eventDateRangeString

        }
        // Date Range Picker section end


        //ON CLICK ADD Participant
        val editParticipant: Button = requireView().findViewById(R.id.button_createevent_editparticipants)
        editParticipant.setOnClickListener {

            // safe already made changes (else done changes will be removed)
            val name = tilEventName.editText?.text.toString()
            val desc = tilEventDesc.editText?.text.toString()
            val participantsIds: List<String> = DataStore.tempCreateEventParticipants

            DataStore.tmpCreateEvent = GroupEvent("someID", name,
                participantsIds, DataStore.userId,
                desc, convertLongToTimestamp(chosenEventStartTime), convertLongToTimestamp(chosenEventEndTime) )

            val action = CreateEventFragmentDirections.actionCreateEventFragmentToEditContactsCreateEventFragment()
            findNavController().navigate(action)
        }

        // ON CLICK CREATE EVENT
        val createEventButton: Button = requireView().findViewById(R.id.button_createevent_createevent)
        createEventButton.setOnClickListener {
            // TO DO GET DATA FROM THE INPUT AND CREATE THE EVENT

            // create new event to push to firebase
            val name = tilEventName.editText?.text.toString()
            val desc = tilEventDesc.editText?.text.toString()
            DataStore.tempCreateEventParticipants.add(DataStore.userId)
            val participantsIds: List<String> = DataStore.tempCreateEventParticipants
            val newEvent = GroupEvent("someID", name,
                participantsIds, DataStore.userId,
                desc, convertLongToTimestamp(chosenEventStartTime), convertLongToTimestamp(chosenEventEndTime) )

            runBlocking {
                val answer = async { Fetcher.createEvent(newEvent) }
                answer.await()
            }

            val action = CreateEventFragmentDirections.actionCreateEventFragmentToEventsFragment()
            findNavController().navigate(action)
        }
    }

}
