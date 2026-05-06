package com.example.journalapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.journalapp.data.entity.Entry
import kotlinx.coroutines.flow.Flow
/*
Data Access Object Interface for Entry table.
Defines methods for access, room generates implementation at compile time.
*/
@Dao
interface EntryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: Entry)

    @Update
    suspend fun update(entry: Entry)

    @Query("""
    UPDATE entries_table
    SET journalTitle = :newTitle
    WHERE journalId = :journalId
    """)
    suspend fun updateJournalTitleForEntries(journalId: Int, newTitle: String)

    @Delete
    suspend fun delete(entry: Entry)

    @Query("""
        SELECT * FROM entries_table
        WHERE (:journalId IS NULL OR journalId = :journalId)
        AND (title LIKE '%' || :searchQuery || '%')
        """)
    fun searchEntries(searchQuery: String, journalId: Int?): Flow<List<Entry>>
}