package com.project.kakao_login

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.project.kakao_login.BuildConfig

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Other initialization code

        // Kakao SDK initialization
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}
