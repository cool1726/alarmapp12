package org.siwonlee.alarmapp12

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.alarm_list.*
import java.util.*

class AlarmList_Activity : AppCompatActivity() {
    val ALARM_SET: Int = 1000
    val FIREBASE_MANAGE: Int = 2000

    lateinit var alarmlist: UserData

    private val prefStorage = "org.siwonlee.alarmapp12.prefs"
    private var currentCategory = "전체 카테고리"

    private var uid: String = ""
    var categorize: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_list)

        val pref = this.getSharedPreferences(prefStorage, MODE_PRIVATE)
        val editor = pref!!.edit()
        //editor.clear() pref에 등록된 알람 데이터 삭제용

        //pref에 저장된 string타입 UserData 클래스를 decode해 alarmlist에 저장
        val strList = pref.getString("list", "")
        if(strList != "")
            alarmlist = GsonBuilder().create().fromJson(strList, UserData::class.java)
        else alarmlist = UserData()
        editor.apply()

        //pref에 uid가 저장되어 있다면 이를 가져오고, 아니라면 uid에 ""를 저장한다
        uid = pref.getString("uid", "")!!
        // category adapter 설정
        makeCategory()

        for(i in 0 until categorize.size) {
            if(currentCategory == categorize[i]) {
                sp_category.setSelection(i)
                break
            }
        }

        // 카테고리 선택 시, makeAdapter()를 호출해 알람뷰 새로고침
        sp_category.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long ) {
                currentCategory = categorize[i]
                alarm_recyclerview.adapter = makeAdapter()
            }
            override fun onNothingSelected(adapterView: AdapterView<*>)  {
                sp_category.setSelection(0)
            }
        }

        // LinearLayoutManager : alarm_list.xml의 alarm_recyclerview에 세로형태로 아이템을 배치한다
        alarm_recyclerview.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        alarm_recyclerview.setHasFixedSize(true)
        alarm_recyclerview.adapter = makeAdapter()

        // 알람 초기 셋팅 : 알람 셋팅 화면으로 이동 (activity_main.xml로 이동)
        fab_add.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("categories", alarmlist.category())
            //알람을 설정한다
            startActivityForResult(intent, this.ALARM_SET)
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
                //알람을 설정, 수정, 혹은 삭제했을 경우
                ALARM_SET -> {
                    // MainActivity에서 보낼 수 있는 모든 정보를 모은다
                    val strData = data!!.getStringExtra("data")
                    val before = data.getIntExtra("before_id", -1)
                    var position = data.getIntExtra("position", -1)
                    val delete = data.getBooleanExtra("delete", false)

                    //알람을 수정하거나 삭제했다면 before_id는 -1이 아니다
                    if(before != -1) {
                        //알람을 삭제한 것이라면 alarmlist에서 알람을 제거한다
                        if (delete) alarmlist.pop(position)
                    }

                    //알람을 수정 혹은 생성했다면 delete는 false이다
                    if (!delete) {
                        //alarmlist에 저장할 Alarm_Data 객체
                        var data = Alarm_Data()
                        if(strData != null)
                            data = GsonBuilder().create().fromJson(strData, Alarm_Data::class.java)

                        //알람을 생성했다면 position은 반드시 -1이다
                        if (position == -1) {
                            position = alarmlist.size()
                            alarmlist.add(data)
                        }
                        //그렇지 않다면 기존에 존재하던 알람을 수정한다
                        else alarmlist.set(position, data)

                        //일요일부터 토요일까지y 검토하며 알람을 설정
                        for(day in 1 .. 7)
                            setAlarm(day, alarmlist.get(position), data.switch[day])
                    }
                }

                //Firebase에 접근했을 경우
                FIREBASE_MANAGE -> {
                    //GoogleSignUpActivity에서 반환한 list를 fetchUserData에 담아
                    val fetchUserData = data!!.getStringExtra("list")

                    //해당 데이터가 "", 즉 이상한 데이터가 아니라면
                    if(fetchUserData != "") {
                        //기존에 존재하는 모든 알람을 해제한 뒤
                        for(data in alarmlist.list) for (day in 1..7) setAlarm(day, data, false)
                        //백업한 데이터를 가져와 alarmlist에 저장하고
                        alarmlist = GsonBuilder().create().fromJson(fetchUserData, UserData::class.java)
                        //백업한 리스트의 모든 알람을 set한다
                        for(data in alarmlist.list) for (day in 1..7) setAlarm(day, data, data.switch[day])
                    }

                    uid = data.getStringExtra("uid")!!
                    editor.putString("uid", uid)
                }
            }
        }

        // 바뀐 alarmlist 때문에 adapter 갱신
        alarm_recyclerview.adapter = makeAdapter()
        makeCategory()

        //alarmlist를 gson을 이용해 String타입으로 형변환한다
        val strList = GsonBuilder().create().toJson(alarmlist, UserData::class.java)
        //형변환한 data를 pref에 저장한다
        editor.putString("list", strList)

        //수정한 정보를 pref에 commit한다
        editor.apply()
    }

    //로그인 등의 메뉴를 앱의 메뉴바에 표시
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.alarm_list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //메뉴의 각 옵션을 선택했을 때 실행할 동작
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            //계정 연동 관리 버튼을 눌렀을 때
            R.id.signin -> {
                //GoogleSignInActivity를 실행시킬 intent를 만든 뒤
                val logInIntent = Intent(this, GoogleSignInActivity::class.java)

                //alarmlist를 gson을 이용해 String타입으로 형변환해 logInIntent에 넣는다
                val strList = GsonBuilder().create().toJson(alarmlist, UserData::class.java)
                logInIntent.putExtra("list", strList)
                logInIntent.putExtra("uid", uid)

                //logInIntent.putExtra()
                startActivityForResult(logInIntent, FIREBASE_MANAGE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setAlarm(day: Int, data: Alarm_Data, set: Boolean) {
        //정보를 this에서 receiver까지 보내는 intent를 생성
        val intent = Intent(this, Alarm_Receiver::class.java)

        //alarmlist에서 알람 설정에 필요한 정보를 가져온다
        val hr = data.hr
        val min = data.min
        val phr = data.phr * -1
        val pmin = data.pmin * -1
        val solver = data.solver

        //알람 정보를 dHHMM로 나타내면 알람이 서로 겹치지 않는다
        val requestCode: Int = (day * 100 + hr) * 100 + min

        //setRepeating이 아니라 알람 해제 시 재등록을 통해 알람을 반복한다
        intent.putExtra("HOUR_OF_DAY", hr)
        intent.putExtra("MINUTE", min)
        intent.putExtra("requestCode", requestCode)
        intent.putExtra("solver", solver)

        //정해진 요일에 맞는 PendingIntent를 설정한다
        val pendingIntent = PendingIntent.getBroadcast(
            this, requestCode,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        //알람을 설정할 AlarmManager 클래스
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //day요일에 알람을 울려야 한다면
        if (set) {
            //알람을 울릴 시각에 대한 정보를 Calendar를 이용해 표시한다
            val cal = Calendar.getInstance()

            //알람을 울릴 시각을 cal에 저장한다
            cal.set(Calendar.HOUR_OF_DAY, hr)
            cal.set(Calendar.MINUTE, min)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            //오늘부터 day 요일까지 남은 일 수
            val diff = (day - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7

            //알람이 울리는 요일이 i 요일이 되도록 cal을 조정한다
            cal.add(Calendar.DATE, diff)

            //지정한 미리 울리기 시간만큼 알람 시간을 앞으로 당긴다
            cal.add(Calendar.HOUR_OF_DAY, phr)
            cal.add(Calendar.MINUTE, pmin)

            //최종적으로 맞춰진 시각이 현재보다 이전이라면 알람을 7일 뒤로 늦춘다
            if (cal.before(Calendar.getInstance())) cal.add(Calendar.DATE, 7)

            //알람 매니저에 알람을 설정한다
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            else
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        }

        //day 요일에 알람을 울리지 않아야 한다면 알람을 취소한다
        else alarmManager.cancel(pendingIntent)
    }

    fun makeCategory() {
        categorize = alarmlist.category()
        categorize.add("전체 카테고리")

        val ret = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categorize
        )

        sp_category.adapter = ret
    }

    fun makeAdapter(): AlarmListAdapter {
        // AlarmlistAdapter의 ViewHolder
        // 선택된 category에 해당하는 alarmlist를 전달하기 위해 getCategoryList 함수 이용
        val adapter = AlarmListAdapter(this, alarmlist.getCategoryList(currentCategory), { position ->
            val cintent = Intent(this, MainActivity::class.java)

            val strData = GsonBuilder().create().toJson(alarmlist.get(position), Alarm_Data::class.java)

            cintent.putExtra("data", strData)
            cintent.putExtra("position", position)
            cintent.putExtra("categories", alarmlist.category())
            cintent.putExtra("isInit", false)

            //알람을 수정한다
            startActivityForResult(cintent, this.ALARM_SET)
        },
            { position -> onLongClickEvent()
            })

        return adapter
    }
}