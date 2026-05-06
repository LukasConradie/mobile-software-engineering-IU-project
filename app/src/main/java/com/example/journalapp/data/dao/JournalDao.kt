package com.example.journalapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.journalapp.data.entity.Journal
import kotlinx.coroutines.flow.Flow
/*
Data Access Object Interface for Journal table.
Defines methods for access, room generates implementation at compile time.
*/
@Dao
interface JournalDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(journal: Journal)

    @Update
    suspend fun update(journal: Journal)

    @Delete
    suspend fun delete(journal: Journal)

    @Query("""
        SELECT COUNT(*) FROM journals_table
        """)
    suspend fun getJournalsCount(): Int

    @Query("""
        SELECT * FROM journals_table
        WHERE title LIKE '%' || :searchQuery || '%'
        """)
    fun searchJournals(searchQuery: String): Flow<List<Journal>>
}