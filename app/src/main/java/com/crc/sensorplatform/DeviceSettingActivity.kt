package com.crc.sensorplatform

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
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
                        Log.e("eleutheria", "click : rbChestPod00")
                        Constants.nSelChestPod = 0

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_CHESTPOD_INDEX, Constants.nSelChestPod)
                        editor.apply()
                    }
                }
                R.id.rbChestPod01 -> {
                    if (checked) {
                        Log.e("eleutheria", "click : rbChestPod01")
                        Constants.nSelChestPod = 1

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_CHESTPOD_INDEX, Constants.nSelChestPod)
                        editor.apply()

                    }
                }
                R.id.rbChestPod02 -> {
                    if (checked) {
                        Log.e("eleutheria", "click : rbChestPod02")
                        Constants.nSelChestPod = 2

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_CHESTPOD_INDEX, Constants.nSelChestPod)
                        editor.apply()

                    }
                }
                R.id.rbChestPod03 -> {
                    if (checked) {
                        Log.e("eleutheria", "click : rbChestPod03")
                        Constants.nSelChestPod = 3

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_CHESTPOD_INDEX, Constants.nSelChestPod)
                        editor.apply()

                    }
                }
                R.id.rbChestPod04 -> {
                    if (checked) {
                        Log.e("eleutheria", "click : rbChestPod04")
                        Constants.nSelChestPod = 4

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
                        Log.e("eleutheria", "click : rbSpO200")
                        Constants.nSelSpO2 = 0

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_SPO2_INDEX, Constants.nSelSpO2)
                        editor.apply()
                    }
                }
                R.id.rbSpO201 -> {
                    if (checked) {
                        Log.e("eleutheria", "click : rbSpO201")
                        Constants.nSelSpO2 = 1

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_SPO2_INDEX, Constants.nSelSpO2)
                        editor.apply()

                    }
                }
                R.id.rbSpO202 -> {
                    if (checked) {
                        Log.e("eleutheria", "click : rbSpO202")
                        Constants.nSelSpO2 = 2

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_SPO2_INDEX, Constants.nSelSpO2)
                        editor.apply()

                    }
                }
                R.id.rbSpO203 -> {
                    if (checked) {
                        Log.e("eleutheria", "click : rbSpO203")
                        Constants.nSelSpO2 = 3

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_SPO2_INDEX, Constants.nSelSpO2)
                        editor.apply()

                    }
                }
                R.id.rbSpO204 -> {
                    if (checked) {
                        Log.e("eleutheria", "click : rbSpO204")
                        Constants.nSelSpO2 = 4

                        val editor = settings!!.edit()
                        editor.putInt(Constants.PREF_SPO2_INDEX, Constants.nSelSpO2)
                        editor.apply()

                    }
                }
            }
        }
    }

}
