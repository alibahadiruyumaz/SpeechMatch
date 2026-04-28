package com.example.speechmatch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.speechmatch.data.local.entity.UserProfileEntity

@Dao
interface UserProfileDao {

    // 1. Profili Kaydet veya Üzerine Yaz
    // Sadece 1 numaralı ID'ye sahip tek bir satır olacağı için hep üzerine yazacaktır.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    // 2. Teşhis Verilerini Getir
    // İleride Minimal Çiftler (Minimal Pairs) algoritması bu fonem hatalarını okuyup
    // kullanıcıya özel zorlu sorular üretecek.
    @Query("SELECT * FROM User_Profile WHERE user_id = 1")
    suspend fun getUserProfile(): UserProfileEntity?
}