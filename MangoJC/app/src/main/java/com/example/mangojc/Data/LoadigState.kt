package com.example.mangojc.Data

sealed class LoadingState{
    data object Loading : LoadingState()
    data object Success : LoadingState()
    data object Failure : LoadingState()
}
