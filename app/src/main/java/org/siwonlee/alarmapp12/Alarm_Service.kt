package org.siwonlee.alarmapp12

import android.content.Intent
import android.app.Service
import android.os.IBinder
import android.widget.Toast

class Alarm_Service : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //알람이 작동함을 알리는 Toast를 출력
        Toast.makeText(this, "Alarm ringing", Toast.LENGTH_LONG).show()
        return START_NOT_STICKY
    }

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
