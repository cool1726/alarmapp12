package org.siwonlee.alarmapp12.alarm

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_list_item.view.*
import android.view.*
import android.widget.Switch
import android.widget.Toast
import org.siwonlee.alarmapp12.Alarm_Data
import org.siwonlee.alarmapp12.R
import org.siwonlee.alarmapp12.solving.toTime
import java.util.*
import kotlin.collections.ArrayList

class AlarmListAdapter(val context: Context, val alarmlist: ArrayList<Alarm_Data>,
                       val alarmItemClick: (Int) -> Unit, val alarmSwitchClick: (Int) -> Unit)
    : RecyclerView.Adapter<AlarmListAdapter.ViewHolder>(){

    override fun getItemCount(): Int = alarmlist.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // LayoutInflater.from으로 view 생성
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.alarm_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // OnCreateViewHolder로 생성된 holder의 view에 데이터가 (반복적으로) 나타나도록 설정한다
        holder.bindItems(alarmlist[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder (itemView) { // View 재사용 위한 ViewHolder

        fun bindItems(data: Alarm_Data) {
            var ringDate = ""
            val days = arrayOf("일 ", "월 ", "화 ", "수 ", "목 ", "금 ", "토 ")
            for (i in 1..7) if (data.switch[i]) ringDate = "${ringDate}${days[i - 1]}"

            val cal = Calendar.getInstance()
            cal.timeInMillis = data.timeInMillis

            if(ringDate == "") {
                val yy = cal.get(Calendar.YEAR)
                val MM = cal.get(Calendar.MONTH)
                val dd = cal.get(Calendar.DAY_OF_MONTH)

                ringDate = "${yy}년 ${MM + 1}월 ${dd}일"
            }

            val hr = cal.get(Calendar.HOUR_OF_DAY)
            val min = cal.get(Calendar.MINUTE)

            var switchBtn = itemView.findViewById<Switch>(R.id.switch1)
            //Log.d("switchButton toggle?", "${data}, ${data.onoff}")
            if(data.onoff) {
                switchBtn.isChecked = true
            }


            // 전달받은 Alarm_Data 형식의 data에서 시간과 요일(날짜)을 불러온다
            // itemView의 list 구성 요소에 text로 전달한다
            itemView.time_in_list.text = "${hr.toTime()}:${min.toTime()}"
            itemView.date_in_list.text = ringDate

            itemView.alarm_name.text = data.name

            // ViewHolder내에서 setOnClickListener 아이템 클릭 event
            // adapterPosition으로 클릭한 아이템의 인덱스값 전달
            itemView.setOnClickListener {
                alarmItemClick(adapterPosition)
            }

            switchBtn.setOnCheckedChangeListener { switchclicked, onoff ->
                alarmSwitchClick(adapterPosition) }
        }

    }
}