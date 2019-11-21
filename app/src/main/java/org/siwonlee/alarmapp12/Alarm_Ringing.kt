package org.siwonlee.alarmapp12

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.alarm_ringing.*
import java.util.*
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*


fun Int.toTime(): String {
    var ret = ""
    if (this < 10) ret = "0"

    return ret + this.toString()
}

class Alarm_Ringing : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_ringing)

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
            this.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        //알람이 울리는 시간을 Calendar로 알아낸다
        var hr: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val min: Int = Calendar.getInstance().get(Calendar.MINUTE)

        if(hr == 0) hr = 12

        time_now.text = "${hr.toTime()}:${min.toTime()}"


        //알람 진동을 울리게 할 Vibrator
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // 알람 울릴 때 1초 진동, 1초 휴식을 반복
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(1000, 1000, 1000, 1000), 0))
        } else {
            v.vibrate(longArrayOf(1000, 1000, 1000, 1000), 0)
        }

        //알람음을 울릴 MediaPlayer
        val sound = intent.getStringExtra("sound")
        var curSound = MediaPlayer.create(applicationContext, R.raw.believer)
        when(sound) {
            "sunny" -> curSound = MediaPlayer.create(applicationContext, R.raw.believer)
            "cloudy" -> curSound = MediaPlayer.create(applicationContext, R.raw.rockabye)
            "rainy" -> curSound = MediaPlayer.create(applicationContext, R.raw.vivalavida)
            "snowy" -> curSound = MediaPlayer.create(applicationContext, R.raw.christmasday)
        }

        // 알람 울릴 때 소리 : 기본 알람소리
        curSound.start()

        //알람 해제 버튼을 눌렀을 때
        bt_alarmoff.setOnClickListener {
            //우선 알람 소리와 진동을 해제한다
            v.cancel()
            curSound.stop()    //음악정지
            curSound.reset()   //음악초기화

            //알람 해제 방식을 읽어들인 뒤
            val solver = intent.extras!!.getInt("solver")
            when(solver) {
                //1번 방식을 택한다면 알람 해제 시 산수 계산을 해야 한다
                1 -> {
                    //산수 계산 액티비티를 띄운다
                    val solveIntent = Intent(this, AlarmSolving1::class.java)
                    startActivity(solveIntent)
                }
            }
            //알람 해제에 성공했으므로 알람 액티비티를 제거한다
            finish()
        }

        //딜레이 버튼을 눌렀을 때
        bt_delay.setOnClickListener {
            //알람 생성에 필요한 정보를 가져온다
            val hr = intent.extras!!.getInt("hr")
            val min = intent.extras!!.getInt("min")
            val solver = intent.extras!!.getInt("solver")

            //Calendar를 버튼을 누른 시점에서 5분 후로 설정한다
            val cal : Calendar = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, hr)
            cal.set(Calendar.MINUTE, min)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.MINUTE, 5)

            //알람을 설정할 intent를 만든다
            val delayIntent = Intent(this, Alarm_Receiver::class.java)
            delayIntent.putExtra("HOUR_OF_DAY", cal.get(Calendar.HOUR_OF_DAY))
            delayIntent.putExtra("MINUTE", cal.get(Calendar.MINUTE))
            delayIntent.putExtra("requestCode", 0)
            delayIntent.putExtra("solver", solver)
            delayIntent.putExtra("sound", sound)

            //임시 알람 정보를 담은 pendingIntent를 만든다
            val pendingIntent = PendingIntent.getBroadcast(this, 0, delayIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            //알람매니저를 생성한 뒤 임시 알람을 추가한다
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            else
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)

            //알람을 5분 후로 미뤘으므로 알람 소리와 진동을 해제한 뒤 액티비티를 끝낸다
            v.cancel()
            curSound.stop()    //음악정지
            curSound.reset()   //음악초기화
            finish()
        }
    }

    //뒤로가기로 알람 해제를 막기 위한 빈 함수
    override fun onBackPressed() { }
    //홈버튼으로 알람 해제를 막기 위한 빈 함수
    //eoverride fun onMenuOpened(featureId: Int, menu: Menu): Boolean { return false }
}