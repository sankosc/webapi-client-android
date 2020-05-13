package jp.co.sankosc.webapiclientsample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import jp.co.sankosc.webapiclient.WebApiClient

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		findViewById<Button>(R.id.button).setOnClickListener {
            val url = "https://develop.sankosc.co.jp/apitest/api/hello"
            val client = WebApiClient()
			client.get<Message>(url, {
				findViewById<TextView>(R.id.textView).text = it.message
			}, {
                findViewById<TextView>(R.id.textView).text = it.message
            })
		}
	}
}

