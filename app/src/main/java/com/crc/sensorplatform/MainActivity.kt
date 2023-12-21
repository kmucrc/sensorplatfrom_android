package com.crc.sensorplatform

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.crc.sensorplatform.base.*
import com.crc.sensorplatform.database.*
import com.crc.sensorplatform.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Math.*
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val mBtHandler = BluetoothHandler()
    private val mBluetoothClassicManager: BluetoothClassicManager =
        BluetoothClassicManager.getInstance()
    private var mIsConnected = false

    lateinit var retrofit : Retrofit
//    var mBluetoothAdapter: BluetoothAdapter? = null
//    var mPairedDevices: MutableSet<BluetoothDevice>? = null
//    var mListPairedDevices: ArrayList<String>? = null
//
//
////    var mBluetoothHandler: Handler? = null
//    private var mThreadConnectedBluetooth: ConnectedBluetoothThread? = null
//    var mBluetoothDevice: BluetoothDevice? = null
//    var mBluetoothSocket: BluetoothSocket? = null
//
//    var pre_device_name = ""
//    val BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//
//    val BT_REQUEST_ENABLE = 1
//
//    val BT_MESSAGE_READ = 2
//
//    val BT_CONNECTING_STATUS = 3

    private lateinit var accelerometerDao : AccelerometerDao
    private lateinit var athleticsDao : AthleticsDao
    lateinit var appContext : Context

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothGatt: BluetoothGatt

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Connected to the BLE device, start discovering services
                gatt.discoverServices()
                binding.tvChestStat.text = getString(R.string.str_main_connect)
//                binding.tvChestStat.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.red)))
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Disconnected from the BLE device, clean up resources
                bluetoothGatt.close()

                binding.tvChestStat.text = getString(R.string.str_main_connect)
//                binding.tvChestStat.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.gray)))
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Services discovered, retrieve the desired service and characteristic
                val service = gatt.getService(CHESTPOD_SERVICE_UUID)
                val characteristic = service?.getCharacteristic(CHESTPOD_CHARACTERISTIC_UUID)

                // Enable notifications on the characteristic
                gatt.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic?.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            // Handle the received data here
            val receivedData = characteristic.value
            // Process the received data as needed
//            Log.e("eleutheria", "receivedData : ${bytesToHexString(receivedData)}")
//            Log.e("eleutheria", "receivedData")

            val receivedString = receivedData.toString(StandardCharsets.UTF_8)

//            Log.e("eleutheria", "receivedString : ${receivedString}")

            if(receivedString.contains("humi")) {
                Constants.strReceivedData += receivedString

                val strData = Constants.strReceivedData
                Constants.strReceivedData = ""

                val jsonObject = JSONObject()

                val keyValuePairList = strData.split("/")

                for (pair in keyValuePairList) {
                    val keyValue = pair.split(":")
                    if (keyValue.size == 2) {
                        val key = keyValue[0].trim()
                        val value = keyValue[1].trim()
                        jsonObject.put(key, value)
                    }
                }

                val jsonString = jsonObject.toString()
//                Log.e("eleutheria", "jsonString : ${jsonString}")

//                sendNetworkData(jsonString)



                val axValue = jsonObject.getString("Ax")
                val ayValue = jsonObject.getString("Ay")
                val azValue = jsonObject.getString("Az")
                val gxValue = jsonObject.getString("Gx")
                val gyValue = jsonObject.getString("Gy")
                val gzValue = jsonObject.getString("Gz")
                val btempValue = jsonObject.getString("btemp")
                val tempValue = jsonObject.getString("temp")
                val humiValue = jsonObject.getString("humi")

                Constants.strBodyTemp = btempValue
                Constants.strTemp = tempValue
                Constants.strHumi = humiValue



                calculateStep(axValue.toFloat(), ayValue.toFloat(), azValue.toFloat())


//                Constants.fAccelX += axValue.toFloat()
//                Constants.fAccelY += ayValue.toFloat()
//                Constants.fAccelZ += azValue.toFloat()

                saveNetworkData(jsonObject)

//                saveAccelData(axValue.toFloat(), ayValue.toFloat(), azValue.toFloat() )

                runOnUiThread {
                    binding.tvAccelXValue.text = axValue
                    binding.tvAccelYValue.text = ayValue
                    binding.tvAccelZValue.text = azValue
                    binding.tvGyroXValue.text = gxValue
                    binding.tvGyroYValue.text = gyValue
                    binding.tvGyroZValue.text = gzValue
                    binding.tvBodyTempValue.text = btempValue + " ℃"
                    binding.tvTempValue.text = tempValue + " ℃"
                    binding.tvHumiValue.text = humiValue + " %"

                    Log.e("eleutheria", "btempValue : ${btempValue}, tempValue : ${tempValue}, humiValue : ${humiValue}")

//                    Log.e("eleutheria", "strData : ${strData}")
//                    Log.e("eleutheria", "Constants.strReceivedData : ${Constants.strReceivedData}")
                }

            } else {
                Constants.strReceivedData += receivedString
            }
        }
    }

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                if (device.name == CHESTPOD_DEVICE_NAME) {
                    // Stop scanning
                    bluetoothLeScanner.stopScan(this)
                    // Connect to the device
                    bluetoothGatt = device.connectGatt(this@MainActivity, false, bluetoothGattCallback)
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            // Handle batch scan results if needed
        }

        override fun onScanFailed(errorCode: Int) {
            // Handle scan failure if needed
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_main)

        val database = AppDatabase.getInstance(applicationContext)
        accelerometerDao = database.accelerometerDao()


        val dbAthletics = AthleticsDatabase.getInstance(applicationContext)
        athleticsDao = dbAthletics.AthleticsDao()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Constants.latitude = location.latitude
                Constants.longitude = location.longitude

                Toast.makeText(applicationContext, "gps : lat : ${Constants.latitude}, lon : ${Constants.longitude}", Toast.LENGTH_SHORT).show()

//                Log.e("eleutheria", "lat : $Constants.latitude, lon : ${Constants.longitude}")
                // Use the latitude and longitude values as needed
                // This code will be called whenever a new location update is received
            }
        }

        // Initialize Bluetooth adapter and manager
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Check if Bluetooth is supported on the device
        if (bluetoothAdapter == null) {
            // Bluetooth is not supported, handle accordingly
            return
        }

        mBluetoothClassicManager.setHandler(mBtHandler)

        // Register for broadcasts when a device is discovered
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        this.registerReceiver(mReceiver, filter)

        // Register for broadcasts when discovery has finished
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(mReceiver, filter)


        // Register for broadcasts when a device is discovered
        filter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        this.registerReceiver(mReceiver, filter)

//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        retrofit = Retrofit.Builder()
            .baseUrl("https://mp4xmp5830.execute-api.ap-northeast-2.amazonaws.com/") // replace with your API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        binding.btConnect.setOnClickListener {
            Log.e("eleutheria", "BLE Address : ${Constants.strChestPodAddress}")
            doDiscovery()
            startBleScan()
        }

        binding.btStart.setOnClickListener {
//            startChestPod()
//            setAcceleData()
//            sendNetworkData()

        }

        appContext = applicationContext
//        setFakeDataDisplay()
    }

    private fun doDiscovery() {

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true)

        // If we're already discovering, stop it
        if (mBluetoothClassicManager.isDiscovering()) {
            mBluetoothClassicManager.cancelDiscovery()
        }

        // Request discover from BluetoothAdapter
        mBluetoothClassicManager.startDiscovery()

    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_UPDATE_INTERVAL,
                LOCATION_UPDATE_DISTANCE,
                locationListener
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onPause() {
        super.onPause()

        locationManager.removeUpdates(locationListener)
    }

    override fun onDestroy() {
        super.onDestroy()

        stopBleScan()
    }

    @SuppressLint("MissingPermission")
    private fun startBleScan() {
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val scanFilter = ScanFilter.Builder()
            // Optional: Set device name or other filters
            // .setDeviceName("Your Device Name")
            // .setDeviceAddress("00:00:00:00:00:00")
            .build()

        val scanFilters = listOf(scanFilter)

        val scanCallbackHandler = Handler(Looper.getMainLooper())

        bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {

        bluetoothLeScanner.stopScan(scanCallback)
    }

    private val mReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val strDeviceAddress = device!!.address
                Constants.strDeviceAddress = strDeviceAddress
                Log.e("eleutheria", "address : ${strDeviceAddress}")

                if (strDeviceAddress.equals(Constants.strOximetryAddress)) {
                    Log.e("eleutheria", "find device Oximetry, Address : ${Constants.strOximetryAddress}")
                    if (mBluetoothClassicManager.state != 2) {
                        mBluetoothClassicManager.connect(Constants.strDeviceAddress)
                    }
                }

                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                setProgressBarIndeterminateVisibility(false)
            }

            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED == action) {
                val scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1)
                val prevMode = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, -1)
                when (scanMode) {
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> {
                        mBluetoothClassicManager.start()
                        Log.e("eleutheria", "SCAN_MODE_CONNECTABLE_DISCOVERABLE")
                    }
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE -> {
                        Log.e("eleutheria", "SCAN_MODE_CONNECTABLE")
                    }
                    BluetoothAdapter.SCAN_MODE_NONE -> {
                        // Bluetooth is not enabled
                        Log.e("eleutheria", "SCAN_MODE_NONE")
                    }
                }
            }
        }
    }

    inner class BluetoothHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BluetoothClassicManager.MESSAGE_READ -> {
                    if (msg.obj != null) {

                        val readBuf = msg.obj as ByteArray
                        // construct a string from the valid bytes in the buffer
                        val readMessage = String(readBuf, 0, msg.arg1)
                        Log.e("eleutheria", "MESSAGE_READ : $readMessage")

//                        sendMessageToActivity(readMessage)
                        val byteHR = ByteBuffer.wrap(byteArrayOf(readBuf.get(1), readBuf.get(0)))
                        val byteSpO2 = ByteBuffer.wrap(byteArrayOf(readBuf.get(3), readBuf.get(2)))
                        val byteHba1c = ByteBuffer.wrap(
                            byteArrayOf(
                                readBuf.get(7),
                                readBuf.get(6),
                                readBuf.get(5),
                                readBuf.get(4)
                            )
                        )

                        val intHR = byteHR.getShort()
                        val intSpO2 = byteSpO2.getShort()
                        val floatHbA1c = byteHba1c.getFloat()

                        Constants.strHbA1c = floatHbA1c.toString()
                        Constants.strSpo2 = intSpO2.toString()
                        Constants.strHeartRate = intHR.toString()

                        Log.e(
                            "eleutheria",
                            "intHR : $intHR, intSpO2 : $intSpO2, floatHbA1c : $floatHbA1c"
                        )

                        binding.tvHbA1cValue.text = floatHbA1c.toString() + " %"
                        binding.tvSpo2Value.text = intSpO2.toString() + " %"
                        binding.tvHRValue.text = intHR.toString() + " BPM"
                    }
                }
                BluetoothClassicManager.MESSAGE_STATE_CHANGE -> {
                    when (msg.arg1) {
                        BluetoothClassicManager.STATE_NONE -> {    // we're doing nothing
                            Log.e("eleutheria", "STATE_NONE")
                            mIsConnected = false
                            binding.tvSpo2Stat.text = getString(R.string.str_main_connect)
//                            binding.tvSpo2Stat.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.gray)))
                        }
                        BluetoothClassicManager.STATE_LISTEN -> {  // now listening for incoming connections
                            Log.e("eleutheria", "STATE_LISTEN")
                            mIsConnected = false
                        }
                        BluetoothClassicManager.STATE_CONNECTING -> {  // connecting to remote
                            Log.e("eleutheria", "STATE_CONNECTING")

                        }
                        BluetoothClassicManager.STATE_CONNECTED -> {   // now connected to a remote device
                            Log.e("eleutheria", "STATE_CONNECTED")
                            mIsConnected = true

                            binding.tvSpo2Stat.text = getString(R.string.str_main_connect)
//                            binding.tvSpo2Stat.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.red)))
//                            moveMainActivity()
                        }
                    }
                }
                BluetoothClassicManager.MESSAGE_DEVICE_NAME -> {
                    if (msg.data != null) {
                        Log.e("eleutheria", "MESSAGE_DEVICE_NAME")
                    }
                }
            }

            super.handleMessage(msg)
        }
    }

    private fun saveAccelData(AccelX : Float, AccelY : Float, AccelZ : Float) {

        val currentTime = System.currentTimeMillis()

//        Log.e("eleutheria", "currentTime : $currentTime")
//        Log.e("eleutheriaX", "AccelX : $AccelX")
//        Log.e("eleutheriaY", "AccelY : $AccelY")
//        Log.e("eleutheriaZ", "AccelZ : $AccelZ")
//        Log.e("eleutheria", "AccelX : $AccelX, AccelY : $AccelY, AccelZ : $AccelZ, fAccelX : ${Constants.fAccelX}, fAccelY : ${Constants.fAccelY}, fAccelZ : ${Constants.fAccelZ}")
//        val accelroData = Accelerometer(0, currentTime, AccelX, AccelY, AccelZ)
        val accelroData = Accelerometer(0, currentTime, AccelX, AccelY, AccelZ, Constants.fAccelX, Constants.fAccelY, Constants.fAccelZ)

        lifecycleScope.launch(Dispatchers.IO) {

            accelerometerDao.insertAll(accelroData)
//            val data = accelerometerDao.getAll(currentTime)
//            Log.e("eleutheria", "DB data : $data")
        }
    }

    private fun saveAthleticsData() {

        val currentTime = System.currentTimeMillis()

        val athleticsData = Athletics(0, currentTime, Constants.strBodyTemp.toFloat(), Constants.strTemp.toFloat(), Constants.strHumi.toFloat(), Constants.strHbA1c.toFloat(), Constants.strSpo2.toInt(), Constants.strHeartRate.toInt())

        lifecycleScope.launch(Dispatchers.IO) {

            athleticsDao.insertAll(athleticsData)
        }
    }

    fun calculateStep(x: Float, y: Float, z: Float) {

        val accel = x * x + y * y + z * z
        val delta = sqrt(accel.toDouble()) - Constants.dLastAccel
        Constants.dLastAccel = sqrt(accel.toDouble())

        if (delta > 2) {
            Constants.strStep += 1
        }
    }

    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val radius = 6371.0 // Radius of the earth in kilometers. Use 3956 for miles

        val latDistance = toRadians(lat2 - lat1)
        val lonDistance = toRadians(lon2 - lon1)

        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(toRadians(lat1)) * cos(toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return radius * c
    }

    @SuppressLint("MissingPermission")
    fun saveNetworkData(jsonObject : JSONObject) {


        Constants.NEW_CURRENT_TIME = System.currentTimeMillis()

        if(Constants.OLD_CURRENT_TIME == 0L) {
            Constants.OLD_CURRENT_TIME = Constants.NEW_CURRENT_TIME
        }

        if(Constants.NEW_CURRENT_TIME >= Constants.OLD_CURRENT_TIME + Constants.STANDARD_TEN_SECONDS) {

            Constants.OLD_CURRENT_TIME = Constants.NEW_CURRENT_TIME

            val resultDistance = calculateDistance(Constants.latitude, Constants.longitude, Constants.oldLatitude, Constants.oldLongitude)
            Constants.oldLatitude = Constants.latitude
            Constants.oldLongitude = Constants.longitude
            Constants.strDistance = resultDistance

            val bodytempJsonObject = JSONObject()
            val tempJsonObject = JSONObject()
            val humiJsonObject = JSONObject()
            val stepJsonObject = JSONObject()
            val distanceJsonObject = JSONObject()
            val hba1cJsonObject = JSONObject()
            val spo2JsonObject = JSONObject()
            val heartrateJsonObject = JSONObject()


            try {
                bodytempJsonObject.put("sensortype", Constants.INDEX_BODYTEMPERATURE)
                bodytempJsonObject.put("value", Constants.strBodyTemp)
            } catch (e: JSONException) {
                Log.e("eleutheria", "bodytemp Error : $e")
            }

            try {
                tempJsonObject.put("sensortype", Constants.INDEX_TEMPERATURE)
                tempJsonObject.put("value", Constants.strTemp)
            } catch (e: JSONException) {
                Log.e("eleutheria", "temp Error : $e")
            }

            try {
                humiJsonObject.put("sensortype", Constants.INDEX_HUMIDITY)
                humiJsonObject.put("value", Constants.strHumi)
            } catch (e: JSONException) {
                Log.e("eleutheria", "Humidity Error : $e")
            }

            try {
                stepJsonObject.put("sensortype", Constants.INDEX_STEP)
                stepJsonObject.put("value", Constants.strStep)
            } catch (e: JSONException) {
                Log.e("eleutheria", "step Error : $e")
            }

            try {
                distanceJsonObject.put("sensortype", Constants.INDEX_DISTANCE)
                distanceJsonObject.put("value", Constants.strDistance)
            } catch (e: JSONException) {
                Log.e("eleutheria", "distance Error : $e")
            }

            try {
                hba1cJsonObject.put("sensortype", Constants.INDEX_HBA1C)
                hba1cJsonObject.put("value", Constants.strHbA1c)
            } catch (e: JSONException) {
                Log.e("eleutheria", "hba1c Error : $e")
            }

            try {
                spo2JsonObject.put("sensortype", Constants.INDEX_SPO2)
                spo2JsonObject.put("value", Constants.strSpo2)
            } catch (e: JSONException) {
                Log.e("eleutheria", "spo2 Error : $e")
            }

            try {
                heartrateJsonObject.put("sensortype", Constants.INDEX_HEARTRATE)
                heartrateJsonObject.put("value", Constants.strHeartRate)
            } catch (e: JSONException) {
                Log.e("eleutheria", "heartrate Error : $e")
            }

            val chestJsonObject = JSONObject()

            try {
                chestJsonObject.put("groupid", Constants.nSelUser)
                chestJsonObject.put("gps", "${Constants.latitude},${Constants.longitude}")
                chestJsonObject.put("bodytemp", bodytempJsonObject)
                chestJsonObject.put("temp", tempJsonObject)
                chestJsonObject.put("humi", humiJsonObject)
                chestJsonObject.put("step", stepJsonObject)
                chestJsonObject.put("distance", distanceJsonObject)
                chestJsonObject.put("hba1c", hba1cJsonObject)
                chestJsonObject.put("spo2", spo2JsonObject)
                chestJsonObject.put("heartrate", heartrateJsonObject)

                Log.e("eleutheria", "chestJsonObject  : ${chestJsonObject.toString()}")
                saveAthleticsData()
            } catch (e: JSONException) {
                Log.e("eleutheria", "chestJsonObject Error : $e")
            }
        }

//        {
//            "groupid": "3",
//            "gps": "37.61200929,126.99483306",
//            "bodytemp": {
//                "sensortype": "16",
//                "value": " 29.96"
//            },
//            "temp": {
//            "sensortype": "17",
//            "value": " 31.17"
//            },
//            "humi": {
//            "sensortype": "17",
//            "value": " 31.17"
//            },
//            "step": {
//            "sensortype": "17",
//            "value": " 31.17"
//            },
//            "distance": {
//            "sensortype": "17",
//            "value": " 31.17"
//            },
//            "hba1c": {
//            "sensortype": "17",
//            "value": " 31.17"
//            },
//            "spo2": {
//            "sensortype": "17",
//            "value": " 31.17"
//            },
//            "heartrate": {
//            "sensortype": "17",
//            "value": " 31.17"
//            }
//        }

//        val keyValuePairList = strData.split("/")
//
//        for (pair in keyValuePairList) {
//            val keyValue = pair.split(":")
//            if (keyValue.size == 2) {
//                val key = keyValue[0].trim()
//                val value = keyValue[1].trim()
//                jsonObject.put(key, value)
//            }
//        }

    }

//    private fun sendNetworkData(jsonString: String) {
    private fun sendNetworkData() {
        val jsonObject = JSONObject()
        jsonObject.put("user", 1)
//        jsonObject.put("gps", "37.1322, 42.9382")
//        jsonObject.put("gx", 0.3)
//        jsonObject.put("gy", 2.3)
//        jsonObject.put("gz", 1.7)

        val workoutData = WorkOut(jsonObject)

        val apiService = retrofit.create(ApiService::class.java)
        val workoutResponse = apiService.getData(workoutData).enqueue(object : Callback<DataResponse> {
            override fun onResponse(call: Call<DataResponse>, response: Response<DataResponse>) {
                if (response.isSuccessful) {
//                    val dataResponse = response.body()
                    val bodyData = response.body()?.body
                    val gson = Gson()
                    val bodyJsonString = Gson().toJson(bodyData)
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    val bodyMap: Map<String, String> = gson.fromJson(bodyJsonString, type)


//                    val responseBodyJson = response.body().get("body")
//                    val bodyData = Gson().fromJson(responseBodyJson, object : TypeToken<Map<String, String>>() {}.type)
                    Log.e("eleutheria", "bodyMap : $bodyMap")
                    // Process the response data
                } else {
                    // Handle error
                    Log.e("eleutheria", "Handle error")
                }
            }

            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                // Handle failure
                Log.e("eleutheria", "onFailure")
            }
        })
        Log.e("eleutheria", "workoutResponse : $workoutResponse")
    }

    fun setAcceleData() {

        val currentTime = System.currentTimeMillis()
        val random = Random()
        val randomXFloat = random.nextFloat()
        val randomYFloat = random.nextFloat()
        val randomZFloat = random.nextFloat()

        val accelroData = Accelerometer(0, currentTime, randomXFloat, randomYFloat, randomZFloat, randomXFloat, randomYFloat, randomZFloat)

        lifecycleScope.launch(Dispatchers.IO) {

            accelerometerDao.insertAll(accelroData)
            val data = accelerometerDao.getAll(currentTime)
            Log.e("eleutheria", "DB data : $data")
        }
    }

    fun setFakeDataDisplay() {
        binding.tvHbA1cValue.text = "5.6 %"
        binding.tvSpo2Value.text = "98 %"
        binding.tvHRValue.text = "112 bpm"

        binding.tvTempValue.text = "23.20 ℃"
        binding.tvHumiValue.text = "75.00 %"
        binding.tvBodyTempValue.text = "34.43 ℃"

        binding.tvAccelXValue.text = "3.44"
        binding.tvAccelYValue.text = "-3.32"
        binding.tvAccelZValue.text = "0.05"

        binding.tvGyroXValue.text = "0.02"
        binding.tvGyroYValue.text = "1.01"
        binding.tvGyroZValue.text = "0.02"

        binding.btConnect.text = "DISCONNECT"
        binding.btStart.text = "DISCONNECT"
        binding.tvSpo2Stat.text = "Connect"
        binding.tvChestStat.text = "Connect"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        return super.onCreateOptionsMenu(menu)

        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mn_device_setting -> {
                Log.e("eleutheria", "Click Device")
                val intent = Intent(this@MainActivity, DeviceSettingActivity::class.java)
                startActivity(intent)
            }
            R.id.mn_accel_graph -> {
                Log.e("eleutheria", "Click Graph")
                val intent = Intent(this@MainActivity, GraphActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val stringBuilder = StringBuilder()
        for (byte in bytes) {
            val hexString = Integer.toHexString(0xFF and byte.toInt())
            if (hexString.length == 1) {
                stringBuilder.append('0')
            }
            stringBuilder.append(hexString)
        }
        return stringBuilder.toString()
    }

    companion object {
        private val TAG = MainActivity::class.java.getSimpleName()

        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val LOCATION_UPDATE_INTERVAL: Long = 10000 // 1 second
        private const val LOCATION_UPDATE_DISTANCE: Float = 5f // 0 meters

        private const val CHESTPOD_DEVICE_NAME = "ESP32_0"
        private val CHESTPOD_SERVICE_UUID = UUID.fromString(Constants.MODULE_SERVICE_UUID_CHESTPOD)
        private val CHESTPOD_CHARACTERISTIC_UUID = UUID.fromString(Constants.MODULE_CHARACTERISTIC_UUID_CHESTPOD)
        private val CLIENT_CHARACTERISTIC_CONFIG_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}