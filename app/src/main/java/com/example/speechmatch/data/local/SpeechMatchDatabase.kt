package com.example.speechmatch.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.speechmatch.data.local.dao.ReviewLogDao
import com.example.speechmatch.data.local.dao.UserProfileDao
import com.example.speechmatch.data.local.dao.VocabularyDao
import com.example.speechmatch.data.local.entity.ReviewLogEntity
import com.example.speechmatch.data.local.entity.UserProfileEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity

/** * Uygulamanın yerel veri depolama merkezini (SQLite/Room) temsil eden ana veritabanı sınıfı.
 * Kelime havuzu, SM-2 algoritma günlükleri ve kullanıcı profili tablolarını barındırır.
 */
@Database(
    entities = [VocabularyEntity::class, ReviewLogEntity::class, UserProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SpeechMatchDatabase : RoomDatabase() {

    /** Kelime havuzu ve çalışma listesi işlemlerini yürüten DAO'ya (Data Access Object) erişim sağlar. */
    abstract val vocabularyDao: VocabularyDao

    /** SM-2 aralıklı tekrar algoritmasının performans kayıtlarını yöneten DAO'ya erişim sağlar. */
    abstract val reviewLogDao: ReviewLogDao

    /** Kullanıcı seviyesi ve teşhis verilerini barındıran DAO'ya erişim sağlar. */
    abstract val userProfileDao: UserProfileDao
}