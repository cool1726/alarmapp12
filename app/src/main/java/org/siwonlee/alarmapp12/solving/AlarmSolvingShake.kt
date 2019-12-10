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
import kotlinx.android.synthetic.main.alarm_solving_math.*
import kotlinx.android.synthetic.main.alarm_solving_shake.*
import org.siwonlee.alarmapp12.R
import org.siwonlee.alarmapp12.alarm.Alarm_Receiver
import java.util.*

class AlarmSolvingShake : AlarmSolvingBasic(), SensorEventListener {
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

        //딜레이 버튼을 눌렀을 때
        shake_delay.setOnClickListener { delay() }
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

