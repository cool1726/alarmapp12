package org.siwonlee.alarmapp12

import kotlin.collections.ArrayList

class UserData (private val list: ArrayList<Alarm_Data>) {
    private val categories = java.util.ArrayList<String>()

    fun getList(): ArrayList<Alarm_Data> {
        return this.list
    }

    fun getCategories(): java.util.ArrayList<String> {
        return this.categories
    }

    fun getCategorySize(): Int {
        return this.categories.size
    }

    fun get(i: Int): Alarm_Data {
        return list[i]
    }

    fun set(i: Int, data: Alarm_Data) {
        list[i] = data
    }

    fun add(data: Alarm_Data) {
        var sw = true
        list.add(data)
        for(i in 0 until categories.size) {
            if(data.category == categories[i]) sw = false
        }

        if(sw) categories.add(data.category)
    }

    fun addCategory(category: String) {
        var sw = true
        for(i in 0 until categories.size)
            if(category == categories[i]) sw = false
        if(sw) categories.add(category)
    }

    fun removeCategory(i: Int) {
        var sw = true
        val category = categories[i]

        for(i in list)
            if(category == i.category) sw = false

        if(category == "기본") sw = false

        if(sw) categories.remove(category)
    }

    fun pop(i: Int) {
        val category = this.get(i).category
        var sw = true
        list.removeAt(i)

        for(i in list)
            if(category == i.category) sw = false

        if(sw) categories.remove(category)
    }

    fun size(): Int {
        return list.size
    }
}