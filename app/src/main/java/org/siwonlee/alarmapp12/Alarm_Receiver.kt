package org.siwonlee.alarmapp12

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build
import android.content.IntentFilter




class Alarm_Receiver : BroadcastReceiver() {
    lateinit var intent: Intent
    override fun onReceive(context: Context, intent: Intent) {
        // intent로부터 전달받은 Boolean
        var hr = intent.extras!!.getInt("hr")
        var min = intent.extras!!.getInt("min")

        this.intent = intent

        // RingtonePlayingService 서비스 intent 생성
        val serviceIntent = Intent(context, Alarm_Service::class.java)
        serviceIntent.putExtra("hr", hr)
        serviceIntent.putExtra("min", min)

        //serviceIntent를 Alarm_Service로 전달한다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(serviceIntent)
        else context.startService(serviceIntent)
    }
}
