package org.siwonlee.alarmapp12

import android.content.Intent
import android.app.Service
import android.content.Context
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import android.media.RingtoneManager
import android.os.PowerManager
import android.view.WindowManager
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T





class Alarm_Service : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        //알람이 작동함을 알리는 Toast를 출력
        Toast.makeText(this, "Alarm ringing", Toast.LENGTH_LONG).show()

        // 알람 울릴 때 5초간 진동
        var v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.EFFECT_TICK));
        } else {
            v.vibrate(5000)
        }

        // 알람 울릴 때 소리 : 기본 알람소리
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone = RingtoneManager.getRingtone(applicationContext, uri)
        ringtone.play()

        return START_NOT_STICKY


    }

        /* WakeLock: 화면 꺼진 상태에서 알람 울리면 화면 켜지게 하기 >> 아직 진행중
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "waketag")
        wakeLock.acquire(3000)
        */


    //이하 작동하지 않아 버린 코드
/*
    var isRunning: Boolean = false
    var uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    //프로그램이 여기서 자꾸 Shutdown됨
    var ringtone = RingtoneManager.getRingtone(applicationContext, uri)
    var ID : Int = 0



    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val getState = intent.extras!!.getBoolean("state")!!
        ID = if (getState) 1 else 0

        // 알람음 재생 X , 알람음 시작 클릭
        if (!this.isRunning && ID == 1) {
            ringtone.play()

            this.isRunning = true
            ID = 0
        }
        // 알람음 재생 O , 알람음 시작 버튼 클릭
        else if (this.isRunning && ID == 0) {
            ringtone.stop()

            this.isRunning = false
            ID = 0
        }
        // 알람음 재생 X , 알람음 종료 버튼 클릭
        else if (!this.isRunning && ID == 0) {
            this.isRunning = false
            ID = 0
        }
        // 알람음 재생 O , 알람음 종료 버튼 클릭
        else if (this.isRunning && ID == 1) {
            this.isRunning = true
            ID = 1
        }

        return START_NOT_STICKY
    }
 */
}
