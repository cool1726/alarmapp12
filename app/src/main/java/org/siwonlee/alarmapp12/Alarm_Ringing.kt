package org.siwonlee.alarmapp12

import android.content.Context
import android.media.RingtoneManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.alarm_ringing.*
import java.util.*

class Alarm_Ringing : AppCompatActivity() {
    val v by lazy { getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    val uri by lazy{ RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) }
    val ringtone by lazy{ RingtoneManager.getRingtone(applicationContext, uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_ringing)

        val hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val min = Calendar.getInstance().get(Calendar.MINUTE)

        time_now.text = "${hr}:${min}"

        // 알람 울릴 때 5초간 진동
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.EFFECT_TICK))
        } else {
            v.vibrate(5000)
        }

        // 알람 울릴 때 소리 : 기본 알람소리
        ringtone.play()

        /* 알람 벨소리/진동이 울리지 않을 경우 시도해볼 것
        val delayHandler = Handler()
        delayHandler.postDelayed(Runnable {
            // 알람 울릴 때 5초간 진동
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.EFFECT_TICK))
            } else {
                v.vibrate(5000)
            }

            // 알람 울릴 때 소리 : 기본 알람소리
            ringtone.play()
        }, 1000)
        */

        bt_alarmoff.setOnClickListener {
            v.cancel()
            ringtone.stop()

            finish()
        }
    }
}