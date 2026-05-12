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

/** * Yankı (echo) ve geri beslemeyi (feedback) önlemek amacıyla cihazın donanımsal
 * kulaklık bağlantı durumunu (Bluetooth, USB-C, Jak) eşzamanlı olarak takip eden gözlemci sınıf.
 */
@Singleton
class HeadsetStateObserverImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HeadsetStateObserver {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /** Arayüz katmanına kulaklık bağlantı durumunu reaktif olarak ileten StateFlow. */
    private val _isHeadsetConnected = MutableStateFlow(checkCurrentAudioDevice())
    override val isHeadsetConnected: StateFlow<Boolean> = _isHeadsetConnected.asStateFlow()

    /** Donanımsal bağlantı değişikliklerini sürücü seviyesinde yakalayan sistem geri çağırımı. */
    private val audioDeviceCallback = object : android.media.AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            _isHeadsetConnected.value = checkCurrentAudioDevice()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            _isHeadsetConnected.value = checkCurrentAudioDevice()
        }
    }

    /** Kulaklık donanım durumunu dinlemeye başlar ve sistem servisine kayıt olur. */
    override fun startObserving() {
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, Handler(Looper.getMainLooper()))
        _isHeadsetConnected.value = checkCurrentAudioDevice()
    }

    /** Bellek sızıntılarını (memory leak) önlemek için sistem servisi dinlemesini sonlandırır. */
    override fun stopObserving() {
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
    }

    /** * Aktif çıkış portlarını tarayarak geçerli bir kulaklık biriminin
     * (Bluetooth A2DP/SCO, Kablolu Jak veya USB-C) bağlı olup olmadığını denetler.
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