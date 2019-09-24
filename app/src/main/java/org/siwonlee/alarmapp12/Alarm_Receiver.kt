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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(serviceIntent)
        else context.startService(serviceIntent)


        /* WakeLock: 화면 꺼진 상태에서 알람 울리면 화면 켜지게 하기 >> 아직 진행중
        // 시스템에서 powermanager 받아옴
        val wakeLock: PowerManager.WakeLock

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        var isScreenOn : Boolean = powerManager.isScreenOn()

        if(!isScreenOn) {
            val wl = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, "MyWakeLock")
            wl.acquire(10000)
            val wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock")
            wl_cpu.acquire(10000)
        }

        //val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        // 객체의 제어레벨을 설정함
        var sCpuWakeLock: PowerManager.WakeLock? =
            pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE, "app:alarm")

        // acquire로 앱을 깨워 cpu를 획득한다.
        sCpuWakeLock.acquire()
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
*/
    }
}