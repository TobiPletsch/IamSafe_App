package de.hda.fbi.nzse.sose22.iamsafe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ExpiredEventsRecyclerViewAdapter(private val onItemClicked: (position: Int) -> Unit) : RecyclerView.Adapter<ExpiredEventsRecyclerViewAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var itemTitle: TextView

        init {
            itemTitle = itemView.findViewById(R.id.textview_unknownstatus_contactname)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = layoutPosition //What to use here
            onItemClicked(position)
        }
    }

    // constructs recyclerView
    override fun onCreateViewHolder( parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.fragment_recyclerview_element_unknownstatus_contact, parent, false)
        return ViewHolder(view, onItemClicked)
    }

    // Determines what the content should be
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemTitle.text = DataStore.events.filter { item -> isDatePast(item.endDate.toDate())}[position].name

    }

    override fun getItemCount(): Int {
        return DataStore.events.count { item -> isDatePast(item.endDate.toDate()) }
    }
}