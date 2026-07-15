package com.example.invenzo_10

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

object NotificationUtils {

    fun setupNotificationButton(activity: AppCompatActivity) {
        val btn = activity.findViewById<View>(R.id.btnNotificationsTop)
        val badge = activity.findViewById<TextView>(R.id.txtNotificationBadge)

        // Sincronizar el badge (número) con la base de datos al iniciar
        updateBadgeFromServer(activity, badge)

        // Escuchar cambios para actualizar el badge en tiempo real
        NotificacionManager.setOnNotificationChangedListener {
            activity.runOnUiThread {
                updateBadgeFromServer(activity, badge)
            }
        }

        btn?.setOnClickListener {
            showNotificationsPopup(activity, it)
        }
    }

    private fun updateBadgeFromServer(activity: AppCompatActivity, badge: TextView?) {
        val prefs = activity.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        activity.lifecycleScope.launch {
            try {
                // CONSULTA EXCLUSIVA A LA TABLA DE NOTIFICACIONES
                val response = RetrofitClient.instance.getNotificaciones("Bearer $token")
                if (response.isSuccessful) {
                    val count = response.body()?.count { !it.leida } ?: 0
                    if (count > 0) {
                        badge?.visibility = View.VISIBLE
                        badge?.text = if (count > 9) "9+" else count.toString()
                    } else {
                        badge?.visibility = View.GONE
                    }
                } else {
                    badge?.visibility = View.GONE
                }
            } catch (e: Exception) {
                badge?.visibility = View.GONE
            }
        }
    }

    private fun showNotificationsPopup(activity: AppCompatActivity, anchorView: View) {
        val prefs = activity.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""

        activity.lifecycleScope.launch {
            try {
                // CONSULTA EXCLUSIVA A LA TABLA DE NOTIFICACIONES
                val response = RetrofitClient.instance.getNotificaciones("Bearer $token")
                val notifications = if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else emptyList()

                displayPopup(activity, anchorView, notifications)
            } catch (e: Exception) {
                displayPopup(activity, anchorView, emptyList())
            }
        }
    }

    private fun displayPopup(context: Context, anchorView: View, notifications: List<Notificacion>) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.layout_notificaciones_popup, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

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

        popupView.findViewById<View>(R.id.btnMarcarLeidas)?.setOnClickListener {
            // Se puede implementar marcar como leídas aquí si se desea
            popupWindow.dismiss()
        }

        popupWindow.elevation = 10f
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.showAsDropDown(anchorView, 0, 10)
    }
}
