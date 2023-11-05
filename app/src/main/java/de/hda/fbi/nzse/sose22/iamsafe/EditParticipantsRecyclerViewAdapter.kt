package de.hda.fbi.nzse.sose22.iamsafe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView


class EditParticipantsRecyclerViewAdapter(private val thisEvent: GroupEvent, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.Adapter<EditParticipantsRecyclerViewAdapter.ViewHolder>() {


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
        // if owner of event, use checkbox layout, else use textview
        val view = layoutInflater.inflate(R.layout.fragment_recyclerview_element_edit_participants, parent, false)
        return ViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val isParticipantOfEvent =
                !thisEvent.participantsIds.find { it == DataStore.tempCurrentParticipants[position].id }
                    .isNullOrEmpty()

            viewHolder.contactCheckBox.isChecked = isParticipantOfEvent
            viewHolder.contactCheckBox.text = DataStore.tempCurrentParticipants[position].name

            viewHolder.contactCheckBox.setOnClickListener {
                if (viewHolder.contactCheckBox.isChecked) {

                    if(DataStore.tempUpdateEventParticipantIds.indexOfFirst { it == DataStore.tempCurrentParticipants[position].id } == -1) {
                        DataStore.tempUpdateEventParticipantIds.add(DataStore.tempCurrentParticipants[position].id)
                    }
                } else {

                    DataStore.tempUpdateEventParticipantIds.remove(DataStore.tempCurrentParticipants[position].id)
                }
            }


    }

    override fun getItemCount(): Int {
        return DataStore.tempCurrentParticipants.count()
    }



}