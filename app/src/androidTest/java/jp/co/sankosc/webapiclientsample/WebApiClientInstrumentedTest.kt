/*
 * Create: 2020/05/13
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
package jp.co.sankosc.webapiclientsample

import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.co.sankosc.webapiclient.WebApiClient
import jp.co.sankosc.webapiclient.WebApiClientException
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author nozaki
 */
@RunWith(AndroidJUnit4::class)
class WebApiClientInstrumentedTest {

    // Test Site Url
    private val baseUrl = "https://develop.sankosc.co.jp/apitest/api/"

    // AccessToken
    private var accessToken: String? = null

    @Before
    fun initialize_login() {
        val url = baseUrl + "login"
        val client = WebApiClient()
        var result = false
        val data = Login("sample@sankosc.co.jp", "sample123")
        client.post<Login, Token>(url, data, {
            accessToken = it.accessToken
            result = true
        }, {
            it.printStackTrace()
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }

    @Test
    fun login_isFail() {
        val url = baseUrl + "login"
        var result = false
        val client = WebApiClient(handleTokenExpired = null, handleAuthFail = {
            result = true
        })
        val data = Login("sample@sankosc.co.jp", "sample789")
        client.post<Login, Token>(url, data, {
            Assert.fail()
            result = true
        }, {
            it.printStackTrace()
            Assert.fail()
            result = true
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }

    @Test
    fun getNoAuth_isSuccess() {
        val url = baseUrl + "hello"
        val client = WebApiClient()
        var result = false
        client.get<Message>(url, {
            Assert.assertEquals("hello", it.message)
            result = true
        }, {
            it.printStackTrace()
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }

    @Test
    fun getWithAUth_isSuccess() {
        val url = baseUrl + "me"
        val client = WebApiClient(accessToken = accessToken)
        var result = false
        client.get<Me>(url, {
            Assert.assertEquals("sample", it.name)
            result = true
        }, {
            it.printStackTrace()
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }

    @Test
    fun postWithAuth_isSuccess() {
        val url = baseUrl + "echo"
        val client = WebApiClient(accessToken = accessToken)
        var result = false
        val data = Message("This is test message.")
        client.post<Message, Message>(url, data, {
            Assert.assertEquals("This is test message.", it.message)
            result = true
        }, {
            it.printStackTrace()
            Assert.fail()
            result = true
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }

    @Test
    fun refresh_isSuccess() {
        val url = baseUrl + "refresh"
        val client = WebApiClient(accessToken = accessToken)
        var result = false
        client.get<Token>(url, {
            result = true
        }, {
            it.printStackTrace()
            Assert.fail()
            result = true
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }

    @Test
    fun refresh_isFail() {
        val url = baseUrl + "refresh"
        var result = false
        val client = WebApiClient(accessToken = "X", handleAuthFail = {
            result = true
        })
        client.get<Token>(url, {
            Assert.fail()
            result = true
        }, {
            it.printStackTrace()
            Assert.fail()
            result = true
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }

    @Test
    fun getTokenRefresh_isSuccess() {
        Thread.sleep(61 * 1000)

        val refreshHandller: (WebApiClient, (String) -> Unit) -> Unit = {client, onRefreshed ->
            val url = baseUrl + "refresh"
            client.refresh<Token>(url, {
                onRefreshed(it.accessToken)
            }, {
                it.printStackTrace()
            })
        }

        val url = baseUrl + "me"
        val client = WebApiClient(
            accessToken = accessToken,
            handleTokenExpired = refreshHandller,
            handleAuthFail = {
                print("refresh token is expired")
            })
        var result = false
        client.get<Me>(url, {
            Assert.assertEquals("sample", it.name)
            result = true
        }, {
            it.printStackTrace()
            Assert.fail()
            result = true
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }

    @Test
    fun postTokenRefresh_isSuccess() {
        Thread.sleep(61 * 1000)

        val refreshHandller: (WebApiClient, (String) -> Unit) -> Unit = {client, onRefreshed ->
            val url = baseUrl + "refresh"
            client.refresh<Token>(url, {
                onRefreshed(it.accessToken)
            }, {
                it.printStackTrace()
            })
        }

        val url = baseUrl + "echo"
        val client = WebApiClient(
            accessToken = accessToken,
            handleTokenExpired = refreshHandller,
            handleAuthFail = {
                print("refresh token is expired")
            })
        var result = false
        client.post<Message, Message>(url, Message("Token Refreshed"), {
            Assert.assertEquals("Token Refreshed", it.message)
            result = true
        }, {
            it.printStackTrace()
            Assert.fail()
            result = true
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }

    @Test
    fun getWithAuth_isFail() {
        val url = baseUrl + "me"
        var result = false
        val client = WebApiClient(handleAuthFail = {
            result = true
        })
        client.get<Me>(url, {
            Assert.fail()
            result = true
        }, {
            it.printStackTrace()
            Assert.fail()
            result = true
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }

    @Test
    fun getWithAuth_isFail2() {
        val url = baseUrl + "me"
        var result = false
        val client = WebApiClient(accessToken = accessToken + "XX", handleAuthFail = {
            result = true
        })
        client.get<Me>(url, {
            Assert.fail()
            result = true
        }, {
            it.printStackTrace()
            Assert.fail()
            result = true
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }

    @Test
    fun postWithAuth_isFail() {
        val url = baseUrl + "echo"
        var result = false
        val client = WebApiClient(handleAuthFail = {
            result = true
        })
        client.post<Message, Message>(url, Message("postWithAuth"), {
            Assert.fail()
            result = true
        }, {
            it.printStackTrace()
            Assert.fail()
            result = true
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }

    @Test
    fun postWithAuth_isFail2() {
        val url = baseUrl + "echo"
        var result = false
        val client = WebApiClient(accessToken = accessToken + "XX", handleAuthFail = {
            result = true
        })
        client.post<Message, Message>(url, Message("postWithAuth"), {
            Assert.fail()
            result = true
        }, {
            it.printStackTrace()
            Assert.fail()
            result = true
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }

    @Test
    fun get_isFail404() {
        val url = baseUrl + "nothing"
        var result = false
        val client = WebApiClient()
        client.get<Me>(url, {
            Assert.fail()
            result = true
        }, {
            if (it is WebApiClientException) {
                Assert.assertEquals(404, it.code)
                result = true
            }
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }

    @Test
    fun post_isFail404() {
        val url = baseUrl + "nothing"
        var result = false
        val client = WebApiClient()
        client.post<Message, Message>(url, Message(""), {
            Assert.fail()
            result = true
        }, {
            if (it is WebApiClientException) {
                Assert.assertEquals(404, it.code)
                result = true
            }
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }
}