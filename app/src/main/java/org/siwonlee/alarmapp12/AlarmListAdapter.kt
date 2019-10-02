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
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.alarm_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: AlarmListAdapter.ViewHolder, position: Int) {
        //holder.time.text = alarmlist.get(position).toString()
        //holder.date.text = alarmlist.get(position).toString()
        holder.bindItems(alarmlist[position])
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //val time = view.time_in_list
        //val date = view.date_in_list

        fun bindItems(data : Alarm_Data) {
            itemView.time_in_list.text = data.ringTime
            itemView.date_in_list.text = data.ringDate

            itemView.setOnClickListener {
                Toast.makeText(itemView.context, "Alarm set in ${data.ringTime} is clicked.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

