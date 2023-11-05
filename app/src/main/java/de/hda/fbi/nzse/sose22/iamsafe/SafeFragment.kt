@file:Suppress("DEPRECATION")

package de.hda.fbi.nzse.sose22.iamsafe

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.CallLog
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.security.auth.callback.Callback


class SafeFragment : Fragment() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    var selectedEventId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationProviderClient= activity?.let {
            LocationServices.getFusedLocationProviderClient(
                it
            )
        }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        runBlocking {
            val answer = async {
                Fetcher.getMe()

                if(!DataStore.addContactIdOnLaunch.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Please wait a moment while we add a contact...", Toast.LENGTH_LONG).show()
                    Fetcher.addContact(DataStore.addContactIdOnLaunch!!, requireContext())
                }

                Fetcher.getEvents()
            }
            answer.await()
            DataStore.addContactIdOnLaunch = null
        }

        val view = inflater.inflate(R.layout.fragment_safe, container, false)

        // --- Initialize Dropdown Menu (Spinner) for selected event ---
        val lastStatusSetAtTime: TextView = view.findViewById(R.id.textview_safe_statusdate)
        val spinner: Spinner = view.findViewById(R.id.spinner_safe_eventsselectionsafe)

        val items = Vector<String>()
        items.add("All Contacts")

        for (event in DataStore.events.filter { !isDatePast(it.endDate.toDate()) }) {
            items.add(event.name)
        }

        spinnerAdapter = ArrayAdapter(requireContext().applicationContext, android.R.layout.simple_spinner_dropdown_item, items)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedEventId = if(position > 0) {
                    DataStore.events.filter { !isDatePast(it.endDate.toDate()) }[position-1].id
                } else {
                    ""
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                return
            }
        }
        // ---

        // Update text to when the last status was set
        if(DataStore.user.lastStatusSetAt != null) {
            lastStatusSetAtTime.text = dateToRelativeTime(DataStore.user.lastStatusSetAt!!.toDate())
        }

        val safeButton: MaterialButton = view.findViewById(R.id.button_safe_iamsafe)
        safeButton.setOnLongClickListener {
            handleSafeButtonClick(safeButton)
        }

        return view
    }

    private fun handleSafeButtonClick(button: MaterialButton): Boolean {

        /* Use of real location
        getCurrentLocation{
            runBlocking {
                val answer = async { Fetcher.markMeAsSafe(selectedEventId, it) }
                answer.await()
            }
        }*/

        runBlocking {
            val answer = async { Fetcher.markMeAsSafe(selectedEventId) }
            answer.await()
        }

        button.setBackgroundColor(button.resources.getColor(R.color.safe_button_success))
       // Toast.makeText(context?.applicationContext, "Status set!", Toast.LENGTH_SHORT).show()

        val lastStatusSetAtTime: TextView? = view?.findViewById(R.id.textview_safe_statusdate)
        lastStatusSetAtTime?.text = getString(R.string.safe_statusSet_justnow)

        Handler().postDelayed({
            button.setBackgroundColor(button.resources.getColor(R.color.cyan_600))
        }, 2000)

        runBlocking {
            val answer = async { Fetcher.getMe() }
            answer.await()
        }
        return true
    }

    //Get Location Functions
    private fun getCurrentLocation(callback: (GeoPoint) -> Unit){
        if(checkPermissions()) {
            if(isLocationEnabled()){
                //get location
                if (activity?.let {
                        ActivityCompat.checkSelfPermission(
                            it,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    } != PackageManager.PERMISSION_GRANTED && activity?.let {
                        ActivityCompat.checkSelfPermission(
                            it,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    } != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    callback(GeoPoint(53.0, 13.0))
                }

                fusedLocationProviderClient.lastLocation.addOnCompleteListener(requireActivity()){ task->
                    val location: Location?=task.result
                    if(location==null) {
                        //Cant get location, set to 0,0
                        callback(GeoPoint(53.0, 13.0))
                    } else{
                        val point = GeoPoint(location.latitude, location.longitude)
                        callback(point)
                    }
                }

            } else {
                //open settings
                Toast.makeText(context, "Turn on location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(intent)
            }
        } else {
            //request premission
            requestPermission()
            callback(GeoPoint(53.0, 13.0))
        }
        //callback(GeoPoint(53.0, 13.0))
    }

    private fun isLocationEnabled(): Boolean{
        val locationManager: LocationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermission() {
        activity?.let {
            ActivityCompat.requestPermissions(
                it, arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_ACCESS_LOCATION
            )
        }
    }

    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    private fun checkPermissions(): Boolean{
        if(activity?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) }
            == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== PERMISSION_REQUEST_ACCESS_LOCATION){
            if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(activity?.applicationContext, "Granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation{

                }
            } else{
                Toast.makeText(activity?.applicationContext, "Denied", Toast.LENGTH_SHORT).show()

            }
        }
    }
}