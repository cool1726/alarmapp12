package org.siwonlee.alarmapp12

import android.content.Context
import android.util.Log
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

val ALARM_ACTIVATE: Int = 1
val ALARM_DEACTIVATE: Int = -1

class UserData(
    var uid: String = "",
    val list: ArrayList<Alarm_Data> = ArrayList(),
    val uidMap: HashMap<String, String>? = HashMap(),
    var markerSet: Marker_Set = Marker_Set()
) {
    fun getCategoryList(currentCategory: String): ArrayList<Alarm_Data> {  // 특정 category에 속한 알람 list
        if(currentCategory == "전체 카테고리")
            return this.list
        else {
            var categorylist = ArrayList<Alarm_Data>()
            for(i in 0 until list.size)
                if(list[i].category == currentCategory)
                    categorylist.add(list[i])
            return categorylist
        }
    }

    fun category(): ArrayList<String> {
        val ret = ArrayList<String>()
        for(data in list)
            if(!(data.category in ret))
                ret.add(data.category)

        ret.sort()

        return ret
    }

    fun get(i: Int): Alarm_Data {
        return list[i]
    }

    fun set(i: Int, data: Alarm_Data) {
        list[i] = data
    }

    fun add(data: Alarm_Data) {
        list.add(data)
    }

    fun pop(data: Alarm_Data) {
        for(i in 0 until list.size)
            if(list[i].isEqual(data)) {
                list.removeAt(i)
                break
            }
    }

    fun size(): Int {
        return list.size
    }

    fun unset(context: Context) {
        for(data in list)
            data.setAlarm(context,ALARM_DEACTIVATE)
    }

    fun set(context: Context) {
        for(data in list)
            data.setAlarm(context, ALARM_ACTIVATE)
    }

    fun sort() {
        Collections.sort(list, kotlin.Comparator {a: Alarm_Data, b: Alarm_Data ->
            //a와 b의 시간을 알아볼 수 있게 하기 위한 Calendar 변수
            val aa = Calendar.getInstance()
            val bb = Calendar.getInstance()

            //aa와 bb에 a와 b를 각각 대입한다
            aa.timeInMillis = a.timeInMillis
            bb.timeInMillis = b.timeInMillis

            //마지막에 반환할 값은 aaa와 bbb를 비교한 값이다
            var aaa = 0
            var bbb = 0

            //a와 b가 설정된 시각의 hour가 다르다면 둘의 hour를 비교한다
            if(aa.get(Calendar.HOUR_OF_DAY) != bb.get(Calendar.HOUR_OF_DAY)) {
                aaa = aa.get(Calendar.HOUR_OF_DAY)
                bbb = bb.get(Calendar.HOUR_OF_DAY)
            }

            //a와 b가 설정된 시각의 hour가 같고 minute가 다르다면 minute를 비교한다
            else if(aa.get(Calendar.MINUTE) != bb.get(Calendar.MINUTE)) {
                aaa = aa.get(Calendar.MINUTE)
                bbb = bb.get(Calendar.MINUTE)
            }

            //a와 b가 설정된 시간이 같다면 알람이 울리는 요일에 대해 비교해
            //더 빠른 요일에 울리는 알람을 먼저 배치한다
            else for(i in 0..7) {
                if(a.switch[i] != b.switch[i]) {
                    aaa = a.switch[i].toInt()
                    bbb = b.switch[i].toInt()
                    break
                }
            }

            aaa.compareTo(bbb)
        })
    }
}