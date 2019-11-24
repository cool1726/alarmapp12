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

    // 서울 좌표 당일 05시 00분 측정 날씨정보 가져오기 (base_date, base_time, nx, ny)
    val weather_url =
        "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastSpaceData?serviceKey=" +
                "4N926ah8IlK9Xzvu1O%2FlEZEbWJHqRQI0VgrqT6vR54oG0ADd%2B8MyeYhQGIvGvGLXc%2F%2F%2BzWpoWAuyKP8PLh87xw%3D%3D" +
                "&base_date=" + "20191124" + "&base_time=0500&nx=60&ny=127&numOfRows=10&pageNo=1&_type=xml"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_parse)

        GetXMLTask().execute()
        //Toast.makeText(this, "${weatherDate}", Toast.LENGTH_LONG).show()
    }

    private inner class GetXMLTask : AsyncTask<String, Void, Document>() {
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

            for (i in 0 until nodeList.getLength()) {
                val node = nodeList.item(i)
                val fstElmnt = node as Element
                val idx = fstElmnt.getElementsByTagName("category")
                // 모든 category 값들을 출력 위한
                // s += "category = "+  idx.item(0).getChildNodes().item(0).getNodeValue() +"\n";
                // 강수확률% POP, fcstValue 강수확률에 해당하는 값
                // 강수형태 PTY : 없음(0), 비(1), 진눈깨비(2), 눈(3), 소나기(4)
                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("POP")) {
                    val gugun = fstElmnt.getElementsByTagName("fcstValue")
                    s += "강수확률 = " + gugun.item(0).getChildNodes().item(0).getNodeValue() + "% \n"
                }

                // 습도% REH, fcstValue 습도에 해당하는 값
                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("REH")) {
                    val gugun = fstElmnt.getElementsByTagName("fcstValue")
                    s += "습도 = " + gugun.item(0).getChildNodes().item(0).getNodeValue() + "% \n"
                }

                // 기온ºC T3H, fcstValue 온도에 해당하는 값
                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("T3H")) {
                    val gugun = fstElmnt.getElementsByTagName("fcstValue")
                    s += "온도 = " + gugun.item(0).getChildNodes().item(0).getNodeValue() + "'C \n"
                }

                // 구름상태 SKY, fcstValue 구름상태에 해당하는 값
                // 맑음(1),  구름많음(3),  흐림(4)
                if (idx.item(0).getChildNodes().item(0).getNodeValue().equals("SKY")) {
                    val gugun = fstElmnt.getElementsByTagName("fcstValue")
                    val cloud_num =
                        Integer.parseInt(gugun.item(0).getChildNodes().item(0).getNodeValue())

                    if (cloud_num == 0 || cloud_num == 1 || cloud_num == 2) {
                        s += "하늘상태 = 맑음\n"
                    } else if (cloud_num == 3 || cloud_num == 4 || cloud_num == 5) {
                        s += "하늘상태 = 구름 조금\n"
                    } else if (cloud_num == 6 || cloud_num == 7 || cloud_num == 8) {
                        s += "하늘상태 = 구름 많음\n"
                    } else if (cloud_num == 9 || cloud_num == 10) {
                        s += "하늘상태 = 흐림\n"
                        // s += "fcstValue 하늘상태 = "+ gugun.item(0).getChildNodes().item(0).getNodeValue() +"\n";
                    }

                    // 모든 카테고리에 대한 fcstValue 값들을 출력 위한
                    // NodeList gugun = fstElmnt.getElementsByTagName("fcstValue");
                    // s += "fcstValue = "+  gugun.item(0).getChildNodes().item(0).getNodeValue() +"\n";
                }
                txtweather.setText(s)

                super.onPostExecute(doc)
            }

        }

    }
}