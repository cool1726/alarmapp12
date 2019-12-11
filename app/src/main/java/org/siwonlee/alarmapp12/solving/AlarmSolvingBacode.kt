package org.siwonlee.alarmapp12.solving

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import org.siwonlee.alarmapp12.R
import org.siwonlee.alarmapp12.alarm.Alarm_Set

class AlarmSolvingBacode : AlarmSolvingBasic() {
    var version = 3 //알람 끌 때는 99, 바코드 초기 설정은 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_solving_bacode)

        //바코드를 초기 등록하는 과정이 아니라면
        //즉, 울리는 알람을 끄는 중이라면
        if(intent.getIntExtra("solving", 99) != 3) {
            version = 99
        }

        // QR code scanner
        val qrScan = IntentIntegrator(this)
        qrScan.setPrompt("알람 설정 시 찍은 바코드를 찍어주세요")
        qrScan.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else { //QR코드, 내용 존재
                try {
                    if(version == 99) {    //알람이 울릴 때
                        val qr = intent.extras!!.getString("qr")
                        if (qr == result.contents) stop()
                    }

                    //바코드 초기 설정
                    /* QR 코드 내용*/
                    val qrintent = Intent(this, Alarm_Set::class.java)
                    qrintent.putExtra("qr", result.contents)

                    //Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()

                    setResult(Activity.RESULT_OK, qrintent)
                    stop()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.v("Exception :::::::::::::", "QR code fail")
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}