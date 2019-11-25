package org.siwonlee.alarmapp12

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import java.util.*
import kotlin.collections.HashMap

data class Alarm_Data(
    var hr: Int = 6,
    var min: Int = 0,
    var phr: Int = 0,
    var pmin: Int = 0,
    var switch: MutableList<Boolean> = mutableListOf(true, false, false, false, false, false, false, false),
    var solver: Int = 0,
    var category: String = "기본"
)

fun Alarm_Data(map: HashMap<String, Any>): Alarm_Data {
    return Alarm_Data(
        hr = (map["hr"] as Long).toInt(),
        min = (map["min"] as Long).toInt(),
        phr = (map["phr"] as Long).toInt(),
        pmin = (map["pmin"] as Long).toInt(),
        switch = map["switch"] as MutableList<Boolean>,
        solver = (map["solver"] as Long).toInt(),
        category = map["category"] as String
    )
}

fun Alarm_Data.isEqual(b: Alarm_Data): Boolean {
    return (this.hr == b.hr &&
            this.min == b.min &&
            this.phr == b.phr &&
            this.pmin == b.pmin &&
            this.switch == b.switch &&
            this.solver == b.solver &&
            this.category == b.category)
}

fun Alarm_Data.setAlarm(context: Context, day: Int, set: Boolean) {
    //정보를 this에서 receiver까지 보내는 intent를 생성
    val intent = Intent(context, Alarm_Receiver::class.java)

    //alarmlist에서 알람 설정에 필요한 정보를 가져온다
    //val hr = this.hr
    //val min = this.min
    //val phr = this.phr * -1
    //val pmin = this.pmin * -1
    //val solver = this.solver

    //알람 정보를 dHHMM로 나타내면 알람이 서로 겹치지 않는다
    val requestCode: Int = (day * 100 + hr) * 100 + min

    //setRepeating이 아니라 알람 해제 시 재등록을 통해 알람을 반복한다
    intent.putExtra("HOUR_OF_DAY", hr)
    intent.putExtra("MINUTE", min)
    intent.putExtra("requestCode", requestCode)
    intent.putExtra("solver", solver)

    //정해진 요일에 맞는 PendingIntent를 설정한다
    val pendingIntent = PendingIntent.getBroadcast(
        context, requestCode,
        intent, PendingIntent.FLAG_UPDATE_CURRENT
    )

    //알람을 설정할 AlarmManager 클래스
    val alarmManager = getSystemService(context, AlarmManager::class.java) as AlarmManager

    //day요일에 알람을 울려야 한다면
    if (set) {
        //알람을 울릴 시각에 대한 정보를 Calendar를 이용해 표시한다
        val cal = Calendar.getInstance()

        //알람을 울릴 시각을 cal에 저장한다
        cal.set(Calendar.HOUR_OF_DAY, hr)
        cal.set(Calendar.MINUTE, min)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        //오늘부터 day 요일까지 남은 일 수
        val diff = (day - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7

        //알람이 울리는 요일이 i 요일이 되도록 cal을 조정한다
        cal.add(Calendar.DATE, diff)

        //지정한 미리 울리기 시간만큼 알람 시간을 앞으로 당긴다
        cal.add(Calendar.HOUR_OF_DAY, phr * -1)
        cal.add(Calendar.MINUTE, pmin * -1)

        //최종적으로 맞춰진 시각이 현재보다 이전이라면 알람을 7일 뒤로 늦춘다
        if (cal.before(Calendar.getInstance())) cal.add(Calendar.DATE, 7)

        //알람 매니저에 알람을 설정한다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        else
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
    }

    //day 요일에 알람을 울리지 않아야 한다면 알람을 취소한다
    else alarmManager.cancel(pendingIntent)
}
// data class 형식으로 알람 데이터 저장 (추가 보완이 필요함)