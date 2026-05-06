package com.example.journalapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.journalapp.R
import com.example.journalapp.data.entity.Entry
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

// Class takes three lambdas as arguments.
class EntryAdapter(
    private val onCardClick: (Entry) -> Unit,
    private val onViewClick: (Entry) -> Unit,
    private val onDeleteClick: (Entry) -> Unit
) : RecyclerView.Adapter<EntryAdapter.EntryViewHolder>() {
    //entry variable initialization.
    private var entries: List<Entry> = emptyList()
    //updates entry variable and refreshes the recycler view.
    fun submitList(list: List<Entry>) {
        entries = list
        notifyItemRangeChanged(0, entries.size)
    }

    //called when a new item is needed for the recycler view. Converts the entry item layout into a view object and returns that object.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_entry, parent, false)
        return EntryViewHolder(view)
    }

    //binds entry data at the given position to the view holder.
    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    //used by recycler view to determine how many rows it should display.
    override fun getItemCount(): Int = entries.size

    inner class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //reference variables for each relevant view on the entry item component.
        private val title = itemView.findViewById<MaterialTextView>(R.id.textEntryTitle)
        private val journalParent = itemView.findViewById<MaterialTextView>(R.id.textJournalParent)
        private val preview = itemView.findViewById<MaterialTextView>(R.id.textEntryPreview)
        private val date = itemView.findViewById<MaterialTextView>(R.id.textEntryDate)
        private val btnView = itemView.findViewById<MaterialButton>(R.id.btnEditEntry)
        private val btnDelete = itemView.findViewById<MaterialButton>(R.id.btnDeleteEntry)

        fun bind(entry: Entry) {
            //binds entry data to the relevant views.
            title.text = entry.title
            journalParent.text = entry.journalTitle
            preview.text = entry.content
            val formatter = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            date.text = formatter.format(java.util.Date(entry.timestamp))

            //attaches listeners to item buttons which calls the corresponding lambda.
            itemView.setOnClickListener { onCardClick(entry) }
            btnView.setOnClickListener { onViewClick(entry) }
            btnDelete.setOnClickListener { onDeleteClick(entry) }
        }
    }
}