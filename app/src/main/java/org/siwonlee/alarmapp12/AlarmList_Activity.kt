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

        // var alarms = intent.getStringArrayExtra("addalarmlist")

        alarmlist.add(Alarm_Data("06:50", "this is sample"))


        alarm_recyclerview.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        alarm_recyclerview.setHasFixedSize(true)

        alarm_recyclerview.adapter = AlarmListAdapter(alarmlist)


        // 알람 셋팅 화면으로 이동 (activity_main.xml로 이동)
        fab_add.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE -> {
                    val time = data!!.getStringExtra("time")
                    val date = data!!.getStringExtra("date")

                    alarmlist.add(Alarm_Data(time, date))
                }
            }
        }
        alarm_recyclerview.adapter = AlarmListAdapter(alarmlist)
    }

}
