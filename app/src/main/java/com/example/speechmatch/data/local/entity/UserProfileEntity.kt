package com.example.speechmatch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Sistemdeki tekil kullanıcı profilini, dil seviyesini ve teşhis verilerini tutan veritabanı tablosu. */
@Entity(tableName = "User_Profile")
data class UserProfileEntity(

    /** Tekil kullanıcı (Single-User) mimarisi için sabitlenmiş kimlik numarası. */
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "user_id")
    val userId: Int = 1,

    /** Kullanıcının mevcut CEFR dil seviyesi (Örn: A1, B2). Müfredat zorluğunu belirler. */
    val currentLevel: String = "A1",

    /** Seviye belirleme sınavından (Placement Test) alınan başlangıç başarı skoru. */
    @ColumnInfo(name = "baseline_score")
    val baselineScore: Double,

    /** Algoritmanın zorlu sorular üretmek için kullanacağı, hatalı okunan fonemlerin listesi. */
    @ColumnInfo(name = "chronic_error_phonemes")
    val chronicErrorPhonemes: String
)