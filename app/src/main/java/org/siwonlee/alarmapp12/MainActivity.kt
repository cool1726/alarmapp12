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
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import java.text.SimpleDateFormat;



class MainActivity : AppCompatActivity() {
    //현재 시간 등을 계산할 때 사용할 Calendar 클래스
    val cal: Calendar = Calendar.getInstance()
    val ctx: Context = this

    //알람 시간을 저장할 파일의 모델과 컨트롤러
    private val prefStorage = "org.siwonlee.alarmapp12.prefs"
    lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

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
        timePicker.setOnTimeChangedListener({ _, hour, minute ->
            // cal에 해당 시/분을 저장한다
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
        })


        //버튼을 누를 시
        bt_set.setOnClickListener {

            // 현재 날짜, 시간
            val now : Long = System.currentTimeMillis()
            val datestr = Date(now).toString()

            var day: String? = null
            var isRepeat: Boolean = false

            //날짜가 바뀌는 등 오류가 발생할 수 있으므로 현재 날짜를 다시 구한다
            cal.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE))

            if (cal.before(Calendar.getInstance())) {
                cal.add(Calendar.DATE, 1)
                Toast.makeText(this@MainActivity, "다음날로 미뤄짐", Toast.LENGTH_SHORT).show()
            }


            if(tg_sunday.isChecked) {
                isRepeat = true
                day = "SUNDAY"
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            }
            if(tg_monday.isChecked) {
                isRepeat = true
                day = "MONDAY"
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }
            if(tg_tuesday.isChecked) {
                isRepeat = true
                day = "TUESDAY"
                cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
            }
            if(tg_wednesday.isChecked) {
                isRepeat = true
                day = "WEDNESDAY"
                cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
            }
            if(tg_thursday.isChecked) {
                isRepeat = true
                day = "THURSDAY"
                cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
            }
            if(tg_friday.isChecked) {
                isRepeat = true
                day = "FRIDAY"
                cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
            }
            if(tg_saturday.isChecked) {
                isRepeat = true
                day = "SATURDAY"
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            }

            var str : String

            //intent에 알림을 울린다는 정보를 저장
            val myIntent = Intent(this, Alarm_Receiver::class.java)
            myIntent.putExtra("hr", cal.get(Calendar.HOUR_OF_DAY))
            myIntent.putExtra("min", cal.get(Calendar.MINUTE))

            if (isRepeat) {
                myIntent.putExtra("dayOfWeek", cal.get(Calendar.DAY_OF_WEEK))
                str = "Alarm set in " + day +
                            "${cal.get(Calendar.HOUR_OF_DAY)} : ${cal.get(Calendar.MINUTE)}"
            }
            else {
                myIntent.putExtra("date", cal.get(Calendar.DATE))
                str = "Alarm set in Day ${cal.get(Calendar.DATE)} " +
                        "${cal.get(Calendar.HOUR_OF_DAY)} : ${cal.get(Calendar.MINUTE)}"
            }


            //알람 설정한 시간을 org.siwonlee.alarmapp12.prefs에 저장
            pref.edit().putLong("Alarm_time", cal.timeInMillis).apply()


            //알람 설정 시간을 확인하기 위해 시:분 형태로 토스트를 출력
            Toast.makeText(this@MainActivity, "현재 시각은 " + datestr, Toast.LENGTH_SHORT).show()
            Toast.makeText(this@MainActivity, str, Toast.LENGTH_SHORT).show()


            //pendingIntent에 intent를 담는다
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                1,
                myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )


            //알람 매니저로 알람 설정
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (isRepeat) {
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis,
                        7 * 24 * 60 * 60 * 1000, pendingIntent)
                }
                else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                }
            }
            else
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)

            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (isRepeat) {
                    if (System.currentTimeMillis() > cal.getTimeInMillis()) {
                        cal.timeInMillis = cal.getTimeInMillis() + (7 * 86400000)
                        Toast.makeText(getApplicationContext(), "next week", Toast.LENGTH_SHORT).show()
                    } else {
                        cal.timeInMillis = cal.getTimeInMillis()
                        Toast.makeText(getApplicationContext(), "this week", Toast.LENGTH_SHORT).show()
                    }
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                    //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis,
                      //  7 * 24 * 60 * 60 * 1000, pendingIntent)
                }
                else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                    Toast.makeText(getApplicationContext(), "반복없이 울림", Toast.LENGTH_SHORT).show()

            }
            else
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)*/
        }

    }
}
