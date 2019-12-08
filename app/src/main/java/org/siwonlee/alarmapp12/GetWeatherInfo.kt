package org.siwonlee.alarmapp12

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.IOException
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory


class GetWeatherInfo : AppCompatActivity() {

    var formatter = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
    var date = Date()
    var weatherDate = formatter.format(date)

    var currentTime = Calendar.getInstance().getTime()
    var ntime: Int = Integer.parseInt(SimpleDateFormat("HHmm").format(currentTime))     //현재시각
    var weatherTime: String = ""    //날씨정보 가져올 특정시간

    // 당일 05시 00분 측정 날씨정보 가져오기 (base_date, base_time, nx, ny)
    // 0210, 0510, 0810, 1110, 1410, 1710, 2010, 2310 날씨업데이트
    var weather_url = ""

    // 현재 내 위치
    var lm : LocationManager? = null
    private val REQUEST_CODE_LOCATION : Int = 2
    var currentLocation : String = ""
    var dlatitude : Double = 0.0
    var dlongitude : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.weather_parse)
        setContentView(R.layout.weather_after_ringing)

        if(ntime < 210) {
            weatherDate = (weatherDate.toInt() - 1).toString()
            weatherTime = "2300"  }
        else if (ntime < 510) weatherTime = "0200"
        else if (ntime < 810) weatherTime = "0500"
        else if (ntime < 1110) weatherTime = "0800"
        else if (ntime < 1410) weatherTime = "1100"
        else if (ntime < 1710) weatherTime = "1400"
        else if (ntime < 2010) weatherTime = "1700"
        else if (ntime < 2310) weatherTime = "2000"
        else if (ntime < 2400) weatherTime = "2300"

        lateinit var mFusedLocationClient: FusedLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //locationManager 이용한 현재 내 위치 찾기
        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        mFusedLocationClient.lastLocation.addOnSuccessListener {
            if(it != null) {
                val myLocation = LatLng(it.latitude, it.longitude)
                Log.d("myLocation", "myLocation : ${myLocation}")
                dlatitude = it.latitude
                dlongitude = it.longitude

                Log.d("CheckCurrentLocation", "현재 내 위치 값: ${dlatitude}, ${dlongitude}")
            }
        }

        /*val mGeocoder = Geocoder(applicationContext, Locale.KOREAN)
        var mResultList: List<Address>? = null
        try {
            mResultList = mGeocoder.getFromLocation(
                dlatitude, dlongitude, 1
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (mResultList != null) {
            //Log.d("CheckCurrentLocation", mResultList[0].getAddressLine(0))
            //currentLocation = mResultList[0].getAddressLine(0)
            //currentLocation = currentLocation.substring(5)  //'대한민국' 글자 빼기 위함

        }*/

        val tmp = convertGRID_GPS(dlatitude, dlongitude)
        Log.d("위경도 -> 격자", "x = " + tmp.x + ", y = " + tmp.y)

        val strLocate = findViewById<TextView>(R.id.location)
        strLocate.setText("${tmp.x.toInt()}, ${tmp.y.toInt()}")


        weather_url = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastSpaceData?serviceKey=" +
                "4N926ah8IlK9Xzvu1O%2FlEZEbWJHqRQI0VgrqT6vR54oG0ADd%2B8MyeYhQGIvGvGLXc%2F%2F%2BzWpoWAuyKP8PLh87xw%3D%3D" +
                "&base_date=" + weatherDate + "&base_time=" + weatherTime +
                "00&nx=" + tmp.x.toInt() + "&ny=" + tmp.y.toInt() + "&numOfRows=10&pageNo=1&_type=xml"

        GetXMLTask().execute()
        Toast.makeText(this, "${weatherTime}", Toast.LENGTH_LONG).show()
    }

    private inner class GetXMLTask() : AsyncTask<String, Void, Document>() {
        var txt_weather: TextView = findViewById(R.id.txt_weather)
        var weatherImage = findViewById<ImageView>(R.id.weatherImage)
        var rainAmount = findViewById<TextView>(R.id.rainamount)
        var temperature = findViewById<TextView>(R.id.temperature)
        var updateT = findViewById<TextView>(R.id.updateTime)

        override fun doInBackground(vararg urls: String): Document? {
            val url: URL
            var doc: Document? = null
            try {
                url = URL(weather_url)
                val dbf = DocumentBuilderFactory.newInstance()
                val db = dbf.newDocumentBuilder()
                doc = db.parse(InputSource(url.openStream()))
                doc!!.getDocumentElement().normalize()
            } catch (e: Exception) {
                Toast.makeText(baseContext, "Parsing Error", Toast.LENGTH_SHORT).show()
            }
            return doc
        }
        override fun onPostExecute(doc: Document) {
            var s = ""
            val nodeList = doc.getElementsByTagName("item")

            s += "위치 : x : ${dlatitude}, y : ${dlongitude}" + "\n"
            s += "시간 : ${weatherDate} ${weatherTime}" + " \n"
            updateT.setText("${weatherTime.substring(0, 2)}시 ${weatherTime.substring(2, 4)}분 발표")

            for (i in 0 until nodeList.getLength()) {
                val node = nodeList.item(i)
                val fstElmnt = node as Element
                val idx = fstElmnt.getElementsByTagName("category")

                val item = fstElmnt.getElementsByTagName("fcstValue").item(0).childNodes.item(0).nodeValue
                val feature = idx.item(0).childNodes.item(0).nodeValue


                // 최저기온
                if (feature.equals("TMN")) {
                    s += "최저기온 = " + item + "℃ \n"
                }

                // 최고기온
                if (feature.equals("TMX")) {
                    s += "최고기온 = " + item + "℃ \n"
                }

                // 강수확률% POP
                if (feature.equals("POP"))
                    s += "강수확률 = " + item + "% \n"

                // 강수형태 PTY : 없음(0), 비(1), 진눈깨비(2), 눈(3), 소나기(4)
                if (feature.equals("PTY")) {
                    val rain = Integer.parseInt(item)
                    when(rain) {
                        0 -> {s += "강수상태 = 비 안 옴" + " \n"}
                        1 -> {
                            s += "강수상태 = 비" + " \n"
                            weatherImage.setImageResource(R.drawable.rainy) }
                        2 -> {
                            s += "강수상태 = 진눈깨비" + " \n"
                            weatherImage.setImageResource(R.drawable.rainy) }
                        3 -> {
                            s += "강수상태 = 눈" + " \n"
                            weatherImage.setImageResource(R.drawable.snowy) }
                        4 -> {
                            s += "강수상태 = 소나기" + " \n"
                            weatherImage.setImageResource(R.drawable.rainy) }
                    }
                }

                // 강수량
                if (feature.equals("R06")) {
                    if(Integer.parseInt(item) > 0) {
                        s += "강수량 = " + item + "mm \n"
                        rainAmount.setText("${item}mm의 비가 예상됩니다")
                    }
                }

                // 적설량
                if (feature.equals("S06")) {
                    if (Integer.parseInt(item) > 0) {
                        s += "적설량 = " + item + "mm \n"
                        rainAmount.setText("${item}cm의 눈이 예상됩니다")
                    }
                }

                // 습도% REH
                if (feature.equals("REH")) s += "습도 = " + item + "% \n"

                // 기온℃ T3H
                if (feature.equals("T3H")) {
                    s += "온도 = " + item + "℃ \n"
                    temperature.setText(item)
                    Log.d("temperature", "temperature : ${item}")
                }

                // 구름상태 SKY : 맑음(1),  구름많음(3),  흐림(4)
                if (feature.equals("SKY")) {
                    val cloud_num =
                        Integer.parseInt(item)

                    if (cloud_num == 1) {
                        s += "하늘상태 = 맑음\n"
                        weatherImage.setImageResource(R.drawable.sunny)
                    }
                    else if (cloud_num == 3) s += "하늘상태 = 구름 많음\n"
                    else if (cloud_num == 4) s += "하늘상태 = 흐림\n"
                }
                txt_weather.setText(s)

                super.onPostExecute(doc)
            }

        }

    }

    private fun convertGRID_GPS(lat_X: Double, lng_Y: Double): LatXLngY {
        val re = 6371.00877 // 지구 반경(km)
        val grid = 5.0 // 격자 간격(km)
        val slat1 = 30.0 // 투영 위도1(degree)
        val slat2 = 60.0 // 투영 위도2(degree)
        val olon = 126.0 // 기준점 경도(degree)
        val olat = 38.0 // 기준점 위도(degree)
        val xo = 43.0 // 기준점 X좌표(GRID)
        val yo = 136.0 // 기준점 Y좌표(GRID)

        // LCC DFS 좌표변환 (lat_X:위도,  lng_Y:경도)

        val degrad = Math.PI / 180.0

        val re_grid = re / grid
        val slat1_degrad = slat1 * degrad
        val slat2_degrad = slat2 * degrad
        val olon_degrad = olon * degrad
        val olat_degrad = olat * degrad

        var sn = Math.tan(Math.PI * 0.25 + slat2_degrad * 0.5) / Math.tan(Math.PI * 0.25 + slat1_degrad * 0.5)
        sn = Math.log(Math.cos(slat1_degrad) / Math.cos(slat2_degrad)) / Math.log(sn)
        var sf = Math.tan(Math.PI * 0.25 + slat1_degrad * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1_degrad) / sn
        var ro = Math.tan(Math.PI * 0.25 + olat_degrad * 0.5)
        ro = re_grid * sf / Math.pow(ro, sn)
        val rs = LatXLngY()


        rs.lat = lat_X
        rs.lng = lng_Y
        var ra = Math.tan(Math.PI * 0.25 + lat_X * degrad * 0.5)
        ra = re_grid * sf / Math.pow(ra, sn)
        var theta = lng_Y * degrad - olon_degrad
        if (theta > Math.PI) theta -= 2.0 * Math.PI
        if (theta < -Math.PI) theta += 2.0 * Math.PI
        theta *= sn
        rs.x = Math.floor(ra * Math.sin(theta) + xo + 0.5)
        rs.y = Math.floor(ro - ra * Math.cos(theta) + yo + 0.5)

        return rs
    }


    internal inner class LatXLngY {
        var lat: Double = 0.toDouble()
        var lng: Double = 0.toDouble()

        var x: Double = 0.toDouble()
        var y: Double = 0.toDouble()
    }

}