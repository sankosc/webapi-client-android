/*
 * Create: 2020/05/06
 * Copyright 2020 Sanko System Co.,Ltd. (SankoSC)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.co.sankosc.webapiclient

import android.os.Handler
import android.os.Looper
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.serializer
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.concurrent.thread

/**
 * @author nozaki
 */
class WebApiClient(
    // Access Token
    var accessToken: String? = null,

    // Auth Failuer Handler
    val handleAuthFail: (() -> Unit)? = null,

    // Refresh Token Handler
    val handleTokenExpired: ((WebApiClient, (String) -> Unit) -> Unit)? = null,

    // Handler
    val handler: Handler? = Handler(Looper.getMainLooper())) {

    // Token Type
    var tokenType = "Bearer"

    // HTTP Client Builder
    val builder = OkHttpClient.Builder()

    // Json Configuration
    var jsonConfiguration = JsonConfiguration(
        isLenient = true,
        ignoreUnknownKeys = true,
        serializeSpecialFloatingPointValues = true
    )

    /**
     * GET API
     */
    fun <T : Any> get(
        url: String,
        loader: DeserializationStrategy<T>,
        handleSuccess: (T) -> Unit,
        handleError: (Exception) -> Unit,
        retryCount: Int = 3,
        isRecursive: Boolean = false) {
        println("get:" + url)
        thread {
            try {
                val request = Request.Builder().url(url)
                if (accessToken != null) {
                    request.header("Authorization", tokenType + " " + accessToken)
                }
                val client = builder.build()
                val response = client.newCall(request.build()).execute()
                if (!response.isSuccessful) response.body?.close()
                if (response.isSuccessful) {
                    val json = response.body?.string() ?: ""
                    response.body?.close()
                    val data = Json(jsonConfiguration).parse(loader, json)
                    invoke { handleSuccess(data) }
                } else if (response.code == 401 && isRecursive == false && handleTokenExpired != null) {
                    refresh { get(url, loader, handleSuccess, handleError, retryCount, true) }
                } else if (response.code == 401 && handleAuthFail != null) {
                    handleAuthFail.invoke()
                } else {
                    throw WebApiClientException(response.code, response.message)
                }
            } catch (e: Exception) {
                if (retryCount > 0) {
                    get(url, loader, handleSuccess, handleError, retryCount - 1, isRecursive)
                } else {
                    invoke { handleError(e) }
                }
            }
        }
    }

    /**
     * POST API
     */
    public fun <Treq : Any, Tres : Any> post(
        url: String,
        postData: Treq,
        reqLoader: SerializationStrategy<Treq>,
        resLoader: DeserializationStrategy<Tres>,
        handleSuccess: (Tres) -> Unit,
        handleError: (Exception) -> Unit,
        retryCount: Int = 3,
        isRecursive: Boolean = false) {
        println("post:" + url)
        thread {
            try {
                val request = Request.Builder().url(url)
                if (accessToken != null) {
                    request.header("Authorization", tokenType + " " + accessToken)
                }
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val body = Json.stringify(reqLoader, postData).toRequestBody(mediaType)
                request.post(body)

                val client = builder.build()
                val response = client.newCall(request.build()).execute()
                if (!response.isSuccessful) response.body?.close()
                if (response.isSuccessful) {
                    val json = response.body?.string() ?: ""
                    response.body?.close()
                    val data = Json(jsonConfiguration).parse(resLoader, json)
                    invoke { handleSuccess(data) }
                } else if (response.code == 401 && isRecursive == false && handleTokenExpired != null) {
                    refresh { post(url, postData, reqLoader, resLoader, handleSuccess, handleError, retryCount, true) }
                } else if (response.code == 401 && handleAuthFail != null) {
                    handleAuthFail.invoke()
                } else {
                    throw WebApiClientException(response.code, response.message)
                }
            } catch (e: Exception) {
                if (retryCount > 0) {
                    post(url, postData, reqLoader, resLoader, handleSuccess, handleError, retryCount - 1, isRecursive)
                } else {
                    invoke { handleError(e) }
                }
            }
        }
    }

    /**
     * Refresh Access Token
     */
    private fun refresh(handleRefreshed: () -> Unit) {
        handleTokenExpired?.invoke(this, {
            accessToken = it
            handleRefreshed()
        })
    }

    /**
     * Invoke a event
     */
    private fun invoke(event: () -> Unit) {
        handler?.post(event) ?: event()
    }

    /**
     * alias GET API
     */
    @OptIn(ImplicitReflectionSerializer::class)
    inline fun <reified T : Any> get(
        url: String,
        noinline handleSuccess: (T) -> Unit,
        noinline handleError: (Exception) -> Unit,
        retryCount: Int = 3
    ) = get(url, T::class.serializer(), handleSuccess, handleError, retryCount)

    /**
     * alias POST API
     */
    @OptIn(ImplicitReflectionSerializer::class)
    inline fun <reified Treq : Any, reified Tres : Any> post(
        url: String,
        postData: Treq,
        noinline handleSuccess: (Tres) -> Unit,
        noinline handleError: (Exception) -> Unit,
        retryCount: Int = 3
    ) = post<Treq, Tres>(url, postData, Treq::class.serializer(), Tres::class.serializer(), handleSuccess, handleError, retryCount)

    /**
     * alias Refresh API
     */
    @OptIn(ImplicitReflectionSerializer::class)
    inline fun <reified T : Any> refresh(
        url: String,
        noinline handleSuccess: (T) -> Unit,
        noinline handleError: (Exception) -> Unit,
        retryCount: Int = 3
    ) = get(url, T::class.serializer(), handleSuccess, handleError, retryCount, true)

    /**
     * alias Refresh API
     */
    @OptIn(ImplicitReflectionSerializer::class)
    inline fun <reified Treq : Any, reified Tres : Any> refresh(
        url: String,
        postData: Treq,
        noinline handleSuccess: (Tres) -> Unit,
        noinline handleError: (Exception) -> Unit,
        retryCount: Int = 3
    ) = post<Treq, Tres>(url, postData, Treq::class.serializer(), Tres::class.serializer(), handleSuccess, handleError, retryCount,  true)
}