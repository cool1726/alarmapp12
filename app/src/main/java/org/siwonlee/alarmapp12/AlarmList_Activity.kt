package org.siwonlee.alarmapp12

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_list.*

class AlarmList_Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_list)

        val alarmlist = arrayListOf<Alarm_Data>(
            Alarm_Data("07:00", "monday"),
            Alarm_Data("08:10", "wednesday"),
            Alarm_Data("09:30", "saturday")
        )

        alarm_recyclerview.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        alarm_recyclerview.setHasFixedSize(true)

        alarm_recyclerview.adapter = AlarmListAdapter(alarmlist)
    }
}
