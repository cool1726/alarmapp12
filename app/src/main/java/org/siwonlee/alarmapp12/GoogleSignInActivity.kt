package org.siwonlee.alarmapp12

import android.app.Activity
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
import kotlinx.android.synthetic.main.google_sign_in_activity.*

class GoogleSignInActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")

    var uid: String = ""
    //서버에서 UserData를 읽어와 nowData에 저장할 변수
    var data: String? = null
    //앱 내부 저장소에서 UserData를 받아와 서버에 저장할 변수
    lateinit var nowData: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.google_sign_in_activity)

        uid = intent.getStringExtra("uid")!!
        nowData = intent.getStringExtra("list")!!

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

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (uid != "") {
                    val hashMap: HashMap<String, String> = dataSnapshot.getValue() as HashMap<String, String>
                    val mapToString = hashMap.get(uid)
                    data = mapToString
                    //data = dataSnapshot.getValue(String::class.java)
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

                val uidTemp = auth.uid
                if(uidTemp != null) uid = uidTemp

                //로그인 직후에는 반드시 액티비티를 종료한다
                endActivity()
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
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

    fun endActivity() {
        val returnIntent = Intent(this, AlarmList_Activity::class.java)

        //백업한 데이터, 혹은 백업된 데이터를 returnIntent에 담는다
        returnIntent.putExtra("list", nowData)
        returnIntent.putExtra("uid", uid)

        //AlarmList_Acitivity에 RESULT_OK 신호와 함께 intent를 넘긴다
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}