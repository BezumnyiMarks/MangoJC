package com.example.mangojc.ViewModels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangojc.Data.AuthBody
import com.example.mangojc.Data.AuthData
import com.example.mangojc.Data.Avatars
import com.example.mangojc.Data.DBProfileData
import com.example.mangojc.Data.LoadingState
import com.example.mangojc.Data.PhoneBody
import com.example.mangojc.Data.PhoneData
import com.example.mangojc.Data.Profile
import com.example.mangojc.Data.ProfileData
import com.example.mangojc.Data.RefreshBody
import com.example.mangojc.Data.RegBody
import com.example.mangojc.Data.RegData
import com.example.mangojc.Data.UserDataBody
import com.example.mangojc.Data.VerifCodeElement
import com.example.mangojc.Repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val rep = Repository()

    val phoneNumberValid = MutableStateFlow(false)
    val verifCodeElements = MutableStateFlow(listOf<VerifCodeElement>())

    val externalStorageImages = MutableStateFlow(listOf<Uri>())

    private val tokenRefreshSate = MutableStateFlow<LoadingState>(LoadingState.Loading)

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading = _authLoading.asStateFlow()

    private val _regLoading = MutableStateFlow(false)
    val regLoading = _regLoading.asStateFlow()


    val profileDataLoading = MutableStateFlow<LoadingState>(LoadingState.Loading)

    val profileDataUploading = MutableStateFlow<LoadingState>(LoadingState.Loading)

    private val _isSuccess = MutableStateFlow(PhoneData(false))
    val isSuccess = _isSuccess.asStateFlow()

    private val _userAuthData = MutableStateFlow(AuthData("", "", 0, false))
    val userAuthData = _userAuthData.asStateFlow()

    private val _userRegData = MutableStateFlow(RegData("", "", 0))
    val userRegData = _userRegData.asStateFlow()

    private val _profileData = MutableStateFlow(Profile(ProfileData()))
    val profileData = _profileData.asStateFlow()

    private val _avatars = MutableStateFlow(Avatars("", "", ""))
    val avatars = _avatars.asStateFlow()

    fun checkUserPhone(phone: String){
        _loading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                Repository().getRetrofitInstance().postUserPhone(PhoneBody(phone))
            }.fold(
                onSuccess = {
                    _isSuccess.value = it
                    _loading.value = false
                },
                onFailure = {
                    Toast.makeText(getApplication<Application>().applicationContext,
                    it.message,
                    Toast.LENGTH_LONG).show()
                    _loading.value = false
                }
            )
        }
    }

    fun sendUserAuthData(phone: String, code: String){
        _authLoading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                Repository().getRetrofitInstance().postAuthData(AuthBody(phone, code))
            }.fold(
                onSuccess = {
                    _userAuthData.value = it
                    _authLoading. value = false
                },
                onFailure = {
                    Toast.makeText(getApplication<Application>().applicationContext,
                    it.message,
                    Toast.LENGTH_LONG).show()
                    _authLoading.value = false
                }
            )
        }
    }

    fun sendUserRegData(phone: String, name: String, userName: String){
        _regLoading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                Repository().getRetrofitInstance().postRegData(RegBody(phone, name, userName))
            }.fold(
                onSuccess = {
                    _userRegData.value = it
                    _regLoading.value = false
                },
                onFailure = {
                    Toast.makeText(getApplication<Application>().applicationContext, it.message, Toast.LENGTH_LONG).show()
                    _regLoading.value = false
                }
            )
        }
    }

    fun getProfileData(dbProfileData: DBProfileData?, context: Context){
        if (dbProfileData?.phone.isNullOrEmpty()){
            profileDataLoading.value = LoadingState.Loading
            viewModelScope.launch {
                kotlin.runCatching {
                    Repository()
                        .getRetrofitInstanceWithToken(context, "ACCESS")
                        .getProfileData()
                }.fold(
                    onSuccess = {
                        _profileData.value = it
                        Log.d("Success", it.toString())
                        profileDataLoading.value = LoadingState.Success
                    },
                    onFailure = {
                        if(it.message?.contains("401") == true){
                            refreshToken(context)
                            tokenRefreshSate.collect{ tokenRefreshState ->
                                when (tokenRefreshState){
                                    LoadingState.Success -> {
                                        getProfileData(dbProfileData, context)
                                    }

                                    LoadingState.Failure -> {
                                        Toast.makeText(context,
                                            it.message,
                                            Toast.LENGTH_LONG).show()
                                        profileDataLoading.value = LoadingState.Failure
                                    }
                                    else -> {

                                    }
                                }

                            }
                        } else  {
                            Toast.makeText(context,
                                it.message,
                                Toast.LENGTH_LONG).show()
                            profileDataLoading.value = LoadingState.Failure
                        }
                    }
                )
            }
        } else _profileData.value = convertToProfileData(dbProfileData)
    }

    fun putProfileData(userDataBody: UserDataBody, context: Context){
        profileDataUploading.value = LoadingState.Loading
        viewModelScope.launch {
            kotlin.runCatching {
                Repository()
                    .getRetrofitInstanceWithToken(context, "ACCESS")
                    .postProfileData(userDataBody)
            }.fold(
                onSuccess = {
                    _avatars.value = it
                    getProfileData(DBProfileData(""), context)
                    profileDataUploading.value = LoadingState.Success
                },
                onFailure = {
                    if(it.message?.contains("401") == true){
                        refreshToken(context)
                        tokenRefreshSate.collect{ tokenRefreshState ->
                            when (tokenRefreshState){
                                LoadingState.Success -> {
                                    putProfileData(userDataBody, context)
                                }

                                LoadingState.Failure -> {
                                    Toast.makeText(context,
                                        it.message,
                                        Toast.LENGTH_LONG).show()
                                }
                                else -> {

                                }
                            }

                        }
                    }
                    else{
                        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        profileDataUploading.value = LoadingState.Failure
                    }
                }
            )
        }
    }

    fun refreshToken(context: Context){
        tokenRefreshSate.value = LoadingState.Loading
        viewModelScope.launch {
            kotlin.runCatching {
                Repository()
                    .getRetrofitInstance()
                    .refreshToken(RefreshBody(rep.getRefreshToken(context).toString()))
            }.fold(
                onSuccess = {
                    rep.saveRefreshToken(context, it.refresh_token ?: "")
                    rep.saveAccessToken(context, it.access_token ?: "")
                    tokenRefreshSate.value = LoadingState.Success
                },
                onFailure = {
                    Toast.makeText(context,
                        it.message,
                        Toast.LENGTH_LONG).show()
                    tokenRefreshSate.value = LoadingState.Failure
                }
            )
        }
    }

    private fun convertToProfileData(dbProfileData: DBProfileData?): Profile{
        return Profile(
            ProfileData(
                dbProfileData?.name,
                dbProfileData?.userName,
                dbProfileData?.birthday,
                dbProfileData?.city,
                dbProfileData?.vk,
                dbProfileData?.instagram,
                dbProfileData?.status,
                dbProfileData?.avatar,
                null,
                null,
                null,
                null,
                dbProfileData?.phone,
                null,
                Avatars("", dbProfileData?.avatar, "")
            )
        )
    }
}