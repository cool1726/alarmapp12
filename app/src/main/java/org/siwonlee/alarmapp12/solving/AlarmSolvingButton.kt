package org.siwonlee.alarmapp12.solving

import android.os.Bundle
import kotlinx.android.synthetic.main.alarm_solving_button.*
import org.siwonlee.alarmapp12.R
import java.util.*

fun Int.toTime(): String {
    var ret = ""
    if (this < 10) ret = "0"

    return ret + this.toString()
}

class AlarmSolvingButton : AlarmSolvingBasic() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_solving_button)

        //알람이 울리는 시간을 Calendar로 알아낸다
        var hr: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val min: Int = Calendar.getInstance().get(Calendar.MINUTE)

        if(hr == 0) hr = 12

        time_now.text = "${hr.toTime()}:${min.toTime()}"
        button_name.text = alarmName

        button_stop.setOnClickListener { stop() }
        button_delay.setOnClickListener { delay() }
    }

    //뒤로가기로 알람 해제를 막기 위한 빈 함수
    override fun onBackPressed() { }
}