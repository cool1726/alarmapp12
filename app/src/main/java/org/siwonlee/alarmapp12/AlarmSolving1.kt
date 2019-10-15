package org.siwonlee.alarmapp12

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.alarm_solving_1.*
import java.util.*

val random = Random()

//size자리 랜덤 정수를 만드는 함수
fun getRandomNumber(size: Int): Int {
    var ret = 0
    for(i in 1..size) ret = ret * 10 + random.nextInt(9) + 1
    return ret
}

class AlarmSolving1 : AppCompatActivity() {
    //amount개의 정수와 +, -기호를 이용한 수식을 만든다
    var amount = 3
    //+, -를 배열에 저장한다
    val op = arrayOf("+", "-")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_solving_1)

        //amount개의 랜덤 정수를 생성해 내림차순으로 정렬한다
        var nums = Array(amount, {getRandomNumber(2)}).sortedDescending()
        //가장 큰 수부터 차례로 사용하여 수식을 만든다
        qstn.text = "${nums[0]}"

        //만들어진 수식의 답을 따로 저장한다
        var answer = nums[0]

        //nums의 1번 항부터 차례로
        for(i in 1 until amount) {
            //각 항 사이의 연산자는 차례로 -, +, -, +, ... 순서로 진행한다
            qstn.text = "${qstn.text} ${op[i % 2]} ${nums[i]}"
            //i가 홀수라면 nums[i]를 answer에서 빼고, 아니라면 더한다
            answer += nums[i] * (1 - 2 * (i % 2))
        }

        bt_alarmoff.setOnClickListener {
            val ans = Integer.parseInt(nswr.text.toString())
            if(ans == answer) finish()
        }
    }
}