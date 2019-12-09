package org.siwonlee.alarmapp12.solving

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import kotlinx.android.synthetic.main.alarm_solving_1.*
import kotlinx.android.synthetic.main.alarm_solving_shake.*
import org.siwonlee.alarmapp12.R
import org.siwonlee.alarmapp12.alarm.Alarm_Receiver
import java.util.*

class AlarmSolvingShake : AppCompatActivity(), SensorEventListener {
    private var mShakeCount: Int = 0
    private var mShakeTime: Long = System.currentTimeMillis()
    var mustShakeTime = Random().nextInt(11) + 10
    private val SHAKE_SKIP_TIME = 500
    private val SHAKE_THRESHOLD_GRAVITY = 2.7F

    lateinit var mSensorManager : SensorManager
    lateinit var mAccelerometer : Sensor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_solving_shake)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        shake_time.text = mustShakeTime.toString()





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



        //딜레이 버튼을 눌렀을 때
        bt_delay2.setOnClickListener {
            //알람 생성에 필요한 정보를 가져온다
            val solver = intent.extras!!.getInt("solver")
            val qr = intent.extras!!.getInt("qr")

            //Calendar를 버튼을 누른 시점에서 5분 후로 설정한다
            val cal : Calendar = Calendar.getInstance()
            cal.timeInMillis = intent.getLongExtra("timeInMillis", Calendar.getInstance().timeInMillis)
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

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(
            this, mAccelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
            )
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if(p0 == null) return
        if(p0.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val gravityX: Float = p0.values[0] / SensorManager.GRAVITY_EARTH
            val gravityY: Float = p0.values[1] / SensorManager.GRAVITY_EARTH
            val gravityZ: Float = p0.values[2] / SensorManager.GRAVITY_EARTH

            val f: Float = (gravityX * gravityX) + (gravityY * gravityY) + (gravityZ * gravityZ)
            val gForce = Math.sqrt(f.toDouble()).toFloat()

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                val currentTime = System.currentTimeMillis()
                if(mShakeTime + SHAKE_SKIP_TIME > currentTime) return

                mShakeTime = currentTime
                mShakeCount++
                Toast.makeText(this, "흔들기 감지", Toast.LENGTH_SHORT).show()

                val shakeToStop: Int = mustShakeTime - mShakeCount
                if(shakeToStop <= 0) finish()
                shake_time.text = shakeToStop.toString()
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Toast.makeText(this, "onAccuracyChanged 호출됨", Toast.LENGTH_SHORT).show()
    }
}

