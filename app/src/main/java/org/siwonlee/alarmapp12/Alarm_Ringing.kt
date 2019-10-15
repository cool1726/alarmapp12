package org.siwonlee.alarmapp12

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.alarm_ringing.*
import java.util.*
import android.view.WindowManager



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

        //알람음을 울릴 RingtoneManager
        val ringtone = RingtoneManager.getRingtone(
            applicationContext,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        )

        // 알람 울릴 때 소리 : 기본 알람소리
        ringtone.play()


        //알람 해제 버튼을 눌렀을 때
        bt_alarmoff.setOnClickListener {
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

            //알람 소리와 진동을 해제한다
            v.cancel()
            ringtone.stop()

            finish()
        }
    }

    //뒤로가기로 알람 해제를 막기 위한 빈 함수
    override fun onBackPressed() { }
}