package com.example.journalapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.journalapp.data.dao.EntryDao
import com.example.journalapp.data.dao.JournalDao
import com.example.journalapp.data.entity.Entry
import com.example.journalapp.data.entity.Journal

@Database(entities = [Journal::class, Entry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao
    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "journal_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }

        fun setTestInstance(database: AppDatabase?) {
            INSTANCE = database
        }
    }
}