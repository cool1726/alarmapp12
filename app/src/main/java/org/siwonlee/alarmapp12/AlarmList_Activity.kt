package org.siwonlee.alarmapp12

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_list.*

class AlarmList_Activity : AppCompatActivity() {
    val REQUEST_SET : Int = 1000
    val REQUEST_CLICK : Int = 2000
    val alarmlist = ArrayList<Alarm_Data>()

    private val prefStorage = "org.siwonlee.alarmapp12.prefs"

    // AlarmlistAdapter의 ViewHolder
    val adapter = AlarmListAdapter(this, alarmlist,
        { position ->
            val cintent = Intent(this, MainActivity::class.java)
            cintent.putExtra("hr", alarmlist[position].hr)
            cintent.putExtra("min", alarmlist[position].min)

            cintent.putExtra("phr", alarmlist[position].phr)
            cintent.putExtra("pmin", alarmlist[position].pmin)

            cintent.putExtra("date", alarmlist[position].ringDate)
            cintent.putExtra("stringSwitch", alarmlist[position].ringSwitch)

            cintent.putExtra("solver", alarmlist[position].solver)

            cintent.putExtra("position", position)

            //알람을 수정한다
            startActivityForResult(cintent, REQUEST_CLICK)},
        { position -> onLongClickEvent() })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_list)

        val pref = this.getSharedPreferences(prefStorage, MODE_PRIVATE)
        val editor = pref!!.edit()
        //editor.clear() pref에 등록된 알람 데이터 삭제용

        //앱이 실행되어 AlarmList_Acitivity가 생성될 때마다 pref에 저장된 값 불러와서 alarmlist 생성
        for (i in 0..24 * 60) {
            if (pref.getString("time${i}", "no alarm") != "no alarm") {
                //알람이 삭제되어 ID 값이 비었을 경우엔 alarmlist에 데이터를 저장하지 않는다
                //but 리사이클러뷰에서 position값(위치)이 바뀌면 Alarm_Data의 ID 값도 바꾸는 방법이 좋을 듯합니다

                val time = pref.getString("time${i}", "no alarm")
                val date = pref.getString("date${i}", "no date")
                val phr = pref.getInt("phr${i}", 0)
                val pmin = pref.getInt("pmin${i}", 0)
                val stringSwitch = pref.getString("stringSwitch${i}", "TFFFFFFF")
                val solver = pref.getInt("solver${i}", 0)

                //ID 순서대로 alarmlist에 알람 추가
                alarmlist.add(Alarm_Data(i / 60, i % 60, phr, pmin, time, date, stringSwitch, solver))
            }
        }

        pref.edit().commit()

        // LinearLayoutManager : alarm_list.xml의 alarm_recyclerview에 세로형태로 아이템을 배치한다
        alarm_recyclerview.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        alarm_recyclerview.setHasFixedSize(true)
        alarm_recyclerview.adapter = adapter


        // 알람 초기 셋팅 : 알람 셋팅 화면으로 이동 (activity_main.xml로 이동)
        fab_add.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            //알람의 정보를 intent에 담아서 전송한다
            //지금은 default가 6시 정각 및 요일 미설정으로 고정되어 있음
            intent.putExtra("hr", 6)
            intent.putExtra("min", 0)
            intent.putExtra("stringSwitch",  "TFFFFFFF")
            intent.putExtra("solver", 0)

            //알람을 설정한다
            startActivityForResult(intent, REQUEST_SET)
        }

        signInButton.setOnClickListener {
            val logInIntent = Intent(this, GoogleSignInActivity::class.java)
            startActivity(logInIntent)
        }
    }

    // item을 오래 클릭할 시 바로 알람 삭제여부를 묻는 dialog 띄우기 (나중에)
    fun onLongClickEvent() { }

    // 특정 REQUEST_CODE로 MainActivity.kt에 결과값을 요구하고 결과값 받기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val pref = this.getSharedPreferences(prefStorage, MODE_PRIVATE)
        val editor = pref!!.edit()

        if (resultCode == Activity.RESULT_OK) { // MainActivity에서 RESULT_OK 사인을 보내면
            when (requestCode) {
                REQUEST_SET -> {   // MainActivity에서 intents로 추가한 time, date 데이터 받아오기 (초기 설정)
                    val hr = data!!.getIntExtra("hr", -1)
                    val min = data!!.getIntExtra("min", -1)
                    val phr = data!!.getIntExtra("phr", -1)
                    val pmin = data!!.getIntExtra("pmin", -1)
                    val time = data!!.getStringExtra("time")
                    val date = data!!.getStringExtra("date")
                    val stringSwitch = data!!.getStringExtra("stringSwitch")
                    val solver = data!!.getIntExtra("solver", 0)

                    if(hr != -1) {
                        // 이 액티비티 내의 alarmlist(Alarm_Data형식의 arraylist)에 받아온 시간, 요일 정보 추가
                        alarmlist.add(Alarm_Data(hr, min, phr, pmin, time, date, stringSwitch, solver))

                        val ID = hr * 60 + min

                        // 알람이 설정될 때마다 sharedPreferences로 데이터 저장
                        editor.putString("time${ID}", time)
                        editor.putString("date${ID}", date)
                        editor.putInt("phr${ID}", phr)
                        editor.putInt("pmin${ID}", pmin)
                        editor.putString("stringSwitch${ID}", stringSwitch)
                        editor.putInt("solver${ID}", solver)
                    }

                    editor.commit()
                }

                REQUEST_CLICK -> {   //recyclerview에서 클릭한 item -> 삭제 또는 수정
                    if (data!!.getBooleanExtra("delete", false)) { //알람 삭제
                        // MainActivity에서 position 값 받아오기
                        val position = data!!.getIntExtra("position", -1)
                        val before_id = data!!.getIntExtra("before_id", -1)

                        if(before_id != -1) {
                            //alarmlist에서 삭제할 알람의 position(인덱스값)으로 항목 삭제
                            alarmlist.removeAt(position)

                            //마찬가지로 pref에 저장된 데이터도 삭제
                            editor.remove("time${before_id}")
                            editor.remove("date${before_id}")
                            editor.remove("phr${before_id}")
                            editor.remove("pmin${before_id}")
                            editor.remove("stringSwitch${before_id}")
                            editor.remove("solver${before_id}")
                        }

                        editor.commit()
                    }
                    else { // 알람 수정
                        //수정될 알람의 alarmlist 인덱스값을 position으로 받아온다
                        val position = data!!.getIntExtra("position", -1)

                        val time = data!!.getStringExtra("time")
                        val date = data!!.getStringExtra("date")
                        val hr = data!!.getIntExtra("hr", -1)
                        val min = data!!.getIntExtra("min", -1)
                        val phr = data!!.getIntExtra("phr", -1)
                        val pmin = data!!.getIntExtra("pmin", -1)
                        val stringSwitch = data!!.getStringExtra("stringSwitch")
                        val solver = data!!.getIntExtra("solver", 0)
                        val before_id = data!!.getIntExtra("before_id", -1)

                        val ID = hr * 60 + min

                        alarmlist[position] = Alarm_Data(hr, min, phr, pmin, time, date, stringSwitch, solver)

                        if(before_id != -1) {
                            //pref의 데이터 삭제 후
                            editor.remove("time${before_id}")
                            editor.remove("date${before_id}")
                            editor.remove("phr${before_id}")
                            editor.remove("pmin${before_id}")
                            editor.remove("stringSwitch${before_id}")
                            editor.remove("solver${before_id}")
                        }

                        //pref에 데이터 추가
                        editor.putString("time${ID}", time)
                        editor.putString("date${ID}", date)
                        editor.putInt("phr${ID}", phr)
                        editor.putInt("pmin${ID}", pmin)
                        editor.putString("stringSwitch${ID}", stringSwitch)
                        editor.putInt("solver${ID}", solver)

                        editor.commit()
                    }
                }
            }
        }
        // 바뀐 alarmlist 때문에 adapter 갱신
        alarm_recyclerview.adapter = adapter
    }
}
