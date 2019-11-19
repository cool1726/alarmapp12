package org.siwonlee.alarmapp12

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.google_sign_in_activity.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.widget.EditText

class GoogleSignInActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")

    var uid: String = ""
    //서버에서 UserData를 읽어와 nowData에 저장할 변수
    var data: UserData? = null
    //앱 내부 저장소에서 UserData를 받아와 서버에 저장할 변수
    lateinit var nowData: UserData
    val ALARM_SET: Int = 1000

    var uidForOthers = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.google_sign_in_activity)

        uid = intent.getStringExtra("uid")!!
        uidText.setText("uid: $uid")

        val strList = intent.getStringExtra("list")!!
        nowData = GsonBuilder().create().fromJson(strList, UserData::class.java)

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
            uid = ""
            endActivity()
        }

        //백업하기
        setBackup.setOnClickListener {
            if(uid != "") {
                db.child(uid).child("").setValue(nowData).addOnSuccessListener {
                    Toast.makeText(this, "백업이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    endActivity()
                }
            }
        }

        getBackup.setOnClickListener {
            if(data != null) {
                nowData = data!!
                Toast.makeText(this, "백업을 가져왔습니다.", Toast.LENGTH_SHORT).show()
                endActivity()
            } else {
                Toast.makeText(this, "백업 데이터가 망가졌습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        setOtherAlarm.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("알람을 설정할 상대의 uid: ")

            val edit = EditText(this)
            dialog.setView(edit)

            dialog.setPositiveButton("확인") { _, _ ->
                uidForOthers = edit.text.toString()
                if(uidForOthers != "") {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("categories", java.util.ArrayList<String>())

                    startActivityForResult(intent, this.ALARM_SET)
                }
            }
            dialog.setNegativeButton("취소") { _, _ -> /* 취소일 때 아무 액션이 없으므로 빈칸 */ }
            dialog.create().show()
        }

        removeOtherAlarm.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("알람을 제거할 상대의 uid: ")

            val edit = EditText(this)
            dialog.setView(edit)

            dialog.setPositiveButton("확인") { _, _ ->
                uidForOthers = edit.text.toString()
                if(uidForOthers != "") {
                    val dbSetter = db.child(uidForOthers).child("").child(uid)
                    dbSetter.removeValue()
                }
            }
            dialog.setNegativeButton("취소") { _, _ -> /* 취소일 때 아무 액션이 없으므로 빈칸 */ }
            dialog.create().show()
        }

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (uid != "") {
                    val map: HashMap<String, HashMap<String, Object>>? = dataSnapshot.getValue() as HashMap<String, HashMap<String, Object>>?
                    if(map != null) {
                        data = UserData(map!![uid]!!["list"] as ArrayList<Alarm_Data>)
                        for(i in map!![uid]!!) if(i.key != "list")
                            data!!.add(Alarm_Data(i.value as HashMap<String, Any>))
                    }
                }

                Toast.makeText(this@GoogleSignInActivity, "Firebase와의 재연결에 성공하였습니다.", Toast.LENGTH_SHORT).show()
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
                uid = FirebaseAuth.getInstance().currentUser!!.uid
                uidText.setText("uid: $uid")

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

                dbSetter.child(uid).child("hr").setValue(data.hr)
                dbSetter.child(uid).child("min").setValue(data.min)
                dbSetter.child(uid).child("phr").setValue(data.phr)
                dbSetter.child(uid).child("pmin").setValue(data.pmin)
                dbSetter.child(uid).child("switch").setValue(data.switch)
                dbSetter.child(uid).child("solver").setValue(data.solver)
                dbSetter.child(uid).child("category").setValue(data.category)
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

    private fun endActivity() {
        val returnIntent = Intent(this, AlarmList_Activity::class.java)

        val strList = GsonBuilder().create().toJson(nowData, UserData::class.java)

        //백업한 데이터, 혹은 백업된 데이터를 returnIntent에 담는다
        returnIntent.putExtra("list", strList)
        returnIntent.putExtra("uid", uid)

        //AlarmList_Acitivity에 RESULT_OK 신호와 함께 intent를 넘긴다
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}