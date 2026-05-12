package com.example.invenzo_10

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlin.random.Random


class ActivityInicio: AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)

        val lineChart = findViewById<LineChart>(R.id.lineChart)

        // 🔹 Generar datos aleatorios
        val entries = ArrayList<Entry>()
        for (i in 0..9) { // 10 puntos
            val yValue = Random.nextInt(1, 10).toFloat()
            entries.add(Entry(i.toFloat(), yValue))
        }

    }
}