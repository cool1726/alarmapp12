package org.siwonlee.alarmapp12

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import android.os.AsyncTask.execute
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import java.lang.reflect.Array.getLength
import android.widget.Toast
import android.os.AsyncTask
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory


class GetWeatherInfo : AppCompatActivity() {

    var formatter = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
    var date = Date()
    var weatherDate = formatter.format(date)

    var currentTime = Calendar.getInstance().getTime()
    var ntime: Int = Integer.parseInt(SimpleDateFormat("HHmm").format(currentTime))     //현재시각
    var weatherTime: String = ""    //날씨정보 가져올 특정시간

    // 서울 좌표 당일 05시 00분 측정 날씨정보 가져오기 (base_date, base_time, nx, ny)
    // 0210, 0510, 0810, 1110, 1410, 1710, 2010, 2310 날씨업데이트

    var weather_url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_parse)

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

        weather_url = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastSpaceData?serviceKey=" +
                    "4N926ah8IlK9Xzvu1O%2FlEZEbWJHqRQI0VgrqT6vR54oG0ADd%2B8MyeYhQGIvGvGLXc%2F%2F%2BzWpoWAuyKP8PLh87xw%3D%3D" +
                    "&base_date=" + weatherDate + "&base_time=" + weatherTime + "00&nx=60&ny=127&numOfRows=10&pageNo=1&_type=xml"

        GetXMLTask().execute()
        Toast.makeText(this, "${weatherTime}", Toast.LENGTH_LONG).show()
    }

    private inner class GetXMLTask() : AsyncTask<String, Void, Document>() {
        var txtweather: TextView = findViewById(R.id.txt_weather)

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

            s += "현재 위치 : nx=60, ny=127 " + " \n"
            s += "시간 : ${weatherDate} ${weatherTime}" + " \n"

            for (i in 0 until nodeList.getLength()) {
                val node = nodeList.item(i)
                val fstElmnt = node as Element
                val idx = fstElmnt.getElementsByTagName("category")

                val value = fstElmnt.getElementsByTagName("fcstValue")

                // 강수확률% POP
                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("POP"))
                    s += "강수확률 = " + value.item(0).getChildNodes().item(0).getNodeValue() + "% \n"

                // 강수형태 PTY : 없음(0), 비(1), 진눈깨비(2), 눈(3), 소나기(4)
                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("PTY")) {
                    val rain = Integer.parseInt(value.item(0).getChildNodes().item(0).getNodeValue())
                    when(rain) {
                        0 -> {s += "강수상태 = 비 안 옴" + " \n"}
                        1 -> {s += "강수상태 = 비" + "% \n"}
                        2 -> {s += "강수상태 = 진눈깨비" + "% \n"}
                        3 -> {s += "강수상태 = 눈" + "% \n"}
                        4 -> {s += "강수상태 = 소나기" + "% \n"}
                    }
                }

                // 습도% REH
                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("REH"))
                    s += "습도 = " + value.item(0).getChildNodes().item(0).getNodeValue() + "% \n"

                // 기온ºC T3H
                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("T3H"))
                    s += "온도 = " + value.item(0).getChildNodes().item(0).getNodeValue() + "'C \n"

                // 구름상태 SKY : 맑음(1),  구름많음(3),  흐림(4)
                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("SKY")) {
                    val cloud_num =
                        Integer.parseInt(value.item(0).getChildNodes().item(0).getNodeValue())

                    if (cloud_num == 1) s += "하늘상태 = 맑음\n"
                    else if (cloud_num == 3) s += "하늘상태 = 구름 많음\n"
                    else if (cloud_num == 4) s += "하늘상태 = 흐림\n"
                }
                txtweather.setText(s)

                super.onPostExecute(doc)
            }

        }

    }
}