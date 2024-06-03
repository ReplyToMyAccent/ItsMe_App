package com.project.kakao_login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kakao.sdk.common.util.Utility
import com.project.kakao_login.ui.theme.Kakao_loginTheme
import org.intellij.lang.annotations.JdkConstants

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.wearable.*
import android.app.Notification
//import com.google.android.gms.wearable.R
import com.project.kakao_login.databinding.RequestGetBinding


class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener {
    private val kakaoAuthViewModel: KakaoAuthViewModel by viewModels()
    private lateinit var binding: RequestGetBinding
    private lateinit var myNotiReply: MyNotiListen2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RequestGetBinding.inflate(layoutInflater)
        val view = binding.root

        setContent {
            Kakao_loginTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KakaoLoginView(kakaoAuthViewModel)
                }
            }
        }

        Wearable.getDataClient(this).addListener(this)

    }
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                val buttonText = dataMapItem.dataMap.getString("button_text")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getDataClient(this).removeListener(this)
    }

}

@Composable
fun KakaoLoginView(viewModel: KakaoAuthViewModel) {
    val isLoggedIn = viewModel.isLoggedIn.collectAsState()
    val loginStatusInfoTitle = if (isLoggedIn.value) "로그인 상태" else "로그아웃 상태"
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            viewModel.kakaoLogin()
        }) {
            Text(text = "카카오 로그인 하기")
        }
        Button(onClick = {
            viewModel.kakaoLogout()
        }) {
            Text(text = "카카오 로그아웃 하기")
        }
        Text(
            text = loginStatusInfoTitle,
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )
    }


}
