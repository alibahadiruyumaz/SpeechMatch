package com.example.speechmatch.domain.repository

import kotlinx.coroutines.flow.StateFlow

/** Donanımsal kulaklık bağlantı durumunu takip eden gözlemci arayüzü (Contract). */
interface HeadsetStateObserver {

    /** Kulaklığın bağlı olup olmadığını reaktif olarak bildiren durum akışı. */
    val isHeadsetConnected: StateFlow<Boolean>

    /** Sistem düzeyinde donanım takibini başlatır. */
    fun startObserving()

    /** Bellek sızıntılarını önlemek için donanım takibini sonlandırır. */
    fun stopObserving()
}