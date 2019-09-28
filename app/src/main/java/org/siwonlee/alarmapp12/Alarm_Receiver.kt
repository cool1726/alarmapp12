package org.siwonlee.alarmapp12

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build


class Alarm_Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
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
    }
}
