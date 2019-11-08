package org.siwonlee.alarmapp12

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_list.*
import java.util.*
import kotlin.collections.ArrayList

class AlarmList_Activity : AppCompatActivity() {
    val REQUEST_SET : Int = 1000
    val REQUEST_CLICK : Int = 2000
    val alarmlist = ArrayList<Alarm_Data>()

    private val prefStorage = "org.siwonlee.alarmapp12.prefs"

    // AlarmlistAdapter의 ViewHolder
    val adapter = AlarmListAdapter(this, alarmlist, { position ->
        val cintent = Intent(this, MainActivity::class.java)
        cintent.putExtra("hr", alarmlist[position].hr)
        cintent.putExtra("min", alarmlist[position].min)

        cintent.putExtra("phr", alarmlist[position].phr)
        cintent.putExtra("pmin", alarmlist[position].pmin)

        cintent.putExtra("date", alarmlist[position].ringDate)
        cintent.putExtra("intSwitch", alarmlist[position].intSwitch)

        cintent.putExtra("solver", alarmlist[position].solver)

        cintent.putExtra("position", position)
        cintent.putExtra("isInit", false)

            //알람을 수정한다
        startActivityForResult(cintent, REQUEST_CLICK)
    }, {
            position -> onLongClickEvent()
    })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_list)

        val pref = this.getSharedPreferences(prefStorage, MODE_PRIVATE)
        val editor = pref!!.edit()
        //editor.clear() pref에 등록된 알람 데이터 삭제용

        //앱이 실행되어 AlarmList_Acitivity가 생성될 때마다 pref에 저장된 값 불러와서 alarmlist 생성
        for(dates in 1..127)  for (hr in 0 until 24)  for (min in 0 until 60) {
            val code = (dates * 100 + hr) * 100 + min
            if (pref.getString("time${code}", "no alarm") != "no alarm") {
                //알람이 삭제되어 ID 값이 비었을 경우엔 alarmlist에 데이터를 저장하지 않는다
                //but 리사이클러뷰에서 position값(위치)이 바뀌면 Alarm_Data의 ID 값도 바꾸는 방법이 좋을 듯합니다

                val time = pref.getString("time${code}", "no alarm")
                val date = pref.getString("date${code}", "no date")
                val phr = pref.getInt("phr${code}", 0)
                val pmin = pref.getInt("pmin${code}", 0)
                val solver = pref.getInt("solver${code}", 0)

                //ID 순서대로 alarmlist에 알람 추가
                alarmlist.add(Alarm_Data(hr, min, phr, pmin, time, date, dates, solver))
            }
        }

        editor.apply()

        // LinearLayoutManager : alarm_list.xml의 alarm_recyclerview에 세로형태로 아이템을 배치한다
        alarm_recyclerview.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        alarm_recyclerview.setHasFixedSize(true)
        alarm_recyclerview.adapter = adapter

        // 알람 초기 셋팅 : 알람 셋팅 화면으로 이동 (activity_main.xml로 이동)
        fab_add.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            //알람을 설정한다
            startActivityForResult(intent, REQUEST_SET)
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
            // MainActivity에서 보낼 수 있는 모든 정보를 모은다
            val hr = data!!.getIntExtra("hr", -1)
            val min = data.getIntExtra("min", -1)
            var intSwitch = data.getIntExtra("intSwitch", 0)

            val solver = data.getIntExtra("solver", 0)
            val phr = data.getIntExtra("phr", -1)
            val pmin = data.getIntExtra("pmin", -1)

            val time = data.getStringExtra("time")
            val date = data.getStringExtra("date")

            val before = data.getIntExtra("before_id", -1)
            var position = data.getIntExtra("position", -1)
            val delete = data.getBooleanExtra("delete", false)

            //알람을 수정하거나 삭제했다면 before_id는 -1이 아니다
            if(before != -1) {
                //알람을 삭제한 것이라면 alarmlist에서 알람을 제거한다
                if (delete) alarmlist.removeAt(position)

                //pref에 존재하는 옛날 정보를 제거한다
                editor.remove("time${before}")
                editor.remove("date${before}")
                editor.remove("phr${before}")
                editor.remove("pmin${before}")
                editor.remove("solver${before}")
            }

            //알람을 수정 혹은 생성했다면 delete는 false이다
            if (!delete) {
                //알람을 생성했다면 position은 반드시 -1이다
                if (position == -1) {
                    position = alarmlist.size
                    alarmlist.add(Alarm_Data(hr, min, phr, pmin, time, date, intSwitch, solver))
                }
                //그렇지 않다면 기존에 존재하던 알람을 수정한다
                else alarmlist[position] = Alarm_Data(hr, min, phr, pmin, time, date, intSwitch, solver)

                val ID = (intSwitch * 100 + hr) * 100 + min

                // 설정/수정한 알람 정보를 pref에 추가한다
                editor.putString("time${ID}", time)
                editor.putString("date${ID}", date)
                editor.putInt("phr${ID}", phr)
                editor.putInt("pmin${ID}", pmin)
                editor.putInt("solver${ID}", solver)

                for(day in 7 downTo 1) {
                    if(intSwitch % 2 == 1) setAlarm(day, position, true)
                    intSwitch /= 2
                }
            }

            //수정한 정보를 pref에 commit한다
            editor.apply()
        }
        // 바뀐 alarmlist 때문에 adapter 갱신
        alarm_recyclerview.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.alarm_list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.signin -> {
                val logInIntent = Intent(this, GoogleSignInActivity::class.java)
                startActivity(logInIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setAlarm(day: Int, position: Int, set: Boolean) {
        //정보를 this에서 receiver까지 보내는 intent를 생성
        val intent = Intent(this, Alarm_Receiver::class.java)

        //alarmlist에서 알람 설정에 필요한 정보를 가져온다
        val hr = alarmlist[position].hr
        val min = alarmlist[position].min
        val phr = alarmlist[position].phr
        val pmin = alarmlist[position].pmin
        val solver = alarmlist[position].solver

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

            cal.add(Calendar.HOUR_OF_DAY, -phr)
            cal.add(Calendar.MILLISECOND, -pmin)

            //오늘부터 day 요일까지 남은 일 수
            val diff = (day - cal.get(Calendar.DAY_OF_WEEK) + 7) % 7

            //알람이 울리는 요일이 i 요일이 되도록 cal을 조정한다
            cal.add(Calendar.DATE, diff)
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
}
