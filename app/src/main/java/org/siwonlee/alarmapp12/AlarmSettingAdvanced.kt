package org.siwonlee.alarmapp12


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.alarm_setting_advanced.*

class AlarmSettingAdvanced : AppCompatActivity() {
    var solver = 0
    var phr = 0
    var pmin = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_setting_advanced)

        solver = intent.extras!!.getInt("solver", 0)
        phr = intent.extras!!.getInt("phr", 0)
        pmin = intent.extras!!.getInt("pmin", 0)

        preHr.setText(phr.toString())
        preMin.setText(pmin.toString())

        solving.adapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.spinnerItem)
        )

        solving.setSelection(solver)

        solving.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                solver = i
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        })

        done.setOnClickListener {
            var str = preHr.toString().toInt()
            phr = str

            str = preMin.toString().toInt()
            pmin = str

            var returnIntent = Intent()
            returnIntent.putExtra("solver", solver)
            returnIntent.putExtra("phr", phr)
            returnIntent.putExtra("pmin", pmin)

            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }
}