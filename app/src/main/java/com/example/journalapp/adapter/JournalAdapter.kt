package com.example.journalapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.journalapp.R
import com.example.journalapp.data.entity.Journal
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

// Class takes three lambdas as arguments.
class JournalAdapter(
    private val onCardClick: (Journal) -> Unit,
    private val onViewClick: (Journal) -> Unit,
    private val onDeleteClick: (Journal) -> Unit
) : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {
    //journals variable initialization.
    private var journals: List<Journal> = emptyList()
    //updates journal variable and refreshes the recycler view.
    fun submitList(list: List<Journal>) {
        journals = list
        notifyItemRangeChanged(0, journals.size)
    }

    //called when a new item is needed for the recycler view. Converts the journal item layout into a view object and returns that object.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_journal, parent, false)
        return JournalViewHolder(view)
    }

    //binds journal data at the given position to the view holder.
    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        holder.bind(journals[position])
    }

    //used by recycler view to determine how many rows it should display.
    override fun getItemCount(): Int = journals.size

    inner class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //reference variables for each relevant view on the journal item component.
        private val title = itemView.findViewById<MaterialTextView>(R.id.textJournalTitle)
        private val description = itemView.findViewById<MaterialTextView>(R.id.textJournalDescription)
        private val date = itemView.findViewById<MaterialTextView>(R.id.textJournalDate)
        private val btnView = itemView.findViewById<MaterialButton>(R.id.btnEditJournal)
        private val btnDelete = itemView.findViewById<MaterialButton>(R.id.btnDeleteJournal)

        fun bind(journal: Journal) {
            //binds journal data to the relevant views.
            title.text = journal.title
            description.text = journal.description
            val formatter = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            date.text = formatter.format(java.util.Date(journal.timestamp))

            //attaches listeners to item buttons which calls the corresponding lambda.
            itemView.setOnClickListener { onCardClick(journal) }
            btnView.setOnClickListener { onViewClick(journal) }
            btnDelete.setOnClickListener { onDeleteClick(journal) }
        }
    }
}