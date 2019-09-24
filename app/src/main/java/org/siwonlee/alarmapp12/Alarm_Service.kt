package org.siwonlee.alarmapp12


import android.content.Intent
import android.app.Service
import android.content.Context
import android.widget.Toast
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.app.NotificationChannel
import android.media.RingtoneManager
import androidx.core.os.HandlerCompat.postDelayed
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.*


class Alarm_Service : Service() {
    /*
    val v by lazy { getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    val uri by lazy{ RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) }
    val ringtone by lazy{ RingtoneManager.getRingtone(applicationContext, uri) }
     */

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
/*

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
            }, 1500)
 */
        }

        return super.onStartCommand(intent, flags, startId)
    }
}
