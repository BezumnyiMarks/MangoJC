package com.example.mangojc

import androidx.room.*
import com.example.mangojc.Data.DBProfileData

@Dao
interface UserDao {

    @Query("SELECT * FROM DBProfileData WHERE phone = :phone")
    suspend fun getProfileData(phone: String): DBProfileData

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileData(profileData: DBProfileData)

}