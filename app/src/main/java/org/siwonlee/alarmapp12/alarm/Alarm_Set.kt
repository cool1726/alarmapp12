package org.siwonlee.alarmapp12.alarm

import android.app.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import java.util.*
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.text.TextUtils.isEmpty
import android.util.Log
import android.view.View
import android.widget.*
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import org.siwonlee.alarmapp12.solving.AlarmSolving4
import org.siwonlee.alarmapp12.Alarm_Data
import org.siwonlee.alarmapp12.R

fun Boolean.toInt() = if (this) 1 else 0

class Alarm_Set : AppCompatActivity() {
    var data: Alarm_Data = Alarm_Data()
    var categories: ArrayList<String> = ArrayList()

    //현재 시간 등을 계산할 때 사용할 Calendar 클래스
    val cal : Calendar = Calendar.getInstance()

    //텍스트뷰를 터치했을 때 바꿀 색
    var tColor = arrayOf(Color.GRAY, /*Color.parseColor("#008577")*/Color.RED)

    val SOL_QR = 44
    val REQ_RINGTONE = 55
    // 앱에서 제공하는 알람벨을 soundArray로 선언했다
    val soundArray = arrayOf("sunny", "cloudy", "rainy", "snowy") //순서대로 believer, rockabye, vivalavida, christmasday
    var curSound = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //알람을 생성하는 것이라면 true, 수정하는 것이라면 false
        val isInit = intent.getBooleanExtra("isInit", true)

        //기존에 설정한 알람이 존재한다면 그 알람을 베이스로 설정한다
        val strData = intent.getStringExtra("data")
        if(strData != null)
            data = GsonBuilder().create().fromJson(strData, Alarm_Data::class.java)

        //알람을 data와 연동시킨 뒤
        cal.timeInMillis = data.timeInMillis
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        //알람이 처음 시작된 경우 시간을 6시 0분으로 맞춘다
        if(isInit) {
            cal.set(Calendar.HOUR_OF_DAY, 6)
            cal.set(Calendar.MINUTE, 0)
        }

        categories = intent.getStringArrayListExtra("categories")!!

        //설정된 요일에 따라 텍스트 색을 다르게 바꿔준다
        date.setTextColor(tColor[data.switch[0].toInt()])
        sun.setTextColor(tColor[data.switch[1].toInt()])
        mon.setTextColor(tColor[data.switch[2].toInt()])
        tue.setTextColor(tColor[data.switch[3].toInt()])
        wed.setTextColor(tColor[data.switch[4].toInt()])
        thu.setTextColor(tColor[data.switch[5].toInt()])
        fri.setTextColor(tColor[data.switch[6].toInt()])
        sat.setTextColor(tColor[data.switch[7].toInt()])

        //timePicker의 초기값을 hr/min으로 지정
        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.hour = cal.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = cal.get(Calendar.MINUTE)
        } else {
            timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY))
            timePicker.setCurrentMinute(cal.get(Calendar.MINUTE))
        }

        //------------------------------------------------------------------setOnClickListener 시작

        //달력 버튼을 누른 경우 어느 한 날의 지정된 시간에 알람을 울리게끔 설정한다
        date.setOnClickListener {
            //listener를 먼저 정의해준 뒤
            val listener =
                DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
            //DatePicker를 Dialog로 띄운다
            val datePicker = DatePickerDialog(this, listener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            datePicker.show()

            //알람이 울릴 날짜를 선택했으므로 알람 요일 선택을 달력으로 바꿔준 뒤
            //일요일부터 월요일까지 설정된 알람을 전부 해제한다
            data.switch[0] = true
            for(i in 1..7) data.switch[i] = false

            date.setTextColor(tColor[data.switch[0].toInt()])
            sun.setTextColor(tColor[data.switch[1].toInt()])
            mon.setTextColor(tColor[data.switch[2].toInt()])
            tue.setTextColor(tColor[data.switch[3].toInt()])
            wed.setTextColor(tColor[data.switch[4].toInt()])
            thu.setTextColor(tColor[data.switch[5].toInt()])
            fri.setTextColor(tColor[data.switch[6].toInt()])
            sat.setTextColor(tColor[data.switch[7].toInt()])
        }

        //각 요일 버튼을 클릭했을 때
        //알람이 해당 요일에 울리는 것을 표시하고
        //이를 텍스트의 색으로 나타낸다
        sun.setOnClickListener{
            data.switch[0] = false
            data.switch[1] = !data.switch[1]
            date.setTextColor(tColor[data.switch[0].toInt()])
            sun.setTextColor(tColor[data.switch[1].toInt()])
        }
        mon.setOnClickListener{
            data.switch[0] = false
            data.switch[2] = !data.switch[2]
            date.setTextColor(tColor[data.switch[0].toInt()])
            mon.setTextColor(tColor[data.switch[2].toInt()])
        }
        tue.setOnClickListener{
            data.switch[0] = false
            data.switch[3] = !data.switch[3]
            date.setTextColor(tColor[data.switch[0].toInt()])
            tue.setTextColor(tColor[data.switch[3].toInt()])
        }
        wed.setOnClickListener{
            data.switch[0] = false
            data.switch[4] = !data.switch[4]
            date.setTextColor(tColor[data.switch[0].toInt()])
            wed.setTextColor(tColor[data.switch[4].toInt()])
        }
        thu.setOnClickListener{
            data.switch[0] = false
            data.switch[5] = !data.switch[5]
            date.setTextColor(tColor[data.switch[0].toInt()])
            thu.setTextColor(tColor[data.switch[5].toInt()])
        }
        fri.setOnClickListener{
            data.switch[0] = false
            data.switch[6] = !data.switch[6]
            date.setTextColor(tColor[data.switch[0].toInt()])
            fri.setTextColor(tColor[data.switch[6].toInt()])
        }
        sat.setOnClickListener{
            data.switch[0] = false
            data.switch[7] = !data.switch[7]
            date.setTextColor(tColor[data.switch[0].toInt()])
            sat.setTextColor(tColor[data.switch[7].toInt()])
        }

        //알람 추가 설정에 대한 listener를 선언
        val advanceListener = View.OnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("알람 추가 설정")

            //alarm_setting_advanced 파일에 정의된 레이아웃과 그 레이아웃에 속한 뷰
            val dialogView = layoutInflater.inflate(R.layout.alarm_setting_advanced, null)
            val dialogName = dialogView.findViewById<EditText>(R.id.alarmName)
            val dialogSolving = dialogView.findViewById<Spinner>(R.id.solving)
            val dialogHr = dialogView.findViewById<EditText>(R.id.preHr)
            val dialogMin = dialogView.findViewById<EditText>(R.id.preMin)
            val categorize = dialogView.findViewById<Spinner>(R.id.categorize)
            val newCat = dialogView.findViewById<EditText>(R.id.newCat)
            val visible = dialogView.findViewById<LinearLayout>(R.id.alarmNewCategorySetter)

            if (data.name != "")
                dialogName.setText(data.name)

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
                if(isEmpty(dialogName.text)) data.name = ""
                else data.name = dialogName.text.toString()

                if (isEmpty(dialogHr.text)) data.phr = 0
                else data.phr = Integer.parseInt(dialogHr.text.toString())

                if (isEmpty(dialogMin.text)) data.pmin = 0
                else data.pmin = Integer.parseInt(dialogMin.text.toString())

                data.category = newCat.text.toString()

                if (data.solver == 3) {
                    val intent = Intent(applicationContext, AlarmSolving4::class.java)
                    intent.putExtra("solving", data.solver)

                    startActivityForResult(intent, SOL_QR)
                }
            }
            builder.setNegativeButton("취소") { _, _ -> /* 취소일 때 아무 액션이 없으므로 빈칸 */ }
            builder.create().show()
        }
        //advance 버튼을 눌렀을 때의 listener를 advanceListener로 정의
        advance.setOnClickListener(advanceListener)


        //(1) R.raw 파일의 앱 자체에 저장한 알람소리를 spinner로 갖고옴
        /*
        var currentSound: String = ""
        val sound = findViewById<Spinner>(R.id.soundName)
        // sound adapter 설정
        sound.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            soundArray)

        sound.setSelection(0)       // 기본값 "전체 카테고리"
        for(i in 0 until soundArray.size) {
            if(currentSound == soundArray[i]) {
                sound.setSelection(i)
                break
            }
        }

        // 알람벨 선택
        sound.setOnItemSelectedListener(object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long ) {
                //currentSound = soundArray[i]

            }
            override fun onNothingSelected(adapterView: AdapterView<*>)  {
                sound.setSelection(0)
            }
        })
        */

        //(2) 시스템에 내장된 알람소리를 가져옴
        selSound.setOnClickListener {
            var intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "알람 벨소리를 선택하세요")
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)  //무음을 리스트에서 제외
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, true)   //기본 벨소리는 리스트에 넣는다
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            startActivityForResult(intent, REQ_RINGTONE)
        }

        // 알람 삭제 버튼 (bt_set.setOnClickListener와 유사)
        bt_delete.setOnClickListener {
            //pendingIntent로 alarmManager의 알람은 여기서 삭제하고
            //알람 recyclerview의 해당 알람 item은 returnIntent로 정보를 넘겨서 삭제하게 한다

            //삭제할 알람 정보를 AlarmList_Acitivity로 넘긴다
            val returnIntent = Intent(this, AlarmList_Activity::class.java)
            returnIntent.putExtra("before", strData)

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
            //만일 알람 설정을 날짜로 하지 않았다면
            if(!data.switch[0]) {
                //날짜가 바뀌는 등 오류가 발생할 수 있으므로 현재 날짜를 다시 구한다
                cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
                cal.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH))
                cal.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE))
            }

            //timePicker의 값이 바뀔 때마다 hr와 min을 가져오지 않고, 알람 설정 버튼을 눌렀을 때 시간을 가져온다
            if (Build.VERSION.SDK_INT >= 23) {
                cal.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                cal.set(Calendar.MINUTE, timePicker.minute)
            } else {
                cal.set(Calendar.HOUR_OF_DAY, timePicker.currentHour)
                cal.set(Calendar.MINUTE, timePicker.currentMinute)
            }

            //알람을 설정한 시간을 data에 저장한다
            data.timeInMillis = cal.timeInMillis
            //AlarmList_Activity에 정보를 넘길 intent
            val returnIntent = Intent()

            //설정한 알람 정보를 AlarmList_Acitivity로 넘긴다
            val newData = GsonBuilder().create().toJson(data, Alarm_Data::class.java)

            returnIntent.putExtra("data", newData)
            returnIntent.putExtra("before", strData)

            //AlarmList_Acitivity에 RESULT_OK 신호와 함께 intent를 넘긴다
            setResult(Activity.RESULT_OK, returnIntent)

            Log.d("TAG", "year: ${cal.get(Calendar.YEAR)}")
            Log.d("TAG", "month: ${cal.get(Calendar.MONTH)}")
            Log.d("TAG", "date: ${cal.get(Calendar.DAY_OF_MONTH)}")
            Log.d("TAG", "hr: ${cal.get(Calendar.HOUR)}")
            Log.d("TAG", "minute: ${cal.get(Calendar.MINUTE)}")

            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                REQ_RINGTONE -> {
                    var pickedUri = intent!!.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI) //노래 경로
                    //data.sound = RingtoneManager.getRingtone(this, pickedUri).getTitle(this)    //노래 제목
                    data.sound = pickedUri.toString()
                    Toast.makeText(this, "${data.sound} selected", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}