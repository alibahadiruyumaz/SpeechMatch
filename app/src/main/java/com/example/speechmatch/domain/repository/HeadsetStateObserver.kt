package com.example.speechmatch.domain.repository

import kotlinx.coroutines.flow.StateFlow

// Kulaklık durumunu dinleyecek sözleşme
interface HeadsetStateObserver {
    val isHeadsetConnected: StateFlow<Boolean>
    fun startObserving()
    fun stopObserving()
}