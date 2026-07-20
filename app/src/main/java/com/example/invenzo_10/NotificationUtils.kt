package com.example.invenzo_10

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

object NotificationUtils {

    private const val CHANNEL_ID = "invenzo_urgent_status_v10"
    private const val CHANNEL_NAME = "Alertas Criticas Invenzo"
    private var lastCount = -1

    fun setupNotificationButton(activity: AppCompatActivity) {
        val btn = activity.findViewById<View>(R.id.btnNotificationsTop)
        val badge = activity.findViewById<TextView>(R.id.txtNotificationBadge)

        createNotificationChannel(activity)
        requestPermission(activity)

        updateBadgeFromServer(activity, badge, false)

        NotificacionManager.setOnNotificationChangedListener {
            activity.runOnUiThread {
                Log.d("INVENZO_NOTIF", "Nueva notificación detectada...")
                updateBadgeFromServer(activity, badge, true)
            }
        }

        btn?.setOnClickListener {
            showNotificationsPopup(activity, it)
        }
    }

    private fun requestPermission(activity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Borramos canal anterior para resetear importancia máxima
            manager.deleteNotificationChannel("invenzo_status_v12") 

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Canal prioritario de Invenzo"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                setShowBadge(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun showSystemNotification(context: Context) {
        try {
            val appContext = context.applicationContext
            val intent = Intent(appContext, ActivityInicio::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            val pendingIntent = PendingIntent.getActivity(
                appContext, System.currentTimeMillis().toInt(), intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bell) 
                .setContentTitle("INVENZO")
                .setContentText("tienes una notificacion nueva")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setFullScreenIntent(pendingIntent, true) // Esto fuerza el popup visual arriba
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(System.currentTimeMillis().toInt(), builder.build())
            
            Log.d("INVENZO_NOTIF", "Notificación enviada con éxito")
        } catch (e: Exception) {
            Log.e("INVENZO_NOTIF", "Error al lanzar: ${e.message}")
        }
    }

    private fun updateBadgeFromServer(activity: AppCompatActivity, badge: TextView?, shouldNotify: Boolean) {
        val token = activity.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", "") ?: ""
        if (token.isEmpty()) return

        activity.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getNotificaciones("Bearer $token")
                if (response.isSuccessful) {
                    val count = response.body()?.count { !it.leida } ?: 0
                    if (count > 0) {
                        badge?.visibility = View.VISIBLE
                        badge?.text = if (count > 9) "9+" else count.toString()
                        
                        // Solo notificamos al sistema si el conteo aumentó
                        if (shouldNotify && count > lastCount) {
                            showSystemNotification(activity)
                        }
                    } else {
                        badge?.visibility = View.GONE
                    }
                    lastCount = count
                }
            } catch (e: Exception) {
                Log.e("INVENZO_NOTIF", "Error de sincronización")
            }
        }
    }

    private fun showNotificationsPopup(activity: AppCompatActivity, anchorView: View) {
        val token = activity.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", "") ?: ""
        activity.lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getNotificaciones("Bearer $token")
                val list = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
                displayPopup(activity, anchorView, list)
            } catch (e: Exception) {
                displayPopup(activity, anchorView, emptyList())
            }
        }
    }

    private fun displayPopup(context: Context, anchorView: View, notifications: List<Notificacion>) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.layout_notificaciones_popup, null)
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        val rv = popupView.findViewById<RecyclerView>(R.id.rvNotificaciones)
        val txtEmpty = popupView.findViewById<TextView>(R.id.txtEmptyPopup)
        if (notifications.isEmpty()) {
            txtEmpty?.visibility = View.VISIBLE
            rv?.visibility = View.GONE
        } else {
            txtEmpty?.visibility = View.GONE
            rv?.visibility = View.VISIBLE
            rv?.layoutManager = LinearLayoutManager(context)
            rv?.adapter = NotificacionAdapter(notifications)
        }
        popupWindow.elevation = 10f
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.showAsDropDown(anchorView, 0, 10)
    }
}