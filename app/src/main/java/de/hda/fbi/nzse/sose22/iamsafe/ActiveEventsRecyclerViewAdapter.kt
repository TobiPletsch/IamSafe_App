package de.hda.fbi.nzse.sose22.iamsafe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ActiveEventsRecyclerViewAdapter(private val onItemClicked: (position: Int) -> Unit) :
    RecyclerView.Adapter<ActiveEventsRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View, private val onItemClicked: (position: Int) -> Unit) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var itemTitle: TextView
        var itemAttendeeCount: TextView

        init {
            itemTitle = itemView.findViewById(R.id.textview_activeeventitem_name)
            itemAttendeeCount =
                itemView.findViewById(R.id.textview_activeeventitem_participantcount)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = layoutPosition
            onItemClicked(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.fragment_recyclerview_element_active_event, parent, false)
        return ViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val groupEvent: GroupEvent =
            DataStore.events.filter { item -> !isDatePast(item.endDate.toDate()) }[position]
        viewHolder.itemTitle.text = groupEvent.name
        viewHolder.itemAttendeeCount.text = groupEvent.participantsIds.size.toString()
    }

    override fun getItemCount(): Int {
        return DataStore.events.count { item -> !isDatePast(item.endDate.toDate()) }
    }
}