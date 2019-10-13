package org.siwonlee.alarmapp12

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_list_item.view.*
import android.content.DialogInterface
import android.util.Log
import android.util.Log.d
import android.view.*
import android.widget.TextView
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import android.view.View.OnLongClickListener


class AlarmListAdapter(val context: Context, val alarmlist: ArrayList<Alarm_Data>,
                       val alarmItemClick: (Int) -> Unit, val alarmItemLongClick: (Int) -> Unit)
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


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder (itemView) { // View 재사용 위한 ViewHolder

        fun bindItems(data : Alarm_Data) {
            // 전달받은 Alarm_Data 형식의 data에서 시간과 요일(날짜)을 불러온다
            // itemView의 list 구성 요소에 text로 전달한다
            itemView.time_in_list.text = data.ringTime
            itemView.date_in_list.text = data.ringDate

            // ViewHolder내에서 setOnClickListener 아이템 클릭 event
            // adapterPosition으로 클릭한 아이템의 인덱스값 전달
            itemView.setOnClickListener {
                alarmItemClick(adapterPosition)
            }
            itemView.setOnLongClickListener {
                alarmItemLongClick(adapterPosition)
                true
            }
        }
    }
}

