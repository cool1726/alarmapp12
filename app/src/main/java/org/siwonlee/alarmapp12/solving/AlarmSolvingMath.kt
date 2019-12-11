package org.siwonlee.alarmapp12.solving

import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.alarm_solving_math.*
import org.siwonlee.alarmapp12.R
import java.util.*

class AlarmSolvingMath : AlarmSolvingBasic() {
    //amount개의 정수와 +, -기호를 이용한 수식을 만든다
    var amount = 3
    //+, -를 배열에 저장한다
    val op = arrayOf("+", "-")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_solving_math)

        //amount개의 랜덤 정수를 생성해 내림차순으로 정렬한다
        var nums = Array(amount, { getRandomNumber(2) }).sortedDescending()
        //가장 큰 수부터 차례로 사용하여 수식을 만든다
        qstn.text = "${nums[0]}"

        //만들어진 수식의 답을 따로 저장한다
        var answer: Int = nums[0]

        //nums의 1번 항부터 차례로
        for(i in 1 until amount) {
            //각 항 사이의 연산자는 차례로 -, +, -, +, ... 순서로 진행한다
            qstn.text = "${qstn.text} ${op[i % 2]} ${nums[i]}"
            //i가 홀수라면 nums[i]를 answer에서 빼고, 아니라면 더한다
            answer += nums[i] * (1 - 2 * (i % 2))
        }

        math_name.text = alarmName

        math_stop.setOnClickListener {
            val nswrnswr = nswr.text.toString()
            var answ = 0

            if(nswrnswr != "") answ = Integer.parseInt(nswrnswr)

            if(answ == answer)  stop()
            else {
                Toast.makeText(this, "틀렸습니다. 다시 입력하세요.", Toast.LENGTH_LONG).show()
                nswr.setText("")
            }
        }
        math_delay.setOnClickListener { delay() }
    }

    //size자리 랜덤 정수를 만드는 함수
    fun getRandomNumber(size: Int): Int {
        val random = Random()

        var ret = 0
        for(i in 1..size) ret = ret * 10 + random.nextInt(9) + 1
        return ret
    }

    //뒤로가기로 알람 해제를 막기 위한 빈 함수
    override fun onBackPressed() { }
}