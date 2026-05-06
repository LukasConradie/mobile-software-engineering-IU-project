package com.example.journalapp

import android.app.Application
import androidx.room.Room
import com.example.journalapp.data.dao.EntryDao
import com.example.journalapp.data.dao.JournalDao
import com.example.journalapp.data.database.AppDatabase
import com.example.journalapp.data.entity.Entry
import com.example.journalapp.data.entity.Journal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MainViewModelTest {
    private lateinit var database: AppDatabase
    private lateinit var journalDao: JournalDao
    private lateinit var entryDao: EntryDao
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()

        journalDao = database.journalDao()
        entryDao = database.entryDao()
        AppDatabase.setTestInstance(database)

        viewModel = MainViewModel(
            RuntimeEnvironment.getApplication() as Application
        )
    }

    @After
    fun tearDown() {
        database.close()
        AppDatabase.setTestInstance(null)
    }
    /*
    Tests setJournalMode function in viewmodel, simply calls it with a false and true value and
    determines if the relevant variables have the correct values.
    */
    @Test
    fun setJournalModeTest() = runBlocking {
        viewModel.setJournalMode(false)
        Assert.assertFalse(viewModel.isJournalMode.value)

        viewModel.setJournalMode(true)
        Assert.assertTrue(viewModel.isJournalMode.value)
        Assert.assertNull(viewModel.selectedJournal.value)
    }
    /*
    Tests selectJournal function, creates a journal value and passes it to selectJournal,
    asserts if the relevant variables have the correct values.
    */
    @Test
    fun selectJournalTest() = runBlocking {
        val journal = Journal(
            title = "Test A Journal",
            description = "Test A Description",
            timestamp = 100L
        )

        viewModel.selectJournal(journal)
        Assert.assertEquals(journal, viewModel.selectedJournal.value)
        Assert.assertFalse(viewModel.isJournalMode.value)
    }
    /*
    Tests clearSelectedJournal function, creates a journal value and passes it to selectJournal,
    asserts if selectedJournal has been updated with the journal value. clearSelectedJournal is called
    and asserts if selectedJournal is now null.
    */
    @Test
    fun clearSelectedJournalTest() = runBlocking {
        val journal = Journal(
            title = "Test B Journal",
            description = "Test B Description",
            timestamp = 100L
        )

        viewModel.selectJournal(journal)
        Assert.assertEquals(journal, viewModel.selectedJournal.value)

        viewModel.clearSelectedJournal()
        Assert.assertNull(viewModel.selectedJournal.value)
    }
    /*
    Tests the searching and sorting functionality of the viewmodel. When changing searchQuery or sortOrder,
    the journals value within the viewmodel is updated with a new list of journals based on the filter.
    Two journals are created and inserted here and using different combinations of states for searchQuery and sortOrder,
    assertions are made to determine whether the viewmodel retrieves the expected journals based on the filter.
    */
    @Test
    fun journalFilterTest() = runBlocking {
        val testJournalOne = Journal(
            title = "Test C Journal",
            description = "Test C Description",
            timestamp = 100L
        )
        val testJournalTwo = Journal(
            title = "Test D Journal",
            description = "Test D Description",
            timestamp = 200L
        )

        journalDao.insert(testJournalOne)
        journalDao.insert(testJournalTwo)

        viewModel.clearSelectedJournal()
        viewModel.setSortOrder("")

        //empty search query without setting sort
        viewModel.setSearchQuery("")
        val firstJournalsResult = viewModel.journals.first()
        Assert.assertEquals(2, firstJournalsResult.size)
        Assert.assertEquals("Test C Journal",firstJournalsResult[0].title)
        Assert.assertEquals("Test D Journal",firstJournalsResult[1].title)

        //specific search query without setting sort
        viewModel.setSearchQuery("Test D")
        val secondJournalsResult = viewModel.journals.first()
        Assert.assertEquals(1, secondJournalsResult.size)
        Assert.assertEquals("Test D Journal",secondJournalsResult[0].title)

        //empty search query with specific sort
        viewModel.setSearchQuery("")
        viewModel.setSortOrder("title DESC")
        val thirdJournalsResult = viewModel.journals.first()
        Assert.assertEquals(2, thirdJournalsResult.size)
        Assert.assertEquals("Test D Journal",thirdJournalsResult[0].title)
        Assert.assertEquals("Test C Journal",thirdJournalsResult[1].title)
    }
    /*
    Similar to the journalsFilterTest. Entries use an additional parameter when filtering, selectedJournal,
    this is used to filter results down to entries from a specific journal. In addition to similar assertions
    as in the journals test, here assertions will also account for specific journal filtering.
    */
    @Test
    fun entriesFilterTest() = runBlocking {
        val testJournalOne = Journal(
            title = "Test E Journal",
            description = "Test E Description",
            timestamp = 100L
        )
        val testJournalTwo = Journal(
            title = "Test F Journal",
            description = "Test F Description",
            timestamp = 100L
        )

        journalDao.insert(testJournalOne)
        journalDao.insert(testJournalTwo)
        val fetchedJournalOne = journalDao.searchJournals("Test E Journal").first().first()
        val fetchedJournalTwo = journalDao.searchJournals("Test F Journal").first().first()

        val testEntryOne = Entry(
            journalId = fetchedJournalOne.id,
            journalTitle = fetchedJournalOne.title,
            title = "Test E-A Entry",
            content = "Test E-A Content",
            timestamp = 200L
        )
        val testEntryTwo = Entry(
            journalId = fetchedJournalOne.id,
            journalTitle = fetchedJournalOne.title,
            title = "Test E-B Entry",
            content = "Test E-B Content",
            timestamp = 200L
        )
        val testEntryThree = Entry(
            journalId = fetchedJournalTwo.id,
            journalTitle = fetchedJournalTwo.title,
            title = "Test F-A Entry",
            content = "Test F-A Content",
            timestamp = 200L
        )
        val testEntryFour = Entry(
            journalId = fetchedJournalTwo.id,
            journalTitle = fetchedJournalTwo.title,
            title = "Test F-B Entry",
            content = "Test F-B Content",
            timestamp = 200L
        )

        entryDao.insert(testEntryOne)
        entryDao.insert(testEntryTwo)
        entryDao.insert(testEntryThree)
        entryDao.insert(testEntryFour)

        viewModel.setSortOrder("")
        viewModel.clearSelectedJournal()

        //empty filter
        viewModel.setSearchQuery("")
        val firstEntriesResult = viewModel.entries.first()
        Assert.assertEquals(4, firstEntriesResult.size)
        Assert.assertEquals("Test E-A Entry",firstEntriesResult[0].title)
        Assert.assertEquals("Test F-A Entry",firstEntriesResult[2].title)

        //specific search query without additional filters
        viewModel.setSearchQuery("Test E-B")
        val secondEntriesResult = viewModel.entries.first()
        Assert.assertEquals(1, secondEntriesResult.size)
        Assert.assertEquals("Test E-B Entry",secondEntriesResult[0].title)

        //specific journal filter
        viewModel.setSearchQuery("")
        viewModel.selectJournal(fetchedJournalTwo)
        val thirdEntriesResult = viewModel.entries.first()
        Assert.assertEquals(2, thirdEntriesResult.size)
        Assert.assertEquals("Test F-A Entry",thirdEntriesResult[0].title)

        //empty search query with specific sort
        viewModel.setSearchQuery("")
        viewModel.setSortOrder("title ASC")
        viewModel.clearSelectedJournal()
        val forthEntriesResult = viewModel.entries.first()
        Assert.assertEquals(4, forthEntriesResult.size)
        Assert.assertEquals("Test F-B Entry",forthEntriesResult[3].title)

        //empty search query with specific journal and sort
        viewModel.setSearchQuery("")
        viewModel.setSortOrder("title DESC")
        viewModel.selectJournal(fetchedJournalOne)
        val fifthEntriesResult = viewModel.entries.first()
        Assert.assertEquals(2, fifthEntriesResult.size)
        Assert.assertEquals("Test E-B Entry",fifthEntriesResult[0].title)

        //specific search query with specific sort
        viewModel.setSearchQuery("-B")
        viewModel.setSortOrder("title ASC")
        viewModel.clearSelectedJournal()
        val sixthEntriesResult = viewModel.entries.first()
        Assert.assertEquals(2, sixthEntriesResult.size)
        Assert.assertEquals("Test E-B Entry",sixthEntriesResult[0].title)
        Assert.assertEquals("Test F-B Entry",sixthEntriesResult[1].title)
    }
}