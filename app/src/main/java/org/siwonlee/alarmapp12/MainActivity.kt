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
import android.widget.*
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*

fun Boolean.toInt() = if (this) 1 else 0

class MainActivity : AppCompatActivity() {
    var before_id = 0
    var data: Alarm_Data = Alarm_Data()
    var categories: ArrayList<String> = ArrayList()

    //현재 시간 등을 계산할 때 사용할 Calendar 클래스
    val cal : Calendar = Calendar.getInstance()

    //텍스트뷰를 터치했을 때 바꿀 색
    var tColor = arrayOf(Color.GRAY, /*Color.parseColor("#008577")*/Color.RED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //알람을 생성하는 것이라면 true, 수정하는 것이라면 false
        val isInit = intent.getBooleanExtra("isInit", true)

        val strData = intent.getStringExtra("data")
        if(strData != null)
            data = GsonBuilder().create().fromJson(strData, Alarm_Data::class.java)

        categories = intent.getStringArrayListExtra("categories")!!

        //알람을 수정하는 것이라면 수정 이전 알람의 정보를 before_id에 담는다
        if(!isInit) {
            var intSwitch = 0

            for(i in 1 .. 7) {
                intSwitch *= 2
                intSwitch += data.switch[i].toInt()
            }

            before_id = (intSwitch * 100 + data.hr) * 100 + data.min

            //정보를 this에서 receiver까지 보내는 intent를 생성
            val intent = Intent(this, Alarm_Receiver::class.java)

            for (day in 1..7) {
                //알람 정보를 dHHMM로 나타내면 알람이 서로 겹치지 않는다
                val requestCode: Int = (day * 100 + data.hr) * 100 + data.min

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

        //설정된 요일에 따라 텍스트 색을 다르게 바꿔준다
        //date.setTextColor(tColor[switch[0].toInt()])
        sun.setTextColor(tColor[data.switch[1].toInt()])
        mon.setTextColor(tColor[data.switch[2].toInt()])
        tue.setTextColor(tColor[data.switch[3].toInt()])
        wed.setTextColor(tColor[data.switch[4].toInt()])
        thu.setTextColor(tColor[data.switch[5].toInt()])
        fri.setTextColor(tColor[data.switch[6].toInt()])
        sat.setTextColor(tColor[data.switch[7].toInt()])

        // 알람 수정 또는 삭제 시 사용됨
        val position = intent.getIntExtra("position", -1)

        //cal의 시간을 알람을 설정한 시간으로 바꾼다
        cal.set(Calendar.HOUR_OF_DAY, data.hr)
        cal.set(Calendar.MINUTE, data.min)

        //알람은 항상 0초에 울린다
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        //timePicker의 초기값을 hr/min으로 지정
        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.hour = data.hr
            timePicker.minute = data.min
        } else {
            timePicker.setCurrentHour(data.hr)
            timePicker.setCurrentMinute(data.min)
        }

        //------------------------------------------------------------------setOnClickListener 시작

        //각 요일 버튼을 클릭했을 때
        //알람이 해당 요일에 울리는 것을 표시하고
        //이를 텍스트의 색으로 나타낸다
        sun.setOnClickListener{
            data.switch[1] = !data.switch[1]
            data.switch[0] = false
            sun.setTextColor(tColor[data.switch[1].toInt()])
        }
        mon.setOnClickListener{
            data.switch[2] = !data.switch[2]
            data.switch[0] = false
            mon.setTextColor(tColor[data.switch[2].toInt()])
        }
        tue.setOnClickListener{
            data.switch[3] = !data.switch[3]
            data.switch[0] = false
            tue.setTextColor(tColor[data.switch[3].toInt()])
        }
        wed.setOnClickListener{
            data.switch[4] = !data.switch[4]
            wed.setTextColor(tColor[data.switch[4].toInt()])
        }
        thu.setOnClickListener{
            data.switch[5] = !data.switch[5]
            data.switch[0] = false
            thu.setTextColor(tColor[data.switch[5].toInt()])
        }
        fri.setOnClickListener{
            data.switch[6] = !data.switch[6]
            data.switch[0] = false
            fri.setTextColor(tColor[data.switch[6].toInt()])
        }
        sat.setOnClickListener{
            data.switch[7] = !data.switch[7]
            data.switch[0] = false
            sat.setTextColor(tColor[data.switch[7].toInt()])
        }

        //알람 추가 설정에 대한 listener를 선언
        val advanceListener = View.OnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("알람 추가 설정")

            val dialogView = layoutInflater.inflate(R.layout.alarm_setting_advanced, null)
            val dialogSolving = dialogView.findViewById<Spinner>(R.id.solving)
            val dialogHr = dialogView.findViewById<EditText>(R.id.preHr)
            val dialogMin = dialogView.findViewById<EditText>(R.id.preMin)
            val categorize = dialogView.findViewById<Spinner>(R.id.categorize)
            val newCat = dialogView.findViewById<EditText>(R.id.newCat)
            val visible = dialogView.findViewById<LinearLayout>(R.id.alarmNewCategorySetter)

            if (data.phr != 0)
                dialogHr.setText(data.phr.toString())
            if (data.pmin != 0)
                dialogMin.setText(data.pmin.toString())

            dialogSolving.adapter = ArrayAdapter(
                applicationContext,
                android.R.layout.simple_spinner_dropdown_item,
                resources.getStringArray(R.array.spinnerItem)
            )

            dialogSolving.setSelection(data.solver)

            dialogSolving.setOnItemSelectedListener(object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected( adapterView: AdapterView<*>, view: View, i: Int, l: Long ) {
                    data.solver = i
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {}
            })

            if(!(data.category in categories))
                categories.add(data.category)

            categories.add("새 카테고리")

            categorize.adapter = ArrayAdapter(
                applicationContext,
                android.R.layout.simple_spinner_dropdown_item,
                categories
            )

            for(i in 0 until categories.size) {
                if(data.category == categories[i]) {
                    categorize.setSelection(i)
                    break
                }
            }

            categorize.setOnItemSelectedListener(object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected( adapterView: AdapterView<*>, view: View, i: Int, l: Long ) {
                    if(i == categories.size - 1) {
                        visible.visibility = View.VISIBLE
                        newCat.setText("")
                    }
                    else {
                        visible.visibility = View.GONE
                        newCat.setText(categories[i])
                    }
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {
                    categorize.setSelection(0)
                    if(categories.size == 1) visible.visibility = View.VISIBLE
                    else visible.visibility = View.GONE
                }
            })

            builder.setView(dialogView)
            builder.setPositiveButton("확인") { _, _ ->
                if (isEmpty(dialogHr.text)) data.phr = 0
                else data.phr = Integer.parseInt(dialogHr.text.toString())

                if (isEmpty(dialogMin.text)) data.pmin = 0
                else data.pmin = Integer.parseInt(dialogMin.text.toString())

                data.category = newCat.text.toString()
            }
            builder.setNegativeButton("취소") { _, _ -> /* 취소일 때 아무 액션이 없으므로 빈칸 */ }
            builder.create().show()
        }
        //advance 버튼을 눌렀을 때의 listener를 advanceListener로 정의
        advance.setOnClickListener(advanceListener)

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

            //timePicker의 값이 바뀔 때마다 hr와 min을 가져오지 않고, 알람 설정 버튼을 눌렀을 때 시간을 가져온다
            if (Build.VERSION.SDK_INT >= 23) {
                data.hr = timePicker.hour
                data.min = timePicker.minute
            } else {
                data.hr = timePicker.currentHour
                data.min = timePicker.currentMinute
            }

            //알람을 설정한 시간:분을 cal에 저장한다
            cal.set(Calendar.HOUR_OF_DAY, data.hr)
            cal.set(Calendar.MINUTE, data.min)

            //AlarmList_Activity에 정보를 넘길 intent
            val returnIntent = Intent()

            // 알람을 수정하는 경우 현재 알람의 위치를 returnIntent에 담는다
            if (position != -1) returnIntent.putExtra("position", position)

            //설정한 알람 정보를 AlarmList_Acitivity로 넘긴다
            val strData = GsonBuilder().create().toJson(data, Alarm_Data::class.java)

            returnIntent.putExtra("data", strData)
            returnIntent.putExtra("before_id", before_id)

            //AlarmList_Acitivity에 RESULT_OK 신호와 함께 intent를 넘긴다
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }
}