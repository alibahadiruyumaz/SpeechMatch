package com.example.speechmatch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "Vocabulary_Table",
    foreignKeys = [
        ForeignKey(
            entity = VocabularyEntity::class,
            parentColumns = ["id"],
            childColumns = ["minimal_pair_id"],
            onDelete = ForeignKey.SET_NULL // Fiziksel silme olursa ilişkili ID null'a düşer, DB çökmez.
        )
    ]
)
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val text: String,

    @ColumnInfo(name = "target_phoneme")
    val targetPhoneme: String,

    @ColumnInfo(name = "cefrLevel")
    val cefrLevel: String, // "A1", "A2", "B1" vb.

    @ColumnInfo(name = "minimal_pair_id", index = true)
    val minimalPairId: Int? = null,

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false
)