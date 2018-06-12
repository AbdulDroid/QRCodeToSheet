package brandit.mac.qrcodetosheet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_scan.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection






class ScanActivity : AppCompatActivity() {

    lateinit var scannedData: String
    private var mToast: Toast? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
/*
        initRetrofit()
*/
        scan_btn.setOnClickListener({
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
            integrator.setPrompt("Scan")
            integrator.setBeepEnabled(false)
            integrator.setCameraId(0)
            integrator.setBarcodeImageEnabled(false)
            integrator.initiateScan()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            scannedData = result.contents
            SendRequest().execute()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    @SuppressLint("StaticFieldLeak")
    inner class SendRequest : AsyncTask<String, Void, String>() {


        override fun onPreExecute() {
            progress_bar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg arg0: String): String {

            try {
                //Enter script URL Here
                val url = URL("https://script.google.com/macros/s/AKfycbz1vJystFs-4p2YTVgt7vOv9g99ll-uHoHvdbIG_qgI00LhCBI/exec")

                val postDataParams = JSONObject()

                //int i;
                //for(i=1;i<=70;i++)


                //    String usn = Integer.toString(i);

                //Passing scanned code as parameter

                postDataParams.put("sdata", scannedData)


                Log.e("params", postDataParams.toString())

                val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
                conn.readTimeout = 15000
                conn.connectTimeout = 15000
                conn.requestMethod = "GET"
                conn.doInput = true
                conn.doOutput = true

                val os = conn.outputStream
                val writer = BufferedWriter(
                        OutputStreamWriter(os, "UTF-8"))
                writer.write(getPostDataString(postDataParams))

                writer.flush()
                writer.close()
                os.close()

                val responseCode = conn.responseCode

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    val input = BufferedReader(InputStreamReader(conn.inputStream))
                    val sb = StringBuffer("")

                    while ((input.readLine()) != null) {

                        sb.append(input.readLine())
                        break
                    }

                    input.close()
                    return sb.toString()

                } else {
                    return "false : $responseCode"
                }
            } catch (e: Exception) {
                return "Exception: " + e.message
            }

        }

        override fun onPostExecute(result: String) {
            progress_bar.visibility = View.GONE
            Log.e("Response", result)
            Toast.makeText(applicationContext, result,
                    Toast.LENGTH_LONG).show()

        }
    }

    private fun getPostDataString(params: JSONObject): String {

        val result = StringBuilder()
        var first = true
        val itr: Iterator<String> = params.keys()

        while (itr.hasNext()) {

            val key: String = itr.next()
            val value: String = params.get(key).toString()

            if (first)
                first = false
            else
                result.append("&")

            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value, "UTF-8"))

        }
        return result.toString()
    }

}
