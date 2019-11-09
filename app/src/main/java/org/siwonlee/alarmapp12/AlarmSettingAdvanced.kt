package org.siwonlee.alarmapp12


import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RatingBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.alarm_setting_advanced.*

class AlarmSettingAdvanced : AppCompatActivity() {
    var solver = 0
    var phr = 0
    var pmin = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.alarm_setting_advanced, null)
        val dialogHr = dialogView.findViewById<EditText>(R.id.preHr)
        val dialogMin = dialogView.findViewById<EditText>(R.id.preMin)

        solver = intent.extras!!.getInt("solver", 0)
        phr = intent.extras!!.getInt("phr", 0)
        pmin = intent.extras!!.getInt("pmin", 0)

        dialogHr.setText(phr.toString())
        dialogMin.setText(pmin.toString())

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

        var returnIntent = Intent()

        builder.setView(dialogView)
        builder.setPositiveButton("확인") { _, _ ->
            phr = dialogHr.toString().toInt()
            pmin = dialogMin.toString().toInt()


            returnIntent.putExtra("solver", solver)
            returnIntent.putExtra("phr", phr)
            returnIntent.putExtra("pmin", pmin)

            setResult(Activity.RESULT_OK, returnIntent)
            finish()
            }
        builder.setNegativeButton("취소") { _, _ -> /* 취소일 때 아무 액션이 없으므로 빈칸 */ }
        builder.create().show()


    }
}