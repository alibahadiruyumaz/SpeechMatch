package com.example.speechmatch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** * SM-2 algoritmasının her kelime için ürettiği tekrar zamanlamalarını ve
 * performans verilerini tutan veritabanı tablosu.
 */
@Entity(
    tableName = "Review_Log",
    foreignKeys = [
        ForeignKey(
            entity = VocabularyEntity::class,
            parentColumns = ["id"],
            childColumns = ["vocab_id"],
            onDelete = ForeignKey.CASCADE // Kelime silinirse, ona ait loglar da temizlenir.
        )
    ],
    indices = [Index(value = ["vocab_id"])] // Hızlı sorgulama için vocab_id indekslenir.
)
data class ReviewLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    val logId: Int = 0,

    /** İlişkili olduğu kelimenin benzersiz kimliği (Foreign Key). */
    @ColumnInfo(name = "vocab_id")
    val vocabId: Int,

    /** Kullanıcının kelimedeki son telaffuz doğruluk skoru (0-5 arası). */
    @ColumnInfo(name = "last_accuracy_score")
    val lastAccuracyScore: Int,

    /** SM-2: Kelimenin öğrenilme kolaylık katsayısı (Varsayılan: 2.5). */
    @ColumnInfo(name = "ease_factor")
    val easeFactor: Double,

    /** SM-2: Kelimenin bir sonraki sorulmasına kalan gün sayısı. */
    @ColumnInfo(name = "interval_days")
    val intervalDays: Int,

    /** SM-2: Kelimenin tekrar çalışılması gereken tarih (Epoch ms). */
    @ColumnInfo(name = "next_review_date")
    val nextReviewDate: Long,

    val consecutiveCorrectAnswers: Int = 0
)