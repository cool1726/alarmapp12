package org.siwonlee.alarmapp12.solving

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.siwonlee.alarmapp12.alarm.Alarm_Receiver
import java.util.*

abstract class AlarmSolvingBasic : AppCompatActivity(){
    lateinit var v: Vibrator
    lateinit var ringtone: Ringtone
    var sound = ""
    var alarmName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //잠금화면 위에서 액티비티를 띄울 수 있게 해준다
        //API 제한 없이 사용할 수 있는 코드들을 사용한다
        this.window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        //API 27부터 사용 불가능한 LayoutParams를 다른 기능으로 대체한다
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            this.window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        //알람 진동을 울리게 할 Vibrator
        v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // 알람 울릴 때 1초 진동, 1초 휴식을 반복
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(1000, 1000, 1000, 1000), 0))
        } else {
            v.vibrate(longArrayOf(1000, 1000, 1000, 1000), 0)
        }

        //string화 된 Uri값을 받아와 다시 Uri로 처리
        sound = intent.getStringExtra("sound")
        Log.d("solving1sound", "${sound} sound")
        var uri = Uri.parse(sound)

        //알람음을 울릴 RingtoneManager
        ringtone = RingtoneManager.getRingtone(applicationContext, uri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val aa = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)    //RingTone이 알람 볼륨으로 울리도록
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            ringtone.audioAttributes = aa
        } else {
            ringtone.streamType = AudioManager.STREAM_ALARM
        }

        //Toast.makeText(this, "${uri} 알람소리", Toast.LENGTH_LONG).show()

        // 알람 울릴 때 소리 : 기본 알람소리
        ringtone.play()

        //알람의 이름을 가져온다
        var aName = intent.getStringExtra("name")
        if(aName != null) alarmName = aName
    }

    fun stop() {
        v.cancel()
        ringtone.stop()
        finish()
    }

    fun delay() {
        //알람 생성에 필요한 정보를 가져온다
        val solver = intent.extras!!.getInt("solver")
        val qr = intent.extras!!.getInt("qr")

        //Calendar를 버튼을 누른 시점에서 5분 후로 설정한다
        val cal : Calendar = Calendar.getInstance()
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.MINUTE, 5)

        //알람을 설정할 intent를 만든다
        val delayIntent = Intent(this, Alarm_Receiver::class.java)
        delayIntent.putExtra("timeInMillis", cal.timeInMillis)
        delayIntent.putExtra("requestCode", 0)
        delayIntent.putExtra("solver", solver)
        delayIntent.putExtra("qr", qr)
        delayIntent.putExtra("sound", sound)

        //임시 알람 정보를 담은 pendingIntent를 만든다
        val pendingIntent = PendingIntent.getBroadcast(this, 0, delayIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //알람매니저를 생성한 뒤 임시 알람을 추가한다
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        else
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)

        stop()
    }

    //뒤로가기로 알람 해제를 막기 위한 빈 함수
    override fun onBackPressed() { }
    //홈버튼으로 알람 해제를 막기 위한 빈 함수
    //override fun onMenuOpened(featureId: Int, menu: Menu): Boolean { return false }
}