package com.crc.sensorplatform

import android.graphics.Color
import android.os.Bundle
import android.provider.Settings.System.DATE_FORMAT
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.crc.sensorplatform.base.Constants
import com.crc.sensorplatform.database.AccelerometerDao
import com.crc.sensorplatform.database.AppDatabase
import com.crc.sensorplatform.databinding.ActivityGraphBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*


class GraphActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGraphBinding

    private lateinit var accelerometerDao : AccelerometerDao

    var nDPYear = 2023
    var nDPMonth = 6
    var nDPDay = 10
    var nTPHour = 18
    var nTPMinute = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_graph)


        val database = AppDatabase.getInstance(applicationContext)
        accelerometerDao = database.accelerometerDao()

        val cal = Calendar.getInstance()
        val nYear = cal.get(Calendar.YEAR)
        val nMonth = cal.get(Calendar.MONTH)
        val nDay = cal.get(Calendar.DAY_OF_MONTH)
        val nHour = cal.get(Calendar.HOUR_OF_DAY)
        val nMinute = cal.get(Calendar.MINUTE)
        val nSecond = cal.get(Calendar.SECOND)

        binding.dpDatepicker.setOnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
            nDPYear = year
            nDPMonth = monthOfYear + 1
            nDPDay = dayOfMonth
//            val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
        }

        binding.tpTimepicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            nTPHour = hourOfDay
            nTPMinute = minute
        }

        binding.btOk.setOnClickListener {
//            getData()

            val timestamp = System.currentTimeMillis()

            val timeZone = TimeZone.getTimeZone(Constants.TIMEZONE) // Replace with your preferred timezone

            // Create a Calendar object
            val calendar = Calendar.getInstance(timeZone)
            calendar.timeInMillis = timestamp

            // Get the year, month, etc.
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // Month is 0-based, so add 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24-hour format
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            Log.e("eleutheria", "timestamp Year : $year, Month : $month, Day : $day, Hour : $hour, Min : $minute, Sec : $second, TimeZoen : $Constants.TIMEZONE, milisec : $timestamp")


            getTimeData()
        }
    }

    private fun getData(lSelTime : Long) {
//        val currentTime = System.currentTimeMillis()
//        val currentTime = 1686298590015

        lifecycleScope.launch(Dispatchers.IO) {
            val datas = accelerometerDao.getAll(lSelTime)
//            Log.e("eleutheria", "DB datas : $datas")

            val dataSetXEntries = mutableListOf<Entry>()
            val dataSetYEntries = mutableListOf<Entry>()
            val dataSetZEntries = mutableListOf<Entry>()

            var fIndex = 0.0f

            for(data in datas) {
//                Log.e("eleutheria", "DB data : $data")
//                val strCTime = longTimeToDatetimeAsString(data.createdTime)
                fIndex++
                data.accelX
                data.accelY
                data.accelZ

                dataSetXEntries.add(Entry(fIndex, data.accelX))
                dataSetYEntries.add(Entry(fIndex, data.accelY))
                dataSetZEntries.add(Entry(fIndex, data.accelZ))

            }

            val dataSet1 = LineDataSet(dataSetXEntries, "Axis X").apply {
                color = Color.BLUE
                valueTextColor = Color.RED
            }

            val dataSet2 = LineDataSet(dataSetYEntries, "Axis Y").apply {
                color = Color.GREEN
                valueTextColor = Color.RED
            }

            val dataSet3 = LineDataSet(dataSetZEntries, "Axis Z").apply {
                color = Color.YELLOW
                valueTextColor = Color.RED
            }

            // Creating a LineData object with all the data sets
            val lineData = LineData(dataSet1, dataSet2, dataSet3)

            binding.mpGraphchart.data = lineData
            binding.mpGraphchart.invalidate() // Refresh the chart
        }
    }

    fun getTimeData() {
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = nDPYear
        calendar[Calendar.MONTH] = nDPMonth - 1 // Months are 0-based in Calendar

        calendar[Calendar.DAY_OF_MONTH] = nDPDay
        calendar[Calendar.HOUR_OF_DAY] = nTPHour
        calendar[Calendar.MINUTE] = nTPMinute
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0


        val millis = calendar.timeInMillis

//        val lSelTime = dateTimeToMilliseconds(nDPYear, nDPMonth, nDPDay, nTPHour, nTPMinute, Constants.TIMEZONE)

        Log.e("eleutheria", "Year : $nDPYear, Month : $nDPMonth, Day : $nDPDay, Hour : $nTPHour, Min : $nTPMinute, TimeZoen : $Constants.TIMEZONE, milisec : $millis")
        getData(millis)
    }

    //long형 타임을 String으로 변환.
    fun longTimeToDatetimeAsString(resultTime: Long): String? {

        val dateFormat =
            SimpleDateFormat(DATE_FORMAT)
        return dateFormat.format(resultTime)
    }

    fun dateTimeToMilliseconds(year: Int, month: Int, day: Int, hour: Int, minute: Int, zoneId: String): Long {
        val dateTime = LocalDateTime.of(year, month, day, hour, minute)
        val zone = ZoneId.of(zoneId)
        val zonedDateTime = ZonedDateTime.of(dateTime, zone)
        return zonedDateTime.toInstant().toEpochMilli()
    }
}