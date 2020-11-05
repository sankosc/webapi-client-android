/*
 * Create: 2020/11/05
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

import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * @author nozaki
 */
class WebApiClientUnitTest {

    // Test Site Url
//    val baseUrl = "http://localhost/api/"
    private val baseUrl = "https://develop.sankosc.co.jp/apitest/api/"

    // AccessToken
    private var accessToken: String? = null

    @Before
    fun initialize_login() {
        val url = baseUrl + "login"
        val client = WebApiClient(handler = null)
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
        val client = WebApiClient(handler = null, handleTokenExpired = null, handleAuthFail = {
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
        val client = WebApiClient(handler = null)
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
    fun postNoAuth_isSuccess() {
        val url = baseUrl + "echo"
        val client = WebApiClient(handler = null)
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
    fun getWithAUth_isSuccess() {
        val url = baseUrl + "me"
        val client = WebApiClient(accessToken = accessToken, handler = null)
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
        val url = baseUrl + "reverse"
        val client = WebApiClient(accessToken = accessToken, handler = null)
        var result = false
        val data = Message("This is test message.")
        client.post<Message, Message>(url, data, {
            Assert.assertEquals(".egassem tset si sihT", it.message)
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
        val client = WebApiClient(accessToken = accessToken, handler = null)
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
        val client = WebApiClient(handler = null, accessToken = "X", handleAuthFail = {
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
            },
            handler = null)
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
            },
            handler = null)
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
        val client = WebApiClient(handler = null, handleAuthFail = {
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
        val client = WebApiClient(accessToken = accessToken + "XX", handler = null, handleAuthFail = {
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
        val url = baseUrl + "reverse"
        var result = false
        val client = WebApiClient(handler = null, handleAuthFail = {
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
        val url = baseUrl + "reverse"
        var result = false
        val client = WebApiClient(accessToken = accessToken + "XX", handler = null, handleAuthFail = {
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
        val client = WebApiClient(handler = null)
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
        val client = WebApiClient(handler = null)
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

    @Test
    fun logout_isSccess() {
        val url = baseUrl + "logout"
        var result = false
        val client = WebApiClient(handler = null, accessToken = accessToken)
        client.post<Empty, Message>(url, Empty(), {
            WebApiClient(handler = null, accessToken = accessToken, handleAuthFail = {
                result = true
            }).get<Me>(baseUrl + "me", {
                Assert.fail()
                result = true
            }, {
                Assert.fail()
                result = true
            })
        }, {
            Assert.fail()
            it.printStackTrace()
            result = true
        })
        for (i in 1..100) {
            if (result) return
            Thread.sleep(100)
        }
        Assert.fail()
    }
}