package com.project.kakao_login.presentation

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import androidx.activity.ComponentActivity
import com.project.kakao_login.R

class ButtonActivity : ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_button)

        val buttonIds = listOf(
            R.id.button1, R.id.button2, R.id.button3,
//            R.id.button4, R.id.button5,
//            R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.button10
        )

        for (id in buttonIds) {
            val button: Button = findViewById(id)
            button.setOnClickListener() {
                val buttonText = button.text.toString()
                // 버튼을 눌렀을 때 할 행위 구현
                sendMessage(buttonText)
                println("Button")
                Toast.makeText(this, buttonText, Toast.LENGTH_SHORT).show()
            }
        }
    }

    //버튼의 텍스트를 안드로이드로 보냄
    private fun sendMessage(text: String) {
        val putDataMapRequest = PutDataMapRequest.create("/button_text").apply {
            dataMap.putString("button_text", text)
        }
        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()
        Wearable.getDataClient(this).putDataItem(putDataRequest)
    }
}