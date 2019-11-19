package org.siwonlee.alarmapp12

class Alarm_Data(
    var hr: Int = 6,
    var min: Int = 0,
    var phr: Int = 0,
    var pmin: Int = 0,
    var switch: MutableList<Boolean> = mutableListOf(true, false, false, false, false, false, false, false),
    var solver: Int = 0,
    var category: String = "기본"
)

// data class 형식으로 알람 데이터 저장 (추가 보완이 필요함)