package org.siwonlee.alarmapp12

import android.app.PendingIntent
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build
import android.app.PendingIntent.CanceledException



class Alarm_Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // intent로부터 전달받은 Boolean
        val hr = intent.extras!!.getInt("hr")
        val min = intent.extras!!.getInt("min")

        // RingtonePlayingService 서비스 intent 생성
        val serviceIntent = Intent(context, Alarm_Service::class.java)

        //serviceIntent를 Alarm_Service로 전달한다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(serviceIntent)
        else context.startService(serviceIntent)

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
        } catch (e: CanceledException) {
            e.printStackTrace()
        }
    }
}