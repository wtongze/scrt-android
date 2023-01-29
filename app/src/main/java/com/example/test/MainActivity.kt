package com.example.test

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.health.services.client.HealthServices
import androidx.wear.widget.WearableLinearLayoutManager
import com.example.test.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


class MainActivity : Activity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private var location: Location? = null

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val healthClient = HealthServices.getClient(this)
        val measureClient = healthClient.measureClient
        GlobalScope.launch {
            val capabilities = measureClient.getCapabilitiesAsync().await()
            Log.i("bbb", capabilities.toString())
        }



        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager;
        locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, mainExecutor) { loc ->
            Log.i("bbb", loc.toString())
        }

        binding.swipeRefresh.setOnRefreshListener {
            getLocation()
        }

        checkPermission()
    }

    override fun onResume() {
        binding.root.keepScreenOn = true
        if (location == null) {
            getLocation()
        } else {
            binding.swipeRefresh.isRefreshing = true
            makeRequest()
        }
        super.onResume()
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        binding.loadingText.text = "Getting Location"
        Log.i("aaa", "start get location")
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location == null) {
                            binding.loadingText.text = "Failed to Get Location"
                            Log.i("aaa", "failed to get location")
                            exitProcess(-1)
                        } else {
                            Log.i("aaa", "get last location ${location.toString()}")
                            this.location = location
                            makeRequest()
                        }
                    }
                } else {
                    Log.i("aaa", "get current location ${location.toString()}")
                    this.location = location
                    makeRequest()
                }
            }
    }

    @SuppressLint("SetTextI18n")
    @OptIn(DelicateCoroutinesApi::class)
    private fun makeRequest() {
        val longitude = location!!.longitude
        val latitude = location!!.latitude
        val cruzMetroAPI = EndpointHelper.getInstance().create(RealTimeAPI::class.java)
        Log.i("aaa", "$latitude $longitude")
        binding.loadingText.text = "Getting HTTP"
        GlobalScope.launch {
            Log.i("aaa", "make request")
            val result = cruzMetroAPI.getResultsByLocation(latitude, longitude)
            Log.i("aaa", "finish request")
            Log.i("aaa", result.raw().request().url().toString())
            MainScope().launch {
                if (result.body()?.isNotEmpty() == true) {
                    setList(result.body()!!)
                    binding.swipeRefresh.isRefreshing = false
                } else {
                    exitProcess(-1)
                }
            }
        }
    }

    private fun setList(data: Array<RealTimeResult>) {
        val adapter = ListAdapter(data)
        val manager = WearableLinearLayoutManager(this, CustomScrollingLayoutCallback())
        binding.root.keepScreenOn = false
        binding.wearableRecyclerView.layoutManager = manager
        binding.wearableRecyclerView.isEdgeItemsCenteringEnabled = true
        binding.wearableRecyclerView.adapter = adapter
        binding.wearableRecyclerView.requestFocus()
        binding.loadingLayout.visibility = View.GONE
    }
}