package com.example.journalapp

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.journalapp.adapter.EntryAdapter
import com.example.journalapp.adapter.JournalAdapter
import com.example.journalapp.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import com.example.journalapp.data.database.AppDatabase
import com.example.journalapp.data.entity.Entry
import com.example.journalapp.data.entity.Journal

class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    //Reference for all views in the layout.
    private lateinit var binding: ActivityMainBinding
    //Adapter references for Recycler View.
    private lateinit var journalAdapter: JournalAdapter
    private lateinit var entryAdapter: EntryAdapter
    //Reference for MainViewModel.
    private val viewModel: MainViewModel by viewModels()
    /*
    onCreate function called at the start of the activity.
    Initializes all reference variables.
    Sets up all components for the activity.
    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(applicationContext)
        //Converts XML layout into view objects and initializes binding.
        binding = ActivityMainBinding.inflate(layoutInflater)
        //Attaches layout to the screen.
        setContentView(binding.root)
        //calls setup functions.
        setupAdapters()
        setupRecycler()
        setupToggle()
        setupSearch()
        setupSortSpinner()
        //Sets listener for fab.
        binding.fabMain.setOnClickListener { fabClick() }
        //Calls observeData function.
        observeData()
    }
    //Sets click behavior for Journal and Entry recycler view items.
    private fun setupAdapters() {

        journalAdapter = JournalAdapter(
            //displays entries for the relevant journal and toggles to entry mode.
            onCardClick = { journal ->
                viewModel.selectJournal(journal)
                binding.toggleViewMode.check(binding.btnToggleEntries.id)
            },
            //opens the journal for detailed viewing or editing.
            onViewClick = { journal -> openJournal(journal) },
            //starts delete dialogue.
            onDeleteClick = { journal -> confirmDeleteJournal(journal) }
        )

        entryAdapter = EntryAdapter(
            //opens the entry for detailed viewing or editing.
            onCardClick = { entry -> openEntry(entry) },
            onViewClick = { entry -> openEntry(entry) },
            //starts delete dialogue.
            onDeleteClick = { entry -> confirmDeleteEntry(entry) }
        )
    }
    //Sets up recycler view component.
    private fun setupRecycler() {
        //assigns a linear layout manager to the recycler view which displays items vertically.
        binding.recyclerMain.layoutManager = LinearLayoutManager(this)
        //sets default adapter. Journals are displayed on start.
        binding.recyclerMain.adapter = journalAdapter
    }
    //Sets up toggle functionality.
    private fun setupToggle() {
        //sets journal toggle to active on start.
        binding.toggleViewMode.check(binding.btnToggleJournals.id)
        viewModel.setJournalMode(true)
        //Sets listener behavior for the toggle button.
        binding.toggleViewMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            //skips 'unchecking' events.
            if (!isChecked) return@addOnButtonCheckedListener
            //executes different actions depending on which button is checked.
            when (checkedId) {
                //on journal toggle, updates view model and attaches journal adapter to the recycler view.
                binding.btnToggleJournals.id -> {
                    viewModel.setJournalMode(true)
                    binding.recyclerMain.adapter = journalAdapter
                    binding.chipActiveFilter.visibility = View.GONE
                }
                //on entry toggle, updates view model and attaches entry adapter to the recycler view.
                binding.btnToggleEntries.id -> {
                    viewModel.setJournalMode(false)
                    binding.recyclerMain.adapter = entryAdapter
                }
            }
        }
    }
    //Sets up search component.
    private fun setupSearch() {
        //attaches a text change listener to the search component, updates the view model when it changes.
        binding.editSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.setSearchQuery(text?.toString().orEmpty())
        }
    }

    private fun setupSortSpinner() {
        //Creates an arrayAdapter and attaches it to the spinner component.
        val adapter = ArrayAdapter.createFromResource(this, R.array.spinner_sort, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = adapter
        //Sets default position to 0.
        binding.spinnerSort.setSelection(0)
        //Attaches a selection listener. Requires two methods: onItemSelected and onNothingSelected.
        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                //updates the view model depending on which sort option is selected.
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when (position) {
                        0 -> viewModel.setSortOrder("timestamp DESC")
                        1 -> viewModel.setSortOrder("timestamp ASC")
                        2 -> viewModel.setSortOrder("title ASC")
                        3 -> viewModel.setSortOrder("title DESC")
                    }
                }
                //does nothing since something is always selected. Required by listener.
                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }
    }
    //Observes data flows from the view model and executes actions depending on the data.
    private fun observeData() {
        //collects the data flow from journals. Send the received list to the journal adapter which updates the recycler view.
        lifecycleScope.launch { viewModel.journals.collectLatest { list ->
                journalAdapter.submitList(list)
                //displays a message if there are no journals.
                if (viewModel.isJournalMode.value) {
                    showEmptyMessage(list.isEmpty(), true)
                }
            }
        }
        //collects the data flow from entries. Send the received list to the entry adapter which updates the recycler view.
        lifecycleScope.launch { viewModel.entries.collectLatest { list ->
                entryAdapter.submitList(list)
                //displays a message if there are no entries.
                if (!viewModel.isJournalMode.value) {
                    showEmptyMessage(list.isEmpty(), false)
                }
            }
        }
        //collects the selected journal flow. Updates the chip component depending on the value.
        lifecycleScope.launch { viewModel.selectedJournal.collectLatest { journal ->
                //if the received value is a journal (i.e. not null). The chip is made visible and the title is displayed.
                //else the chip's visibility is set to gone.
                if (journal != null) {
                    binding.chipActiveFilter.text =  getString(R.string.selected_journal, journal.title)
                    binding.chipActiveFilter.visibility = View.VISIBLE
                } else {
                    binding.chipActiveFilter.visibility = View.GONE
                }
            }
        }
        binding.chipActiveFilter.setOnCloseIconClickListener { viewModel.clearSelectedJournal() }
    }
    /*
    Used to change the text and visibility of a textview component.
    This textview component is used to give the user basic information.
     */
    private fun showEmptyMessage(isEmpty: Boolean, isJournalMode: Boolean) {
        binding.textEmptyMessage.visibility =
            if (isEmpty) {
                View.VISIBLE
            } else {
                View.GONE
            }

        if (isEmpty) {
            binding.textEmptyMessage.text =
                if (isJournalMode) {
                    "Create a new journal to get started"
                } else {
                    "No entries found"
                }
        }
    }
    /*
    start activity functions for CreateJournalActivity and CreateEntryActivity.
    openJournal + openEntry are used by adapters to start the activities using existing journal/entry data to view them.
    createJournal + createEntry are used by fab to start the activities to create new journal/entry data.
    Intents are used to send data between activities.
     */
    private fun openJournal(journal: Journal) {
        val intent = Intent(this, CreateJournalActivity::class.java)
        intent.putExtra("journalId", journal.id)
        intent.putExtra("title", journal.title)
        intent.putExtra("description", journal.description)
        intent.putExtra("timestamp", journal.timestamp)

        startActivity(intent)
    }

    private fun openEntry(entry: Entry) {
        val intent = Intent(this, CreateEntryActivity::class.java)
        intent.putExtra("entryId", entry.id)
        intent.putExtra("journalId", entry.journalId)
        intent.putExtra("title", entry.title)
        intent.putExtra("content", entry.content)
        intent.putExtra("timestamp", entry.timestamp)
        intent.putExtra("journalTitle", entry.journalTitle)


        startActivity(intent)
    }

    private fun createJournal() {
        startActivity(Intent(this, CreateJournalActivity::class.java))
    }

    private fun createEntry(preselectedJournalId: Int?) {
        val intent = Intent(this, CreateEntryActivity::class.java)
        preselectedJournalId?.let { intent.putExtra("journalId", it) }

        startActivity(intent)
    }
    //Confirmation dialogs for journal and entry deletion. Used by adapters.
    private fun confirmDeleteJournal(journal: Journal) {
        AlertDialog.Builder(this)
            //Title and message of dialog
            .setTitle("Delete Journal").setMessage("Are you sure you want to delete this journal and all its entries?")
            //Confirm button
            .setPositiveButton("Confirm") { _, _ ->
                lifecycleScope.launch {
                    db.journalDao().delete(journal)
                }
                //Confirmation of deletion message.
                Toast.makeText(this, "${journal.title} and all its entries have been deleted.", Toast.LENGTH_SHORT).show()
            }
            //Cancel button
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteEntry(entry: Entry) {
        AlertDialog.Builder(this)
            //Title and message of dialog
            .setTitle("Delete Entry").setMessage("Are you sure you want to delete this entry?")
            //Confirm button
            .setPositiveButton("Confirm") { _, _ ->
                lifecycleScope.launch {
                    db.entryDao().delete(entry)
                }
                //Confirmation of deletion message.
                Toast.makeText(this, "${entry.title} has been deleted.", Toast.LENGTH_SHORT).show()
            }
            //Cancel button
            .setNegativeButton("Cancel", null)
            .show()
    }
    //handles what happens when the fab is clicked.
    private fun fabClick() {
        //Gets state variables from MainViewModel.
        val isJournalMode = viewModel.isJournalMode.value
        val selectedJournal = viewModel.selectedJournal.value

        lifecycleScope.launch {
            //Journal count for context.
            val journalCount = db.journalDao().getJournalsCount()
            //When statement - performs different action depending on context.
            when {
                isJournalMode -> createJournal() //Fab should launch the CreateJournalActivity is journal mode is toggled.
                journalCount == 0 -> createJournal() //Fab should launch the CreateJournalActivity if there are no journals.
                //Considering the above cases are false, fab should launch the CreateEntryActivity, either passing a journalId or null value (if no journal is selected).
                selectedJournal == null -> createEntry(null)
                else -> createEntry(selectedJournal.id)
            }
        }
    }
}