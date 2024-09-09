package com.example.mangojc.Repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.example.mangojc.Data.AuthBody
import com.example.mangojc.Data.AuthData
import com.example.mangojc.Data.Avatar
import com.example.mangojc.Data.Avatars
import com.example.mangojc.Data.PhoneBody
import com.example.mangojc.Data.PhoneData
import com.example.mangojc.Data.Profile
import com.example.mangojc.Data.ProfileData
import com.example.mangojc.Data.RefreshBody
import com.example.mangojc.Data.RegBody
import com.example.mangojc.Data.RegData
import com.example.mangojc.Data.UserDataBody
import com.google.gson.Gson
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.Base64

private const val BASE_URL = "https://plannerok.ru/api/v1/users/"

private const val PREFS_TOKENS = "PREFS_TOKENS"
private const val ACCESS_TOKEN = "ACCESS_TOKEN"
private const val REFRESH_TOKEN = "REFRESH_TOKEN"
class Repository {

     fun saveAccessToken(context: Context, token: String){
          val prefs = context.getSharedPreferences(PREFS_TOKENS, MODE_PRIVATE)
          prefs.edit().putString(ACCESS_TOKEN, token).apply()
     }


     private fun getAccessToken(context:Context): String?{
          val prefs = context.getSharedPreferences(PREFS_TOKENS, MODE_PRIVATE)
          return prefs.getString(ACCESS_TOKEN, "")
     }

     fun saveRefreshToken(context: Context, token: String){
          val prefs = context.getSharedPreferences(PREFS_TOKENS, MODE_PRIVATE)
          prefs.edit().putString(REFRESH_TOKEN, token).apply()
     }


     fun getRefreshToken(context:Context): String?{
          val prefs = context.getSharedPreferences(PREFS_TOKENS, MODE_PRIVATE)
          return prefs.getString(REFRESH_TOKEN, "")
     }


     fun getRetrofitInstanceWithToken(context: Context, tokenType: String): SearchUserWithToken{
          val httpLoggingInterceptor = HttpLoggingInterceptor()
          httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
          var okHttpClient = OkHttpClient.Builder().build()
          when(tokenType){
               "ACCESS" -> {
                    okHttpClient = OkHttpClient.Builder()
                         .addInterceptor {
                              val request = it.request().newBuilder()
                                   .addHeader("Authorization", "Bearer ${getAccessToken(context)}")
                                   .build()
                              return@addInterceptor it.proceed(request)
                         }
                         .addInterceptor(httpLoggingInterceptor)
                         .build()
               }
               else -> {
                    okHttpClient = OkHttpClient.Builder()
                         .addInterceptor {
                              val request = it.request().newBuilder()
                                   .addHeader("Authorization", "Bearer ${getRefreshToken(context)}")
                                   .build()
                              return@addInterceptor it.proceed(request)
                         }
                         .addInterceptor(httpLoggingInterceptor)
                         .build()
               }
          }

          val retrofit = Retrofit.Builder()
               .baseUrl(BASE_URL)
               .client(okHttpClient)
               .addConverterFactory(GsonConverterFactory.create())
               .build()

          return retrofit.create(SearchUserWithToken::class.java)
     }

     fun getRetrofitInstance(): SearchUser{
          val retrofit = Retrofit.Builder()
               .baseUrl(BASE_URL)
               .addConverterFactory(GsonConverterFactory.create())
               .build()

          return retrofit.create(SearchUser::class.java)
     }
}

interface SearchUser{
     @POST("send-auth-code/")
     suspend fun postUserPhone(@Body userPhone: PhoneBody): PhoneData

     @POST("check-auth-code/")
     suspend fun postAuthData(@Body authData: AuthBody): AuthData

     @POST("register/")
     suspend fun postRegData(@Body regData: RegBody): RegData
}

interface SearchUserWithToken{
     @PUT("me/")
     suspend fun postProfileData(@Body userDataBody: UserDataBody): Avatars

     @GET("me/")
     suspend fun getProfileData(): Profile

     @GET("refresh-token/")
     suspend fun refreshToken(@Body refreshData: RefreshBody): RegData
}
