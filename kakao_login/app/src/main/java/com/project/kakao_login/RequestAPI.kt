package com.project.kakao_login

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HeaderMap


interface RequestAPI {
    @GET("/file/api/response/")
    fun getPosts(@HeaderMap headers: Map<String, String>): Call<List<GetItem>>

}
