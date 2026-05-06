// Code for Entry Room-Database Entity, defines structure of the entry table.
package com.example.journalapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "entries_table",
    //Attaches each entry to a parent journal via a foreign key.
    foreignKeys = [
        ForeignKey(
            entity = Journal::class,
            //"id" column in journal table is referenced and journalId in entry table must match it.
            parentColumns = ["id"],
            childColumns = ["journalId"],
            //If parent journal is deleted all child entries are deleted as well.
            onDelete = ForeignKey.CASCADE
        )
    ],
    //Creates index on journalId, improves filtering by journal functionality.
    indices = [Index("journalId")]
)
//Kotlin dataclass for entry.
data class Entry(
    //@PrimaryKey marks id as the primary key and will autogenerate its value.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val journalId: Int,
    val journalTitle: String,
    val title: String,
    val content: String,
    val timestamp: Long
)