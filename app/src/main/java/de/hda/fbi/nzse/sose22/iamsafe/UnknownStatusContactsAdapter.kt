package de.hda.fbi.nzse.sose22.iamsafe
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UnknownStatusContactsAdapter(private val onItemClicked: (position: Int) -> Unit) : RecyclerView.Adapter<UnknownStatusContactsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.fragment_recyclerview_element_unknownstatus_contact, parent, false)
        return ViewHolder(view, onItemClicked)
    }

    inner class ViewHolder(itemView: View, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var itemTitle: TextView

        init {
            itemTitle = itemView.findViewById(R.id.textview_unknownstatus_contactname)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View){
            val position = layoutPosition //to do get adapterPosition (index of list item)
            onItemClicked(position)
        }
    }

    override fun getItemCount() =
        DataStore.contacts.count { isLastStatusSetLongAgo(it.lastStatusSetAt?.toDate()) }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        DataStore.contacts.filter{ isLastStatusSetLongAgo(it.lastStatusSetAt?.toDate())}[position].name.also { viewHolder.itemTitle.text = it }
    }

}