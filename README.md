# WebAPI Client
This is a Web API client library for Android/Kotlin.
You can access to API servers easly.

This library
- parse json by using Kotlinx Json. You only need to define data structure.
- retry a connection, and refresh a token.
- connect to a server on background thread.

## Install
This library use [Kotolinx Json](https://github.com/Kotlin/kotlinx.serialization). So, it need to be installed, too.

Edit bundle.gradle(Project) and gradle:bundle.gradle(app).

```gradle:bundle.gradle(Project)
dependencies {
    classpath "org.jetbrains.kotlin:kotlin-serialization:$kotlin_version"
}
```

```gradle:bundle.gradle(app)
apply plugin: 'kotlinx-serialization'

dependencies {
    implementation "org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0"
    implementation 'jp.co.sankosc:webapi-client:1.0.1'
}
```

## Json data
See [Kotolinx Json](https://github.com/Kotlin/kotlinx.serialization).

```kotlin:Example
import kotlinx.serialization.*

@Serializable
data class Message(
	val message: String?
)

@Serializable
data class Login(
    val email: String,
    val password: String
)

@Serializable
data class Token(
    @SerialName("access_token")
    val accessToken: String
)
```

## Get

```kotlin:Example
val url = "https://develop.sankosc.co.jp/apitest/api/hello"
val client = WebApiClient()
client.get<Message>(url, {
    // Success
    print(it.message)
}, {
    // Error
    it.printStackTrace()
})
```

## Post

```kotlin:Example
val url = "https://develop.sankosc.co.jp/apitest/api/echo"
val client = WebApiClient()
client.post<Message, Message>(url, Message("post data"), {
    // Success
    print(it.message)
}, {
    // Error
    it.printStackTrace()
})
```

## Error handling
Pass a exception object to error handler.
When the error is caused server, pass WebApiClientException.
WebApiClientException has StatusCode.

```kotlin:Example
client.get<Message>(url, {
    // Success
}, {
    // Error
    if (it is WebApiClientException) {
        print(it.code)
    }
})
```

## Access to a Authorizad API
Set AccessToken to WebApiClient instance. Then use Get, Post method.
This library access to the server with header which include `Authorization: Bearer [Access Token]`.

```kotlin:Example
val url = "https://develop.sankosc.co.jp/apitest/api/login"
val client = WebApiClient()
val data = Login("sample@sankosc.co.jp", "sample123")
client.post<Login, Token>(url, data, {
    client.accessToken = it.accessToken
}, {
    it.printStackTrace()
})
```

AccessToken can be set by constructor.

```kotlin:Example
val client = WebApiClient(accessToken = "[Saved Token]")
```

## Authentication error handling
Define handleAuthFail on constructor.

```kotlin:Example
val client = WebApiClient(handleAuthFail = {
    // Authentication error handling
})
```

## Refresh AccessToken
Define handleTokenExpired on constructor.
This library call handleTokenExpired when receive a response with 401 Code.

handleTokenExpired has two parameters. A WebApiClient instance and onRefreshed callback function.
When you call onRefreshed, this library will access to last called API with new token.

```kotlin:Example
val client = WebApiClient(handleTokenExpired= {client, onRefreshed ->
    // Refresh token
    client.refresh<Token>(url, {
        onRefreshed(it.accessToken)
    }, {
        it.printStackTrace()
    })
})
```

Use refresh method instead of get or post method.
The difference of between both is Authentication error handling.
Other function is same.

## Parameters


```kotlin:Constructor
val client = WebApiClient(
	accessToken = "",

	handleAuthFail = {
	},

	handleTokenExpired = {client, onRefreshed ->
	},

	handler = Handler()
)
```

|Name|Description|Initial Value|
|---|---|---|
|accessToken|Used when access to authrized API|null|
|handleAuthFail|Handler for authentication error|null|
|handleTokenExpired|Handler for refreshing expired token|null|
|handler|Use when call callback function. If set to null, call on direct|Handler(Looper.getMainLooper()))|

```kotlin:Properties
client.accessToken = ""

client.tokenType = "Bearer"

client.jsonConfiguration = JsonConfiguration(
	isLenient = true,
	ignoreUnknownKeys = true,
	serializeSpecialFloatingPointValues = true
)
```

|Name|Description|Initial Value|
|---|---|---|
|accessToken|Used when access to authrized API|null|
|tokenType|Used with accessToken when access to authrized API|"Bearer"|
|jsonConfiguration|The configuration used by Kotlinx Json|The above values|

## License
```
   Copyright 2020 Sanko System Co.,Ltd. (SankoSC)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```