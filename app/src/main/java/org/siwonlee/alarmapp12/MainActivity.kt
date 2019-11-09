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
import android.text.TextUtils.isEmpty
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import kotlinx.android.synthetic.main.activity_main.*


fun Boolean.toInt() = if (this) 1 else 0

var ADVANCE_SETTING = 2019

class MainActivity : AppCompatActivity() {
    //액티비티 생성 시 알람 설정 시간을 받아온다
    var hr = 6
    var min = 0
    var phr = 0
    var pmin = 0
    var solver = 0
    var switch = booleanArrayOf(true, false, false, false, false, false, false, false)

    var before_id = -1

    var ringDate : String = ""        // 한글로 날짜 저장
    var intSwitch = 0
    private val days = arrayOf("일", "월", "화", "수", "목", "금", "토")

    //현재 시간 등을 계산할 때 사용할 Calendar 클래스
    val cal : Calendar = Calendar.getInstance()

    //텍스트뷰를 터치했을 때 바꿀 색
    var tColor = arrayOf(Color.GRAY, /*Color.parseColor("#008577")*/Color.RED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //알람을 생성하는 것이라면 true, 수정하는 것이라면 false
        val isInit = intent.getBooleanExtra("isInit", true)

        //알람 설정 시간과 분은 그대로 받아온다
        hr = intent.getIntExtra("hr", 6)
        min = intent.getIntExtra("min", 0)
        phr = intent.getIntExtra("phr", 0)
        pmin = intent.getIntExtra("pmin", 0)

        //알람 해제 방식을 받아와 설정한다
        solver = intent.getIntExtra("solver", 0)

        //알람 설정 요일은 booleanArray을 Int로 압축한 값을 가져오므로
        //이를 intSwitch에 넣고 decode해야 한다
        intSwitch = intent.getIntExtra("intSwitch", 0)

        //알람을 수정하는 것이라면 수정 이전 알람의 정보를 before_id에 담는다
        if(!isInit) {
            before_id = (intSwitch * 100 + hr) * 100 + min

            //정보를 this에서 receiver까지 보내는 intent를 생성
            val intent = Intent(this, Alarm_Receiver::class.java)

            for (day in 1..7) {
                //알람 정보를 dHHMM로 나타내면 알람이 서로 겹치지 않는다
                val requestCode: Int = (day * 100 + hr) * 100 + min

                //정해진 요일에 맞는 PendingIntent를 설정한다
                val pendingIntent = PendingIntent.getBroadcast(
                    this, requestCode,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT
                )
                //알람을 설정할 AlarmManager 클래스
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                alarmManager.cancel(pendingIntent)
            }
        }

        //intSwitch를 decode한다
        for(i in 7 downTo 0) {
            switch[i] = (intSwitch % 2 == 1)
            intSwitch /= 2
        }

        //설정된 요일에 따라 텍스트 색을 다르게 바꿔준다
        //date.setTextColor(tColor[switch[0].toInt()])
        sun.setTextColor(tColor[switch[1].toInt()])
        mon.setTextColor(tColor[switch[2].toInt()])
        tue.setTextColor(tColor[switch[3].toInt()])
        wed.setTextColor(tColor[switch[4].toInt()])
        thu.setTextColor(tColor[switch[5].toInt()])
        fri.setTextColor(tColor[switch[6].toInt()])
        sat.setTextColor(tColor[switch[7].toInt()])

        // 알람 수정 또는 삭제 시 사용됨
        val position = intent.getIntExtra("position", -1)

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

        //------------------------------------------------------------------setOnClickListener 시작

        //timePicker를 변경할 때마다
        timePicker.setOnTimeChangedListener({_, hour, minute ->
            //cal에 해당 시/분을 저장한다
            hr = hour
            min = minute
        })

        //각 요일 버튼을 클릭했을 때
        //알람이 해당 요일에 울리는 것을 표시하고
        //이를 텍스트의 색으로 나타낸다
        sun.setOnClickListener{
            switch[1] = !switch[1]
            switch[0] = false
            sun.setTextColor(tColor[switch[1].toInt()])
        }
        mon.setOnClickListener{
            switch[2] = !switch[2]
            switch[0] = false
            mon.setTextColor(tColor[switch[2].toInt()])
        }
        tue.setOnClickListener{
            switch[3] = !switch[3]
            switch[0] = false
            tue.setTextColor(tColor[switch[3].toInt()])
        }
        wed.setOnClickListener{
            switch[4] = !switch[4]
            wed.setTextColor(tColor[switch[4].toInt()])
        }
        thu.setOnClickListener{
            switch[5] = !switch[5]
            switch[0] = false
            thu.setTextColor(tColor[switch[5].toInt()])
        }
        fri.setOnClickListener{
            switch[6] = !switch[6]
            switch[0] = false
            fri.setTextColor(tColor[switch[6].toInt()])
        }
        sat.setOnClickListener{
            switch[7] = !switch[7]
            switch[0] = false
            sat.setTextColor(tColor[switch[7].toInt()])
        }

        //알람에 대한 추가 설정이 필요하다면
        advance.setOnClickListener({
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.alarm_setting_advanced, null)
            val dialogSolving = dialogView.findViewById<Spinner>(R.id.solving)
            val dialogHr = dialogView.findViewById<EditText>(R.id.preHr)
            val dialogMin = dialogView.findViewById<EditText>(R.id.preMin)

            if (phr != 0)
                dialogHr.setText(phr.toString())
            if (pmin != 0)
                dialogMin.setText(pmin.toString())

            dialogSolving.adapter = ArrayAdapter(
                applicationContext,
                android.R.layout.simple_spinner_dropdown_item,
                resources.getStringArray(R.array.spinnerItem)
            )

            dialogSolving.setSelection(solver)

            dialogSolving.setOnItemSelectedListener(object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>,
                    view: View,
                    i: Int,
                    l: Long
                ) {
                    solver = i
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {}
            })

            builder.setView(dialogView)
            builder.setPositiveButton("확인") { _, _ ->
                if (isEmpty(dialogHr.text)) phr = 0
                else phr = Integer.parseInt(dialogHr.text.toString())

                if (isEmpty(dialogMin.text)) pmin = 0
                else pmin = Integer.parseInt(dialogMin.text.toString())
            }
            builder.setNegativeButton("취소") { _, _ -> /* 취소일 때 아무 액션이 없으므로 빈칸 */ }
            builder.create().show()
        })

        // 알람 삭제 버튼 (bt_set.setOnClickListener와 유사)
        bt_delete.setOnClickListener {
            //pendingIntent로 alarmManager의 알람은 여기서 삭제하고
            //알람 recyclerview의 해당 알람 item은 returnIntent로 정보를 넘겨서 삭제하게 한다

            //삭제할 알람 정보를 AlarmList_Acitivity로 넘긴다
            val returnIntent = Intent(this, AlarmList_Activity::class.java)
            returnIntent.putExtra("position", position)
            returnIntent.putExtra("delete", true)
            returnIntent.putExtra("before_id", before_id)

            //삭제할지 여부를 묻는 Dialog
            val builder = AlertDialog.Builder(this)
            builder.setMessage("알람을 삭제하시겠습니까?")

            //삭제버튼 클릭 :  함수 delAlarm 호출
            //알람 생성시에 했던 것과 똑같이 for문으로 day값 전달
            // alarmManager의 알람 삭제가 이루어지면 AlarmList_Activity로 돌아감
            builder.setPositiveButton("삭제") { _, _ ->
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

           //액티비티를 종료하기 전, 알람을 설정한 요일을 intSwitch에 저장한다
            intSwitch = 0
            for(i in 0..7) {
                intSwitch *= 2
                if(switch[i]) intSwitch += 1
            }

            //AlarmList_Activity에 정보를 넘길 intent
            val returnIntent = Intent()

            // 알람을 수정하는 경우 현재 알람의 위치를 returnIntent에 담는다
            if (position != -1) returnIntent.putExtra("position", position)

            //알람의 요일 설정 정보를 stringDate에 담는다
            ringDate = ""
            for(i in 1..7) if(switch[i]) ringDate = "${ringDate} ${days[i - 1]}"

            //설정한 알람 정보를 AlarmList_Acitivity로 넘긴다
            returnIntent.putExtra("hr", hr)
            returnIntent.putExtra("min", min)
            returnIntent.putExtra("intSwitch", intSwitch)

            returnIntent.putExtra("solver", solver)
            returnIntent.putExtra("phr", phr)
            returnIntent.putExtra("pmin", pmin)

            returnIntent.putExtra("time", "${hr.toTime()}:${min.toTime()}")
            returnIntent.putExtra("date", ringDate)

            returnIntent.putExtra("before_id", before_id)

            //AlarmList_Acitivity에 RESULT_OK 신호와 함께 intent를 넘긴다
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            solver = data!!.getIntExtra("solver", solver)
            phr = data.getIntExtra("phr", phr)
            pmin = data.getIntExtra("pmin", pmin)
        }
    }
}