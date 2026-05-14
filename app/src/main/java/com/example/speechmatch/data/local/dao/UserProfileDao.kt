package com.example.speechmatch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.speechmatch.data.local.entity.UserProfileEntity

/** Kullanıcı profillerini ve teşhis verilerini yöneten DAO. */
@Dao
interface UserProfileDao {

    /** Yeni profil ekler veya mevcutsa günceller (UPSERT). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity): Long

    /** Netflix ekranı (Kim İzliyor?) için sistemdeki TÜM profilleri liste halinde getirir. */
    @Query("SELECT * FROM User_Profile")
    suspend fun getAllProfiles(): List<UserProfileEntity>

    /** İstenilen spesifik bir kullanıcı profilini id'sine göre getirir. */
    @Query("SELECT * FROM User_Profile WHERE user_id = :userId")
    suspend fun getUserProfile(userId: Int): UserProfileEntity?
    @Delete
    suspend fun deleteProfile(profile: UserProfileEntity)
}