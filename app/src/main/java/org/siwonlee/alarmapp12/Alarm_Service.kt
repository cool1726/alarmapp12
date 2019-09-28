package org.siwonlee.alarmapp12

import android.app.*
import android.content.Intent
import android.content.Context
import androidx.core.app.NotificationCompat
import android.os.*

class Alarm_Service : Service() {
    val context = this

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

        //알람이 한 번 울릴 때마다 Service에서 알람을 반복시켜야 한다
        alarmService(intent)
        alarmReassign(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            stopForeground(true)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun alarmService(intent: Intent) {
        //pendingIntent 설정을 위한 intent
        val alarmIntent = Intent(context, Alarm_Ringing::class.java)

        //알람 해제 액티비티를 띄울 PendingIntent
        val p = PendingIntent.getActivity(
            context,
            1,
            alarmIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        //try catch문으로 알람 해제 액티비티를 띄운다
        try {
            p.send()
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }

    private fun alarmReassign(intent: Intent) {
        var timeInMillis = intent.extras!!.getLong("timeInMillis")
        val requestCode = intent.extras!!.getInt("requestCode")

        //timeInMillis는 지금 알람이 울릴 시간 정보이므로 timeInMillis에 7일을 더한다
        timeInMillis += 1000 * 60 * 60 * 24 * 7

        //정보를 this에서 receiver까지 보내는 intent를 생성
        val repeatIntent = Intent(this, Alarm_Receiver::class.java)

        //setRepeating이 아니라 알람 해제 시 재등록을 통해 알람을 반복한다
        repeatIntent.putExtra("timeInMillis", timeInMillis)
        repeatIntent.putExtra("requestCode", requestCode)

        //intent에 해당하는 pendingIntent를 생성
        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, repeatIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //알람매니저를 생성한 뒤 알람을 추가한다
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        else
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }
}
