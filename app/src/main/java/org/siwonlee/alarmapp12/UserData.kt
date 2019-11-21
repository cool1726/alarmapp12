package org.siwonlee.alarmapp12

import kotlin.collections.ArrayList

class UserData (private val list: ArrayList<Alarm_Data>) {
    private val categories = java.util.ArrayList<String>()

    fun getList(): ArrayList<Alarm_Data> {
        return this.list
    }

    fun getCategoryList(currentCategory: String): ArrayList<Alarm_Data> {  // 특정 category에 속한 알람 list
        var categorylist = ArrayList<Alarm_Data>()
        if(currentCategory == "전체 카테고리")
            return this.list
        else {
            for(i in 0 until list.size) {
                if(list[i].category == currentCategory)
                    categorylist.add(list[i])
            }
            return categorylist
        }
    }

    fun getCategories(): java.util.ArrayList<String> {
        return this.categories
    }

    fun getCategorySize(): Int {
        return this.categories.size
    }

    fun get(id: Int): Alarm_Data { // id값 -> 알람데이터 찾기
        var index  = -1
        var data_id = -1
        for(i in list) {
            data_id = (i.intSwitch * 100 + i.hr) * 100 + i.min
            if (id == data_id) index = list.indexOf(i)     // 삭제할 id에 해당하는 알람데이터 찾기
        }
        return list[index]
    }

    fun getp(p: Int): Alarm_Data { // position값 -> 알람데이터 찾기
        return list[p]
    }

    fun set(id: Int, data: Alarm_Data) { // id값 -> 알람데이터 수정
        var index  = -1
        var data_id = -1
        for(i in list) {
            data_id = (i.intSwitch * 100 + i.hr) * 100 + i.min
            if (id == data_id) index = list.indexOf(i)     // 삭제할 id에 해당하는 알람데이터 찾기
        }
        list[index] = data
    }

    fun subPosition(position: Int) { // 알람 삭제로 position값 -1만큼 변경
        for(i in list)
            if(position == i.position) i.position -= 1
    }

    fun getDataClicked(category: String, position: Int): Alarm_Data? { //category별 view에서 클릭된 알람데이터 가져오기
        var pos = -1
        var data : Alarm_Data? = null
        if(category == "전체 카테고리")
            return list[position]
        else {
            for(i in list) {
                if(category == i.category) pos++
                if(pos == position) data = i
            }
            return data
        }
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
        if(category == "전체 카테고리") sw = false

        if(sw) categories.remove(category)
    }

    fun pop(p: Int) { // id : 삭제할 알람데이터의 고유 id값
        /*var index = -1
        for(i in list)
            if(id == i.id) index = list.indexOf(i)    // 삭제할 id에 해당하는 알람데이터 찾기
*/
        val category = this.get(p).category
        var sw = true
        list.removeAt(p)

        for(i in list)
            if(category == i.category) sw = false

        if(category == "기본") sw = false
        if(category == "전체 카테고리") sw = false

        if(sw) categories.remove(category)
    }

    fun size(): Int {
        return list.size
    }
}