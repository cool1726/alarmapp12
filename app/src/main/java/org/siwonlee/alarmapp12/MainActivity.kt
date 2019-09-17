package org.siwonlee.alarmapp12

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import android.widget.TimePicker
import android.widget.Button
import android.widget.Toast
import java.util.*

class MainActivity : AppCompatActivity() {
    var cal : Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var hr : Int = cal.get(Calendar.HOUR_OF_DAY)
        var min : Int = cal.get(Calendar.MINUTE)

        val timePicker: TimePicker = findViewById(R.id.timePicker)
        timePicker.setIs24HourView(true)
        timePicker.setOnTimeChangedListener({timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)

            hr = hour; min = minute;
        })

        val set: Button = findViewById(R.id.button)
        set.setOnClickListener{
            val str : String = hr.toString() + ":" + min.toString()
            Toast.makeText(this@MainActivity, str, Toast.LENGTH_SHORT).show()
        }

        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.setHour(hr)
            timePicker.setMinute(min)
        } else {
            timePicker.setCurrentHour(hr)
            timePicker.setCurrentMinute(min)
        }
    }
}
