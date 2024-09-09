package com.example.mangojc.Data

import android.content.Context
import android.widget.EditText
import com.hbb20.CountryCodePicker

class CCP(context: Context): CountryCodePicker(context) {
    private val editText = EditText(context)
    private var phoneNumberValid = false

    init {
        setAutoDetectedCountry(true)
        registerCarrierNumberEditText(editText)
        setPhoneNumberValidityChangeListener {
            phoneNumberValid = it
        }
    }

    fun setText(text: String){
        editText.setText(text)
    }

    fun getPhoneNumberValidity(): Boolean{
        return phoneNumberValid
    }
}