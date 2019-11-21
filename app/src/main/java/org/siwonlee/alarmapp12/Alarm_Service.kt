package org.siwonlee.alarmapp12

import android.app.*
import android.content.Intent
import android.content.Context
import androidx.core.app.NotificationCompat
import android.os.*
import java.util.*

class Alarm_Service : Service() {
    private val context = this

    var hr = 0
    var min = 0
    var requestCode = 0
    var solver = 0
    var sound: String = ""

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        hr = intent.extras!!.getInt("HOUR_OF_DAY")
        min = intent.extras!!.getInt("MINUTE")
        requestCode = intent.extras!!.getInt("requestCode")
        solver = intent.extras!!.getInt("solver")
        sound = intent.getStringExtra("sound")

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

        //알람을 울리게 만들 액티비티를 실행한다
        alarmService(intent)
        //0번 requestCode에는 알람을 5분 뒤에 울리게 만드는 임시 알람을 배정한다
        if(requestCode != 0) alarmReassign(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            stopForeground(true)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun alarmService(intent: Intent) {

        //pendingIntent 설정을 위한 intent
        val alarmIntent = Intent(context, Alarm_Ringing::class.java)
        alarmIntent.putExtra("hr", hr)
        alarmIntent.putExtra("min", min)
        alarmIntent.putExtra("requestCode", requestCode)
        alarmIntent.putExtra("solver", solver)
        alarmIntent.putExtra("sound", sound)

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
        //이 함수가 불리는 날짜는 알람이 울려야 하는 요일일 것이므로
        //알람이 울리는 날에서 7일 뒤에 다시 알람을 울리도록 설정
        val cal : Calendar = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hr)
        cal.set(Calendar.MINUTE, min)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DATE, 7)

        //정보를 this에서 receiver까지 보내는 intent를 생성
        val repeatIntent = Intent(this, Alarm_Receiver::class.java)

        //setRepeating이 아니라 알람 해제 시 재등록을 통해 알람을 반복한다
        repeatIntent.putExtra("HOUR_OF_DAY", cal.get(Calendar.HOUR_OF_DAY))
        repeatIntent.putExtra("MINUTE", cal.get(Calendar.MINUTE))
        repeatIntent.putExtra("requestCode", requestCode)
        repeatIntent.putExtra("solver", solver)
        repeatIntent.putExtra("sound", sound)

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
