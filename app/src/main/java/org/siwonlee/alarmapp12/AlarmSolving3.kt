package org.siwonlee.alarmapp12

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import android.content.Intent



class AlarmSolving3 : AppCompatActivity() {

    var version = 3 //알람 끌 때는 99, 바코드 초기 설정은 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_solving_3)

        //바코드를 초기 등록하는 과정이 아니라면
        //즉, 울리는 알람을 끄는 중이라면
        if(intent.getIntExtra("solving", 99) != 3) {
            //액티비티를 넘어가면 알람 진동과 알람음이 제거되므로
            //   모든 알람 해제 액티비티에 알람음과 알람 진동을 추가한다
            version = 99

/*          //바코드 찍는 중에도 알람음이 이어져서 계속 울려야 하므로 백그라운드 재생 필요 (추가 예정)
            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(1000, 1000, 1000, 1000), 0))
            } else {
                v.vibrate(longArrayOf(1000, 1000, 1000, 1000), 0)
            }
            val ringtone = RingtoneManager.getRingtone(
                applicationContext,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            )
            //ringtone.play()*/
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
                        if (qr == result.contents) {
                            //ringtone.stop()
                            finish()
                        }
                    }

                    //바코드 초기 설정
                    /* QR 코드 내용*/
                    val qrintent = Intent(this, MainActivity::class.java)
                    qrintent.putExtra("qr", result.contents)

                    //Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()

                    setResult(Activity.RESULT_OK, qrintent)
                    finish()

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
    override fun onBackPressed() { }
}