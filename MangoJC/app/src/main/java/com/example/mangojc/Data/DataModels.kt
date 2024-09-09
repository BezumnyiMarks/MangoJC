package com.example.mangojc.Data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.google.gson.annotations.SerializedName


data class PhoneData (
    @SerializedName("is_success")
    val isSuccess: Boolean
)

data class PhoneBody (
    @SerializedName("phone")
    val phone: String
)

data class AuthData (
    @SerializedName("access_token")
    val access_token: String? = null,

    @SerializedName("refresh_token")
    val refresh_token: String? = null,

    @SerializedName("user_id")
    val user_id: Int? = null,

    @SerializedName("is_user_exists")
    val is_user_exists: Boolean? = null
)

data class AuthBody (
    @SerializedName("phone")
    val phone: String,

    @SerializedName("code")
    val code: String
)

data class RegData (
    @SerializedName("access_token")
    val access_token: String? = null,

    @SerializedName("refresh_token")
    val refresh_token: String? = null,

    @SerializedName("user_id")
    val user_id: Int? = null
)

data class RegBody (
    @SerializedName("phone")
    val phone: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("username")
    val userName: String
)

data class RefreshBody (
    @SerializedName("refresh_token")
    val refresh_token: String
)

data class UserDataBody (
    @SerializedName("name")
    val name: String,

    @SerializedName("username")
    val userName: String,

    @SerializedName("birthday")
    val birthday: String,

    @SerializedName("city")
    val city: String,

    @SerializedName("vk")
    val vk: String,

    @SerializedName("instagram")
    val instagram: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("avatar")
    val avatar: Avatar
)

data class Avatar(
    @SerializedName("filename")
    val filename: String,

    @SerializedName("base_64")
    val base64: String
)

data class Avatars(
    @SerializedName("avatar")
    val avatar: String? = null,

    @SerializedName("bigAvatar")
    val bigAvatar: String? = null,

    @SerializedName("miniAvatar")
    val miniAvatar: String? = null
)

data class Profile(
    @SerializedName("profile_data")
    val profileData: ProfileData,
)

data class ProfileData (
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("username")
    val userName: String? = null,

    @SerializedName("birthday")
    val birthday: String? = null,

    @SerializedName("city")
    val city: String? = null,

    @SerializedName("vk")
    val vk: String? = null,

    @SerializedName("instagram")
    val instagram: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("avatar")
    val avatar: String? = null,

    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("last")
    val last: String? = null,

    @SerializedName("online")
    val online: Boolean? = null,

    @SerializedName("created")
    val created: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("completed_task")
    val completed_task: Long? = null,

    @SerializedName("avatars")
    val avatars: Avatars? = null
)