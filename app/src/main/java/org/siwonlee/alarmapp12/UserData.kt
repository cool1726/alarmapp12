package org.siwonlee.alarmapp12

import kotlin.collections.ArrayList

class UserData(
    var uid: String = "",
    val list: ArrayList<Alarm_Data> = ArrayList(),
    val uidMap: HashMap<String, String>? = HashMap()
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

    fun pop(i: Int) {
        list.removeAt(i)
    }

    fun size(): Int {
        return list.size
    }
}