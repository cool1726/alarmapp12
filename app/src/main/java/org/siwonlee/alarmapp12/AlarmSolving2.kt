package org.siwonlee.alarmapp12

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
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.alarm_ringing.*
import kotlinx.android.synthetic.main.alarm_solving_1.*
import java.util.*

val random = Random()

//size자리 랜덤 정수를 만드는 함수
fun getRandomNumber(size: Int): Int {
    var ret = 0
    for(i in 1..size) ret = ret * 10 + random.nextInt(9) + 1
    return ret
}

class AlarmSolving2 : AppCompatActivity() {
    //amount개의 정수와 +, -기호를 이용한 수식을 만든다
    var amount = 3
    //+, -를 배열에 저장한다
    val op = arrayOf("+", "-")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_solving_1)

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
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        //알람이 울리는 시간을 Calendar로 알아낸다
        var hr: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val min: Int = Calendar.getInstance().get(Calendar.MINUTE)

        if(hr == 0) hr = 12


        //알람 진동을 울리게 할 Vibrator
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // 알람 울릴 때 1초 진동, 1초 휴식을 반복
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(1000, 1000, 1000, 1000), 0))
        } else {
            v.vibrate(longArrayOf(1000, 1000, 1000, 1000), 0)
        }


        //string화 된 Uri값을 받아와 다시 Uri로 처리
        val sound = intent.getStringExtra("sound")
        var uri = Uri.parse(sound)

        //알람음을 울릴 RingtoneManager
        val ringtone : Ringtone = RingtoneManager.getRingtone(applicationContext, uri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val aa = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)    //RingTone이 알람 볼륨으로 울리도록
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            ringtone.audioAttributes = aa
        } else {
            ringtone.streamType = AudioManager.STREAM_ALARM
        }

        Toast.makeText(this, "${uri} 알람소리", Toast.LENGTH_LONG).show()

        // 알람 울릴 때 소리 : 기본 알람소리
        ringtone.play()

        //amount개의 랜덤 정수를 생성해 내림차순으로 정렬한다
        var nums = Array(amount, {getRandomNumber(2)}).sortedDescending()
        //가장 큰 수부터 차례로 사용하여 수식을 만든다
        qstn.text = "${nums[0]}"

        //만들어진 수식의 답을 따로 저장한다
        var answer = nums[0]

        //nums의 1번 항부터 차례로
        for(i in 1 until amount) {
            //각 항 사이의 연산자는 차례로 -, +, -, +, ... 순서로 진행한다
            qstn.text = "${qstn.text} ${op[i % 2]} ${nums[i]}"
            //i가 홀수라면 nums[i]를 answer에서 빼고, 아니라면 더한다
            answer += nums[i] * (1 - 2 * (i % 2))
        }

        //알람 해제 버튼을 눌렀을 때
        bt_alarmoff2.setOnClickListener {
            if(Integer.parseInt(nswr.text.toString()) == answer) {
                v.cancel()
                ringtone.stop()
                finish()
            }
            else {
                Toast.makeText(this, "틀렸습니다. 다시 입력하세요.", Toast.LENGTH_LONG).show()
                qstn.text= ""
            }
        }

        //딜레이 버튼을 눌렀을 때
        bt_delay2.setOnClickListener {
            //알람 생성에 필요한 정보를 가져온다
            val solver = intent.extras!!.getInt("solver")
            val qr = intent.extras!!.getInt("qr")

            //Calendar를 버튼을 누른 시점에서 5분 후로 설정한다
            val cal : Calendar = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, hr)
            cal.set(Calendar.MINUTE, min)
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

            //알람을 5분 후로 미뤘으므로 알람 소리와 진동을 해제한 뒤 액티비티를 끝낸다
            v.cancel()
            ringtone.stop()
            finish()
        }
    }

    //뒤로가기로 알람 해제를 막기 위한 빈 함수
    override fun onBackPressed() { }
}