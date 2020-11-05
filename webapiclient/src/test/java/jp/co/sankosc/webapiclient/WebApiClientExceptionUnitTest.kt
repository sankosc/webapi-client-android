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
import org.junit.Test

/**
 * @author nozaki
 */
class WebApiClientExceptionUnitTest {
    @Test
    fun exception_throw() {
        try {
            throw WebApiClientException(200, "test exception")
        } catch (e: Exception) {
            if (e is WebApiClientException) {
                Assert.assertEquals(200, e.code)
                Assert.assertEquals("200;test exception", e.message)
            } else {
                Assert.fail()
            }
        }
    }
}