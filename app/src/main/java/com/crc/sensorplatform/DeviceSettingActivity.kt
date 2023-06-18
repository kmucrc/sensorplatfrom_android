package com.crc.sensorplatform

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.crc.sensorplatform.base.Constants
import com.crc.sensorplatform.databinding.ActivityDeviceSettingBinding
import java.util.zip.Inflater

class DeviceSettingActivity : AppCompatActivity() {
    private  lateinit var binding : ActivityDeviceSettingBinding
    private var settings: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_device_setting)

        settings = getSharedPreferences(Constants.SHARED_PREF_SEUPDATA, Context.MODE_PRIVATE)

        val options = resources.getStringArray(R.array.users)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spUser.adapter = adapter

        binding.spUser.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = options[position]
                // Handle the selected option here
                Constants.nSelUser = position

                val editor = settings!!.edit()
                editor.putInt(Constants.PREF_USER_INDEX, Constants.nSelUser)
                editor.apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where no option is selected
            }
        }

        binding.spUser.setSelection(Constants.nSelUser)

        binding.rbChestPod01.text = Constants.MODULE_ADDRESS_CHESTPOD1
        binding.rbChestPod02.text = Constants.MODULE_ADDRESS_CHESTPOD2
        binding.rbChestPod03.text = Constants.MODULE_ADDRESS_CHESTPOD3
        binding.rbChestPod04.text = Constants.MODULE_ADDRESS_CHESTPOD4

        binding.rbSpO201.text = Constants.MODULE_ADDRESS_OXIMETRY1
        binding.rbSpO202.text = Constants.MODULE_ADDRESS_OXIMETRY2
        binding.rbSpO203.text = Constants.MODULE_ADDRESS_OXIMETRY3
        binding.rbSpO204.text = Constants.MODULE_ADDRESS_OXIMETRY4

        setInitialCheck()

//        binding.rbChestPod02.isChecked = true
    }

    private fun setInitialCheck() {
        when(Constants.nSelChestPod) {
            0 -> {
                binding.rgChestPod.check(R.id.rbChestPod00)
            }
            1 -> {
                binding.rgChestPod.check(R.id.rbChestPod01)
            }
            2 -> {
                binding.rgChestPod.check(R.id.rbChestPod02)
            }
            3 -> {
                binding.rgChestPod.check(R.id.rbChestPod03)
            }
            4 -> {
                binding.rgChestPod.check(R.id.rbChestPod04)
            }
        }

        when(Constants.nSelSpO2) {
            0 -> {
                binding.rgSpO2.check(R.id.rbSpO200)
            }
            1 -> {
                binding.rgSpO2.check(R.id.rbSpO201)
            }
            2 -> {
                binding.rgSpO2.check(R.id.rbSpO202)
            }
            3 -> {
                binding.rgSpO2.check(R.id.rbSpO203)
            }
            4 -> {
                binding.rgSpO2.check(R.id.rbSpO204)
            }
        }
    }

    fun onChestPodRadioClicked(view: View) {
        if(view is RadioButton) {
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.rbChestPod00 -> {
                    if (checked) {
//                        Log.e("eleutheria", "click : rbChestPod00")
                        Constants.nSelChestPod = 0

                        Constants.strChestPodAddress = Constants.MODULE_ADDRESS_CHESTPOD0

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_CHESTPOD_INDEX, Constants.nSelChestPod)
                        editor.apply()
                    }
                }
                R.id.rbChestPod01 -> {
                    if (checked) {
//                        Log.e("eleutheria", "click : rbChestPod01")
                        Constants.nSelChestPod = 1

                        Constants.strChestPodAddress = Constants.MODULE_ADDRESS_CHESTPOD1

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_CHESTPOD_INDEX, Constants.nSelChestPod)
                        editor.apply()

                    }
                }
                R.id.rbChestPod02 -> {
                    if (checked) {
//                        Log.e("eleutheria", "click : rbChestPod02")
                        Constants.nSelChestPod = 2

                        Constants.strChestPodAddress = Constants.MODULE_ADDRESS_CHESTPOD2

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_CHESTPOD_INDEX, Constants.nSelChestPod)
                        editor.apply()

                    }
                }
                R.id.rbChestPod03 -> {
                    if (checked) {
//                        Log.e("eleutheria", "click : rbChestPod03")
                        Constants.nSelChestPod = 3

                        Constants.strChestPodAddress = Constants.MODULE_ADDRESS_CHESTPOD3

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_CHESTPOD_INDEX, Constants.nSelChestPod)
                        editor.apply()

                    }
                }
                R.id.rbChestPod04 -> {
                    if (checked) {
//                        Log.e("eleutheria", "click : rbChestPod04")
                        Constants.nSelChestPod = 4

                        Constants.strChestPodAddress = Constants.MODULE_ADDRESS_CHESTPOD4

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_CHESTPOD_INDEX, Constants.nSelChestPod)
                        editor.apply()

                    }
                }
            }
        }
    }

    fun onSpO2RadioClicked(view: View) {
        if(view is RadioButton) {
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.rbSpO200 -> {
                    if (checked) {
//                        Log.e("eleutheria", "click : rbSpO200")
                        Constants.nSelSpO2 = 0

                        Constants.strOximetryAddress = Constants.MODULE_ADDRESS_OXIMETRY0

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_SPO2_INDEX, Constants.nSelSpO2)
                        editor.apply()
                    }
                }
                R.id.rbSpO201 -> {
                    if (checked) {
//                        Log.e("eleutheria", "click : rbSpO201")
                        Constants.nSelSpO2 = 1

                        Constants.strOximetryAddress = Constants.MODULE_ADDRESS_OXIMETRY1

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_SPO2_INDEX, Constants.nSelSpO2)
                        editor.apply()

                    }
                }
                R.id.rbSpO202 -> {
                    if (checked) {
//                        Log.e("eleutheria", "click : rbSpO202")
                        Constants.nSelSpO2 = 2

                        Constants.strOximetryAddress = Constants.MODULE_ADDRESS_OXIMETRY2

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_SPO2_INDEX, Constants.nSelSpO2)
                        editor.apply()

                    }
                }
                R.id.rbSpO203 -> {
                    if (checked) {
//                        Log.e("eleutheria", "click : rbSpO203")
                        Constants.nSelSpO2 = 3

                        Constants.strOximetryAddress = Constants.MODULE_ADDRESS_OXIMETRY3

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_SPO2_INDEX, Constants.nSelSpO2)
                        editor.apply()

                    }
                }
                R.id.rbSpO204 -> {
                    if (checked) {
//                        Log.e("eleutheria", "click : rbSpO204")
                        Constants.nSelSpO2 = 4

                        Constants.strOximetryAddress = Constants.MODULE_ADDRESS_OXIMETRY4

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_SPO2_INDEX, Constants.nSelSpO2)
                        editor.apply()

                    }
                }
            }
        }
    }

}
