package org.siwonlee.alarmapp12.map

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import org.siwonlee.alarmapp12.R
import org.siwonlee.alarmapp12.UserData
import org.siwonlee.alarmapp12.alarm.Alarm_Receiver

class Alarm_Map : Fragment(), OnMapReadyCallback {
    lateinit var mMap: GoogleMap
    var markerSet = Marker_Set()
    lateinit var loc: LocationManager

    private val prefStorage = "org.siwonlee.alarmapp12.prefs"

    var myPos: Marker? = null
    val REQUEST_ACCESS_FINE_LOCATION = 1

    lateinit var alarmlist: UserData

    lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.alarm_map, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // val strSet = {"markerList":[]}
        //val strSet = activity!!.intent.getStringExtra("set")
        val pref = activity!!.getSharedPreferences(prefStorage, Context.MODE_PRIVATE)
        val strList = pref.getString("list", "")
        if(strList != "")
            alarmlist = GsonBuilder().create().fromJson(strList, UserData::class.java)
        else alarmlist = UserData()

        val strSet : String = GsonBuilder().create().toJson(alarmlist.markerSet, Marker_Set::class.java)

        Log.d("strSet", strSet)
        if(strSet != "")
            markerSet = GsonBuilder().create().fromJson(strSet, Marker_Set::class.java)

        // OS가 Marshmallow 이상일 경우 권한체크를 해야 합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionCheck = activity!!.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)

            // 권한 없음
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_ACCESS_FINE_LOCATION
                )
            }
        }

        val mapFragment = getChildFragmentManager().findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // grantResults[0] 거부 -> -1
        // grantResults[0] 허용 -> 0 (PackageManager.PERMISSION_GRANTED)
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) { // ACCESS_FINE_LOCATION 에 대한 권한 거부.
            activity!!.finish()
        }
    }

    //지도 사용이 가능해진 경우
    override fun onMapReady(googleMap: GoogleMap) {
        //mMap에 googleMap을 할당한 뒤
        mMap = googleMap
        //LocationManager를 정의한다
        loc = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //기존에 저장된 좌표에 대해 마커를 표시해준 뒤
        for(coord in markerSet.markerList) {
            try {
                val markerOptions: MarkerOptions = MarkerOptions().position(LatLng(coord.lat, coord.lon))
                mMap.addMarker(markerOptions)
            } catch(e: SecurityException) {
                break
            }
        }

        //앱 실행 직후의 사용자의 위치를 지도에 표시한다
        mFusedLocationClient.lastLocation.addOnSuccessListener {
            if(it != null) {
                if(myPos != null) myPos!!.remove()
                val myLocation = LatLng(it.latitude, it.longitude)
                myPos = mMap.addMarker(MarkerOptions().position(myLocation).title("Current Location"))
                myPos!!.showInfoWindow()
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17.toFloat()))
            }
        }

        mMap.setOnMapClickListener{ latLng ->
            val requestCode = (latLng.latitude * 10).toInt() * 1000 + (latLng.longitude * 10).toInt()

            val intent = Intent(activity, Alarm_Receiver::class.java)
            intent.putExtra("hr", 0)
            intent.putExtra("min", 0)
            intent.putExtra("requestCode", 0)
            intent.putExtra("solver", 0)
            intent.putExtra("sound", "")
            val pIntent = PendingIntent.getBroadcast(
                activity, requestCode,
                intent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            val dialog = AlertDialog.Builder(activity!!).setTitle("알람 정보를 입력하세요")

            val dialogView = layoutInflater.inflate(R.layout.map_alarm_setting, null)
            val name = dialogView.findViewById<EditText>(R.id.mapSettingName)
            val range = dialogView.findViewById<EditText>(R.id.mapSettingRange)

            dialog.setPositiveButton("설정") { _, _ ->
                val markerOptions: MarkerOptions = MarkerOptions().position(latLng).title(name.text.toString())
                if(markerOptions.title == "") markerOptions.title("새 장소 알람")

                var mapRange = 10
                if(range.text.toString() != "")
                    mapRange = range.text.toString().toInt()

                try {
                    loc.addProximityAlert(latLng.latitude, latLng.longitude, mapRange.toFloat(), -1, pIntent)
                    Toast.makeText(activity, "위치 알람을 설정했습니다.", Toast.LENGTH_SHORT).show()
                } catch(e: SecurityException) {
                    Toast.makeText(activity, "권한 에러: 위치 사용 권한을 취득하지 못했습니다.", Toast.LENGTH_SHORT).show()
                }

                markerSet.add(mMap.addMarker(markerOptions))
            }
            dialog.setNegativeButton("취소") { _, _ ->  }
            dialog.setView(dialogView)
            dialog.create().show()

            if(myPos != null) myPos!!.showInfoWindow()
        }

        mMap.setOnMarkerClickListener {
            if(it.title != "Current Location") onMarkerClick(it)

            true
        }
    }

    fun onMarkerClick(it: Marker) {
        val requestCode = (it.position.latitude * 10).toInt() * 1000 + (it.position.longitude * 10).toInt()

        val intent = Intent(activity, Alarm_Receiver::class.java)
        val pIntent = PendingIntent.getBroadcast(
            activity, requestCode,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            loc.removeProximityAlert(pIntent)

            markerSet.pop(it)
            it.remove()

            if(myPos != null) myPos!!.showInfoWindow()

            Toast.makeText(activity, "위치 알람을 제거했습니다.", Toast.LENGTH_SHORT).show()
        } catch(e: SecurityException) {
            Toast.makeText(activity, "권한 에러: 위치 사용 권한을 취득하지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    /*override fun onBackPressed() {
        //AlarmList_Activity에 정보를 넘길 intent
        val returnIntent = Intent()

        //설정한 알람 정보를 AlarmList_Acitivity로 넘긴다
        val strSet = GsonBuilder().create().toJson(markerSet, Marker_Set::class.java)
        returnIntent.putExtra("set", strSet)

        //AlarmList_Acitivity에 RESULT_OK 신호와 함께 intent를 넘긴다
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
        super.onBackPressed()
    }*/

    override fun onDestroy() {
        //onBackPressed()
        super.onDestroy()
    }
}