package org.siwonlee.alarmapp12

class UserData (private val list: ArrayList<Alarm_Data>) {
    fun getList(): ArrayList<Alarm_Data> {
        return this.list
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