package com.example.speechmatch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.speechmatch.data.local.entity.UserProfileEntity

/** Kullanıcı profili ve teşhis verilerini (fonem hataları, seviye vb.) yöneten DAO. */
@Dao
interface UserProfileDao {

    /** Tekil kullanıcı profilini veritabanına ekler veya mevcutsa günceller (UPSERT). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    /** Sistemdeki aktif kullanıcı profilini ve algoritma için gerekli teşhis verilerini getirir. */
    @Query("SELECT * FROM User_Profile WHERE user_id = 1")
    suspend fun getUserProfile(): UserProfileEntity?
}