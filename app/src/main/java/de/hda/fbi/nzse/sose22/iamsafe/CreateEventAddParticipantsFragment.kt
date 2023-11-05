package de.hda.fbi.nzse.sose22.iamsafe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class CreateEventAddParticipantsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        runBlocking {
            val answer = async {
                 Fetcher.getContacts()
            }
            answer.await()
        }

        val view : View = inflater.inflate(R.layout.fragment_add_participants_create_event, container, false)
        val recyclerViewEditContacts : RecyclerView = view.findViewById(R.id.recyclerview_addparticipantscreateevent_participantlist)

        recyclerViewEditContacts.adapter = CreateEventAddParticipantsRecyclerViewAdapter {
        onListItemClick()
        }

        return view
    }

    private fun onListItemClick() {
    }
}