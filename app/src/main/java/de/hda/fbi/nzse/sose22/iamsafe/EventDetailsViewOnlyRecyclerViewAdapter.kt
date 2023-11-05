package de.hda.fbi.nzse.sose22.iamsafe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class EventDetailsViewOnlyRecyclerViewAdapter(
    private val onItemClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<EventDetailsViewOnlyRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View, private val onItemClicked: (position: Int) -> Unit) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var contactTextView : TextView

        init {
            contactTextView = itemView.findViewById(R.id.textview_recyclerviewelementeditcontactsnotowner_participantname)
        }

        override fun onClick(v: View){
            val position = layoutPosition //to do get adapterPosition (index of list item)
            onItemClicked(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.fragment_recyclerview_element_view_participants, parent, false)
        return ViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.contactTextView.text = DataStore.tempCurrentParticipants[position].name

    }

    override fun getItemCount(): Int {
        return DataStore.tempCurrentParticipants.count()
    }


}