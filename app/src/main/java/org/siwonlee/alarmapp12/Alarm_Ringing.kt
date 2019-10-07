package org.siwonlee.alarmapp12

import android.app.KeyguardManager
import android.content.Context
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
    val v by lazy { getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    val uri by lazy{ RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) }
    val ringtone by lazy{ RingtoneManager.getRingtone(applicationContext, uri) }

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

        // 알람 울릴 때 5초간 진동
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v.vibrate(5000)
        }

        // 알람 울릴 때 소리 : 기본 알람소리
        ringtone.play()

        bt_alarmoff.setOnClickListener {
            v.cancel()
            ringtone.stop()

            finish()
        }
    }
}