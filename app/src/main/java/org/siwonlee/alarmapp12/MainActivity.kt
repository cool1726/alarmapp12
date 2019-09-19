package org.siwonlee.alarmapp12

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import android.widget.Toast
import java.util.*
import android.content.Context
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    //현재 시간 등을 계산할 때 사용할 Calendar 클래스
    val cal : Calendar = Calendar.getInstance()

    //알람 시간을 저장할 파일의 모델과 컨트롤러
    private val prefStorage = "org.siwonlee.alarmapp12.prefs"
    lateinit var pref : SharedPreferences


    //알람을 울릴 시간을 리시버에 전달할 AlarmManager와 Intent
    val alarmManager : AlarmManager by lazy {
        getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    val myIntent : Intent by lazy {
        Intent(this, Alarm_Receiver::class.java)
    }
    lateinit var pendingIntent : PendingIntent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //모델 prefStorage를 컨트롤러 pref와 연결함
        pref = this.getSharedPreferences(prefStorage, MODE_PRIVATE)
        //알람 시간을 이미 저장했다면 해당 시간을, 아니라면 현재 시간을 cal에 저장한다
        cal.timeInMillis = pref.getLong("Alarm_time", cal.timeInMillis)
        //알람은 항상 0초에 울린다
        cal.set(Calendar.SECOND, 0)


        //timePicker의 초기값을 현재 시간으로 지정
        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.hour = cal.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = cal.get(Calendar.MINUTE)
        } else {
            timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY))
            timePicker.setCurrentMinute(cal.get(Calendar.MINUTE))
        }


        //timePicker를 변경할 때마다
        timePicker.setOnTimeChangedListener({_, hour, minute ->
            //cal에 해당 시/분을 저장한다
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
        })


        //버튼을 누를 시
        bt_set.setOnClickListener{
            //날짜가 바뀌는 등 오류가 발생할 수 있으므로 현재 날짜를 다시 구한다
            cal.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE))
            //알람 시간이 이미 지났다면 내일 알람으로 지정한다
            if (cal.before(Calendar.getInstance())) cal.add(Calendar.DATE, 1)


            //알람 설정한 시간을 org.siwonlee.alarmapp12.prefs에 저장
            pref.edit().putLong("Alarm_time", cal.timeInMillis).apply()


            //intent에 알림을 울린다는 정보를 저장
            myIntent.putExtra("state", true)
            //pendingIntent에 intent를 담는다
            pendingIntent = PendingIntent.getBroadcast(this, 1, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)


            //알람 설정 시간을 확인하기 위해 시:분 형태로 토스트를 출력
            val str = "Alarm set in ${cal.get(Calendar.HOUR_OF_DAY)} : ${cal.get(Calendar.MINUTE)}"
            Toast.makeText(this@MainActivity, str, Toast.LENGTH_SHORT).show()


            //알람 매니저에 알람을 설정
            if (Build.VERSION.SDK_INT >= 23)
                //여기서 동작함 => SDK_INT는 23 이상 26 이하
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            else
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        }
    }
}