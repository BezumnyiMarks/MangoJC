package com.example.mangojc.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DBProfileData")
data class DBProfileData(
    @PrimaryKey
    @ColumnInfo(name = "phone")
    val phone: String,

    @ColumnInfo(name = "name")
    val name: String? = null,

    @ColumnInfo(name = "userName")
    val userName: String? = null,

    @ColumnInfo(name = "birthday")
    val birthday: String? = null,

    @ColumnInfo(name = "city")
    val city: String? = null,

    @ColumnInfo(name = "status")
    val status: String? = null,

    @ColumnInfo(name = "vk")
    val vk: String? = null,

    @ColumnInfo(name = "instagram")
    val instagram: String? = null,

    @ColumnInfo(name = "avatar")
    val avatar: String? = null
)


