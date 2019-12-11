package org.siwonlee.alarmapp12.alarm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import org.siwonlee.alarmapp12.R

class Alarm_Set_Barcode : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_solving_bacode)

        // QR code scanner
        val qrScan = IntentIntegrator(this)
        qrScan.setPrompt("알람이 울릴 때 다시 찍을 수 있는 바코드를 등록해주세요")
        qrScan.setOrientationLocked(false)
        qrScan.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
            try {
                val qrIntent = Intent()
                qrIntent.putExtra("qr", result.contents)

                setResult(Activity.RESULT_OK, qrIntent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                //Log.v("Exception :::::::::::::", "QR code fail")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}