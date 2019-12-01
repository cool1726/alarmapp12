package org.siwonlee.alarmapp12

import android.app.*
import android.content.Intent
import android.content.Context
import androidx.core.app.NotificationCompat
import android.os.*
import android.util.Log
import java.util.*

class Alarm_Service : Service() {
    private val context = this

    var timeInMillis: Long = 0
    var requestCode = 0
    var solver = 0
    var qr: String? = ""
    var sound: String? = ""

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        timeInMillis = intent.extras!!.getLong("timeInMillis")
        requestCode = intent.extras!!.getInt("requestCode")
        solver = intent.extras!!.getInt("solver")
        qr = intent.extras!!.getString("qr")
        sound = intent.extras!!.getString("sound")

        Log.d("servicesound", sound)
        Log.d("servicesolver", "${solver}")

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
        alarmService()
        //0번 requestCode에는 알람을 5분 뒤에 울리게 만드는 임시 알람을 배정한다
        //requestCode가 8만보다 크거나 같다면, 즉 요일 반복으로 설정한 것이 아니라면 알람을 재등록하지 않는다
        if(requestCode != 0 && (requestCode < 80000))
            alarmReassign()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            stopForeground(true)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun alarmService() {
        var alarmIntent = Intent(context, AlarmSolving1::class.java)
        if(solver == 0) {   //(1) 버튼 눌러 해제하는 방법
            //pendingIntent 설정을 위한 intent
            alarmIntent = Intent(context, AlarmSolving1::class.java)
            alarmIntent.putExtra("timeInMillis", timeInMillis)
            alarmIntent.putExtra("requestCode", requestCode)
            alarmIntent.putExtra("solver", solver)
            alarmIntent.putExtra("qr", qr)
            alarmIntent.putExtra("sound", sound)
        }
        else if (solver == 1) {   //(2) 수학문제 풀어 해제하는 방법
            alarmIntent = Intent(context, AlarmSolving2::class.java)
            alarmIntent.putExtra("timeInMillis", timeInMillis)
            alarmIntent.putExtra("requestCode", requestCode)
            alarmIntent.putExtra("solver", solver)
            alarmIntent.putExtra("qr", qr)
            alarmIntent.putExtra("sound", sound)
        }
        else if (solver == 2) {
            alarmIntent = Intent(context, AlarmSolving1::class.java)    //흔들기 부분 수정 필요
            alarmIntent.putExtra("timeInMillis", timeInMillis)
            alarmIntent.putExtra("requestCode", requestCode)
            alarmIntent.putExtra("solver", solver)
            alarmIntent.putExtra("qr", qr)
            alarmIntent.putExtra("sound", sound)
        }
        else {    //(4) 바코드 찍어 해제하는 방법
            alarmIntent = Intent(context, AlarmSolving4::class.java)
            alarmIntent.putExtra("timeInMillis", timeInMillis)
            alarmIntent.putExtra("requestCode", requestCode)
            alarmIntent.putExtra("solver", solver)
            alarmIntent.putExtra("qr", qr)
            alarmIntent.putExtra("sound", sound)
        }

        //알람 해제 액티비티를 띄울 PendingIntent
        val p = PendingIntent.getActivity(
            context,
            requestCode,
            alarmIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        //try catch문으로 알람 해제 액티비티를 띄운다
        try {
            p.send()
            Log.d("service", "pendingIntent delivered")
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }

    private fun alarmReassign() {
        //이 함수가 불리는 날짜는 알람이 울려야 하는 요일일 것이므로
        //알람이 울리는 날에서 7일 뒤에 다시 알람을 울리도록 설정
        val cal : Calendar = Calendar.getInstance()
        cal.timeInMillis = timeInMillis
        cal.add(Calendar.DATE, 7)

        //정보를 this에서 receiver까지 보내는 intent를 생성
        val repeatIntent = Intent(this, Alarm_Receiver::class.java)

        //setRepeating이 아니라 알람 해제 시 재등록을 통해 알람을 반복한다
        repeatIntent.putExtra("timeInMillis", cal.timeInMillis)
        repeatIntent.putExtra("requestCode", requestCode)
        repeatIntent.putExtra("solver", solver)
        repeatIntent.putExtra("qr", qr)
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
