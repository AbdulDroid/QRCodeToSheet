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
import pub.devrel.easypermissions.EasyPermissions
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection






class ScanActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

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


//    private fun updateSpreadSheet() {
//        val f_name: String = name.text.toString()
//        val name_size: Int = f_name.split(" ").size
//        val u_email: String = email.text.toString()
//        val gen: String = gender.text.toString()
//        val u_age: String = age.text.toString()
//        val u_height: String = height.text.toString()
//        val u_origin: String = origin.text.toString()
//
//        if (TextUtils.isEmpty(f_name) || name_size < 2) {
//            name.error = ("Please enter your full name")
//            name.requestFocus()
//        } else if (TextUtils.isEmpty(u_email) || !Patterns.EMAIL_ADDRESS.matcher(u_email).matches()) {
//            email.error = ("Please enter a valid email address")
//            email.requestFocus()
//        } else if (TextUtils.isEmpty(gen)) {
//            gender.error = ("Please enter your gender")
//            gender.requestFocus()
//        } else if (TextUtils.isEmpty(u_age) || u_age.toInt() < 13) {
//            age.error = ("Please enter age above 13 to continue")
//            age.requestFocus()
//        } else if (TextUtils.isEmpty(u_height)) {
//            height.error = ("Please enter your height in cm")
//            height.requestFocus()
//        } else if (TextUtils.isEmpty(u_origin)) {
//            origin.error = ("Please enter your state of origin")
//            origin.requestFocus()
//        } else {
//            progress_bar.visibility = View.VISIBLE
//            apiService.response(f_name, u_email, gen, u_age, u_height, u_origin)
//                    .enqueue(object : Callback<ResponseBody> {
//                        override fun onResponse(@NonNull call: Call<ResponseBody>?, @NonNull response: Response<ResponseBody>?) {
//                            if (response!!.isSuccessful)
//                                onSheetUpdated(response.body().toString())
//                            else
//                                onError(response.errorBody()!!.string())
//
//                            Log.e("Response", "" + response.code() + ": " + response.toString())
//                        }
//
//                        override fun onFailure(@NonNull call: Call<ResponseBody>?, @NonNull t: Throwable?) {
//                            Log.e("Response", t!!.message, t)
//                        }
//                    })
//        }
//    }

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
                val url = URL("https://script.google.com/macros/s/AKfycbwA4GnUWHQwvRX635hI009yPaTeS5svwsn3FHPxMSLoGb4XY3g/dev")

                val postDataParams = JSONObject()

                //int i;
                //for(i=1;i<=70;i++)


                //    String usn = Integer.toString(i);

                //Passing scanned code as parameter

                postDataParams.put("sdata", scannedData)/*
                postDataParams.put("email", email.text.toString())
                postDataParams.put("gender", gender.text.toString())
                postDataParams.put("age", age.text.toString())
                postDataParams.put("height", height.text.toString())
                postDataParams.put("origin", origin.text.toString())*/


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
            result.append(URLEncoder.encode(value.toString(), "UTF-8"))

        }
        return result.toString()
    }

    /*private fun initRetrofit() {
        val gson: Gson = GsonBuilder()
                .setLenient()
                .create()
        val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()

        val netInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(ApiService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient
                        .addNetworkInterceptor(netInterceptor)
                        .connectTimeout(100, TimeUnit.SECONDS)
                        .readTimeout(100, TimeUnit.SECONDS).build())
                .build()

        apiService = retrofit.create(ApiService::class.java)
    }*/
}
