package com.example.ubicompapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var tvGarageDoors: TextView
    private lateinit var tvGarageDoorsValue: TextView
    private lateinit var swGarage: SwitchCompat
    private lateinit var tvBlinds: TextView
    private lateinit var tvBlindsValue: TextView
    private lateinit var swBlinds: SwitchCompat
    private lateinit var tvLights: TextView
    private lateinit var tvLightsValue: TextView
    private lateinit var swLights: SwitchCompat
    private lateinit var tvHvac: TextView
    private lateinit var tvHvacValue: TextView
    private lateinit var swHvac: SwitchCompat
    private lateinit var clEngine: ConstraintLayout
    private lateinit var clLights: ConstraintLayout
    private lateinit var clHvac: ConstraintLayout
    private lateinit var clBlinds: ConstraintLayout
    private lateinit var bottomMenu: BottomNavigationView

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tvGarageDoors = findViewById(R.id.tv_control_garage_doors)
        tvGarageDoorsValue = findViewById(R.id.tv_control_doors_value)
        tvLights = findViewById(R.id.tv_control_lights)
        tvLightsValue = findViewById(R.id.tv_control_lights_value)
        tvHvac = findViewById(R.id.tv_control_hvac)
        tvHvacValue = findViewById(R.id.tv_control_hvac_value)
        tvBlinds = findViewById(R.id.tv_control_blinds)
        tvBlindsValue = findViewById(R.id.tv_control_blinds_value)
        swGarage = findViewById(R.id.sw_garage)
        swLights = findViewById(R.id.sw_lights)
        swHvac = findViewById(R.id.sw_hvac)
        swBlinds = findViewById(R.id.sw_blinds)
        clEngine = findViewById(R.id.cl_garage)
        clLights = findViewById(R.id.cl_lights)
        clHvac = findViewById(R.id.cl_hvac)
        clBlinds = findViewById(R.id.cl_blinds)
        bottomMenu = findViewById(R.id.nav_bottom)

        bottomMenu.menu.findItem(R.id.homeFragment).isChecked = true

        bottomMenu.menu.findItem(R.id.hvacFragment).setOnMenuItemClickListener {
            val intent = Intent(this@HomeActivity , ControlsActivity::class.java)
            startActivity(intent)
            return@setOnMenuItemClickListener false
        }

        bottomMenu.menu.findItem(R.id.assistantFragment).setOnMenuItemClickListener {
            val intent = Intent(this@HomeActivity, AssistantActivity::class.java)
            startActivity(intent)
            return@setOnMenuItemClickListener false
        }

        bottomMenu.menu.findItem(R.id.lightsFragment).setOnMenuItemClickListener {
            val intent = Intent(this@HomeActivity, LightsActivity::class.java)
            startActivity(intent)
            return@setOnMenuItemClickListener false
        }

        if (!isPermissionsGranted(this@HomeActivity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val permissions = mutableSetOf(
                    Manifest.permission.NEARBY_WIFI_DEVICES
                )
                ActivityCompat.requestPermissions(this@HomeActivity, permissions.toTypedArray(), 600)
            }
        }

        requestNearbyWifiDevicesPermission()

        setHomeControls()
    }

    override fun onResume() {
        super.onResume()
        setHomeControls()
    }

    private fun setHomeControls()
    {
        val preferences = this.getSharedPreferences(Constants.PREFERENCE_SMART_HOME, Context.MODE_PRIVATE)
        val edit = preferences.edit()

        if(preferences.getBoolean("garage", false)) {
            swGarage.isChecked = true
            tvGarageDoorsValue.text = swGarage.textOn
        } else {
            swGarage.isChecked = false
            tvLightsValue.text = swGarage.textOff
        }

        if(preferences.getBoolean("lights", false)) {
            swLights.isChecked = true
            tvLightsValue.text = swLights.textOn
        } else {
            swLights.isChecked = false
            tvLightsValue.text = swLights.textOff
        }

        if(!preferences.getBoolean("doors_locked", false)) {
            swBlinds.isChecked = true
            tvBlindsValue.text = swBlinds.textOn
        } else {
            swBlinds.isChecked = false
            tvBlindsValue.text = swBlinds.textOff
        }

        if(preferences.getBoolean("hvac", false)) {
            swHvac.isChecked = true
            tvHvacValue.text = swHvac.textOn
        } else {
            swHvac.isChecked = false
            tvHvacValue.text = swHvac.textOff
        }

        swGarage.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                tvGarageDoorsValue.text = swGarage.textOn
            } else {
                tvGarageDoorsValue.text = swGarage.textOff
            }
            edit.putBoolean("garage", isChecked)
            edit.commit()
        }

        swLights.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                tvLightsValue.text = swLights.textOn
            } else {
                tvLightsValue.text = swLights.textOff
            }
            edit.putBoolean("lights", isChecked)
            edit.commit()
        }

        swHvac.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                tvHvacValue.text = swHvac.textOn
            } else {
                tvHvacValue.text = swHvac.textOff
            }
            edit.putBoolean("hvac", isChecked)
            edit.commit()
        }

        swBlinds.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                tvBlindsValue.text = swBlinds.textOn
            } else {
                tvBlindsValue.text = swBlinds.textOff
            }
            edit.putBoolean("doors_locked", !isChecked)
            edit.commit()
        }
    }

    private fun isPermissionsGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private val isNearbyDevicesPermissionGranted
        get() = hasPermission(Manifest.permission.NEARBY_WIFI_DEVICES)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNearbyWifiDevicesPermission() {
        if(isNearbyDevicesPermissionGranted)
            return
        ActivityCompat.requestPermissions(this@HomeActivity, arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES),
            Constants.LOCAL_NETWORK_CODE
        )
    }

    private fun hasPermission(permissionType: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }
}