package com.crc.sensorplatform

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.provider.SyncStateContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.crc.sensorplatform.base.BluetoothClassicManager
import com.crc.sensorplatform.base.Constants
import com.crc.sensorplatform.base.Constants.Companion.strDeviceAddress
import com.crc.sensorplatform.bluetooth.BluetoothLeService
import com.crc.sensorplatform.bluetooth.SampleGattAttributes
import com.crc.sensorplatform.databinding.ActivityMainBinding
import java.nio.ByteBuffer
import java.util.ArrayList
import java.util.HashMap

class MainActivity : AppCompatActivity() {
    private  lateinit var binding : ActivityMainBinding

    private val mBtHandler = BluetoothHandler()
    private val mBluetoothClassicManager: BluetoothClassicManager = BluetoothClassicManager.getInstance()
    private var mIsConnected = false

    private var mDeviceAddress: String? = null

    var mBluetoothLeService: BluetoothLeService? = null
    private var mGattCharacteristics: ArrayList<ArrayList<BluetoothGattCharacteristic>>? =
        ArrayList()
    private var mIsBLEConnected = false
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null

    private val LIST_NAME = "NAME"
    private val LIST_UUID = "UUID"
    // Code to manage Service lifecycle.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            if (!mBluetoothLeService!!.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService!!.connect(mDeviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothLeService = null
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothLeService.ACTION_GATT_CONNECTED == action) {
                mIsBLEConnected = true

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
                mIsBLEConnected = false

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService!!.supportedGattServices)
                activeNotification()
                Log.e("eleutheria", "ACTION_GATT_SERVICES_DISCOVERED")

//                val intent = Intent(this@LoadingDrivingActivity, LoadingFrontActivity::class.java)
//                val intent = Intent(this@MainActivity, LoadingFrontClassicActivity::class.java)
//                val intent = Intent(this@LoadingDrivingActivity, MainActivity::class.java)
//                startActivity(intent)
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE == action) {
                parsingData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
//                Log.e("eleutheria", "ACTION_DATA_AVAILABLE")
//                val intent = Intent(this@LoadingDrivingActivity, LoadingFrontActivity::class.java)
//                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_main)

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

        binding.btOximetryConnect.setOnClickListener{
            doDiscovery()
        }

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

    override fun onResume() {
        super.onResume()

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        if (mBluetoothLeService != null) {
            val result = mBluetoothLeService!!.connect(mDeviceAddress)
            Log.d("eleutheria", "Connect request result=" + result)
        }
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(mGattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Make sure we're not doing discovery anymore
        mBluetoothClassicManager.cancelDiscovery()
        mBluetoothClassicManager.stop()
        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver)

        unbindService(mServiceConnection)
        mBluetoothLeService = null
    }

    private fun parsingData(data: String?) {
        if (data != null) {
            Log.e("eleutheria", "driving data : ${data}")
//          G:0!/
            //System.out.println(data);
        }
    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                var strDeviceAddress = device!!.address
                Constants.strDeviceAddress = strDeviceAddress
                Log.e("eleutheria", "address : ${strDeviceAddress}")

                if(strDeviceAddress.equals(Constants.MODULE_ADDRESS_OXIMETRY)) {
                    Log.e("eleutheria", "find device Oximetry")
                    if(mBluetoothClassicManager.state != 2 ) {
                        mBluetoothClassicManager.connect(Constants.strDeviceAddress)
                    }
                }
                device.bondState
                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                setProgressBarIndeterminateVisibility(false)
            }

            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED == action) {
                val scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1)
                val prevMode = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, -1)
                when(scanMode) {
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
                        var byteHR = ByteBuffer.wrap( byteArrayOf(readBuf!!.get(1), readBuf!!.get(0)) )
                        var byteSpO2 = ByteBuffer.wrap( byteArrayOf(readBuf!!.get(3), readBuf!!.get(2)) )
                        var byteHba1c = ByteBuffer.wrap( byteArrayOf(readBuf!!.get(7), readBuf!!.get(6), readBuf!!.get(5), readBuf!!.get(4)) )

                        val intHR = byteHR.getShort()
                        val intSpO2 = byteSpO2.getShort()
                        val floatHbA1c = byteHba1c.getFloat()

                        Log.e("eleutheria", "intHR : $intHR, intSpO2 : $intSpO2, floatHbA1c : $floatHbA1c")

                        binding.tvHbA1cValue.text = floatHbA1c.toString() + " %"
                        binding.tvSpo2Value.text = intSpO2.toString() + " %"
                        binding.tvHRValue.text = intHR.toString() + " BPM"
                    }
                }
                BluetoothClassicManager.MESSAGE_STATE_CHANGE -> {
                    when(msg.arg1) {
                        BluetoothClassicManager.STATE_NONE -> {    // we're doing nothing
                            Log.e("eleutheria", "STATE_NONE")
                            mIsConnected = false
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
//                            moveMainActivity()
                        }
                    }
                }
                BluetoothClassicManager.MESSAGE_DEVICE_NAME -> {
                    if(msg.data != null) {
                        Log.e("eleutheria", "MESSAGE_DEVICE_NAME")
                    }
                }
            }

            super.handleMessage(msg)
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

        binding.btOximetryConnect.text = "DISCONNECT"
        binding.btChestConnect.text = "DISCONNECT"
        binding.tvConStat.text = "Connect"
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid: String? = null
        val unknownServiceString = resources.getString(R.string.str_bluetooth_unknown_service)
        val unknownCharaString = resources.getString(R.string.str_bluetooth_unknown_characteristic)
        val gattServiceData = ArrayList<HashMap<String, String>>()
        val gattCharacteristicData = ArrayList<ArrayList<HashMap<String, String>>>()
        mGattCharacteristics = ArrayList<ArrayList<BluetoothGattCharacteristic>>()

        // Loops through available GATT Services.
        for (gattService in gattServices) {
            val currentServiceData = HashMap<String, String>()
            uuid = gattService.uuid.toString()
            Log.e("eleutheria", "uuid : " + uuid)
            println(uuid)
            currentServiceData.put(
                LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString)
            )
            currentServiceData.put(LIST_UUID, uuid)
            gattServiceData.add(currentServiceData)

            val gattCharacteristicGroupData = ArrayList<HashMap<String, String>>()
            val gattCharacteristics = gattService.characteristics
            val charas = ArrayList<BluetoothGattCharacteristic>()

            // Loops through available Characteristics.
            for (gattCharacteristic in gattCharacteristics) {
                charas.add(gattCharacteristic)
                val currentCharaData = HashMap<String, String>()
                uuid = gattCharacteristic.uuid.toString()
                println(uuid)
                println(currentCharaData)

                currentCharaData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString)
                )
                currentCharaData.put(LIST_UUID, uuid)
                gattCharacteristicGroupData.add(currentCharaData)
            }
            mGattCharacteristics!!.add(charas)
            gattCharacteristicData.add(gattCharacteristicGroupData)
        }
    }

    private fun activeNotification() {
        if (mGattCharacteristics != null) {
            val characteristic = mGattCharacteristics!![2][0]
            val charaProp = characteristic.properties
            if (charaProp or BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.
                if (mNotifyCharacteristic != null) {
                    mBluetoothLeService!!.setDrivingCharacteristicNotification(
                        mNotifyCharacteristic!!, false)
                    mNotifyCharacteristic = null
                }

                Log.e("eleutheria", "activeNotification")
//                mBluetoothLeService!!.readCharacteristic(characteristic)
            }

            if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {

                Log.e("eleutheria", "charaProp : $charaProp, notify : ${BluetoothGattCharacteristic.PROPERTY_NOTIFY}")
                mNotifyCharacteristic = characteristic
                mBluetoothLeService!!.setDrivingCharacteristicNotification(
                    characteristic, true)
            }
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.getSimpleName()

        private fun makeGattUpdateIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
            return intentFilter
        }
    }
}