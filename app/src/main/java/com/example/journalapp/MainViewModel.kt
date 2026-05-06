// MainViewModel: Holds state variables required throughout application.
package com.example.journalapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.journalapp.data.database.AppDatabase
import com.example.journalapp.data.entity.Entry
import com.example.journalapp.data.entity.Journal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    //Linking Database with ViewModel.
    private val db = AppDatabase.getDatabase(application)
    /*
    Various State Variables.
    Internal variables (private) uses MutableStateFlow to actively emit changes that occur to the variable.
    Public variables are exposed as read-only StateFlow variables. Initialized by its private counterpart.

     */
    //isJournalMode is used to represent the state of the Journals/Entries Toggle.
    private val _isJournalMode = MutableStateFlow(true)
    val isJournalMode: StateFlow<Boolean> = _isJournalMode
    //selectedJournal is used to indicate the currently selected Journal, used for the chip component.
    private val _selectedJournal = MutableStateFlow<Journal?>(null)
    val selectedJournal: StateFlow<Journal?> = _selectedJournal
    //set function for journal mode. Clears selected journal if journal mode is active.
    fun setJournalMode(enabled: Boolean) {
        _isJournalMode.value = enabled
        if (enabled) {
            clearSelectedJournal()
        }
    }
    //Used by main activity for journal card clicks.
    //Sets selected journal and sets journal mode to false since on journal card click entry mode is toggled active.
    fun selectJournal(journal: Journal) {
        _selectedJournal.value = journal
        _isJournalMode.value = false
    }
    //Clears (sets to null) the selectedJournal variable.
    fun clearSelectedJournal() {
        _selectedJournal.value = null
    }

    //sortOrder represents the state of the sort spinner, set function used by main activity to update the value.
    private val _sortOrder = MutableStateFlow("timestamp DESC")
    fun setSortOrder(order: String) {
        _sortOrder.value = order
    }
    //searchQuery represents the current value of the search text input, set function used by main activity to update the value.
    private val _searchQuery = MutableStateFlow("")
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    /*
    Flow used here to retrieve data based on most recent search, sort, and journals data.
    Final result is a list of journals filtered by the current search input and selected sort.
    */
    val journals: Flow<List<Journal>> = //Data stream emitting lists of journals
        //Combines flows (query and sort) into a kotlin pair.
        combine(_searchQuery, _sortOrder) {
            query, sort -> Pair(query, sort) }.flatMapLatest {
            /*
            flatMapLatest calls searchJournals which is mapped according to the transform passed in map{ }.
            Each time the main flow (searchQuery and sortOrder) emits a new value, flatMapLatest cancels
            the previous query and sort transform, and starts again using the new flow data.
            */
            (query, sort) -> db.journalDao().searchJournals(query).map {
                //WHEN kotlin statement performs action based on given value (i.e. sort).
                //kotlin sortedBy- returns a new sorted version of the given list
                list -> when (sort) {
                        "timestamp DESC" -> list.sortedByDescending { it.timestamp }
                        "timestamp ASC" -> list.sortedBy { it.timestamp }
                        "title ASC" -> list.sortedBy { it.title }
                        "title DESC" -> list.sortedByDescending { it.title }
                        else -> list
                    }
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    /*
    Flow used here to retrieve data based on most recent search, sort, selected journal, and entries data.
    Final result is a list of entries filtered by the current search input, selected sort, and selected journal.
    */
    val entries: Flow<List<Entry>> = //Data stream emitting lists of entries
        //Combines flows (query, sort, and journalId (if received)) into a kotlin triple.
        combine(_searchQuery, _sortOrder, _selectedJournal) {
            query, sort, journal -> Triple(query, sort, journal?.id) }.flatMapLatest {
            /*
            flatMapLatest calls searchEntries which is mapped according to the transform passed in map{ }.
            Each time the main flow (searchQuery, sortOrder, selectedJournal) emits a new value, flatMapLatest cancels
            the previous query and sort transform, and starts again using the new flow data.
            */
                (query, sort, journalId) -> db.entryDao().searchEntries(query, journalId).map {
                    //WHEN kotlin statement performs action based on given value (i.e. sort).
                    //kotlin sortedBy- returns a new sorted version of the given list
                    list -> when (sort) {
                        "timestamp DESC" -> list.sortedByDescending { it.timestamp }
                        "timestamp ASC" -> list.sortedBy { it.timestamp }
                        "title ASC" -> list.sortedBy { it.title }
                        "title DESC" -> list.sortedByDescending { it.title }
                        else -> list
                    }
                }
        }
}