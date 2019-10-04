package org.siwonlee.alarmapp12

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alarm_list.*
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.View
import androidx.core.app.ActivityCompat.startActivityForResult



class AlarmList_Activity : AppCompatActivity() {

    val REQUEST_CODE : Int = 3000
    val alarmlist = ArrayList<Alarm_Data>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_list)

        // alarm_list에 추가될 샘플 알람
        alarmlist.add(Alarm_Data("06:50", "this is sample"))

        // LinearLayoutManager : alarm_list.xml의 alarm_recyclerview에 세로형태로 아이템을 배치한다
        alarm_recyclerview.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        alarm_recyclerview.setHasFixedSize(true)
        // AlarmlistAdapter의 ViewHolder
        alarm_recyclerview.adapter = AlarmListAdapter(alarmlist)


        // 알람 셋팅 화면으로 이동 (activity_main.xml로 이동)
        fab_add.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
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
