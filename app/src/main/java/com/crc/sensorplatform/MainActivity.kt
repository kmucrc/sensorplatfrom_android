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
import com.crc.sensorplatform.base.BluetoothClassicManager
import com.crc.sensorplatform.base.Constants
import com.crc.sensorplatform.database.Accelerometer
import com.crc.sensorplatform.database.AccelerometerDao
import com.crc.sensorplatform.database.AppDatabase
import com.crc.sensorplatform.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val mBtHandler = BluetoothHandler()
    private val mBluetoothClassicManager: BluetoothClassicManager =
        BluetoothClassicManager.getInstance()
    private var mIsConnected = false


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
                binding.tvChestStat.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.red)))
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Disconnected from the BLE device, clean up resources
                bluetoothGatt.close()

                binding.tvChestStat.text = getString(R.string.str_main_disconnect)
                binding.tvChestStat.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.gray)))
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

                sendNetworkData(jsonString)

                val axValue = jsonObject.getString("Ax")
                val ayValue = jsonObject.getString("Ay")
                val azValue = jsonObject.getString("Az")
                val gxValue = jsonObject.getString("Gx")
                val gyValue = jsonObject.getString("Gy")
                val gzValue = jsonObject.getString("Gz")
                val btempValue = jsonObject.getString("btemp")
                val tempValue = jsonObject.getString("temp")
                val humiValue = jsonObject.getString("humi")

                saveAccelData(axValue.toFloat(), ayValue.toFloat(), azValue.toFloat() )

                runOnUiThread {
                    binding.tvAccelXValue.text = axValue
                    binding.tvAccelYValue.text = ayValue
                    binding.tvAccelZValue.text = azValue
                    binding.tvGyroXValue.text = gxValue
                    binding.tvGyroYValue.text = gyValue
                    binding.tvGyroZValue.text = gzValue
                    binding.tvBodyTempValue.text = btempValue
                    binding.tvTempValue.text = tempValue
                    binding.tvHumiValue.text = humiValue

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



        binding.btConnect.setOnClickListener {
            Log.e("eleutheria", "BLE Address : ${Constants.strChestPodAddress}")
            doDiscovery()
            startBleScan()
        }

        binding.btStart.setOnClickListener {
//            startChestPod()
//            setAcceleData()

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

                if (strDeviceAddress.equals(Constants.strChestPodAddress)) {
                    Log.e("eleutheria", "find device Oximetry, Address : ${Constants.strChestPodAddress}")
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
                            binding.tvSpo2Stat.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.gray)))
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

                            binding.tvSpo2Stat.text = getString(R.string.str_main_disconnect)
                            binding.tvSpo2Stat.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.red)))
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
        val accelroData = Accelerometer(0, currentTime, AccelX, AccelY, AccelZ)

        lifecycleScope.launch(Dispatchers.IO) {

            accelerometerDao.insertAll(accelroData)
            val data = accelerometerDao.getAll(currentTime)
            Log.e("eleutheria", "DB data : $data")
        }
    }

    private fun sendNetworkData(jsonString: String) {

    }

    fun setAcceleData() {

        val currentTime = System.currentTimeMillis()
        val random = Random()
        val randomXFloat = random.nextFloat()
        val randomYFloat = random.nextFloat()
        val randomZFloat = random.nextFloat()

        val accelroData = Accelerometer(0, currentTime, randomXFloat, randomYFloat, randomZFloat)

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