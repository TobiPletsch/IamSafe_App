package de.hda.fbi.nzse.sose22.iamsafe

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*


class ContactDetailViewFragment : Fragment() {
    private val args: ContactDetailViewFragmentArgs by navArgs()

    private lateinit var currentContact : User

    private lateinit var btnOpenMaps : Button
    private lateinit var btnMuteContact : Button
    private lateinit var btnDisableLocationSharing : Button
    private lateinit var btnDeleteContact : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_contact_detail_view, container, false)

       btnOpenMaps = view.findViewById(R.id.button_contactdetailview_opengooglemaps)!!
        btnOpenMaps.setOnClickListener {
            openMaps()
        }

        btnMuteContact = view.findViewById(R.id.button_contactdetailview_mutecontact)!!
        btnMuteContact.setOnClickListener {
            handleMute()
        }

        btnDisableLocationSharing = view.findViewById(R.id.button_contactdetailview_stoplocationsharing)!!
        btnDisableLocationSharing.setOnClickListener {
            handleShareLocation()
        }

        btnDeleteContact = view.findViewById(R.id.button_contactdetailview_deletecontact)!!
        btnDeleteContact.setOnClickListener {
            handleDelete()
        }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isKnownStatus = args.statusKnown
        currentContact = if(isKnownStatus){
            DataStore.contacts.filter { !isLastStatusSetLongAgo(it.lastStatusSetAt?.toDate()) }[args.index]
        } else {
            DataStore.contacts.filter { isLastStatusSetLongAgo(it.lastStatusSetAt?.toDate()) }[args.index]
        }

        setData()
    }

    private fun openMaps(){
        if(currentContact.locationBlockedUserIds.contains(DataStore.userId)){
            Toast.makeText(context, "No location available", Toast.LENGTH_SHORT).show()
        } else{
            val link = "https://www.google.de/maps/place/" + currentContact.lastStatusLocation.latitude.toString() +
                    "N+" + currentContact.lastStatusLocation.longitude.toString() +"E"
            val uri: Uri = Uri.parse(link)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    private fun handleMute(){
        if(DataStore.user.notificationBlacklistUserIds.contains(currentContact.id)){
            //User is muted, remove from list
            DataStore.user.notificationBlacklistUserIds.remove(currentContact.id)
        } else{
            //User is not muted, add to list
            DataStore.user.notificationBlacklistUserIds.addElement(currentContact.id)
        }
        setMuteIcon()
        runBlocking {
            val answer = async { Fetcher.saveUserOptions() }
            answer.await()
        }
    }

    //Check if this Contact is muted or not and set the icon
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setMuteIcon(){
        val materialTextView = view?.findViewById(R.id.textview_contactdetailview_mutecontact) as MaterialTextView
        val materialButton = view?.findViewById(R.id.button_contactdetailview_mutecontact) as MaterialButton
        if(DataStore.user.notificationBlacklistUserIds.contains(currentContact.id)){
            //User is muted, set to unmute icon
            val drawable = requireContext().resources.getDrawable(android.R.drawable.ic_lock_silent_mode,requireContext().theme)
            materialButton.icon = drawable
            materialTextView.text = getString(R.string.contact_detailview_mtf_unmuteContact)
        } else{
            //User is not muted, set to unmute icon
            val drawable = requireContext().resources.getDrawable(android.R.drawable.ic_lock_silent_mode_off,requireContext().theme)
            materialButton.icon = drawable
            materialTextView.text = getString(R.string.contact_detailview_mtf_muteContact)
        }
    }

    private fun handleShareLocation(){
        if(DataStore.user.locationBlockedUserIds.contains(currentContact.id)){
            //Location sharing is blocked, remove from list
            DataStore.user.locationBlockedUserIds.remove(currentContact.id)
        } else{
            //Location sharing is not blocked, add to list
            DataStore.user.locationBlockedUserIds.addElement(currentContact.id)
        }
        setLocationIcon()
        runBlocking {
            val answer = async { Fetcher.saveUserOptions() }
            answer.await()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables") // causes issues that by using belows method will not appear
    private fun setLocationIcon(){
        val materialTextView = view?.findViewById(R.id.textview_contactdetailview_stoplocationsharing) as MaterialTextView
        val materialButton = view?.findViewById(R.id.button_contactdetailview_stoplocationsharing) as MaterialButton
        if(DataStore.user.locationBlockedUserIds.contains(currentContact.id)){
            //Location sharing is blocked, set to unblock icon
            val drawable = requireContext().resources.getDrawable(R.drawable.ic_location_stop_sharing,requireContext().theme) //SET RIGHT ICON HERE
            materialButton.icon = drawable
            materialTextView.text =  getString(R.string.contact_detailview_btn_enableLocSharing)
        } else{
            //Location sharing is not blocked, set to block icon
            val drawable = requireContext().resources.getDrawable(R.drawable.ic_location_sharing,requireContext().theme)
            materialButton.icon = drawable
            materialTextView.text = getString(R.string.contact_detailview_btn_disableLocSharing)
        }
    }

    private fun handleDelete(){
        val contactId = currentContact.id

        runBlocking {
            val answer = async { Fetcher.removeContact(contactId) }
            answer.await()
        }
        // DataStore.deleteContact(args.index, args.statusKnown)

        // action can be found in navigation_bottom.xml -> is an edge in the graph
        findNavController().navigate(ContactDetailViewFragmentDirections.actionContactDetailViewFragmentToContactsFragment())
    }

    private fun setData(){
        //Set Name
        val materialTextView = view?.findViewById(R.id.textview_contactdetailview_contactname) as MaterialTextView
        materialTextView.text= currentContact.name
        //Set last update
        setLastUpdate()
        setMuteIcon()
        setLocationIcon()
    }

    private fun setLastUpdate(){
       val minAgo = getMinutesAgo(currentContact.lastStatusSetAt?.toDate(), Date())
        val status = view?.findViewById(R.id.statusLabelContactDetailStatus) as TextView
        val lastUpdate = view?.findViewById(R.id.textview_safe_statusdate) as TextView

        // Set as unknown if older than 48 hours
        val minutesIn48Hours = 48 * 60
        if(minAgo > minutesIn48Hours){
            status.text = ""
            lastUpdate.text = getString(R.string.contact_detailview_lastupdate_unknown)
        } else{
            lastUpdate.text = dateToRelativeTime(currentContact.lastStatusSetAt?.toDate())
        }
    }

}