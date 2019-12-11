package org.siwonlee.alarmapp12

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import org.siwonlee.alarmapp12.alarm.Alarm_Receiver
import java.util.*
import kotlin.collections.HashMap

data class Alarm_Data(
    var timeInMillis: Long = Calendar.getInstance().timeInMillis,
    var phr: Int = 0,
    var pmin: Int = 0,
    var switch: MutableList<Boolean> = mutableListOf(false, false, false, false, false, false, false, false),
    var name: String = "",
    var solver: Int = 0,
    var qr: String = "",
    var category: String = "기본",
    var sound: String = "",
    var onoff: Boolean = true
)

fun Alarm_Data(map: HashMap<String, Any>): Alarm_Data {
    var category: String? = map["category"] as String
    var name: String? = map["name"] as String
    var onoff: Boolean? = map["onoff"] as Boolean
    var phr: Int? = (map["phr"] as Long).toInt()
    var pmin: Int? = (map["pmin"] as Long).toInt()
    var qr: String? = map["qr"] as String
    var solver: Int? = map["solver"] as Int
    var sound: String? = map["sound"] as String
    var switch: MutableList<Boolean> = map["switch"] as MutableList<Boolean>
    var timeInMillis: Long? = map["timeInMillis"] as Long

    if (category == null) category = ""
    if (name == null) name = ""
    if (onoff == null) onoff = true
    if (phr == null) phr = 0
    if (pmin == null) pmin = 0
    if (qr == null) qr = ""
    if (solver == null) solver = 0
    if (sound == null) sound = ""
    if (timeInMillis == null) timeInMillis = 0

    return Alarm_Data(
        category = category,
        name = name,
        onoff = onoff,
        phr = phr,
        pmin = pmin,
        qr = qr,
        solver = solver,
        sound = sound,
        switch = switch,
        timeInMillis = timeInMillis
    )
}

fun Alarm_Data.isEqual(other: Alarm_Data): Boolean {
    return (this.timeInMillis == other.timeInMillis &&
            this.phr == other.phr &&
            this.pmin == other.pmin &&
            this.switch == other.switch &&
            this.name == other.name &&
            this.solver == other.solver &&
            this.qr == other.qr &&
            this.category == other.category &&
            this.sound == other.sound &&
            this.onoff == other.onoff)
}

fun Alarm_Data.setAlarm(context: Context, set: Int) {
    Log.d("TAG", "setAlarm")
    Log.d("Data Sound", sound)

    val ALARM_ACTIVATE = 1
    val ALARM_DEACTIVATE = -1
    //알람을 설정할 AlarmManager 클래스
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    //Alarm_Data의 timeInMillis를 Calendar 객체를 이용해 해석 가능하게 바꾼다
    val cal = Calendar.getInstance()
    cal.timeInMillis = this.timeInMillis
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    //시간과 분은 자주 쓰이므로 변수로 정의한다
    val hr = cal.get(Calendar.HOUR_OF_DAY)
    val min = cal.get(Calendar.MINUTE)

    //지정한 미리 울리기 시간만큼 알람 시간을 앞으로 당긴다
    cal.add(Calendar.HOUR_OF_DAY, -phr)
    cal.add(Calendar.MINUTE, -pmin)

    //지정한 날짜에 알람이 울리게 하고 싶다면
    if(this.switch[0]) {
        //알람이 울릴 년/월/일 정보를 받아온다
        val yy = (cal.get(Calendar.YEAR) % 10) + 10
        val dd = cal.get(Calendar.DAY_OF_YEAR)

        //requestCode를 YYdddHHmm 형식으로 나타내면 Int 타입 범위 안에 들어가면서
        //알람끼리 서로 겹치지 않는다
        val requestCode: Int = (yy * 1000 + dd) * 10000 + (hr * 100 + min)

        val intent = Intent(context, Alarm_Receiver::class.java)
        //setRepeating이 아니라 알람 해제 시 재등록을 통해 알람을 반복한다
        intent.putExtra("timeInMillis", cal.timeInMillis)
        intent.putExtra("requestCode", requestCode)
        intent.putExtra("solver", solver)
        intent.putExtra("sound", sound)

        Toast.makeText(context, "${solver}  ${sound}", Toast.LENGTH_LONG).show()

        //주어진 날짜로 설정된 PendingIntent를 생성한다
        val dateIntent = PendingIntent.getBroadcast(
            context, requestCode,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        //알람을 설정하고자 한다면 알람을 설정한다
        if(set == ALARM_ACTIVATE && !cal.before(Calendar.getInstance())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, dateIntent)
            else
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, dateIntent)
        }
        //그렇지 않다면 알람을 취소한다
        else if (set == ALARM_DEACTIVATE) alarmManager.cancel(dateIntent)
    }

    //일요일부터 토요일까지 알람을 설정한 요일이 있다면
    for(day in 1..7) {
        //cal이 울리는 년/월/일은 미지수이므로 이를 오늘로 고정한다
        cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
        cal.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH))
        cal.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE))

        if (this.switch[day]){
            val temp = (day - cal.get(Calendar.DAY_OF_WEEK))
            //알람이 울리는 요일이 다음 day 요일이 되도록 cal을 조정한다
            cal.add(Calendar.DATE, temp)

            //최종적으로 맞춰진 시각이 현재보다 이전이라면 알람을 7일 뒤로 늦춘다
            if (cal.before(Calendar.getInstance()))
                cal.add(Calendar.DATE, 7)

            //알람 정보를 dHHMM로 나타내면 알람이 서로 겹치지 않는다
            val requestCode: Int = (day * 100 + hr) * 100 + min

            //정보를 this에서 receiver까지 보내는 intent를 생성
            val intent = Intent(context, Alarm_Receiver::class.java)
            //setRepeating이 아니라 알람 해제 시 재등록을 통해 알람을 반복한다
            intent.putExtra("timeInMillis", cal.timeInMillis)
            intent.putExtra("requestCode", requestCode)
            intent.putExtra("solver", solver)
            intent.putExtra("qr", qr)
            intent.putExtra("sound", sound)
            intent.putExtra("name", name)

            //정해진 요일에 맞는 PendingIntent를 설정한다
            val dayIntent = PendingIntent.getBroadcast(
                context, requestCode,
                intent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            //알람을 설정하고자 한다면 알람 매니저에 알람을 설정한다
            if (set == ALARM_ACTIVATE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, dayIntent)
                else
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, dayIntent)
            }

            //알람을 제거하고자 한다면 alarmManager에서 알람을 취소한다
            else if (set == ALARM_DEACTIVATE) alarmManager.cancel(dayIntent)
        }
    }
}