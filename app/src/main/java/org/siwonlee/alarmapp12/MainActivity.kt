package org.siwonlee.alarmapp12

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import java.util.*
import android.content.Context
import android.graphics.Color
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

fun Boolean.toInt() = if (this) 1 else 0

class MainActivity : AppCompatActivity() {
    //현재 시간 등을 계산할 때 사용할 Calendar 클래스
    val cal : Calendar = Calendar.getInstance()

    var days = arrayOf(false, false, false, false, false, false, false, false)
    var tColor = arrayOf(Color.GRAY, /*Color.parseColor("#008577")*/Color.RED)

    //알람 시간을 저장할 파일의 모델과 컨트롤러
    private val prefStorage = "org.siwonlee.alarmapp12.prefs"
    lateinit var pref : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //모델 prefStorage를 컨트롤러 pref와 연결함
        pref = this.getSharedPreferences(prefStorage, MODE_PRIVATE)
        //알람 시간을 이미 저장했다면 해당 시간을, 아니라면 현재 시간을 cal에 저장한다
        cal.timeInMillis = pref.getLong("Alarm_time", cal.timeInMillis)
        //알람은 항상 0초에 울린다
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

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

        //각 요일 버튼을 클릭했을 때
        //알람이 해당 요일에 울리는 것을 표시하고
        //이를 텍스트의 색으로 나타낸다
        sun.setOnClickListener{
            days[1] = !days[1]
            sun.setTextColor(tColor[days[1].toInt()])
        }
        mon.setOnClickListener{
            days[2] = !days[2]
            mon.setTextColor(tColor[days[2].toInt()])
        }
        tue.setOnClickListener{
            days[3] = !days[3]
            tue.setTextColor(tColor[days[3].toInt()])
        }
        wed.setOnClickListener{
            days[4] = !days[4]
            wed.setTextColor(tColor[days[4].toInt()])
        }
        thu.setOnClickListener{
            days[5] = !days[5]
            thu.setTextColor(tColor[days[5].toInt()])
        }
        fri.setOnClickListener{
            days[6] = !days[6]
            fri.setTextColor(tColor[days[6].toInt()])
        }
        sat.setOnClickListener{
            days[7] = !days[7]
            sat.setTextColor(tColor[days[7].toInt()])
        }

        //버튼을 누를 시
        bt_set.setOnClickListener{
            //날짜가 바뀌는 등 오류가 발생할 수 있으므로 현재 날짜를 다시 구한다
            cal.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE))

            //알람 설정한 시간을 org.siwonlee.alarmapp12.prefs에 저장
            pref.edit().putLong("Alarm_time", cal.timeInMillis).apply()

            //알람 설정 시간을 확인하기 위해 시:분 형태로 토스트를 출력
            var isSet = false

            //알람 매니저를 생성하여 알람을 설정
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

            //각 요일에 대해 i번 요일에 알람을 울리게 설정했다면
            for(i in 1..7) {
                //정보를 this에서 receiver까지 보내는 intent를 생성
                val myIntent = Intent(this, Alarm_Receiver::class.java)
                //setRepeating이 아니라 알람 해제 시 재등록을 통해 알람을 반복한다
                myIntent.putExtra("timeInMillis", cal.timeInMillis)
                myIntent.putExtra("requestCode", i)
                myIntent.putExtra("trig", days[i])

                //intent에 해당하는 pendingIntent를 생성
                val pendingIntent = PendingIntent.getBroadcast(this, i, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                //만일 i요일에 알람을 울려야 한다면
                if(days[i]){
                    //알람이 설정되었음을 표시한다
                    isSet = true
                    //오늘로부터 가장 가까운 i요일까지 걸리는 일수
                    val date_diff = (i - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7

                    //알람 삭제를 위해 requestCode를 pref에 저장한다
                    pref.edit().putInt("requestCode", i).apply()

                    //i요일이 되도록 cal을 조정한다
                    cal.add(Calendar.DATE, date_diff)

                    //알람 매니저에 알람을 설정
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                    else
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)

                    //날짜를 다시 오늘로 맞춘다
                    cal.add(Calendar.DATE, -date_diff)
                }

                //만일 i요일에 알람을 울리지 않아야 한다면 알람을 취소한다
                else alarmManager.cancel(pendingIntent)
            }

            if(isSet) Toast.makeText(getApplicationContext(), "Alarm has been set.", Toast.LENGTH_LONG).show()
        }
        //onCreate의 끝
    }
    //클래스의 끝
}
