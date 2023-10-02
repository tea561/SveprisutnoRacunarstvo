package com.example.ubicompapplication

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ubicompapplication.Constants.Companion.LED_SERVICE
import com.example.ubicompapplication.Constants.Companion.LOCAL_NETWORK_CODE
import com.example.ubicompapplication.Constants.Companion.NOTIFICATION_CODE
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.math.BigInteger
import java.util.UUID


@SuppressLint("MissingPermission")
class LightsActivity : AppCompatActivity() {

    private lateinit var llTopControls: LinearLayout
    private lateinit var bottomMenu: BottomNavigationView
    private lateinit var preferences: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor
    private lateinit var bluetoothGatt: BluetoothGatt
    private lateinit var redButton: Button
    private lateinit var greenButton: Button
    private lateinit var blueButton: Button
    private lateinit var btnScan: ImageView
    private lateinit var tvScan: TextView
    private lateinit var svScan: ScrollView
    private lateinit var rvScannedDevices: RecyclerView
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var scanResults =  mutableListOf<ScanResult>()
    private val scanResultAdapter: BleDeviceAdapter by lazy {
        BleDeviceAdapter(scanResults) { result ->
            if (isScanning) {
                stopBleScan()
            }
            with(result.device){
                Log.w("ScanResultAdapter", "Connecting to $address")
                connectGatt(this@LightsActivity, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lights)

        llTopControls = findViewById(R.id.ll_activate)
        bottomMenu = findViewById(R.id.nav_bottom)
        btnScan = findViewById(R.id.iv_ble)
        tvScan = findViewById(R.id.tv_tap_to_connect)
        svScan = findViewById(R.id.sv_scanned_devices)
        rvScannedDevices = findViewById(R.id.rv_scanned_devices)
        redButton = findViewById(R.id.red_button)
        greenButton = findViewById(R.id.green_button)
        blueButton = findViewById(R.id.blue_button)


        btnScan.setOnClickListener {
            if(bluetoothAdapter.isEnabled)
            {
                if(isScanning){
                    llTopControls.visibility = View.GONE
                    svScan.visibility = View.VISIBLE
                    rvScannedDevices.visibility = View.VISIBLE
                    tvScan.text = "Tap to stop scan"
                    rvScannedDevices.adapter = scanResultAdapter
                    rvScannedDevices.layoutManager = LinearLayoutManager(this@LightsActivity)
                    scanResultAdapter.setOnItemClickListener(object : BleDeviceAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            Log.w("ScanResultAdapter", "Connecting to ${scanResults[position].device.address}")
                            scanResults[position].device.connectGatt(this@LightsActivity, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
                            Toast.makeText(this@LightsActivity, "Successfully connected ${scanResults[position].device.name} [${scanResults[position].device.address}]", Toast.LENGTH_SHORT).show()
                        }
                    })
                    stopBleScan()
                }
                else {
                    tvScan.text = "Tap to connect sensors"
                    llTopControls.visibility = View.VISIBLE
                    svScan.visibility = View.GONE
                    rvScannedDevices.visibility = View.GONE
                    startBleScan()
                }
            }
        }

        redButton.setOnClickListener {
            Log.d("SmartLamp", "Red color")
            //if(this::bluetoothGatt.isInitialized)
            sendIntCommand(bluetoothGatt, 3)
        }

        greenButton.setOnClickListener {
            Log.d("SmartLamp", "Green color")
            if(this::bluetoothGatt.isInitialized)
                sendIntCommand(bluetoothGatt, 4)
        }

        blueButton.setOnClickListener {
            Log.d("SmartLamp", "Blue color")
            if(this::bluetoothGatt.isInitialized)
                sendIntCommand(bluetoothGatt, 5)
        }

        bottomMenu.menu.findItem(R.id.lightsFragment).isChecked = true

        bottomMenu.menu.findItem(R.id.homeFragment).setOnMenuItemClickListener {
            val intent = Intent(this@LightsActivity , HomeActivity::class.java)
            startActivity(intent)
            return@setOnMenuItemClickListener false
        }

        bottomMenu.menu.findItem(R.id.hvacFragment).setOnMenuItemClickListener {
            val intent = Intent(this@LightsActivity, ControlsActivity::class.java)
            startActivity(intent)
            return@setOnMenuItemClickListener false
        }

        bottomMenu.menu.findItem(R.id.assistantFragment).setOnMenuItemClickListener {
            val intent = Intent(this@LightsActivity, AssistantActivity::class.java)
            startActivity(intent)
            return@setOnMenuItemClickListener false
        }

        val bluetoothManager: BluetoothManager? = ContextCompat.getSystemService(applicationContext, BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager!!.adapter
        bleScanner = bluetoothAdapter.bluetoothLeScanner

        checkBTState()

        preferences = this.getSharedPreferences(Constants.PREFERENCE_SMART_HOME, Context.MODE_PRIVATE)
        edit = preferences.edit()

    }


    private val gattCallback = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i("BluetoothGattCallback", "Read characteristic $uuid:\n${value.toHexString()}")
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Read not permitted for $uuid!")
                    }
                    else -> {
                        Log.e("BluetoothGattCallback", "Characteristic read failed for $uuid, error: $status")
                    }
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i("BluetoothGattCallback", "Wrote to characteristic ${this?.uuid} | value: ${this?.value?.toHexString()}")
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.e("BluetoothGattCallback", "Write exceeded connection ATT MTU!")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback", "Write not permitted for ${this?.uuid}!")
                    }
                    else -> {
                        Log.e("BluetoothGattCallback", "Characteristic write failed for ${this?.uuid}, error: $status")
                    }
                }
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    bluetoothGatt = gatt
                    Log.w("BluetoothGattCallback", "Gatt: $bluetoothGatt")
                    Handler(Looper.getMainLooper()).post {
                        bluetoothGatt.discoverServices()
                        Log.w("BluetoothGattCallback", "discover called")
                    }
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        // send 2 for blinking LED to notify user that connection was successful
//                        //sendIntCommand(bluetoothGatt, 2)
//                    }, 6000)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w("GATT", "$newState")
                Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                //bluetoothGatt = gatt
                Log.w("BluetoothGattCallback", "Discovered ${services.size} services for ${device.address}")
                printGattTable()
                readChValue()
            }
        }
    }

    private lateinit var bleScanner: BluetoothLeScanner

    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) {
                scanResults[indexQuery] = result
            } else {
                with(result.device) {
                    Log.i("ScanCallback", "Found BLE device! Name: ${result.device.name ?: "Unnamed"}, address: $address")
                }
                scanResults.add(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallback", "onScanFailed: code $errorCode")
        }
    }
    private var isScanning: Boolean = false

    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first?")
            return
        }
        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                separator = "\n|--",
                prefix = "|--"
            ) { it.uuid.toString() }
            Log.i("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable"
            )
        }
    }

    private fun hasPermission(permissionType: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun isPermissionsGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startBleScan() {
        if (!isLocationPermissionGranted) {
            requestLocationPermission()
        }
        else {
            scanResults.clear()
            scanResultAdapter.notifyDataSetChanged()
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private fun requestLocationPermission() {
        if(isLocationPermissionGranted)
            return
        ActivityCompat.requestPermissions(this@LightsActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            Constants.LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    private fun BluetoothGattCharacteristic.isWritable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    private fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    private fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }

    private fun readChValue() {
        val characteristic = bluetoothGatt.getService(UUID.fromString(LED_SERVICE))?.getCharacteristic(UUID.fromString(Constants.BUTTON_CHARACTERISTIC))
        if (characteristic?.isReadable() == true) {
            bluetoothGatt.readCharacteristic(characteristic)
        }
    }

    private fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        val writeType = when {
            characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> error("Characteristic ${characteristic.uuid} cannot be written to")
        }

        bluetoothGatt.let { gatt ->
            characteristic.writeType = writeType
            characteristic.value = payload
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(characteristic, payload, writeType)
            } else {
                gatt.writeCharacteristic(characteristic)?: error("Not connected to a BLE device!")
            }
        }
    }

    fun ByteArray.toHexString(): String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }

    override fun onDestroy() {
        super.onDestroy()
        sendIntCommand(bluetoothGatt, 0)
    }

//    private fun startForegroundService() {
//        val serviceIntent = Intent(this, StabilityService::class.java)
//        startService(serviceIntent)
//    }
//
//    private fun stopForegroundService() {
//        val serviceIntent = Intent(this, StabilityService::class.java)
//        stopService(serviceIntent)
//    }

    private fun readRuleValue(payload: String, key: String): ArrayList<String> {
        val listOfValues = arrayListOf<String>()
        val jsonObject: JsonObject = Json.decodeFromString(payload)
        val values = jsonObject[key].toString()
        listOfValues.add(values.split(',').toString().replace(" ", ""))
        return listOfValues
    }

    private fun readSingleRuleValue(payload: String, key: String): String {
        val jsonObject: JsonObject = Json.decodeFromString(payload)
        return jsonObject[key].toString()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
                }
            }
            NOTIFICATION_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Notification permission denied!", Toast.LENGTH_SHORT).show()
                }
            }
            LOCAL_NETWORK_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Local network permission granted!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Local network permission denied!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendIntCommand(bleGatt: BluetoothGatt, input: Int) {
        writeCharacteristic(bleGatt.getService(UUID.fromString(LED_SERVICE)).getCharacteristic(UUID.fromString(Constants.LED_CHARACTERISTIC)), BigInteger.valueOf(input.toLong()).toByteArray())
    }

    private fun requiredPermissions(): Array<String>{
        val sdkVersion: Int = this@LightsActivity.applicationInfo.targetSdkVersion
        //android 12
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && sdkVersion >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        //android 9 and lower
        else if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && sdkVersion <= Build.VERSION_CODES.P){
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun checkBTState(){
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this@LightsActivity, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
        }
        else {
            if (!bluetoothAdapter.isEnabled) {
                Toast.makeText(this@LightsActivity, "You need to enable Bluetooth.", Toast.LENGTH_SHORT)
                    .show()
//                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                try {
//                    activity?.startActivityFromFragment(this, enableBtIntent, REQUEST_ENABLE_BT)
//                } catch (e: ActivityNotFoundException) {
//                    // display error state to the user
//                }
            } else {
                val permission: String = requiredPermissions()[0]
                if (ActivityCompat.checkSelfPermission(
                        this@LightsActivity,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this@LightsActivity, requiredPermissions(), 1)
                    return
                }
                if (bluetoothAdapter.isDiscovering) {
                    Toast.makeText(
                        this@LightsActivity,
                        "Device discovering process...",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this@LightsActivity, "Bluetooth enabled.", Toast.LENGTH_SHORT)
                        .show()
                }

            }
        }
    }

}