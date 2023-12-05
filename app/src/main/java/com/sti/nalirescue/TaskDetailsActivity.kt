// TaskDetailsActivity.kt
package com.sti.nalirescue

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigInteger

class TaskDetailsActivity : AppCompatActivity() {
    private lateinit var rescueDetails: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable the back button in the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val taskId = intent.getIntExtra("TASK_ID", -1)
        val taskTitle = intent.getStringExtra("TASK_TITLE")
        val taskDescription = intent.getStringExtra("TASK_DESCRIPTION")
        val tasklocation_ = intent.getStringExtra("TASK_LOCATION")
        val taskDirection = intent.getStringExtra("TASK_DIRECTION")

        // Use these details to populate your TaskDetailsActivity UI
        val titleTextView: TextView = findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = findViewById(R.id.descriptionTextView)
        val locationTextView: TextView = findViewById(R.id.locationTextView)
        val townTextView: TextView = findViewById(R.id.townTextView)
        val buttonDirection: Button = findViewById(R.id.respondButton)
        rescueDetails = findViewById(R.id.editRescueText)


        val sessionManager = SessionManager(this)
        val role = sessionManager.getRoleID()
        val device = sessionManager.getUserID()
        showToast(role.toString())

        val buttonApply: Button = findViewById(R.id.applyButton)

        // Inside your onCreateView method where you set up the OkHttpClient
        val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                // Log the message as desired, e.g., to Android Logcat
                // You can also write it to a file or use any other logging mechanism
                Log.d("API Request", message)
            }
        }).apply {
            level = HttpLoggingInterceptor.Level.BODY // Set the desired logging level
        }
        // Inside onCreateView method

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
// Create an instance of your Retrofit service interface with the custom OkHttpClient
        val apiInterface = Retrofit.Builder()
            .baseUrl("https://naliproject.xyz/api/") // Replace with your CodeIgniter API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // Set the OkHttpClient with the custom logging interceptor
            .build()
            .create(ApiInterface::class.java)



        titleTextView.text = taskTitle
        descriptionTextView.text = taskDescription
        locationTextView.text = tasklocation_
        buttonDirection.setOnClickListener {
            // Replace "https://www.example.com" with the actual URL you want to open
            val url = taskDirection
            Log.e("URL",url.toString())
//
//            // Create an Intent with ACTION_VIEW and the URI of the webpage
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            // Start the activity with the intent
            startActivity(intent)
        }



        val spinner: Spinner = findViewById(R.id.itemSpinner)

// Define an array of items
        val items = arrayOf("RESPONDING", "CANCLED", "DONE")

// Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)

// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

// Apply the adapter to the spinner
        spinner.adapter = adapter





        buttonApply.setOnClickListener {
            // Validate descriptionRescue
            val rescueDets = rescueDetails.text.toString()
            val selectedResp:String = spinner.selectedItem.toString()
    showToast(rescueDets)
            val call = apiInterface.updatetask(
                taskId.toString(),
                rescueDets,
                selectedResp,


            )
            Log.d("DEVICE", "Request URL: ${call.request().url}")
            // Enqueue the call to run asynchronously
            call.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    Log.e("API-UPDATE", response.toString())
                    if (response.code() == 201) { // 201 Created is typically returned for successful registration
                        val apiResponse = response.body()
                        if (apiResponse != null && apiResponse.status == true) {
                            Log.e("API-UPDATE", apiResponse.toString())
                            // Registration was successful
                            Toast.makeText(applicationContext, "${apiResponse.message}", Toast.LENGTH_SHORT).show()
                            // Registration was successful, navigate to LoginActivity
                            finish()
                        } else {
                            Log.e("API", apiResponse.toString())

                        }
                    } else {
                        // Registration request failed
                        Toast.makeText(applicationContext, "Registration request 1 failed", Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    // Registration request failed
                    Toast.makeText(applicationContext, "Registration request failed.", Toast.LENGTH_SHORT).show()
                    t.printStackTrace()
                }
            })

        }
    }


    private fun showToast(message:String){
        Toast.makeText(this, message ,Toast.LENGTH_SHORT).show()
    }

    // Handle the back button press
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
