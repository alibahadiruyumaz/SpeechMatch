package com.example.speechmatch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Review_Log",
    foreignKeys = [
        ForeignKey(
            entity = VocabularyEntity::class,
            parentColumns = ["id"],
            childColumns = ["vocab_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vocab_id"])]
)
data class ReviewLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    val logId: Int = 0,

    @ColumnInfo(name = "vocab_id")
    val vocabId: Int,

    @ColumnInfo(name = "last_accuracy_score")
    val lastAccuracyScore: Int,

    @ColumnInfo(name = "ease_factor")
    val easeFactor: Double,

    @ColumnInfo(name = "interval_days")
    val intervalDays: Int,

    @ColumnInfo(name = "next_review_date")
    val nextReviewDate: Long
)