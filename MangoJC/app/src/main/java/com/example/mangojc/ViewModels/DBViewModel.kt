package com.example.mangojc.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangojc.Data.DBProfileData
import com.example.mangojc.Data.ProfileData
import com.example.mangojc.UserDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DBViewModel(private val userDao: UserDao): ViewModel() {
    val dbBusy = MutableStateFlow(false)


    suspend fun getProfileData(phone: String): DBProfileData{
        return userDao.getProfileData(phone)
    }

    fun addProfileData(profileData: DBProfileData){
        dbBusy.value = true
        Log.d("INSERT", "INSERT")
        viewModelScope.launch {
            userDao.insertProfileData(profileData)
            dbBusy.value = false
        }
    }
}