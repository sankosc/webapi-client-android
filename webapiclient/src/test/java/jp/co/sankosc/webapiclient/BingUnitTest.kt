/*
 * Create: 2020/11/05
 * Copyright 2020 Sanko System Co.,Ltd.
 */

package jp.co.sankosc.webapiclient

import org.junit.Assert
import org.junit.Test

/**
 * @author nozaki
 */
class BingUnitTest {
    @Test
    fun getBing_isSuccess() {
        val url = "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1&setmkt=ja-jp"
        val client = WebApiClient(handler = null)
        var result = false
        client.get<BingData>(url, {
            for (image in it.images) {
                println(image.title + " https://www.bing.com" + image.url)
            }
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
}