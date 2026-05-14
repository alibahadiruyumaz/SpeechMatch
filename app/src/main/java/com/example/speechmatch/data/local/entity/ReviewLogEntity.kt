package com.example.speechmatch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** * SM-2 algoritmasının her kullanıcı ve her kelime için ürettiği özel zamanlamaları tutar.
 */
@Entity(
    tableName = "Review_Log",
    foreignKeys = [
        // Kelimeyle ilişkilendirme
        ForeignKey(
            entity = VocabularyEntity::class,
            parentColumns = ["id"],
            childColumns = ["vocab_id"],
            onDelete = ForeignKey.CASCADE
        ),
        // KULLANICIYLA İLİŞKİLENDİRME
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE // Profil silinirse o kişinin tüm geçmişi silinir.
        )
    ],
    indices = [
        Index(value = ["vocab_id"]),
        Index(value = ["user_id"]) // Kullanıcı bazlı sorgular için indeksleme.
    ]
)
data class ReviewLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    val logId: Int = 0,

    /** Bu kaydın hangi kullanıcıya ait olduğunu belirten kimlik . */
    @ColumnInfo(name = "user_id")
    val userId: Int,

    /** İlişkili olduğu kelimenin kimliği. */
    @ColumnInfo(name = "vocab_id")
    val vocabId: Int,

    /** Son telaffuz skoru (0-5 arası). */
    @ColumnInfo(name = "last_accuracy_score")
    val lastAccuracyScore: Int,

    /** SM-2: Kolaylık katsayısı. */
    @ColumnInfo(name = "ease_factor")
    val easeFactor: Double,

    /** SM-2: Tekrar aralığı (gün). */
    @ColumnInfo(name = "interval_days")
    val intervalDays: Int,

    /** SM-2: Bir sonraki çalışma tarihi. */
    @ColumnInfo(name = "next_review_date")
    val nextReviewDate: Long,

    val consecutiveCorrectAnswers: Int = 0
)