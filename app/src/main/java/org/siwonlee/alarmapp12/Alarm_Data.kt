package org.siwonlee.alarmapp12

data class Alarm_Data(val hr: Int, val min: Int, val ahr: IntArray?, val amin: IntArray?, val ringTime: String?, val ringDate: String?, val ringSwitch: String?, val ID: Int, val solver: Int)
// data class 형식으로 알람 데이터 저장 (추가 보완이 필요함)