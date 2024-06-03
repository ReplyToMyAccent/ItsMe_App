package com.project.kakao_login

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class KakaoAuthViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "KakaoAuthViewModel"
        const val PREF_NAME = "login_pref"
        const val IS_LOGGED_IN = "is_logged_in"
        const val USER_ID = "user_id"
    }

    private val context = application.applicationContext
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val isLoggedIn = MutableStateFlow<Boolean>(false)

    init {
        isLoggedIn.value = sharedPreferences.getBoolean(IS_LOGGED_IN, false)
    }

    fun kakaoLogin() {
        viewModelScope.launch {
            isLoggedIn.emit(handleKakaoLogin())
            if (isLoggedIn.value) {
                sharedPreferences.edit().putBoolean(IS_LOGGED_IN, true).apply()
            }
        }
    }

    fun kakaoLogout() {
        viewModelScope.launch {
            if (handleKakaoLogout()) {
                isLoggedIn.emit(false)
                sharedPreferences.edit().remove(IS_LOGGED_IN).apply()
                sharedPreferences.edit().remove(USER_ID).apply()
            }
        }
    }

    private suspend fun handleKakaoLogout(): Boolean =
        suspendCoroutine { continuation ->
            UserApiClient.instance.logout { error ->
                if (error != null) {
                    Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error)
                    continuation.resume(false)
                } else {
                    Log.i(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨")
                    continuation.resume(true)
                }
            }
        }

    private suspend fun handleKakaoLogin(): Boolean =
        suspendCoroutine<Boolean> { continuation ->
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오계정으로 로그인 실패", error)
                    continuation.resume(false)
                } else if (token != null) {
                    Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
                    continuation.resume(true)
                    // 성공적으로 로그인한 경우 사용자 정보를 가져옴
                    fetchUserInfo()
                }
            }

            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                    if (error != null) {
                        Log.e(TAG, "카카오톡으로 로그인 실패", error)
                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                    } else if (token != null) {
                        Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                        // 성공적으로 로그인한 경우 사용자 정보를 가져옴
                        fetchUserInfo()
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
            }
        }

    private fun fetchUserInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                // 에러 처리
                Log.e(TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                // 사용자 정보가 성공적으로 가져옴
                val userId = user.id.toString()
                Log.i(TAG, "사용자 ID: $userId")
                sharedPreferences.edit().putString(USER_ID, userId).apply()

                // Start RequestActivity
                val intent = Intent(context, RequestActivity::class.java).apply {
                    putExtra("USER_ID", userId)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }
}



// TODO : sharedpreference에 사용자 ID 저장해놓기
// TODO : 로그인된 상태면 바로 자신의 현재 파일 업로드 현황 띄워주기
// TODO : 새로고침 버튼(get) 누르면 현재 Postgresql에 있는 답장 리스트로 최신화
// TODO : 최신화된 답장 리스트도 sharedprefernce에 로그아웃할때까지 계속 저장해놓고 지속적으로 최신화

// TODO : 유환 -> sharedprefernce에 있는 답장 리스트 wearos로 넘기기
// TODO : 건우 -> wearos로 넘어온 답장 리스트 chip으로 띄우기
// TODO : 성호 -> 답장 리스트 선택되었을 때 이벤트 캐치해서 모바일에서 답장 전송
// TODO : 나중에 POSTGRESQL에 선택 로그 쌓는 것 까지 구현 (POST 요청)
