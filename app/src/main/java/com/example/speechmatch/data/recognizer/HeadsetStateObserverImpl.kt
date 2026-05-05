package com.example.speechmatch.data.recognizer

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import com.example.speechmatch.domain.repository.HeadsetStateObserver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeadsetStateObserverImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HeadsetStateObserver {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Uygulama ilk açıldığında donanım durumunu hemen kontrol ederek başlarız
    private val _isHeadsetConnected = MutableStateFlow(checkCurrentAudioDevice())
    override val isHeadsetConnected: StateFlow<Boolean> = _isHeadsetConnected.asStateFlow()

    // Donanım değişikliklerini sürücü seviyesinde yakalayan modern sistem kancası
    private val audioDeviceCallback = object : android.media.AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            _isHeadsetConnected.value = checkCurrentAudioDevice()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            _isHeadsetConnected.value = checkCurrentAudioDevice()
        }
    }

    override fun startObserving() {
        // Main Looper üzerinden sistem servislerine kayıt oluyoruz
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, Handler(Looper.getMainLooper()))
        // Mevcut durumu Flow şelalesine pompala
        _isHeadsetConnected.value = checkCurrentAudioDevice()
    }

    override fun stopObserving() {
        // Bellek sızıntısını önlemek için takibi bırakıyoruz
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
    }

    /**
     * ALGORİTMİK TARAMA: Cihazın tüm aktif çıkış portlarını analiz eder.
     * Bluetooth (A2DP/SCO), USB-C ve Analog Jack birimlerini kapsar.
     */
    private fun checkCurrentAudioDevice(): Boolean {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return devices.any { device ->
            device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    device.type == AudioDeviceInfo.TYPE_USB_HEADSET
        }
    }
}