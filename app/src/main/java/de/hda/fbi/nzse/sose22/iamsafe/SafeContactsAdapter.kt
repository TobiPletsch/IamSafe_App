package de.hda.fbi.nzse.sose22.iamsafe
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SafeContactsAdapter(private val onItemClicked: (position: Int) -> Unit) : RecyclerView.Adapter<SafeContactsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.fragment_safe_contact, parent, false)
        return ViewHolder(view, onItemClicked)
    }

    inner class ViewHolder(itemView: View, private val onItemClicked: (position: Int) -> Unit) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var contactName: TextView
        var contactStatusTime: TextView

        init {
            contactName = itemView.findViewById(R.id.textview_safecontact_name)
            contactStatusTime = itemView.findViewById(R.id.textview_safecontact_statustime)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View){
            val position = layoutPosition //to do get adapterPosition (index of list item)
            onItemClicked(position)
        }
    }

    override fun getItemCount() =
        DataStore.contacts.count { !isLastStatusSetLongAgo(it.lastStatusSetAt?.toDate()) }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val safeContacts = DataStore.contacts.filter { !isLastStatusSetLongAgo(it.lastStatusSetAt?.toDate())}

        viewHolder.contactName.text = safeContacts[position].name

        viewHolder.contactStatusTime.text =
            dateToRelativeTime(safeContacts[position].lastStatusSetAt?.toDate())
    }

}