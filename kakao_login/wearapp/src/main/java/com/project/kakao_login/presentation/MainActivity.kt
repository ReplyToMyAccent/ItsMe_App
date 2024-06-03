package com.project.kakao_login.presentation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.project.kakao_login.databinding.ActivityMainBinding

import com.google.android.gms.wearable.*
import android.util.Log

import com.project.kakao_login.R

class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
//        setContentView(R.layout.activity_main)

        Wearable.getDataClient(this).addListener(this)

//        val button : Button = findViewById(R.id.button1)

        binding.button1.setOnClickListener {
            sendMessage("Main Button 1")
        }
        binding.button2.setOnClickListener {
            sendMessage("Main Button 2")
        }
        binding.button3.setOnClickListener {
            sendMessage("Main Button 3")
        }

        createNotificationChannel()
        binding.buttonSendNotification.setOnClickListener{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED){
                // 권한이 허용된 경우 알림을 보냅니다.
                sendNotification("Hello Wear OS", "This is a test notification")
            }
        }
        //버튼 누르면 알림 보내기
        val buttonSendNotification: Button = findViewById(R.id.button_send_notification)
        buttonSendNotification.setOnClickListener(){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED){
                // 권한이 허용된 경우 알림을 보냅니다.
                sendNotification("Hello Wear OS", "This is a test notification")
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        Wearable.getDataClient(this).removeListener(this)
    }

    private fun sendMessage(text: String) {
        val putDataMapRequest = PutDataMapRequest.create("/button_text").apply {
            dataMap.putString("button_text", text)
        }
        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()
        Wearable.getDataClient(this).putDataItem(putDataRequest)
    }

    // 안드로이드에서 온 메세지를 노티로 띄워줌
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                if(event.dataItem.uri.path == "/notification_info"){
                    println("Wear OnDataChanged")
                    val text = dataMapItem.dataMap.getString("text") // 메세지
                    val title = dataMapItem.dataMap.getString("title") // 보낸사람
                    val packageName = dataMapItem.dataMap.getString("packageName") // 패키지명

                    Log.d("WearMainActivity", "Received notification - Title: $title, Text: $text")
                    sendNotification(title.toString(), text.toString())
                }
                else if(event.dataItem.uri.path == "/reply_list"){
                    println("Wear OnDataChanged")
                    val text = dataMapItem.dataMap.getString("room_list") // 메세지
                    val title = dataMapItem.dataMap.getString("reply_list") // 보낸사람

                    Log.d("WearMainActivity", "Received notification - Title: $text, Text: $title")
                    sendNotification(title.toString(), text.toString())
                }
            }
        }
    }


    private val channelId = "wear_os_channel_id"
    private val channelName = "Wear OS Channel"
    private val channelDescription = "Channel for Wear OS notifications"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your app.
            sendNotification("Hello Wear OS", "This is a test notification")
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied.
        }
    }

    private fun createNotificationChannel() {
        // 알림 채널은 Android 8.0 (API 26) 이상에서만 필요합니다.
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }
        // 알림 채널을 시스템에 등록합니다.
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun sendNotification(title: String, text: String) {
        val intent = Intent(this, ButtonActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "wear_os_channel_id"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)
        //권한을 확인하고 알림을 보냄
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            notificationManager.notify(1,notificationBuilder.build())
            println("성공")
        }
        else{
            println("실패")
        }
    }
}
