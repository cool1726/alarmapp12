package org.siwonlee.alarmapp12

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import android.widget.TimePicker
import android.widget.Button
import android.widget.Toast
import java.util.*
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity() {
    //현재 시간 등을 계산할 때 사용할 Calendar 클래스
    val cal : Calendar = Calendar.getInstance()

    //알람 시간을 저장할 파일의 모델과 컨트롤러
    val pref_fName = "org.siwonlee.alarmapp12.prefs"
    lateinit var pref : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //알람 시간을 저장할 모델의 컨트롤러를 설정
        pref = this.getSharedPreferences(pref_fName, MODE_PRIVATE)
        cal.setTimeInMillis(pref.getLong("Alarm_time", cal.timeInMillis))

        //hr: 시간, min: 분을 저장할 공간
        var hr : Int = cal.get(Calendar.HOUR_OF_DAY)
        var min : Int = cal.get(Calendar.MINUTE)

        //메인 액티비티의 timePicker
        val timePicker: TimePicker = findViewById(R.id.timePicker)
        //timePicker를 변경할 때마다 hr과 min을 업데이트한다
        timePicker.setOnTimeChangedListener({timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
        })

        //메인 액티비티의 Set 버튼
        val set: Button = findViewById(R.id.button)
        //버튼을 누를 시 설정한 시:분을 토스트로 출력한다
        set.setOnClickListener{
            cal.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE))
            if (cal.before(Calendar.getInstance())) {
                cal.add(Calendar.DATE, 1)
            }

            //알람 설정 시간을 확인하기 위해 시:분 형태로 토스트를 출력
            val str = hr.toString() + " : " + min.toString()
            Toast.makeText(this@MainActivity, str, Toast.LENGTH_SHORT).show()

            //알람 설정한 시간을 org.siwonlee.alarmapp12.prefs에 저장
            pref.edit().putLong("Alarm_time", cal.timeInMillis).apply()
        }

        //timePicker의 초기값을 현재 시간으로 지정
        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.setHour(hr)
            timePicker.setMinute(min)
        } else {
            timePicker.setCurrentHour(hr)
            timePicker.setCurrentMinute(min)
        }
    }
}