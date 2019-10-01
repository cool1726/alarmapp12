package org.siwonlee.alarmapp12

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.view.WindowManager

class Alarm_Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //WakeLock을 사용할 PowerManager를 선언
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        //핸드폰의 잠금화면 위에서도 액티비티를 띄우기 위한 WakeLock
        var wakeLock = powerManager.newWakeLock(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE, "alarmapp12::AlarmLockTag")

        //wakeLock을 acquire한다
        wakeLock.acquire()

        // intent로부터 전달받은 Boolean
        val timeInMillis = intent.extras!!.getLong("timeInMillis")
        val requestCode = intent.extras!!.getInt("requestCode")

        // RingtonePlayingService 서비스 intent 생성
        val serviceIntent = Intent(context, Alarm_Service::class.java)
        serviceIntent.putExtra("timeInMillis", timeInMillis)
        serviceIntent.putExtra("requestCode", requestCode)

        //serviceIntent를 Alarm_Service로 전달한다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(serviceIntent)
        else context.startService(serviceIntent)

        //acquire한 wakeLock을 release한다
        if (wakeLock != null) {
            wakeLock.release()
            wakeLock = null
        }
        //onReceive의 끝
    }
    //Alarm_Receiver의 끝
}
