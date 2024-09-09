package com.example.mangojc

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mangojc.Data.DBProfileData

@Database(entities = [DBProfileData::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract  fun userDao(): UserDao
}