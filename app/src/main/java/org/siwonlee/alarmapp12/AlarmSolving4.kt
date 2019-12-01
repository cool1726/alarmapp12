package org.siwonlee.alarmapp12

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
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
import android.view.WindowManager
import kotlinx.android.synthetic.main.alarm_ringing.*
import java.util.*


class AlarmSolving4 : AppCompatActivity() {

    var version = 3 //알람 끌 때는 99, 바코드 초기 설정은 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_solving_3)

        //잠금화면 위에서 액티비티를 띄울 수 있게 해준다
        //API 제한 없이 사용할 수 있는 코드들을 사용한다
        this.window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        //API 27부터 사용 불가능한 LayoutParams를 다른 기능으로 대체한다
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            this.window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        //알람 진동을 울리게 할 Vibrator
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // 알람 울릴 때 1초 진동, 1초 휴식을 반복
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(1000, 1000, 1000, 1000), 0))
        } else {
            v.vibrate(longArrayOf(1000, 1000, 1000, 1000), 0)
        }

        //string화 된 Uri값을 받아와 다시 Uri로 처리
        val sound = intent.getStringExtra("sound")
        var uri = Uri.parse(sound)

        //알람음을 울릴 RingtoneManager
        val ringtone : Ringtone = RingtoneManager.getRingtone(applicationContext, uri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val aa = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)    //RingTone이 알람 볼륨으로 울리도록
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            ringtone.audioAttributes = aa
        } else {
            ringtone.streamType = AudioManager.STREAM_ALARM
        }

        Toast.makeText(this, "${uri} 알람소리", Toast.LENGTH_LONG).show()

        // 알람 울릴 때 소리 : 기본 알람소리
        ringtone.play()

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