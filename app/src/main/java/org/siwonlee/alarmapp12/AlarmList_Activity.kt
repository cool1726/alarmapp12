package org.siwonlee.alarmapp12

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_list.*
import org.json.JSONArray
import android.R.attr.key
import android.view.View
import org.json.JSONException


class AlarmList_Activity : AppCompatActivity() {
    val REQUEST_SET : Int = 1000
    val REQUEST_CLICK : Int = 2000
    val alarmlist = ArrayList<Alarm_Data>()

    private val prefStorage = "org.siwonlee.alarmapp12.prefs"
    val pref by lazy {this.getSharedPreferences(prefStorage, MODE_PRIVATE)}

    //설정한 알람의 개수
    var index = 0

    // AlarmlistAdapter의 ViewHolder
    val adapter = AlarmListAdapter(this, alarmlist,
        { position ->
            val cintent = Intent(this, MainActivity::class.java)
            cintent.putExtra("hr", alarmlist[position].hr)
            cintent.putExtra("min", alarmlist[position].min)
            cintent.putExtra("position", position)
            cintent.putExtra("stringswitch", alarmlist[position].ringDate)
            cintent.putExtra("datearray", alarmlist[position].arrayDate)
            cintent.putExtra("index", alarmlist[position].index)

            //알람을 수정한다
            startActivityForResult(cintent, REQUEST_CLICK)},
        { position -> onLongClickEvent() })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_list)

        index = pref.getInt("index", 0)

        // LinearLayoutManager : alarm_list.xml의 alarm_recyclerview에 세로형태로 아이템을 배치한다
        alarm_recyclerview.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        alarm_recyclerview.setHasFixedSize(true)
        alarm_recyclerview.adapter = adapter


        // 알람 초기 셋팅 : 알람 셋팅 화면으로 이동 (activity_main.xml로 이동)
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
            startActivityForResult(intent, REQUEST_SET)
        }
    }

    // item을 오래 클릭할 시 바로 알람 삭제여부를 묻는 dialog 띄우기 (나중에)
    fun onLongClickEvent() { }

    // 특정 REQUEST_CODE로 MainActivity.kt에 결과값을 요구하고 결과값 받기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) { // MainActivity에서 RESULT_OK 사인을 보내면
            when (requestCode) {
                REQUEST_SET -> {   // MainActivity에서 intents로 추가한 time, date 데이터 받아오기 (초기 설정)
                    val hr = data!!.getIntExtra("hr", -1)
                    val min = data!!.getIntExtra("min", -1)
                    val time = data!!.getStringExtra("time")
                    val date = data!!.getStringExtra("switch")
                    val datearray = data!!.getBooleanArrayExtra("datearray")
                    val index = data!!.getIntExtra("index", 0)

                    // 이 액티비티 내의 alarmlist(Alarm_Data형식의 arraylist)에 받아온 시간, 요일 정보 추가
                    alarmlist.add(Alarm_Data(hr, min, time, date, datearray, index))
                }
                REQUEST_CLICK -> {   // recyclerview에서 클릭한 item -> 삭제 또는 수정
                    if (data!!.getBooleanExtra("delete", false)) { //알람 삭제
                        // MainActivity에서 position 값 받아오기
                        val position = data!!.getIntExtra("position", -1)

                        // alarmlist에서 삭제할 알람의 position(인덱스값)으로 항목 삭제
                        alarmlist.removeAt(position)
                    }
                    else { // 알람 수정
                        // 수정될 알람의 alarmlist 인덱스값을 position으로 받아온다
                        val position = data!!.getIntExtra("position", -1)

                        val hr = data!!.getIntExtra("hr", -1)
                        val min = data!!.getIntExtra("min", -1)
                        val time = data!!.getStringExtra("time")
                        val date = data!!.getStringExtra("switch")
                        val datearray = data!!.getBooleanArrayExtra("datearray")
                        val index = data!!.getIntExtra("index", 0)

                        alarmlist[position] = Alarm_Data(hr, min, time, date, datearray, index)
                    }
                }
            }
        }
        // 바뀐 alarmlist 때문에 adapter 갱신
        alarm_recyclerview.adapter = adapter
    }
}
