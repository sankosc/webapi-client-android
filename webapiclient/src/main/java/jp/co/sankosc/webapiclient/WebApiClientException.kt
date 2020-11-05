/*
 * Create: 2020/11/05
 * Copyright 2020 Sanko System Co.,Ltd.
 */

package jp.co.sankosc.webapiclient

/**
 * @author nozaki
 */
class WebApiClientException(val code: Int, message: String) : Exception(code.toString() + ";" + message) {
}