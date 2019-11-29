package org.siwonlee.alarmapp12

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.*
import kotlin.collections.HashMap

data class Alarm_Data(
    var timeInMillis: Long = Calendar.getInstance().timeInMillis,
    var phr: Int = 0,
    var pmin: Int = 0,
    var switch: MutableList<Boolean> = mutableListOf(false, false, false, false, false, false, false, false),
    var solver: Int = 0,
    var qr: String = "",
    var category: String = "기본",
    var sound: String = ""
)

fun Alarm_Data(map: HashMap<String, Any>): Alarm_Data {
    return Alarm_Data(
        timeInMillis = map["timeInMillis"] as Long,
        phr = (map["phr"] as Long).toInt(),
        pmin = (map["pmin"] as Long).toInt(),
        switch = map["switch"] as MutableList<Boolean>,
        solver = (map["solver"] as Long).toInt(),
        qr = map["qr"] as String,
        category = map["category"] as String,
        sound = (map["sound"] as String)
    )
}

fun Alarm_Data.isEqual(other: Alarm_Data): Boolean {
    return (this.timeInMillis == other.timeInMillis &&
            this.phr == other.phr &&
            this.pmin == other.pmin &&
            this.switch == other.switch &&
            this.solver == other.solver &&
            this.qr == other.qr &&
            this.category == other.category &&
            this.sound == other.sound)
}

fun Alarm_Data.setAlarm(context: Context, set: Int) {
    val ALARM_ACTIVATE = 1
    val ALARM_DEACTIVATE = -1
    //알람을 설정할 AlarmManager 클래스
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    //Alarm_Data의 timeInMillis를 Calendar 객체를 이용해 해석 가능하게 바꾼다
    val cal = Calendar.getInstance()
    cal.timeInMillis = this.timeInMillis

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
        intent.putExtra("timeInMillis", timeInMillis)
        intent.putExtra("requestCode", requestCode)
        intent.putExtra("solver", solver)
        intent.putExtra("sound", sound)

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

    //cal이 울리는 년/월/일은 미지수이므로 이를 오늘로 고정한다
    cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
    cal.set(Calendar.DAY_OF_YEAR, Calendar.getInstance().get(Calendar.DAY_OF_YEAR))

    //일요일부터 토요일까지 알람을 설정한 요일이 있다면
    for(day in 1..7) if (this.switch[day]){
        //알람이 울리는 요일이 다음 day 요일이 되도록 cal을 조정한다
        cal.add(
            Calendar.DATE,
            (day - cal.get(Calendar.DAY_OF_WEEK))
        )

        //최종적으로 맞춰진 시각이 현재보다 이전이라면 알람을 7일 뒤로 늦춘다
        if (cal.before(Calendar.getInstance())) cal.add(Calendar.DATE, 7)

        //알람 정보를 dHHMM로 나타내면 알람이 서로 겹치지 않는다
        val requestCode: Int = (day * 100 + hr) * 100 + min

        //정보를 this에서 receiver까지 보내는 intent를 생성
        val intent = Intent(context, Alarm_Receiver::class.java)
        //setRepeating이 아니라 알람 해제 시 재등록을 통해 알람을 반복한다
        intent.putExtra("timeInMillis", timeInMillis)
        intent.putExtra("requestCode", requestCode)
        intent.putExtra("solver", solver)
        intent.putExtra("sound", sound)

        //정해진 요일에 맞는 PendingIntent를 설정한다
        val dayIntent = PendingIntent.getBroadcast(
            context, requestCode,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        //알람을 설정하고자 한다면 알람 매니저에 알람을 설정한다
        if (set == ALARM_ACTIVATE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, dayIntent)
            else
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, dayIntent)
        }

        //알람을 제거하고자 한다면 alarmManager에서 알람을 취소한다
        else if (set == ALARM_DEACTIVATE) alarmManager.cancel(dayIntent)
    }
}

// data class 형식으로 알람 데이터 저장 (추가 보완이 필요함)