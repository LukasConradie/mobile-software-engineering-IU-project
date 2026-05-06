package com.example.journalapp

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.journalapp.data.database.AppDatabase
import com.example.journalapp.data.entity.Entry
import com.example.journalapp.data.entity.Journal
import com.example.journalapp.databinding.ActivityCreateEntryBinding
import kotlinx.coroutines.launch

class CreateEntryActivity : AppCompatActivity() {
    //Variable declaration and initialization. (binding and db are initialized in onCreate).
    private lateinit var binding: ActivityCreateEntryBinding
    private lateinit var db: AppDatabase
    private var isEditMode = false
    private var entryId: Int = -1
    private var preselectedJournalId: Int = -1
    private var journals: List<Journal> = emptyList()

    //Runs on activity start.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(applicationContext)

        //Initializes binding with the relevant layout and sets that layout as the content view.
        binding = ActivityCreateEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Tries to get a entryId and preselectedJournalId from the intent message, if none are received the default values are -1.
        entryId = intent.getIntExtra("entryId", -1)
        preselectedJournalId = intent.getIntExtra("journalId", -1)

        //isEditMode is set to true if an existing entryId was received. Changes certain components' visibility depending on this.
        isEditMode = entryId != -1
        if (isEditMode) {
            binding.textEntryDate.visibility = View.VISIBLE
            binding.spinnerJournal.visibility = View.GONE
            binding.textJournalParent.visibility = View.VISIBLE
        } else {
            binding.textEntryDate.visibility = View.GONE
            binding.textJournalParent.visibility = View.GONE
            //sets up spinner if not editing.
            setupSpinner()
        }

        //Calls populateIfEditing and setupButtons.
        populateIfEditing()
        setupButtons()
    }

    //if isEditMode is true the layout views are populated with the received intent data.
    private fun populateIfEditing() {
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val timestamp = intent.getLongExtra("timestamp", -1L)
        val journalTitle= intent.getStringExtra("journalTitle")

        if (isEditMode) {
            binding.editEntryTitle.setText(title)
            binding.editEntryContent.setText(content)
            val formatter = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            binding.textEntryDate.text = formatter.format(java.util.Date(timestamp))
            binding.textJournalParent.text = journalTitle
        }
    }

    //Sets up the spinner component to display all journals for which entries can be created.
    private fun setupSpinner() {
        lifecycleScope.launch {
            db.journalDao().searchJournals("").collect { list ->
                //list of all journals
                journals = list

                //Creates an arrayAdapter and attaches it to the spinner component.
                val adapter = ArrayAdapter(this@CreateEntryActivity, android.R.layout.simple_spinner_item, list.map { it.title })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                //binds the adapter.
                binding.spinnerJournal.adapter = adapter
                binding.spinnerJournal.visibility = View.VISIBLE

                //if a journal was preselected it will be made the selected option in the spinner.
                if (preselectedJournalId != -1) {
                    val index =
                        list.indexOfFirst { it.id == preselectedJournalId }
                    if (index >= 0) {
                        binding.spinnerJournal.setSelection(index)
                    }
                }
            }
        }
    }

    //Sets up the buttons on the CreateEntryActivity with listeners and behavior.
    private fun setupButtons() {
        //attaches a listener to the confirm button.
        binding.btnConfirmJournal.setOnClickListener {
            //gets data from user input.
            val title = binding.editEntryTitle.text.toString()
            val content = binding.editEntryContent.text.toString()

            lifecycleScope.launch {
                //reference to entryDao
                val dao = db.entryDao()
                //either calls the dao's update or insert depending on whether an entry is being edited or added.
                if (entryId == -1) {
                    //gets the index of the selected journal.
                    val selectedIndex = binding.spinnerJournal.selectedItemPosition

                    dao.insert(
                        Entry(
                            journalId = journals[selectedIndex].id,
                            title = title,
                            content = content,
                            journalTitle = journals[selectedIndex].title,
                            timestamp = System.currentTimeMillis()
                        )
                    )

                } else {
                    dao.update(
                        Entry(
                            id = entryId,
                            journalId = intent.getIntExtra("journalId", -1),
                            title = title,
                            content = content,
                            journalTitle = intent.getStringExtra("journalTitle") ?: "",
                            timestamp = intent.getLongExtra("timestamp", -1L)
                        )
                    )
                }
                finish()
            }
        }
        //attaches a listener to the cancel button. It simply finishes the activity and returns to the main activity. Unsaved changes are lost.
        binding.btnCancelJournal.setOnClickListener { finish() }
    }

}