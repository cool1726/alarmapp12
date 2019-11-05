package org.siwonlee.alarmapp12

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_list.*
import android.R.attr.key
import java.nio.file.Files.size
import org.json.JSONArray
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.text.TextUtils.isEmpty
import org.json.JSONException



class AlarmList_Activity : AppCompatActivity() {
    val REQ_BASIC_SET : Int = 1000
    val REQ_PREV_SET : Int = 2000
    val REQ_WEEK_SET : Int = 3000
    val REQUEST_CLICK : Int = 4000
    val alarmlist = ArrayList<Alarm_Data>()

    private val prefStorage = "org.siwonlee.alarmapp12.prefs"


    //설정한 알람의 개수
    var size = 0

    // AlarmlistAdapter의 ViewHolder
    val adapter = AlarmListAdapter(this, alarmlist,
        { position ->
            val cintent = Intent(this, MainActivity::class.java)
            cintent.putExtra("hr", alarmlist[position].hr)
            cintent.putExtra("min", alarmlist[position].min)

            cintent.putExtra("date", alarmlist[position].ringDate)
            cintent.putExtra("stringSwitch", alarmlist[position].ringSwitch)

            cintent.putExtra("ID", alarmlist[position].ID)
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

        size = pref.getInt("size", 0)

        //앱이 실행되어 AlarmList_Acitivity가 생성될 때마다 pref에 저장된 값 불러와서 alarmlist 생성
        for (i in 0..size) {
            if (pref.getString("time${i}", "no alarm") != "no alarm") {
                //알람이 삭제되어 ID 값이 비었을 경우엔 alarmlist에 데이터를 저장하지 않는다
                //but 리사이클러뷰에서 position값(위치)이 바뀌면 Alarm_Data의 ID 값도 바꾸는 방법이 좋을 듯합니다

                val time = pref.getString("time${i}", "no alarm")
                val date = pref.getString("date${i}", "no date")
                val hr = pref.getInt("hr${i}", 6)
                val min = pref.getInt("min${i}", 0)
                val stringSwitch = pref.getString("stringSwitch${i}", "TFFFFFFF")
                val solver = pref.getInt("solver", 0)

                // json -> array
                val ahrstr = pref.getString("ahr", null)
                val aminstr = pref.getString("amin", null)

                val ahr = intArrayOf()
                if (ahrstr != null) {
                    try {
                        val a = JSONArray(ahrstr)
                        for (i in 0 until a.length()) {
                            val url = Integer.parseInt(a.optString(i))
                            ahr.plus(url)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                val amin = intArrayOf()
                if (aminstr != null) {
                    try {
                        val a = JSONArray(aminstr)
                        for (i in 0 until a.length()) {
                            val url = Integer.parseInt(a.optString(i))
                            amin.plus(url)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                //ID 순서대로 alarmlist에 알람 추가
                alarmlist.add(Alarm_Data(hr, min, ahr, amin, time, date, stringSwitch, i, solver))
            }
        }

        pref.edit().commit()

        // LinearLayoutManager : alarm_list.xml의 alarm_recyclerview에 세로형태로 아이템을 배치한다
        alarm_recyclerview.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        alarm_recyclerview.setHasFixedSize(true)
        alarm_recyclerview.adapter = adapter


        // 알람 초기 셋팅 : 알람 셋팅 화면으로 이동 (activity_main.xml로 이동)
        fab_add.setOnClickListener{
            size += 1
            val intent = Intent(this, MainActivity::class.java)
            //알람의 정보를 intent에 담아서 전송한다
            //지금은 default가 6시 정각 및 요일 미설정으로 고정되어 있음
            intent.putExtra("hr", 6)
            intent.putExtra("min", 0)
            intent.putExtra("ID", size)
            intent.putExtra("stringSwitch",  "TFFFFFFF")
            intent.putExtra("solver", 0)

            //알람의 수를 하나 늘렸으므로 이를 pref에 기록한다
            pref.edit().putInt("size", size).apply()

            //알람을 설정한다
            startActivityForResult(intent, REQ_BASIC_SET)
        }

        // () 요일 알람 초기셋팅 : 알람 셋팅 화면으로 이동 (previous_alarmset.xml로 이동)
        fab_add2.setOnClickListener {
            size += 1
            val intent = Intent(this, PreviousAlarmAcitivity::class.java)
            //알람의 정보를 intent에 담아서 전송한다
            //지금은 default가 6시 정각 및 요일 미설정으로 고정되어 있음
            intent.putExtra("hr", 6)
            intent.putExtra("min", 0)
            intent.putExtra("ID", size)
            intent.putExtra("stringSwitch",  "TFFFFFFF")
            intent.putExtra("solver", 0)

            //알람의 수를 하나 늘렸으므로 이를 pref에 기록한다
            pref.edit().putInt("size", size).apply()

            //알람을 설정한다
            startActivityForResult(intent, REQ_PREV_SET)
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
                REQ_BASIC_SET -> {   // MainActivity에서 intents로 추가한 time, date 데이터 받아오기 (초기 설정)
                    val hr = data!!.getIntExtra("hr", -1)
                    val min = data!!.getIntExtra("min", -1)
                    val time = data!!.getStringExtra("time")
                    val date = data!!.getStringExtra("date")
                    val stringSwitch = data!!.getStringExtra("stringSwitch")
                    val ID = data!!.getIntExtra("ID", 0)
                    val solver = data!!.getIntExtra("solver", 0)

                    // 이 액티비티 내의 alarmlist(Alarm_Data형식의 arraylist)에 받아온 시간, 요일 정보 추가
                    alarmlist.add(Alarm_Data(hr, min, null, null, time, date, stringSwitch, ID, solver))

                    // 알람이 설정될 때마다 sharedPreferences로 데이터 저장
                    editor.putString("time${ID}", time)
                    editor.putString("date${ID}", date)
                    editor.putInt("hr${ID}", hr)
                    editor.putInt("min${ID}", min)
                    editor.putString("stringSwitch${ID}", stringSwitch)
                    editor.putInt("solver${ID}", solver)

                    editor.commit()
                }

                REQ_PREV_SET -> {
                    var ahrstr : String = ""
                    var aminstr : String = ""
                    val hr = data!!.getIntExtra("prehr", 0)
                    val min = data!!.getIntExtra("premin", 0)
                    val ahr = data!!.getIntArrayExtra("ahr")
                    val amin = data!!.getIntArrayExtra("amin")
                    val time = data!!.getStringExtra("time")
                    val date = data!!.getStringExtra("date")

                    val stringSwitch = data!!.getStringExtra("stringSwitch")
                    val ID = data!!.getIntExtra("ID", 0)
                    val solver = data!!.getIntExtra("solver", 0)

                    // array -> json (ahr)
                    if (ahr != null) {
                        val ahr_arr = JSONArray()
                        for (i in 0 until ahr.size) {
                            ahr_arr.put(ahr.get(i))
                        }
                        if ( !(ahr.isEmpty()) )
                            editor.putString(ahrstr, ahr_arr.toString())
                        else
                            editor.putString(ahrstr, null)
                        editor.commit()
                    }


                    // array -> json (amin)
                    if (amin != null) {
                        val amin_arr = JSONArray()
                        for (i in 0 until amin.size) {
                            amin_arr.put(amin.get(i))
                        }
                        if ( !(amin.isEmpty()) )
                            editor.putString(aminstr, amin_arr.toString())
                        else
                            editor.putString(aminstr, null)
                        editor.commit()
                    }


                    // 이 액티비티 내의 alarmlist(Alarm_Data형식의 arraylist)에 받아온 시간, 요일 정보 추가
                    alarmlist.add(Alarm_Data(hr, min, ahr, amin, time, date, stringSwitch, ID, solver))

                    // 알람이 설정될 때마다 sharedPreferences로 데이터 저장
                    editor.putString("time${ID}", time)
                    editor.putString("date${ID}", date)
                    editor.putInt("hr${ID}", hr)
                    editor.putInt("min${ID}", min)
                    editor.putString("ahr${ID}", ahrstr)
                    editor.putString("amin${ID}", ahrstr)
                    editor.putString("stringSwitch${ID}", stringSwitch)
                    editor.putInt("solver${ID}", solver)

                    editor.commit()
                }

                REQUEST_CLICK -> {   //recyclerview에서 클릭한 item -> 삭제 또는 수정
                    if (data!!.getBooleanExtra("delete", false)) { //알람 삭제
                        // MainActivity에서 position 값 받아오기
                        val position = data!!.getIntExtra("position", -1)
                        val ID = data!!.getIntExtra("ID", 0)

                        //alarmlist에서 삭제할 알람의 position(인덱스값)으로 항목 삭제
                        alarmlist.removeAt(position)

                        //마찬가지로 pref에 저장된 데이터도 삭제
                        editor.remove("time${ID}")
                        editor.remove("date${ID}")
                        editor.remove("hr${ID}")
                        editor.remove("min${ID}")
                        editor.remove("stringSwitch${ID}")
                        editor.remove("solver${ID}")

                        editor.remove("ahr${ID}")
                        editor.remove("amin${ID}")

                        editor.commit()
                    }
                    else { // 알람 수정
                        var ahrstr : String = ""
                        var aminstr : String = ""

                        //수정될 알람의 alarmlist 인덱스값을 position으로 받아온다
                        val position = data!!.getIntExtra("position", -1)

                        val hr = data!!.getIntExtra("prehr", 0)
                        val min = data!!.getIntExtra("premin", 0)
                        val ahr = data!!.getIntArrayExtra("hr")
                        val amin = data!!.getIntArrayExtra("min")
                        val time = data!!.getStringExtra("time")
                        val date = data!!.getStringExtra("date")
                        val stringSwitch = data!!.getStringExtra("stringSwitch")
                        val ID = data!!.getIntExtra("ID", 0)
                        val solver = data!!.getIntExtra("solver", 0)

                        alarmlist.add(Alarm_Data(hr, min, ahr, amin, time, date, stringSwitch, ID, solver))

                        // array -> json (ahr)
                        val ahr_arr = JSONArray()
                        for (i in 0 until ahr.size) {
                            ahr_arr.put(ahr.get(i))
                        }
                        if ( ahr != null )
                            editor.putString(ahrstr, ahr_arr.toString())
                        else
                            editor.putString(ahrstr, null)
                        editor.commit()

                        // array -> json (amin)
                        val amin_arr = JSONArray()
                        for (i in 0 until ahr.size) {
                            amin_arr.put(ahr.get(i))
                        }
                        if ( ahr != null )
                            editor.putString(aminstr, amin_arr.toString())
                        else
                            editor.putString(aminstr, null)
                        editor.commit()

                        //pref의 데이터 삭제 후
                        editor.remove("time${ID}")
                        editor.remove("date${ID}")
                        editor.remove("hr${ID}")
                        editor.remove("min${ID}")
                        editor.remove("stringSwitch${ID}")
                        editor.remove("solver${ID}")

                        editor.remove("ahr${ID}")
                        editor.remove("amin${ID}")

                        //pref에 데이터 추가
                        editor.putString("time${ID}", time)
                        editor.putString("date${ID}", date)
                        editor.putInt("hr${ID}", hr)
                        editor.putInt("min${ID}", min)
                        editor.putString("stringSwitch${ID}", stringSwitch)
                        editor.putInt("solver${ID}", solver)

                        editor.putString("ahr${ID}", ahrstr)
                        editor.putString("amin${ID}", ahrstr)
                    }
                }
            }
        }
        // 바뀐 alarmlist 때문에 adapter 갱신
        alarm_recyclerview.adapter = adapter
    }
}
