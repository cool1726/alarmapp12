package org.siwonlee.alarmapp12

data class Alarm_Data(
    var hr: Int = 6,
    var min: Int = 0,
    var phr: Int = 0,
    var pmin: Int = 0,
    var switch: MutableList<Boolean> = mutableListOf(true, false, false, false, false, false, false, false),
    var solver: Int = 0,
    var category: String = "기본",
    var sound: String = ""
)

fun Alarm_Data(map: HashMap<String, Any>): Alarm_Data {
    return Alarm_Data(
        hr = (map["hr"] as Long).toInt(),
        min = (map["min"] as Long).toInt(),
        phr = (map["phr"] as Long).toInt(),
        pmin = (map["pmin"] as Long).toInt(),
        switch = map["switch"] as MutableList<Boolean>,
        solver = (map["solver"] as Long).toInt(),
        category = map["category"] as String,
        sound = (map["sound"] as String)
    )
}

fun Alarm_Data.isEqual(other: Alarm_Data): Boolean {
    return (this.hr == other.hr &&
            this.min == other.min &&
            this.phr == other.phr &&
            this.pmin == other.pmin &&
            this.switch == other.switch &&
            this.solver == other.solver &&
            this.category == other.category &&
            this.sound == other.sound)
}

// data class 형식으로 알람 데이터 저장 (추가 보완이 필요함)