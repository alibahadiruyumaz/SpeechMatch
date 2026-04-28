package com.example.speechmatch.data.recognizer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import com.example.speechmatch.domain.repository.HeadsetStateObserver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class HeadsetStateObserverImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HeadsetStateObserver {

    private val _isHeadsetConnected = MutableStateFlow(false)
    override val isHeadsetConnected = _isHeadsetConnected.asStateFlow()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Bir donanım değişikliği olduğunda modern metodu tetikle
            checkCurrentHeadsetState()
        }
    }

    override fun startObserving() {
        checkCurrentHeadsetState()

        // Kablo takılmasını veya Bluetooth ses yönlendirme değişikliklerini dinle
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY) // Kulaklık aniden çıkarsa tetiklenir
        }
        context.registerReceiver(receiver, filter)
    }

    override fun stopObserving() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            // Ajan zaten yoksa çökmeyi engelle
        }
    }

    // Modern API ile tüm ses çıkış cihazlarını tarayan fonksiyon
    private fun checkCurrentHeadsetState() {
        // Cihaza bağlı olan tüm ses çıkış donanımlarını listele
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

        // Listenin içinde bizim işimize yarayan bir kulaklık türü var mı diye bak
        val hasHeadset = devices.any { device ->
            device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||     // Mikrofonlu Jak
                    device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||  // Mikrofonsuz Jak
                    device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||    // Standart Bluetooth
                    device.type == AudioDeviceInfo.TYPE_USB_HEADSET          // Yeni nesil USB-C kulaklıklar
        }

        _isHeadsetConnected.value = hasHeadset
    }
}