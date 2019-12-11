package org.siwonlee.alarmapp12.solving

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.util.Log
import android.view.WindowManager
import org.siwonlee.alarmapp12.MainActivity
import org.siwonlee.alarmapp12.R


class AlarmSolvingBacode : AlarmSolvingBasic() {

    var qr : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_solving_bacode)

        qr = intent.extras!!.getString("qr")

        // QR code scanner
        val qrScan = IntentIntegrator(this)
        qrScan.setPrompt("알람 설정 시 찍은 바코드를 찍어주세요")
        qrScan.setOrientationLocked(false)
        qrScan.initiateScan()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
            else {
                try {
                    //Log.d("qr result num : ", result.contents)
                    //Log.d("qr saved num : ", qr)
                    if (qr == result.contents) {
                        Log.d("qr result : ", "correct!")
                        stop()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    //Log.v("Exception :::::::::::::", "QR code fail")
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    //뒤로가기로 알람 해제를 막기 위한 빈 함수
    //override fun onBackPressed() { }
}