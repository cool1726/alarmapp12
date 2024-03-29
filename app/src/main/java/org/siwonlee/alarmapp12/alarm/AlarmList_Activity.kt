package org.siwonlee.alarmapp12.alarm

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import java.util.*
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import kotlinx.android.synthetic.main.alarm_list.*
import org.siwonlee.alarmapp12.*
import org.siwonlee.alarmapp12.friends.GoogleSignInActivity
import org.siwonlee.alarmapp12.information.GetWeatherInfo
import org.siwonlee.alarmapp12.map.Alarm_Map
import org.siwonlee.alarmapp12.map.Marker_Set

class AlarmList_Activity : Fragment() {
    val ALARM_SET: Int = 1000
    val FIREBASE_MANAGE: Int = 2000
    val MAP_ALARM: Int = 3000
    val WEATHER_INFO: Int = 9999

    lateinit var alarmlist: UserData

    private val prefStorage = "org.siwonlee.alarmapp12.prefs"
    private var currentCategory = "전체 카테고리"

    var categorize: ArrayList<String> = ArrayList()

    companion object {
        fun newInstance() = AlarmList_Activity()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.alarm_list, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.alarm_recyclerview)

        val pref = activity?.getSharedPreferences(prefStorage, MODE_PRIVATE)
        val editor = pref!!.edit()
        //editor.clear() pref에 등록된 알람 데이터 삭제용

        //pref에 저장된 string타입 UserData 클래스를 decode해 alarmlist에 저장
        val strList = pref.getString("list", "")
        if(strList != "")
            alarmlist = GsonBuilder().create().fromJson(strList, UserData::class.java)
        else alarmlist = UserData()
        editor.apply()

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
                recyclerView.adapter = makeAdapter()
            }
            override fun onNothingSelected(adapterView: AdapterView<*>)  {
                sp_category.setSelection(0)
            }
        }

        val mapIntent = Intent(activity, Alarm_Map::class.java)
        val strSet : String = GsonBuilder().create().toJson(alarmlist.markerSet, Marker_Set::class.java)
        mapIntent.putExtra("set", strSet)
        if(mapIntent != null) {
            Log.d("mapintent", strSet)
        }

        // LinearLayoutManager : alarm_list.xml의 alarm_recyclerview에 세로형태로 아이템을 배치한다
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = makeAdapter()

        // 알람 초기 셋팅 : 알람 셋팅 화면으로 이동 (activity_main.xml로 이동)
        fab_add.setOnClickListener{
            val intent = Intent(activity, Alarm_Set::class.java)
            intent.putExtra("categories", alarmlist.category())
            //알람을 설정한다
            startActivityForResult(intent, this.ALARM_SET)
        }
    }

    // item을 오래 클릭할 시 바로 알람 삭제여부를 묻는 dialog 띄우기 (나중에)
    fun onLongClickEvent() { }

    // 특정 REQUEST_CODE로 Alarm_set.kt에 결과값을 요구하고 결과값 받기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val pref = activity?.getSharedPreferences(prefStorage, MODE_PRIVATE)
        val editor = pref!!.edit()

        if (resultCode == Activity.RESULT_OK) { // Alarm_Set에서 RESULT_OK 사인을 보내면
            when (requestCode) {
                //알람을 설정, 수정, 혹은 삭제했을 경우
                ALARM_SET -> {
                    // Alarm_set에서 보낼 수 있는 모든 정보를 모은다
                    val strData = data!!.getStringExtra("data")
                    val before = data.getStringExtra("before")

                    //알람을 수정하거나 삭제했다면 before_id는 -1이 아니다
                    if(before != null) {
                        val popData = GsonBuilder().create().fromJson(before, Alarm_Data::class.java)
                        alarmlist.unset(activity!!.applicationContext)
                        //alarmlist에서 알람을 제거한다
                        alarmlist.pop(popData)
                    }

                    //알람을 수정 혹은 생성했다면 delete는 false이다
                    if (strData != null) {
                        //alarmlist에 저장할 Alarm_Data 객체
                        val data = GsonBuilder().create().fromJson(strData, Alarm_Data::class.java)
                        //alarmlist에 data를 추가한 뒤 알람을 갱신한다
                        alarmlist.add(data)
                        alarmlist.set(activity!!.applicationContext)
                    }

                    //리스트에 변경이 있었으므로 알람을 정렬한다
                    alarmlist.sort()
                }

                //Firebase에 접근했을 경우
                FIREBASE_MANAGE -> {
                    //GoogleSignUpActivity에서 반환한 list를 fetchUserData에 담아
                    val fetchUserData = data!!.getStringExtra("list")

                    //해당 데이터가 "", 즉 이상한 데이터가 아니라면
                    if(fetchUserData != "") {
                        //기존에 존재하는 모든 알람을 해제한 뒤
                        alarmlist.unset(activity!!.applicationContext)
                        //백업한 데이터를 가져와 alarmlist에 저장하고
                        alarmlist = GsonBuilder().create().fromJson(fetchUserData, UserData::class.java)
                        //백업한 리스트의 모든 알람을 set한 다음 정렬한다
                        alarmlist.set(activity!!.applicationContext)
                        alarmlist.sort()
                    }
                }

                MAP_ALARM -> {
                    val strSet = data!!.getStringExtra("set")!!
                    alarmlist.markerSet = GsonBuilder().create().fromJson(strSet, Marker_Set::class.java)
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

    /*//로그인 등의 메뉴를 앱의 메뉴바에 표시
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
                val logInIntent = Intent(activity, GoogleSignInActivity::class.java)

                //alarmlist를 gson을 이용해 String타입으로 형변환해 logInIntent에 넣는다
                val strList = GsonBuilder().create().toJson(alarmlist, UserData::class.java)
                logInIntent.putExtra("list", strList)

                //logInIntent.putExtra()
                startActivityForResult(logInIntent, FIREBASE_MANAGE)
            }
            //날씨 정보 버튼을 눌렀을 때
            R.id.weather -> {
                //GetWeatherInfo를 실행시킬 intent를 만들고
                val weatherIntent = Intent(activity, GetWeatherInfo::class.java)
                //현재 위치정보를 intent로 전달하는 부분 필요함
                startActivityForResult(weatherIntent, WEATHER_INFO)
            }
            R.id.mapAlarm -> {
                val mapIntent = Intent(activity, Alarm_Map::class.java)
                val strSet = GsonBuilder().create().toJson(alarmlist.markerSet, Marker_Set::class.java)
                mapIntent.putExtra("set", strSet)
                //알람을 설정한다
                startActivityForResult(mapIntent, this.MAP_ALARM)
            }
        }
        return super.onOptionsItemSelected(item)
    }*/

    fun makeCategory() {
        categorize = alarmlist.category()
        categorize.add("전체 카테고리")

        val ret = ArrayAdapter(
            activity!!.applicationContext,
            android.R.layout.simple_spinner_dropdown_item,
            categorize
        )

        sp_category.adapter = ret
    }

    fun makeAdapter(): AlarmListAdapter {
        // AlarmlistAdapter의 ViewHolder
        val smallList = alarmlist.getCategoryList(currentCategory)
        // 선택된 category에 해당하는 alarmlist를 전달하기 위해 getCategoryList 함수 이용
        val adapter = AlarmListAdapter(activity!!.applicationContext, smallList, { position ->
            val cintent = Intent(activity, Alarm_Set::class.java)

            var newPos = 0

            for(i in 0 until alarmlist.size()) {
                if(alarmlist.get(i).isEqual(smallList[position])) {
                    newPos = i
                    break
                }
            }

            val strData = GsonBuilder().create().toJson(alarmlist.get(newPos), Alarm_Data::class.java)

            cintent.putExtra("data", strData)
            cintent.putExtra("categories", alarmlist.category())
            cintent.putExtra("isInit", false)

            //알람을 수정한다
            startActivityForResult(cintent, this.ALARM_SET)
        },
            { position ->
                if( alarmlist.onoff(position) ) {
                    alarmlist.set1(activity!!.applicationContext, position)
                    //Log.d("switch onoff", "${position} switch on" )
                }
                else {
                    alarmlist.unset1(activity!!.applicationContext, position)
                    //Log.d("switch onoff", "${position} switch off" )
                }
                alarmlist.sort()

                val pref = activity?.getSharedPreferences(prefStorage, MODE_PRIVATE)
                val editor = pref!!.edit()
                //alarmlist를 gson을 이용해 String타입으로 형변환한다
                val strList = GsonBuilder().create().toJson(alarmlist, UserData::class.java)
                //형변환한 data를 pref에 저장한다
                editor.putString("list", strList)

                //수정한 정보를 pref에 commit한다
                editor.apply()

            })

        return adapter
    }
}