package com.example.journalapp.data.database

import androidx.room.Room
import com.example.journalapp.data.dao.EntryDao
import com.example.journalapp.data.dao.JournalDao
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
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var journalDao: JournalDao
    private lateinit var entryDao: EntryDao

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()

        journalDao = database.journalDao()
        entryDao = database.entryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    /*
    Insert DAO functionality test for both Journal and Entry.
    Begins with Journal, inserts a test journal, fetches it from the database using searchJournals,
    and asserts that the returned data matches the test data.

    Next the same is done for entry using the test Journal as its parent.
     */
    @Test
    fun insertJournalAndEntryTest() = runBlocking {
        //Journal
        val journal = Journal(
            title = "Test A Journal",
            description = "Test A Description",
            timestamp = 100L
        )

        journalDao.insert(journal)

        val testJournal = journalDao.searchJournals("Test A Journal").first().first()

        Assert.assertEquals("Test A Journal", testJournal.title)
        Assert.assertEquals("Test A Description", testJournal.description)
        Assert.assertEquals(100L, testJournal.timestamp)
        Assert.assertTrue(testJournal.id > 0)
        //Entry
        val entry = Entry(
            journalId = testJournal.id,
            journalTitle = testJournal.title,
            title = "Test A Entry",
            content = "Test A Content",
            timestamp = 200L
        )

        entryDao.insert(entry)

        val testEntry = entryDao.searchEntries("Test A Entry", testJournal.id).first().first()

        Assert.assertEquals(testJournal.id, testEntry.journalId)
        Assert.assertEquals("Test A Entry", testEntry.title)
        Assert.assertEquals("Test A Content", testEntry.content)
        Assert.assertEquals(200L, testEntry.timestamp)
        Assert.assertTrue(testEntry.id > 0)
    }
    /*
    Update DAO functionality test for both Journal and Entry.
    Inserts a test journal, fetches it's ID and updates it with a new title and description.
    Retrieves the updated journal and asserts if the correct journal has been retrieved and updated.

    The same is done for entry.
     */
    @Test
    fun updateJournalAndEntryTest() = runBlocking {
        //Journal
        val testJournal = Journal(
            title = "Test B Journal",
            description = "Test B Description",
            timestamp = 100L
        )

        journalDao.insert(testJournal)
        val testJournalId = journalDao.searchJournals("Test B Journal").first().first().id

        val updatedJournal = Journal(
            id = testJournalId,
            title = "Test C Journal",
            description = "Test C Description",
            timestamp = 100L
        )

        journalDao.update(updatedJournal)

        val fetchedJournal = journalDao.searchJournals("Test C Journal").first().first()

        Assert.assertEquals(testJournalId, fetchedJournal.id)
        Assert.assertEquals("Test C Journal", fetchedJournal.title)
        Assert.assertEquals("Test C Description", fetchedJournal.description)
        //Entry
        val testEntry = Entry(
            journalId = fetchedJournal.id,
            journalTitle = fetchedJournal.title,
            title = "Test B Entry",
            content = "Test B Content",
            timestamp = 200L
        )

        entryDao.insert(testEntry)
        val testEntryId = entryDao.searchEntries("Test B Entry",fetchedJournal.id).first().first().id

        val updatedEntry = Entry(
            id = testEntryId,
            journalId = fetchedJournal.id,
            title = "Test C Entry",
            content = "Test C Content",
            journalTitle = fetchedJournal.title,
            timestamp = 200L
        )

        entryDao.update(updatedEntry)
        val fetchedEntry = entryDao.searchEntries("Test C Entry", fetchedJournal.id).first().first()

        Assert.assertEquals(testEntryId, fetchedEntry.id)
        Assert.assertEquals("Test C Entry", fetchedEntry.title)
        Assert.assertEquals("Test C Content", fetchedEntry.content)
    }
    /*
    Journal delete test.
    Inserts a Journal and then deletes it, asserts whether no results are returned when searching for the journal.
    */
    @Test
    fun deleteJournalOnlyTest() = runBlocking {
        val testJournal = Journal(
            title = "Test D Journal",
            description = "Test D Description",
            timestamp = 100L
        )

        journalDao.insert(testJournal)

        val fetchedJournal = journalDao.searchJournals("Test D Journal").first().first()
        journalDao.delete(fetchedJournal)

        val journals = journalDao.searchJournals("Test D Journal").first()
        Assert.assertTrue(journals.isEmpty())
    }
    /*
    Journal with an Entry delete test.
    Inserts a Journal and creates an entry for it. Deletes the journal and asserts whether no results are returned when searching for both the journal and entry.
    */
    @Test
    fun deleteJournalWithEntryTest() = runBlocking {
        val testJournal = Journal(
            title = "Test E Journal",
            description = "Test E Description",
            timestamp = 100L
        )

        journalDao.insert(testJournal)

        val fetchedJournal = journalDao.searchJournals("Test E Journal").first().first()

        val testEntry = Entry(
            journalId = fetchedJournal.id,
            journalTitle = fetchedJournal.title,
            title = "Test E Entry",
            content = "Test E Content",
            timestamp = 200L
        )

        entryDao.insert(testEntry)

        journalDao.delete(fetchedJournal)

        val journals = journalDao.searchJournals("Test E Journal").first()
        val entries = entryDao.searchEntries("Test E Entry", fetchedJournal.id).first()

        Assert.assertTrue(journals.isEmpty())
        Assert.assertTrue(entries.isEmpty())
    }
    /*
    Entry delete test.
    Inserts a Journal and creates an entry for it. Deletes the entry and asserts whether no results are returned when searching for the entry.
    */
    @Test
    fun deleteEntryOnlyTest() = runBlocking {
        val testJournal = Journal(
            title = "Test F Journal",
            description = "Test F Description",
            timestamp = 100L
        )

        journalDao.insert(testJournal)

        val fetchedJournal = journalDao.searchJournals("Test F Journal").first().first()

        val testEntry = Entry(
            journalId = fetchedJournal.id,
            journalTitle = fetchedJournal.title,
            title = "Test F Entry",
            content = "Test F Content",
            timestamp = 200L
        )

        entryDao.insert(testEntry)

        val fetchedEntry = entryDao.searchEntries("Test F Entry", fetchedJournal.id).first().first()
        entryDao.delete(fetchedEntry)

        val entries = entryDao.searchEntries("Test F Entry", fetchedJournal.id).first()

        Assert.assertTrue(entries.isEmpty())
    }
    /*
    Journals Count test.
    Inserts two test journals and asserts that the returned count is 2.
    */
    @Test
    fun getJournalsCountTest() = runBlocking {
        val testJournalOne = Journal(
            title = "Test G Journal",
            description = "Test G Description",
            timestamp = 100L
        )
        val testJournalTwo = Journal(
            title = "Test H Journal",
            description = "Test H Description",
            timestamp = 100L
        )

        journalDao.insert(testJournalOne)
        journalDao.insert(testJournalTwo)

        val count = journalDao.getJournalsCount()

        Assert.assertEquals(2, count)
    }
    /*
    Search test for Journals.
    Creates two journals and retrieves different lists using searchJournals. Asserts that the correct data is returned.
    */
    @Test
    fun searchJournalsTest() = runBlocking {
        val testJournalOne = Journal(
            title = "Test I Journal",
            description = "Test I Description",
            timestamp = 100L
        )
        val testJournalTwo = Journal(
            title = "Test J Journal",
            description = "Test J Description",
            timestamp = 100L
        )

        journalDao.insert(testJournalOne)
        journalDao.insert(testJournalTwo)

        val testJournalsA = journalDao.searchJournals("Test").first()
        Assert.assertEquals(2, testJournalsA.size)

        val testJournalsB = journalDao.searchJournals("Test I").first()

        Assert.assertEquals(1, testJournalsB.size)
        Assert.assertEquals("Test I Journal", testJournalsB[0].title)
    }
    /*
    Search test for Entries.
    Creates two journals and creates one entry for each. Retrieves different lists using searchEntries. Asserts that the correct data is returned.
    */
    @Test
    fun searchEntriesTest() = runBlocking {
        val testJournalOne = Journal(
            title = "Test K Journal",
            description = "Test K Description",
            timestamp = 100L
        )
        val testJournalTwo = Journal(
            title = "Test L Journal",
            description = "Test L Description",
            timestamp = 100L
        )

        journalDao.insert(testJournalOne)
        journalDao.insert(testJournalTwo)

        val fetchedJournalOne = journalDao.searchJournals("Test K Journal").first().first()
        val fetchedJournalTwo = journalDao.searchJournals("Test L Journal").first().first()

        val testEntryOne = Entry(
            journalId = fetchedJournalOne.id,
            journalTitle = fetchedJournalOne.title,
            title = "Test K Entry",
            content = "Test K Content",
            timestamp = 200L
        )

        val testEntryTwo = Entry(
            journalId = fetchedJournalTwo.id,
            journalTitle = fetchedJournalTwo.title,
            title = "Test L Entry",
            content = "Test L Content",
            timestamp = 200L
        )

        entryDao.insert(testEntryOne)
        entryDao.insert(testEntryTwo)

        val testEntriesA = entryDao.searchEntries("Test", null).first()
        Assert.assertEquals(2, testEntriesA.size)

        val testEntriesB = entryDao.searchEntries("Test L", null).first()
        Assert.assertEquals(1, testEntriesB.size)

        val testEntriesC = entryDao.searchEntries("", fetchedJournalOne.id).first()
        Assert.assertEquals(1, testEntriesC.size)
        Assert.assertEquals("Test K Entry", testEntriesC[0].title)
        Assert.assertEquals("Test K Content", testEntriesC[0].content)
        Assert.assertEquals(fetchedJournalOne.id, testEntriesC[0].journalId)
    }
}