package org.siwonlee.alarmapp12

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import java.util.*
import android.content.Context
import android.graphics.Color
import kotlinx.android.synthetic.main.activity_main.*

fun Boolean.toInt() = if (this) 1 else 0

class MainActivity : AppCompatActivity() {
        //액티비티 생성 시 알람 설정 시간을 받아온다
    var hr = 6
    var min = 0
    var index = 0
    var switch = booleanArrayOf(true, false, false, false, false, false, false, false)

    var stringDate : String = ""        // 한글로 날짜 저장
    var stringSwitch : String = ""      // T/F로 날짜 저장
    val days = arrayOf("일 ", "월 ", "화 ", "수 ", "목 ", "금 ", "토 ")

    //현재 시간 등을 계산할 때 사용할 Calendar 클래스
    val cal : Calendar = Calendar.getInstance()

    //텍스트뷰를 터치했을 때 바꿀 색
    var tColor = arrayOf(Color.GRAY, /*Color.parseColor("#008577")*/Color.RED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //알람 설정 시간과 분은 그대로 받아온다
        hr = intent.getIntExtra("hr", 6)
        min = intent.getIntExtra("min", 0)

        // 알람 수정 또는 삭제 시 사용됨
        val position = intent.getIntExtra("position", -1)


        //알람 설정 요일은 String타입으로 받아오기 때문에 각 글자를 decoding해준다
        // 'TFFFFFFF' 형식의 (저장된) stringSwitch값
        stringSwitch = intent.getStringExtra("stringSwitch") ?: "TFFFFFFF"

        for(i in 1..7) switch[i] = (stringSwitch[i] == 'T')

        //설정된 요일에 따라 텍스트 색을 다르게 바꿔준다
        sun.setTextColor(tColor[switch[1].toInt()])
        mon.setTextColor(tColor[switch[2].toInt()])
        tue.setTextColor(tColor[switch[3].toInt()])
        wed.setTextColor(tColor[switch[4].toInt()])
        thu.setTextColor(tColor[switch[5].toInt()])
        fri.setTextColor(tColor[switch[6].toInt()])
        sat.setTextColor(tColor[switch[7].toInt()])

        //현재 알람이 몇 번째 알람인지 받아온다
        index = intent.getIntExtra("index", 0)

        //cal의 시간을 알람을 설정한 시간으로 바꾼다
        cal.set(Calendar.HOUR_OF_DAY, hr)
        cal.set(Calendar.MINUTE, min)

        //알람은 항상 0초에 울린다
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        //timePicker의 초기값을 hr/min으로 지정
        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.hour = hr
            timePicker.minute = min
        } else {
            timePicker.setCurrentHour(hr)
            timePicker.setCurrentMinute(min)
        }

        //timePicker를 변경할 때마다
        timePicker.setOnTimeChangedListener({_, hour, minute ->
            //cal에 해당 시/분을 저장한다
            hr = hour
            min = minute
        })

        //------------------------------------------------------------------setOnClickListener 시작

        //각 요일 버튼을 클릭했을 때
        //알람이 해당 요일에 울리는 것을 표시하고
        //이를 텍스트의 색으로 나타낸다
        sun.setOnClickListener{
            switch[1] = !switch[1]
            sun.setTextColor(tColor[switch[1].toInt()])
        }
        mon.setOnClickListener{
            switch[2] = !switch[2]
            mon.setTextColor(tColor[switch[2].toInt()])
        }
        tue.setOnClickListener{
            switch[3] = !switch[3]
            tue.setTextColor(tColor[switch[3].toInt()])
        }
        wed.setOnClickListener{
            switch[4] = !switch[4]
            wed.setTextColor(tColor[switch[4].toInt()])
        }
        thu.setOnClickListener{
            switch[5] = !switch[5]
            thu.setTextColor(tColor[switch[5].toInt()])
        }
        fri.setOnClickListener{
            switch[6] = !switch[6]
            fri.setTextColor(tColor[switch[6].toInt()])
        }
        sat.setOnClickListener{
            switch[7] = !switch[7]
            sat.setTextColor(tColor[switch[7].toInt()])
        }



        // 알람 삭제 버튼 (bt_set.setOnClickListener와 유사)
        bt_delete.setOnClickListener {
            //pendingIntent로 alarmManager의 알람은 여기서 삭제하고
            //알람 recyclerview의 해당 알람 item은 returnIntent로 정보를 넘겨서 삭제하게 한다

            //삭제할 알람 정보를 AlarmList_Acitivity로 넘긴다
            val returnIntent = Intent(this, AlarmList_Activity::class.java)
            returnIntent.putExtra("position", position)
            returnIntent.putExtra("delete", true)
            returnIntent.putExtra("index", index)

            //삭제할지 여부를 묻는 Dialog
            val builder = AlertDialog.Builder(this)
            builder.setMessage("알람을 삭제하시겠습니까?")

            //삭제버튼 클릭 :  함수 delAlarm 호출
            //알람 생성시에 했던 것과 똑같이 for문으로 requestCode값 전달
            // alarmManager의 알람 삭제가 이루어지면 AlarmList_Activity로 돌아감
            builder.setPositiveButton("삭제") { _, _ ->
                for(i in 1..7) delAlarm(i)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
            builder.setNegativeButton("취소") { _, _ ->  }

            builder.create().show()
        }



        // 알람 저장 버튼
        bt_set.setOnClickListener {

            //날짜가 바뀌는 등 오류가 발생할 수 있으므로 현재 날짜를 다시 구한다
            cal.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE))

            //알람을 설정한 시간:분을 cal에 저장한다
            cal.set(Calendar.HOUR_OF_DAY, hr)
            cal.set(Calendar.MINUTE, min)

            //액티비티를 종료하기 전, 알람을 설정한 요일을 stringSwitch에 저장한다
            stringDate = ""

            //AlarmList_Activity에 정보를 넘길 intent
            val returnIntent = Intent()

            //설정한 알람 정보를 AlarmList_Acitivity로 넘긴다
            returnIntent.putExtra("hr", hr)
            returnIntent.putExtra("min", min)
            returnIntent.putExtra("time", "${hr.toTime()}:${min.toTime()}")
            returnIntent.putExtra("stringSwitch", stringSwitch)
            returnIntent.putExtra("index", index)

            // 알람 수정 or 초기 셋팅
            if (position != -1) {
                returnIntent.putExtra("position", position)
                for(i in 1..7) { // 기존 알람을 삭제하고 다시 저장한다
                    delAlarm(i)
                    setAlarm(i)
                }
            }
            else {  // 알람 수정이 아닐 경우, 그냥 저장한다
                for(i in 1..7) setAlarm(i)
            }

            returnIntent.putExtra("date", stringDate)

            //AlarmList_Acitivity에 RESULT_OK 신호와 함께 intent를 넘긴다
            setResult(Activity.RESULT_OK, returnIntent)
            finish()

        }
    }



    // requestCode: setAlarm과 동일하게 받아온다
    // 동일한 requestCode와 intent로 pendingIntent를 구분해 alarmManager에서 삭제한다
    fun delAlarm(requestCode: Int) {
        val intent = Intent(this, Alarm_Receiver::class.java)

        intent.putExtra("HOUR_OF_DAY", hr)
        intent.putExtra("MINUTE", min)
        intent.putExtra("requestCode", index * 7 + requestCode)

        //setAlarm 시 설정한 pendingIntent와 동일하게 설정
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            //한 알람 객체당 알람을 최대 7개 설정하므로
            //index를 이용해 각 알람 객체 당 알람이 겹치지 않게 설정한다
            index * 7 + requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 알람을 삭제한다
        alarmManager.cancel(pendingIntent)
    }


    //requestCode: 알람이 울리는 요일을 Calendar의 요일 형식으로 넘김
    fun setAlarm(requestCode: Int) {
        //정보를 this에서 receiver까지 보내는 intent를 생성
        val intent = Intent(this, Alarm_Receiver::class.java)

        //setRepeating이 아니라 알람 해제 시 재등록을 통해 알람을 반복한다
        intent.putExtra("HOUR_OF_DAY", hr)
        intent.putExtra("MINUTE", min)
        //한 알람 객체당 알람을 최대 7개 설정하므로
        //index를 이용해 각 알람 객체 당 알람이 겹치지 않게 설정한다
        intent.putExtra("requestCode", index * 7 + requestCode)

        //정해진 요일에 맞는 PendingIntent를 설정한다
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            //한 알람 객체당 알람을 최대 7개 설정하므로
            //index를 이용해 각 알람 객체 당 알람이 겹치지 않게 설정한다
            index * 7 + requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        //알람을 설정할 AlarmManager 클래스
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if(switch[requestCode]) {
            //현재 요일에 알람이 울림을 stringSwitch에 표시
            stringDate = "${stringDate}${days[requestCode - 1]}"

            //오늘부터 requestCode요일까지 남은 일 수
            var date_diff = (requestCode - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7

            //알람이 울리는 요일이 i요일이 되도록 cal을 조정한다
            cal.add(Calendar.DATE, date_diff)
            if(cal.before(Calendar.getInstance())) {
                cal.add(Calendar.DATE, 7)
                date_diff += 7
            }

            //알람 매니저에 알람을 설정한다
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            else
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)

            //날짜를 다시 오늘로 맞춘다
            cal.add(Calendar.DATE, -date_diff)
        }

        //만일 requestCode요일에 알람을 울리지 않아야 한다면 알람을 취소한다
        else alarmManager.cancel(pendingIntent)
    }
}