package de.hda.fbi.nzse.sose22.iamsafe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*

class ContactsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        runBlocking {
            val answer = async { Fetcher.getContacts() }
            answer.await()
        }

        val view : View = inflater.inflate(R.layout.fragment_contacts, container, false)
        val recyclerViewSafeContacts : RecyclerView = view.findViewById(R.id.recyclerview_contacts_markedassafecontactlist)
        val recyclerViewUnknownStatusContacts : RecyclerView = view.findViewById(R.id.recyclerview_contacts_unkownstatuscontactslist)

        val adapter = SafeContactsAdapter{
                position -> onListItemClick(position, true)
        }

        val unknownStatusAdapter = UnknownStatusContactsAdapter {
                position -> onListItemClick(position, false)
        }

        recyclerViewSafeContacts.adapter = adapter
        recyclerViewUnknownStatusContacts.adapter = unknownStatusAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ADD CONTACT
        //Set action for addContact button to navigate to the Options_Fragment
        val buttonAddContact: FloatingActionButton = view.findViewById(R.id.floatingactionbutton_contacts_addcontact)
        buttonAddContact.setOnClickListener {
            runBlocking {
                val shortLink = async { Fetcher.getAddMeContactDynamicLink()}
                shareDeepLink(shortLink.await())
            }
        }

    }

    // For the onClick function of the Contact recyclerView
    private fun onListItemClick(pos: Int, statusKnown : Boolean){
        //Toast.makeText(context, "Detail View" , Toast.LENGTH_SHORT).show();
        Log.d("Position", pos.toString())
        val action = ContactsFragmentDirections.actionContactsFragmentToContactDetailViewFragment(pos, statusKnown)
        findNavController().navigate(action)
    }


    private fun shareDeepLink(deepLink: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Invite people to add you as contact")
        intent.putExtra(Intent.EXTRA_TEXT, deepLink)

        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
    }
}