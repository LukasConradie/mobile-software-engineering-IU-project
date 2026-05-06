// Code for Journal Room-Database Entity, defines structure of the journal table.
package com.example.journalapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "journals_table"
    )
//Kotlin dataclass for journal.
data class Journal(
    //@PrimaryKey marks id as the primary key and will autogenerate its value.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val timestamp: Long
)