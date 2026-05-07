package com.example.invenzo_10

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlin.random.Random


class ActivityInicio: AppCompatActivity() {
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

        val dataSet = LineDataSet(entries, "Datos aleatorios")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(Color.RED)

        val lineData = LineData(dataSet)

        // 🔹 Asignar datos y refrescar
        lineChart.data = lineData
        lineChart.description.text = "Gráfica con datos random"
        lineChart.animateX(1000)
        lineChart.invalidate()

    }
}