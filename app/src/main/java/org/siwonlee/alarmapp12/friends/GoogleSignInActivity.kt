package org.siwonlee.alarmapp12.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.siwonlee.alarmapp12.R
import org.siwonlee.alarmapp12.UserData

class GoogleSignInActivity : Fragment() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")

    //서버에서 UserData를 읽어와 nowData에 저장할 변수
    var data: UserData? = null
    //앱 내부 저장소에서 UserData를 받아와 서버에 저장할 변수
    lateinit var nowData: UserData
    val ALARM_SET: Int = 1000

    var uidForOthers = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.google_sign_in_activity, container, false)
    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.google_sign_in_activity)

        val strList = intent.getStringExtra("list")!!
        nowData = GsonBuilder().create().fromJson(strList, UserData::class.java)

        //화면의 맨 위에 자신의 uid를 표시한다
        uidText.setText("uid: ${nowData.uid}")
        //자신의 uid를 클릭하면 uid를 클립보드에 복사한다
        uidText.setOnClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData: ClipData = ClipData.newPlainText("com.siwonlee.alarm12", nowData.uid)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this,"ID가 복사되었습니다.",Toast.LENGTH_SHORT).show()
        }


        //현재 UserData를 저장해 서버에 백업할 때 사용할 변수
        data = nowData

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //로그인
        signinButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        //로그아웃
        signoutButton.setOnClickListener {
            googleSignInClient.signOut()
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            nowData.uid = ""
            endActivity()
        }

        //백업하기
        setBackup.setOnClickListener {
            if(nowData.uid != "") {
                db.child(nowData.uid).child("").child("UserData").setValue(nowData).addOnSuccessListener {
                    Toast.makeText(this, "백업이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    endActivity()
                }
            }
        }

        //백업 가져오기
        getBackup.setOnClickListener {
            if(data != null) {
                nowData = data!!
                Toast.makeText(this, "백업을 가져왔습니다.", Toast.LENGTH_SHORT).show()
                endActivity()
            } else {
                Toast.makeText(this, "백업 데이터가 망가졌습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        //타인의 알람을 관리하는 함수
        setOtherAlarm.setOnClickListener {
            val otherAlarmDialog = AlertDialog.Builder(this)
            otherAlarmDialog.setTitle("타 사용자 알람 설정")

            val dialogView = layoutInflater.inflate(R.layout.other_alarm_manager, null)

            //현재 사용자가 알람을 관리할 수 있는 사용자를 고르게 할 Spinner
            val select = dialogView.findViewById<Spinner>(R.id.selectOther)
            //알람을 관리할 수 있는 사용자를 추가할 때 사용할 EditText
            val visible = dialogView.findViewById<LinearLayout>(R.id.otherUserInputLayout)
            val nameSet = dialogView.findViewById<EditText>(R.id.otherUserName)
            val uidSet = dialogView.findViewById<EditText>(R.id.otherUserUID)

            //현재 사용자가 관리할 수 있는 사용자의 집합
            val list: ArrayList<String> = ArrayList()
            if(nowData.uidMap != null)
                for(name in nowData.uidMap!!)
                    list.add(name.key)
            //새로운 사용자를 추가할 때 고를 item
            list.add("새 사용자 추가")

            //select의 adapter를 정의한다
            select.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                list
            )

            //select에서 아이템을 고를 때
            select.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                //어느 아이템이 선택되었다면
                override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long ) {
                    //해당 아이템이 "새 사용자 선택"일 경우
                    if(i == list.size - 1) {
                        //사용자 정보 입력 칸을 보이게 한 뒤 정보입력 칸을 비운다
                        visible.visibility = View.VISIBLE
                        nameSet.setText("")
                        uidSet.setText("")
                    }
                    //그렇지 않다면
                    else {
                        //사용자 정보 입력 칸을 안보이게 한 뒤 정보입력 칸을 선택된 정보로 채운다
                        visible.visibility = View.GONE
                        nameSet.setText(list[i])
                        uidSet.setText(nowData.uidMap!![list[i]])
                    }
                }

                //처음 상태에선 0번째 아이템을 고른 뒤, 해당 아이템이 "새 사용자 추가"라면 사용자 정보를 입력시킨다
                override fun onNothingSelected(adapterView: AdapterView<*>) {
                    select.setSelection(0)
                    if (list.size == 1) visible.visibility = View.VISIBLE
                    else visible.visibility = View.GONE
                }
            })

            //어느 사용자의 알람을 추가하거나 삭제할 경우
            otherAlarmDialog.setPositiveButton("알람 추가/수정") { _, _ ->
                //nowData와 Firebase에 해당 사용자 정보를 추가한 뒤
                nowData.uidMap!![nameSet.text.toString()] = uidSet.text.toString()
                db.child(nowData.uid).child("uidMap").child(nameSet.text.toString()).setValue(uidSet.text.toString())

                //알람을 추가할 사용자의 uid를 긁어와
                uidForOthers = uidSet.text.toString()
                if(uidForOthers != "") {
                    //해당 사용자에게 알람을 설정한다
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("categories", java.util.ArrayList<String>())

                    startActivityForResult(intent, this.ALARM_SET)
                }
            }

            //알람을 제거하고자 하는 경우
            otherAlarmDialog.setNeutralButton("알람 제거") { _, _ ->
                uidForOthers = uidSet.text.toString()
                //Firebase에서 해당 사용자에게 추가한 알람을 제거한다
                if(uidForOthers != "") db.child(uidForOthers).child("").child(nowData.uid).removeValue()
            }

            //어느 사용자를 관리 대상에서 삭제할 경우
            otherAlarmDialog.setNegativeButton("사용자 삭제") { _, _ ->
                uidForOthers = uidSet.text.toString()
                //해당 사용자의 알람을 먼저 제거한 뒤
                if(uidForOthers != "") db.child(uidForOthers).child("").child(nowData.uid).removeValue()
                if(nowData.uidMap != null) {
                    //nowData와 Firebase에서 해당 사용자에 대한 정보를 삭제한다
                    nowData.uidMap!!.remove(nameSet.text.toString())
                    db.child(nowData.uid).child("uidMap").child(nowData.uid).removeValue()
                }
            }

            otherAlarmDialog.setView(dialogView)
            otherAlarmDialog.create().show()
        }

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //로그인이 되어있고 백업을 했다면
                if (nowData.uid != "" && dataSnapshot.getValue() != null) {
                    //dataSnapshot을 이용해 db에서 자신의 정보를 HashMap으로 읽어온 뒤
                    val map: HashMap<String, Any>? =
                        (dataSnapshot.getValue() as HashMap<String, HashMap<String, Any>>)[nowData.uid]
                    //이렇게 긁어온 값이 null이 아니라면, 즉 이미 백업을 한 적이 있다면
                    if(map != null) {
                        //map의 UserData 항목을 가져와 해당 항목의 list, uidmap, markerSet을 가져온 뒤
                        val mapmap = map["UserData"] as HashMap<String, Any>
                        var list = mapmap["list"] as ArrayList<Alarm_Data>?
                        var uidMap = mapmap["uidMap"] as HashMap<String, String>?
                        var markerSet = mapmap["uidMap"] as Marker_Set?

                        //null 체크를 하고
                        if(list == null) list = ArrayList()
                        if(uidMap == null) uidMap = HashMap()
                        if(markerSet == null) markerSet = Marker_Set()

                        //data를 새로 할당한다
                        data = UserData(
                            uid = nowData.uid,
                            list = list,
                            uidMap = uidMap,
                            markerSet = markerSet
                        )

                        //타인이 설정한 알람을 받아와 data와 nowData에 저장한다
                        for(i in map) if(i.key != "UserData") {
                            val addData = Alarm_Data(i.value as HashMap<String, Any>)
                            data!!.add(addData)
                            nowData.add(addData)
                        }
                    }

                    //로그인이 되어있다면 Firebase와의 연결에 성공했으므로 토스트를 띄워 안전함을 알린다
                    Toast.makeText(this@GoogleSignInActivity, "서버와의 연결에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)

                //처리 과정이 지나치게 빠르면 Firebase에서 uid를 받아올 수 없다
                while(FirebaseAuth.getInstance().currentUser == null);

                //Firebasse에서 uid를 받아온다
                nowData.uid = FirebaseAuth.getInstance().currentUser!!.uid
                uidText.setText("uid: ${nowData.uid}")

                //로그인 직후에는 반드시 액티비티를 종료한다
                endActivity()
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == ALARM_SET) {
            val dbSetter = db.child(uidForOthers).child("")
            val strData = data!!.getStringExtra("data")
            val delete = data.getBooleanExtra("delete", false)

            //알람을 생성했다면 delete는 false이다
            if (!delete) {
                //alarmlist에 저장할 Alarm_Data 객체
                var data = Alarm_Data()
                if(strData != null)
                    data = GsonBuilder().create().fromJson(strData, Alarm_Data::class.java)

                //알람을 그대로 저장하는 것이 아니라, 알람의 개별 성분을 전부 뜯어내 Firebase에 저장한다
                dbSetter.child(nowData.uid).child("timeInMillis").setValue(data.timeInMillis)
                dbSetter.child(nowData.uid).child("phr").setValue(data.phr)
                dbSetter.child(nowData.uid).child("pmin").setValue(data.pmin)
                dbSetter.child(nowData.uid).child("switch").setValue(data.switch)
                dbSetter.child(nowData.uid).child("solver").setValue(data.solver)
                dbSetter.child(nowData.uid).child("category").setValue(data.category)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "앱이 구글 계정과 연동되었습니다.", Toast.LENGTH_SHORT).show()
                    //updateUI(user)
                } /*else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(main_layout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    updateUI(null)
                }*/
            }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    override fun onBackPressed() {
        endActivity()
        super.onBackPressed()
    }

    private fun endActivity() {
        val returnIntent = Intent(this, AlarmList_Activity::class.java)

        val strList = GsonBuilder().create().toJson(nowData, UserData::class.java)

        //백업한 데이터, 혹은 백업된 데이터를 returnIntent에 담는다
        returnIntent.putExtra("list", strList)

        //AlarmList_Acitivity에 RESULT_OK 신호와 함께 intent를 넘긴다
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }*/
}