package org.siwonlee.alarmapp12

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_list.*

class AlarmList_Activity : AppCompatActivity() {
    val REQUEST_CODE : Int = 3000
    val alarmlist = ArrayList<Alarm_Data>()

    private val prefStorage = "org.siwonlee.alarmapp12.prefs"
    val pref by lazy {this.getSharedPreferences(prefStorage, MODE_PRIVATE)}

    //설정한 알람의 개수
    var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_list)

        // LinearLayoutManager : alarm_list.xml의 alarm_recyclerview에 세로형태로 아이템을 배치한다
        alarm_recyclerview.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        alarm_recyclerview.setHasFixedSize(true)
        // AlarmlistAdapter의 ViewHolder
        alarm_recyclerview.adapter = AlarmListAdapter(alarmlist)

        index = pref.getInt("index", 0)

        // 알람 셋팅 화면으로 이동 (activity_main.xml로 이동)
        fab_add.setOnClickListener{
            index += 1
            val intent = Intent(this, MainActivity::class.java)
            //알람의 정보를 intent에 담아서 전송한다
            //지금은 default가 6시 정각 및 요일 미설정으로 고정되어 있으나
            // 알람 수정을 지원할 경우 이 또한 바꾸어야 한다
            intent.putExtra("hr", 6)
            intent.putExtra("min", 0)
            intent.putExtra("index", index)
            intent.putExtra("switch",  "TFFFFFFF")

            //알람의 수를 하나 늘렸으므로 이를 pref에 기록한다
            pref.edit().putInt("index", index).apply()

            //알람을 설정한다
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    // 특정 REQUEST_CODE로 MainActivity.kt에 결과값을 요구하고 결과값 받기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) { // MainActivity에서 RESULT_OK 사인을 보내면
            when (requestCode) {
                REQUEST_CODE -> {   // MainActivity에서 intents로 추가한 time, date 데이터 받아오기
                    val time = data!!.getStringExtra("time")
                    val date = data!!.getStringExtra("date")

                    // 이 액티비티 내의 alarmlist(Alarm_Data형식의 arraylist)에 받아온 시간, 요일 정보 추가
                    alarmlist.add(Alarm_Data(time, date))
                }
            }
        }
        alarm_recyclerview.adapter = AlarmListAdapter(alarmlist)
    }
}
