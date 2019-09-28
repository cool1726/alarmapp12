package org.siwonlee.alarmapp12


import android.content.Intent
import android.app.Service
import android.content.Context
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.os.*


class Alarm_Service : Service() {
    val context = this

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val hr = intent.extras!!.getInt("hr")
        val min = intent.extras!!.getInt("min")

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

        alarmService(hr, min)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            stopForeground(true)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun alarmService(hr: Int, min: Int) {
        val alarmIntent = Intent(context, Alarm_Ringing::class.java)
        alarmIntent.putExtra("hr", hr)
        alarmIntent.putExtra("min", min)

        val p = PendingIntent.getActivity(
            context,
            1,
            alarmIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        try {
            p.send()
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }
}
