package com.project.kakao_login

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import com.faendir.rhino_android.RhinoAndroidHelper
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import org.mozilla.javascript.Context
import com.google.android.gms.wearable.Wearable


class MyNotiListen2 : NotificationListenerService(), DataClient.OnDataChangedListener {

    companion object {
        var execContext: android.content.Context? = null
    }
    override fun onCreate() {
        super.onCreate()
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    private fun sendMessage(title: String, text: String, packageName: String) {
        val putDataMapRequest = PutDataMapRequest.create("/notification_info").apply {
            dataMap.putString("title", title)
            dataMap.putString("text", text)
            dataMap.putString("packageName", packageName)
        }
        val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()
        Wearable.getDataClient(this).putDataItem(putDataRequest)
        Log.d("AndroidSendMessage", "WEAROS로 메세지 전송완료")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val packageName: String = sbn?.packageName ?: "Null"
        val extras = sbn?.notification?.extras

        val extraTitle: String = extras?.getString(Notification.EXTRA_TITLE).toString()
        val extraText: String = extras?.get(Notification.EXTRA_TEXT).toString()

        Log.d(
            "TAG", "onNotificationPosted:\n"
                    + "PackageName: $packageName"
                    + "Title: $extraTitle\n"
                    + "Text: $extraText\n"
        )
        if (packageName == "jp.naver.line.android") {
            sendMessage(extraTitle, extraText, packageName)
        }

    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/button_text") {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                val buttonText = dataMapItem.dataMap.getString("button_text")
                logActiveNotifications(buttonText.toString())
            }
        }
    }

    fun logActiveNotifications(myreply: String) {
        val activeNotifications = getActiveNotifications()
        for (notification in activeNotifications) {
            if (notification.packageName == "jp.naver.line.android") {
                val wExt = Notification.WearableExtender(notification?.notification)
                val action = wExt.actions.firstOrNull(){act ->
                    act.remoteInputs != null && act.remoteInputs.isNotEmpty() &&
                            (act.title.toString().contains("reply", true) || act.title.toString()
                                .contains("답장", true))
                }
                println("wEXT, action : " + wExt + " / " + action)
                if (action != null){
                    execContext = applicationContext
                    callResponder(action, myreply)
                }
            }
        }
    }

    fun callResponder(session: Notification.Action?, myreply: String){
        val parseContext = RhinoAndroidHelper.prepareContext()
        val replier = MyNotiListen2.SessionCacheReplier(session)
        parseContext.optimizationLevel = -1
        replier.reply(myreply)
        Context.exit()
    }

    class SessionCacheReplier (private val session : Notification.Action?){
        fun reply(value: String){
            if (session == null){ return }

            val sendIntent = Intent()
            val msg = Bundle()

            session.remoteInputs?.forEach { inputable -> msg.putCharSequence(inputable.resultKey, value)}

            RemoteInput.addResultsToIntent(session.remoteInputs, sendIntent, msg)

            try {
                session.actionIntent.send(MyNotiListen2.execContext, 0, sendIntent)
            }catch (e:PendingIntent.CanceledException){
                // 예외 처리
            }
        }
    }


}