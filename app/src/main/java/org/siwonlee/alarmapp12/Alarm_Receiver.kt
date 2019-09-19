package org.siwonlee.alarmapp12

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build


class Alarm_Receiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // intent로부터 전달받은 Boolean
        val trig = intent.extras!!.getBoolean("state")

        // RingtonePlayingService 서비스 intent 생성
        val serviceIntent = Intent(context, Alarm_Service::class.java)

        // Alarm_Service로 trig 보내기
        serviceIntent.putExtra("state", trig)

        //serviceIntent를 Alarm_Service로 전달한다
        if (Build.VERSION.SDK_INT >= 26)
            context.startForegroundService(serviceIntent)
        else context.startService(serviceIntent)
    }
}