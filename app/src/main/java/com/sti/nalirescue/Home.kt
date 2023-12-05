package com.sti.nalirescue

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sti.nalirescue.R
import okhttp3.OkHttpClient
import com.sti.nalirescue.CustomDialog
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Home : Fragment() {
    private val BASE_URL = "https://naliproject.xyz/api/"
    private lateinit var adapter: TaskAdapter
    private val CHANNEL_ID = "MyChannel"
    private val NOTIFICATION_ID = 1
    var googleMapsNavigationLink =""
    private val handler = Handler(Looper.getMainLooper())
    private val fetchRunnable = object : Runnable {
        override fun run() {
            // Call the function to fetch data
            fetchData()
            // Schedule the next data fetch after a delay (e.g., 10 seconds)
            handler.postDelayed(this, 10 * 1000) // 10 seconds in milliseconds
        }
    }
    // Declare customOkHttpClient as a class-level property
    private val customOkHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.d("API Request", message)
            }
        }).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        startPeriodicDataFetch()
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

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

        val sessionManager = SessionManager(view.context)
        val role = sessionManager.getRoleID()


        adapter = TaskAdapter { task -> onTaskItemClick(task) }
        recyclerView.adapter = adapter


        return view
    }

    private fun startPeriodicDataFetch() {
        // Start the initial data fetch
        fetchData()
        // Schedule the next data fetch after a delay (e.g., 10 seconds)
        handler.postDelayed(fetchRunnable, 10 * 1000) // 10 seconds in milliseconds
    }
    private fun fetchData() {
        Log.d("FetchData", "Fetching data.")
        val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.d("API Request", message)
            }
        }).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
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

        val sessionManager = SessionManager(requireContext())
        val role = sessionManager.getRoleID()
        val assign = sessionManager.getAssignValue()
        showToast(assign.toString())
        var emer_type = ""
        if (role == 13) {
            emer_type = "PNP"
        } else if (role == 14) {
            emer_type = "MEDICAL"
        }else if (role == 16){
            emer_type = "RELATIVE"
        }

        val call = apiInterface.check(emer_type,assign)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    Log.e("USERINFO", apiResponse.toString())
                    if (apiResponse != null) {
                        if (apiResponse.status == true) {
                            val apiTasks = apiResponse.tasks
                            val tasks = apiTasks.map {
                                Task(
                                    it.taskId.toInt(),
                                    it.taskTitle,
                                    it.device_id,
                                    it.emergency_type,
                                    it.location,
                                    it.link,
                                    it.message,
                                    it.description,
                                    it.type,
                                    it.status,

                                    it.createdDtm
                                )


                            }
                            val existingTasks = adapter.getTasks()
                            // Check if there are new tasks
                            val newTasks = tasks.filterNot { existingTasks.contains(it) }
                            // Update the adapter with the new tasks
                            adapter.setTasks(tasks)
                            val firstDeviceId: String? = tasks.firstOrNull()?.device_id
                            val firstTaskTItle: String? = tasks.firstOrNull()?.taskTitle
                            val firstMessage: String? = tasks.firstOrNull()?.message
                            val location_area : String? = tasks.firstOrNull()?.location
                            val firstLocation: String? = tasks.firstOrNull()?.link


                            // Check if firstLocation is not null and starts with the specified prefix
                            if (!firstLocation.isNullOrBlank() && firstLocation.startsWith("https://www.google.com/maps?q=")) {
                                // Trim the prefix
                                val coordinates = firstLocation.removePrefix("https://www.google.com/maps?q=")

                                 googleMapsNavigationLink = "https://www.google.com/maps/dir/?api=1&destination=$coordinates"
                                Log.e("GPS",googleMapsNavigationLink)
                            } else {
                                // Handle the case where the URL format is not as expected
                                println("Invalid URL format or location is null/blank.")
                            }

                            val firstDate: String? = tasks.firstOrNull()?.createdDtm
                            // Check if new tasks are added
                            if (newTasks.isNotEmpty()) {
                                Log.d("Notification", "New tasks added: ${newTasks.size}")
                                // Notify user about new tasks
                              // Replace with your actual link
                                val customDialog = CustomDialog(requireContext(), googleMapsNavigationLink)
                                customDialog.show()
                                customDialog.setDialogTitle(firstDeviceId.toString().toUpperCase())
                                customDialog.setDialogDetails(firstTaskTItle.toString())
                                customDialog.setDialogLocation(location_area.toString())
                                customDialog.setDialogMessage(firstMessage.toString())
                                customDialog.setDialogDate(firstDate.toString())
                            }
                        } else {
                            showToast("API response indicates failure: ${apiResponse.message}")
                        }
                    } else {
                        showToast("API response is null")
                    }
                } else {
                    showToast("Waiting for new Data")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                // Handle failure here
                showToast("Failed to fetch data from the API")
            }
        })
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    private fun showNotification(nali : String) {
        Log.d("Notification", "Notification is being shown.")
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a Notification Channel (for Android Oreo and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "My Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create an Intent to launch the app when the notification is clicked
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // or FLAG_MUTABLE
        )


        // Create a Notification
        val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.nali)
            .setContentTitle(nali)
            .setContentText("You have new tasks to review.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Set the PendingIntent for the notification
            .setAutoCancel(true) // Automatically removes the notification when clicked
            .build()

        // Show the Notification
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    // Add the onDestroyView method to remove the callback when the fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        // Remove the callback to prevent memory leaks
        handler.removeCallbacks(fetchRunnable)
    }

    private fun onTaskItemClick(task: Task) {
        // Open TaskDetailsActivity and pass task details
        val intent = Intent(requireContext(), TaskDetailsActivity::class.java)
        intent.putExtra("TASK_ID", task.taskId)
        intent.putExtra("TASK_TITLE", task.taskTitle)
        intent.putExtra("TASK_DESCRIPTION", task.message)
        intent.putExtra("TASK_LOCATION", task.location)
        intent.putExtra("TASK_DIRECTION",googleMapsNavigationLink)
        intent.putExtra("TASK_LINK",task.link)
        startActivity(intent)
    }
}