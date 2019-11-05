package org.siwonlee.alarmapp12

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Build.ID
import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.previous_alarmset.*
import kotlinx.android.synthetic.main.previous_alarmset.preHr2
import kotlinx.android.synthetic.main.previous_alarmset.preMin2
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*


class PreviousAlarmAcitivity : AppCompatActivity() {

    var hr = 6
    var min = 0

    var prehr : Int = 0
    var premin : Int = 0

    var ahr = arrayOf(0, 0, 0, 0, 0, 0, 0)
    var amin = arrayOf(0, 0, 0, 0, 0, 0, 0)
    var switch = booleanArrayOf(true, false, false, false, false, false, false, false)

    var ringDate : String = ""        // 한글로 날짜 저장
    var stringSwitch : String = ""      // T/F로 날짜 저장
    val days = arrayOf("일 ", "월 ", "화 ", "수 ", "목 ", "금 ", "토 ")

    //현재 시간 등을 계산할 때 사용할 Calendar 클래스
    val cal : Calendar = Calendar.getInstance()

    var tColor = arrayOf(Color.GRAY, /*Color.parseColor("#008577")*/Color.RED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.previous_alarmset)

        // 알람 수정 또는 삭제 시 사용됨
        val position = intent.getIntExtra("position", -1)

        //cal의 시간을 알람을 설정한 시간으로 바꾼다
        cal.set(Calendar.HOUR_OF_DAY, hr)
        cal.set(Calendar.MINUTE, min)

        //알람은 항상 0초에 울린다
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        sun_time.setOnClickListener {
            showTimePicker(0, sun_time)
        }
        mon_time.setOnClickListener {
            showTimePicker(1, mon_time)
        }
        tue_time.setOnClickListener {
            showTimePicker(2, tue_time)
        }
        wed_time.setOnClickListener {
            showTimePicker(3, wed_time)
        }
        thu_time.setOnClickListener {
            showTimePicker(4, thu_time)
        }
        fri_time.setOnClickListener {
            showTimePicker(5, fri_time)
        }
        sat_time.setOnClickListener {
            showTimePicker(6, sat_time)
        }

        // 알람 삭제 버튼 (bt_set2.setOnClickListener와 유사)
        bt_del2.setOnClickListener {
            //pendingIntent로 alarmManager의 알람은 여기서 삭제하고
            //알람 recyclerview의 해당 알람 item은 returnIntent로 정보를 넘겨서 삭제하게 한다

            //삭제할 알람 정보를 AlarmList_Acitivity로 넘긴다
            val returnIntent = Intent(this, AlarmList_Activity::class.java)
            returnIntent.putExtra("position", position)
            returnIntent.putExtra("delete", true)

            //에러 방지를 위해 남겨둠
            returnIntent.putExtra("ID", ID)

            //삭제할지 여부를 묻는 Dialog
            val builder = AlertDialog.Builder(this)
            builder.setMessage("알람을 삭제하시겠습니까?")

            //삭제버튼 클릭 :  함수 delAlarm 호출
            //알람 생성시에 했던 것과 똑같이 for문으로 day값 전달
            // alarmManager의 알람 삭제가 이루어지면 AlarmList_Activity로 돌아감
            builder.setPositiveButton("삭제") { _, _ ->
                for(i in 0..6)
                    setAlarm(i, false)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
            builder.setNegativeButton("취소") { _, _ ->  }

            builder.create().show()
        }

        bt_set2.setOnClickListener {
            //날짜가 바뀌는 등 오류가 발생할 수 있으므로 현재 날짜를 다시 구한다
            cal.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE))

            //알람을 설정한 시간:분을 cal에 저장한다
            cal.set(Calendar.HOUR_OF_DAY, hr)
            cal.set(Calendar.MINUTE, min)

            if (isEmpty(preHr2.text))  prehr = 0
            else  prehr = Integer.parseInt(preHr2.text.toString())

            if (isEmpty(preMin2.text))  premin = 0
            else  premin = Integer.parseInt(preMin2.text.toString())

            //액티비티를 종료하기 전, 알람을 설정한 요일을 stringSwitch에 저장한다
            stringSwitch = ""
            for(i in 0..7) {
                if(switch[i]) stringSwitch = "${stringSwitch}T"
                else stringSwitch = "${stringSwitch}F"
            }

            //AlarmList_Activity에 정보를 넘길 intent
            val returnIntent = Intent()

            //에러 방지를 위해 남겨둠
            returnIntent.putExtra("ID", ID)

            // 알람을 수정하는 경우 현재 알람의 위치를 returnIntent에 담는다
            if (position != -1) returnIntent.putExtra("position", position)

            //알람의 요일 설정 정보를 stringDate에 담는다
            //ringDate = ""
            //각 요일마다 알람을 설정하거나 삭제한다
            for(i in 0..6) {
                setAlarm(i, switch[i + 1])
            }

            //설정한 알람 정보를 AlarmList_Acitivity로 넘긴다
            returnIntent.putExtra("hr", ahr)
            returnIntent.putExtra("min", amin)
            returnIntent.putExtra("time", "${(prehr).toTime()}:${(premin).toTime()}")
            returnIntent.putExtra("stringSwitch", stringSwitch)
            //returnIntent.putExtra("solver", solver)
            returnIntent.putExtra("date", ringDate)

            returnIntent.putExtra("prehr", prehr)
            returnIntent.putExtra("premin", premin)

            Toast.makeText(this, "${prehr}H ${premin}M previous on ${ringDate}", Toast.LENGTH_LONG).show()

            //AlarmList_Acitivity에 RESULT_OK 신호와 함께 intent를 넘긴다
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

    }

    fun setAlarm(day: Int, set: Boolean) {
        //정보를 this에서 receiver까지 보내는 intent를 생성
        val intent = Intent(this, Alarm_Receiver::class.java)

        //알람 정보를 dHHMM로 나타내면 알람이 서로 겹치지 않는다
        val requestCode: Int = (day * 10000) + (ahr[day] * 100 + amin[day])

        cal.set(Calendar.HOUR_OF_DAY, ahr[day])
        cal.set(Calendar.MINUTE, amin[day])

        cal.add(Calendar.HOUR_OF_DAY, -prehr)
        cal.add(Calendar.MINUTE, -premin)

        //setRepeating이 아니라 알람 해제 시 재등록을 통해 알람을 반복한다
        intent.putExtra("HOUR_OF_DAY", cal.get(Calendar.HOUR_OF_DAY))
        intent.putExtra("MINUTE", cal.get(Calendar.MINUTE))

        intent.putExtra("requestCode", requestCode)

        //정해진 요일에 맞는 PendingIntent를 설정한다
        val pendingIntent = PendingIntent.getBroadcast(
            this, requestCode,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        //알람을 설정할 AlarmManager 클래스
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //day요일에 알람을 울려야 한다면
        if(set) {
            //현재 요일에 알람이 울림을 stringSwitch에 표시
            //ringDate = "${ringDate}${days[day]}"

            //오늘부터 day요일까지 남은 일 수
            var date_diff = (day + 1 - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7

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

        //만일 day요일에 알람을 울리지 않아야 한다면 알람을 취소한다
        else alarmManager.cancel(pendingIntent)
    }

    fun showTimePicker(i: Int, v: TextView) {

        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener{timePicker, hour, minute ->
            hr = hour
            min = minute

            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)

            switch[i+1] = !switch[i+1]
            ahr[i] = hour
            amin[i] = minute

            v.text = SimpleDateFormat("HH:mm").format(cal.time)
            ringDate = "${ringDate}${days[i]}"
        }, cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), false).show()


    }

}