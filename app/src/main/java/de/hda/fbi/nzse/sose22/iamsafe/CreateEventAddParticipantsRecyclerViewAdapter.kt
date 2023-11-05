package de.hda.fbi.nzse.sose22.iamsafe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView

class CreateEventAddParticipantsRecyclerViewAdapter(private val onItemClicked: (position: Int) -> Unit)
                                            : RecyclerView.Adapter<CreateEventAddParticipantsRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var contactCheckBox : CheckBox

        init {
            contactCheckBox = itemView.findViewById(R.id.checkbox_recyclerviewelementeditcontactstowner_participantname)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View){
            val position = layoutPosition //to do get adapterPosition (index of list item)
            onItemClicked(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val layout : Int = R.layout.fragment_recyclerview_element_edit_participants
        val view = layoutInflater.inflate(layout, parent, false)
        return ViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            val isParticipant : Boolean = evaluateParticipant(DataStore.contacts[position].id)
            viewHolder.contactCheckBox.isChecked = isParticipant
            viewHolder.contactCheckBox.text = DataStore.contacts[position].name

            viewHolder.contactCheckBox.setOnClickListener {
                if (viewHolder.contactCheckBox.isChecked) {
                    DataStore.tempCreateEventParticipants.add(DataStore.contacts[position].id)

                } else {
                    DataStore.tempCreateEventParticipants.remove(DataStore.contacts[position].id)
                }
            }

    }

    override fun getItemCount(): Int {
        return DataStore.contacts.count()
    }

    private fun evaluateParticipant(id : String) : Boolean {
        // if no participants, return false
        if(DataStore.tempCreateEventParticipants.isEmpty())
            return false

        // return true if participant
        DataStore.tempCreateEventParticipants.forEach { if(it == id ) return true }

        // coming to this point means no match found -> no participant
        return false

    }
}