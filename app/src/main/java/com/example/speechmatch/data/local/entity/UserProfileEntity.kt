package com.example.speechmatch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "User_Profile")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "user_id")
    val userId: Int = 1,

    @ColumnInfo(name = "baseline_score")
    val baselineScore: Int,

    @ColumnInfo(name = "chronic_error_phonemes")
    val chronicErrorPhonemes: String
)