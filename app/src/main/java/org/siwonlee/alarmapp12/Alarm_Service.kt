package org.siwonlee.alarmapp12

import android.app.*
import android.content.Intent
import android.content.Context
import androidx.core.app.NotificationCompat
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import java.util.*

class Alarm_Service : Service() {
    private val context = this

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
        //showNotify(intent)
        alarmService(intent)
        alarmReassign(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            stopForeground(true)

        return super.onStartCommand(intent, flags, startId)
    }

    /*private fun showNotify(intent: Intent) {
        val notify = intent.extras!!.getLong("notify")

        val CHANNEL_ID = "Alarm"
        val CHANNEL_NAME = "Remaining Time"

        val manager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager // NotificationManager로 캐스팅
        var builder: NotificationCompat.Builder? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "this is alarm notification"
            }
            // Register the channel with the system
            manager.createNotificationChannel(channel)
            builder = NotificationCompat.Builder(this, CHANNEL_ID)
        }else {
            builder = NotificationCompat.Builder(this)
        }

        builder.setContentTitle("Alarm Notification") // 제목
        builder.setContentText("${notify/1000*60*60}시간 ${notify/1000*60}분 후 알람이 울립니다") // 내용
        builder.setSmallIcon(android.R.drawable.ic_menu_view)
        builder.setAutoCancel(true)
        builder.setWhen(notify)
        builder.setDefaults(Notification.DEFAULT_VIBRATE)

        //builder.setAutoCancel(true)
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)  // pendingIntent를 실행


        //val notify = builder.build() // 만들어진 알림 메시지를 매니저에게 요청
        //manager.notify(index, notify)         // 매니저는 알림 메시지를 폰에 알려줌
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
        }
    }*/

    private fun alarmService(intent: Intent) {
        //알람 해제 방식을 intent에서 받아와 alarmIntent에 전달한다
        val solver = intent.extras!!.getInt("solver")

        //pendingIntent 설정을 위한 intent
        val alarmIntent = Intent(context, Alarm_Ringing::class.java)
        alarmIntent.putExtra("solver", solver)

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
        var hr = intent.extras!!.getInt("HOUR_OF_DAY")
        var min = intent.extras!!.getInt("MINUTE")
        val requestCode = intent.extras!!.getInt("requestCode")
        val solver = intent.extras!!.getInt("solver")

        //이 함수가 불리는 날짜는 알람이 울려야 하는 요일일 것이므로
        //알람이 울리는 날에서 7일 뒤에 다시 알람을 울리도록 설정
        val cal : Calendar = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hr)
        cal.set(Calendar.MINUTE, min)
        cal.add(Calendar.DATE, 7)

        //정보를 this에서 receiver까지 보내는 intent를 생성
        val repeatIntent = Intent(this, Alarm_Receiver::class.java)

        //setRepeating이 아니라 알람 해제 시 재등록을 통해 알람을 반복한다
        repeatIntent.putExtra("HOUR_OF_DAY", cal.get(Calendar.HOUR_OF_DAY))
        repeatIntent.putExtra("MINUTE", cal.get(Calendar.MINUTE))
        repeatIntent.putExtra("requestCode", requestCode)
        repeatIntent.putExtra("solver", solver)

        //intent에 해당하는 pendingIntent를 생성
        val pendingIntent = PendingIntent.getBroadcast(this, requestCode, repeatIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //알람매니저를 생성한 뒤 알람을 추가한다
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        else
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
    }
}
