package org.siwonlee.alarmapp12

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_list_item.view.*


class AlarmListAdapter(val alarmlist: ArrayList<Alarm_Data>)
    : RecyclerView.Adapter<AlarmListAdapter.ViewHolder>(){

    override fun getItemCount(): Int = alarmlist.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmListAdapter.ViewHolder {
        // LayoutInflater.from으로 view 생성
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.alarm_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: AlarmListAdapter.ViewHolder, position: Int) {
        // OnCreateViewHolder로 생성된 holder의 view에 데이터가 (반복적으로) 나타나도록 설정한다
        holder.bindItems(alarmlist[position])
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) { // View 재사용 위한 ViewHolder

        fun bindItems(data : Alarm_Data) {
            // 전달받은 Alarm_Data 형식의 data에서 시간과 요일(날짜)을 불러온다
            // itemView의 list 구성 요소에 text로 전달한다
            itemView.time_in_list.text = data.ringTime
            itemView.date_in_list.text = data.ringDate

            // 아이템이 클릭되었을 때 토스트 띄우기
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, "Alarm set in ${data.ringTime} is clicked.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

