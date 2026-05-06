package com.example.journalapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.journalapp.data.database.AppDatabase
import com.example.journalapp.data.entity.Journal
import com.example.journalapp.databinding.ActivityCreateJournalBinding
import kotlinx.coroutines.launch

class CreateJournalActivity : AppCompatActivity() {
    //Variable declaration and initialization. (binding and db are initialized in onCreate).
    private lateinit var binding: ActivityCreateJournalBinding
    private lateinit var db: AppDatabase
    private var isEditMode = false
    private var journalId: Int = -1

    //Runs on activity start.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(applicationContext)

        //Initializes binding with the relevant layout and sets that layout as the content view.
        binding = ActivityCreateJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Tries to get a journalId from the intent message, if none are received the default value is -1.
        journalId = intent.getIntExtra("journalId", -1)

        //isEditMode is set to true if an existing journalId was received. Changes the date text view visibility depending on this.
        isEditMode = journalId != -1
        if (isEditMode) {
            binding.textJournalDate.visibility = View.VISIBLE
        } else {
            binding.textJournalDate.visibility = View.GONE
        }
        //Calls populateIfEditing and setupButtons.
        populateIfEditing()
        setupButtons()
    }

    //if isEditMode is true the layout views are populated with the received intent data.
    private fun populateIfEditing() {
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val timestamp = intent.getLongExtra("timestamp", -1L)

        if (isEditMode) {
            binding.editJournalTitle.setText(title)
            binding.editJournalDescription.setText(description)
            val formatter = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            binding.textJournalDate.text = formatter.format(java.util.Date(timestamp))
        }
    }

    //Sets up the buttons on the CreateJournalActivity with listeners and behavior.
    private fun setupButtons() {
        //attaches a listener to the confirm button.
        binding.btnConfirmJournal.setOnClickListener {
            //gets data from user input.
            val title = binding.editJournalTitle.text.toString()
            val description = binding.editJournalDescription.text.toString()

            lifecycleScope.launch {
                //reference to journalDao

                //either calls the dao's update or insert depending on whether a journal is being edited or added.
                if (journalId == -1) {
                    db.journalDao().insert(
                        Journal(
                            title = title,
                            description = description,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } else {
                    db.journalDao().update(
                        Journal(
                            id = journalId,
                            title = title,
                            description = description,
                            timestamp = intent.getLongExtra("timestamp", -1L)
                        )
                    )
                }
                db.entryDao().updateJournalTitleForEntries(journalId, title)
                finish()
            }
        }
        //attaches a listener to the cancel button. It simply finishes the activity and returns to the main activity. Unsaved changes are lost.
        binding.btnCancelJournal.setOnClickListener { finish() }
    }

}