package com.example.speechmatch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/** * Uygulamanın temel kelime havuzunu ve "Minimal Pair" (benzer sesli kelime)
 * hiyerarşisini barındıran veritabanı tablosu.
 */
@Entity(
    tableName = "Vocabulary_Table",
    foreignKeys = [
        ForeignKey(
            entity = VocabularyEntity::class,
            parentColumns = ["id"],
            childColumns = ["minimal_pair_id"],
            // Bağlı olduğu kelime fiziksel olarak silinirse çökmeyi önlemek için ID'yi null yapar.
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class VocabularyEntity(
    /** Kelimenin benzersiz kimlik numarası. */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** Telaffuz edilecek hedef kelime (Örn: "Ship"). */
    val text: String,

    /** Kelimenin test ettiği spesifik hedef ses/fonem (Örn: "/ʃ/"). */
    @ColumnInfo(name = "target_phoneme")
    val targetPhoneme: String,

    /** Kelimenin uluslararası zorluk derecesi (Örn: "A1", "B2"). */
    @ColumnInfo(name = "cefrLevel")
    val cefrLevel: String,

    /** Sesletim hatası durumunda çapraz sorgulanacak (Örn: "Sheep") kelimenin referans kimliği. */
    @ColumnInfo(name = "minimal_pair_id", index = true)
    val minimalPairId: Int? = null,

    /** SM-2 barajını (180 gün) geçerek tam öğrenilmiş kabul edilen kelimelerin durum bayrağı. */
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false
)