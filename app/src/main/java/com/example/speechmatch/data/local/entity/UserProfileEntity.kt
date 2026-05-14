package com.example.speechmatch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** * Çoklu profil (Multi-Profile) mimarisini destekleyen kullanıcı tablosu.
 * Her profilin kendine has bir ismi, seviyesi ve istatistikleri bulunur.
 */
@Entity(tableName = "User_Profile")
data class UserProfileEntity(

    /** Her kullanıcı için otomatik artan benzersiz kimlik numarası. */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    val userId: Int = 0,

    /** Profil ismi (Örn: "Ali", "Hoca", "Ziyaretçi"). */
    @ColumnInfo(name = "profile_name")
    val profileName: String,

    /** Kullanıcının mevcut CEFR dil seviyesi (Örn: A1, B2). */
    val currentLevel: String = "A1",

    /** Seviye belirleme sınavından alınan başlangıç skoru. */
    @ColumnInfo(name = "baseline_score")
    val baselineScore: Double,

    /** Bu profile özel hatalı okunan fonemlerin listesi. */
    @ColumnInfo(name = "chronic_error_phonemes")
    val chronicErrorPhonemes: String
)