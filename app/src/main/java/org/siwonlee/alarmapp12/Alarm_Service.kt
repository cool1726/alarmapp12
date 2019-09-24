package org.siwonlee.alarmapp12

import android.content.Intent
import android.app.Service
import android.content.Context
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import android.media.RingtoneManager
import android.R
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.app.NotificationChannel


class Alarm_Service : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //NotificationChannel의 ID
            val CHANNEL_ID = "Alarm_Service"
            //NotificationChannel
            val channel = NotificationChannel(
                CHANNEL_ID, "Alarm Title",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            ///channel을 시스템에 등록한다
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )

            //channel을 사용할 notification을 생성
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Alarm_Service")
                .setContentText("Alarm_Title").build()

            //포어그라운드에서 notification을 준다
            startForeground(1, notification)
        }

        //알람이 작동함을 알리는 Toast를 출력
        Toast.makeText(this, "Alarm ringing", Toast.LENGTH_LONG).show()

        // 알람 울릴 때 5초간 진동
        var v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.EFFECT_TICK));
        } else {
            v.vibrate(5000)
        }

        // 알람 울릴 때 소리 : 기본 알람소리
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone = RingtoneManager.getRingtone(applicationContext, uri)
        ringtone.play()

        /* WakeLock: 화면 꺼진 상태에서 알람 울리면 화면 켜지게 하기 >> 아직 진행중
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "waketag")
        wakeLock.acquire(3000)
        */

        return super.onStartCommand(intent, flags, startId)
    }
}
