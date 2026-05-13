package com.example.invenzo_10

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.random.Random

class ActivityInicio : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_inicio)

        val lineChart = findViewById<LineChart>(R.id.lineChart)

        // 🔹 Datos aleatorios
        val entries = ArrayList<Entry>()

        for (i in 0..9) {

            val yValue = Random.nextInt(1, 10).toFloat()

            entries.add(
                Entry(
                    i.toFloat(),
                    yValue
                )
            )
        }

        // 🔹 Configuración gráfica
        val dataSet = LineDataSet(entries, "Ventas")

        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 5f
        dataSet.setCircleColor(Color.BLUE)

        val lineData = LineData(dataSet)

        lineChart.data = lineData
        lineChart.description.isEnabled = false
        lineChart.animateY(1000)
        lineChart.invalidate()

        // 🔹 Bottom Navigation
        val bottomNav =
            findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.home -> {
                    true
                }

                R.id.products -> {
                    startActivity(Intent(this, ProductosActivity::class.java))
                    true
                }

                R.id.reports -> {
                    startActivity(Intent(this, ReportesActivity::class.java))
                    true
                }

                R.id.more -> {
                    startActivity(Intent(this, MasOpcionesActivity::class.java))
                    true
                }

                else -> false
            }
        }

        // 🔹 Floating Button
        val fab =
            findViewById<FloatingActionButton>(
                R.id.floatingActionButton
            )
    }
}