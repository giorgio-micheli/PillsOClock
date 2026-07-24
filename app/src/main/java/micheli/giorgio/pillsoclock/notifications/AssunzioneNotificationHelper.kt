package micheli.giorgio.pillsoclock.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import micheli.giorgio.pillsoclock.MainActivity
import micheli.giorgio.pillsoclock.R

object AssunzioneNotificationHelper {

    const val ASSUNZIONE_CHANNEL_ID = "assunzione_channel"
    const val ASSUNZIONE_CHANNEL_DESCRIPTION = "Utilizzata per notificare il momento dell'assunzione di una medicina"

    fun mostraNotifica(
        context: Context,
        idAssunzionePrevista: Int,
        nomeMedicinale: String,
        dosaggio: String?,
        orarioPrevisto: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            idAssunzionePrevista,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val testoContenuto = if (dosaggio.isNullOrBlank()) {
            "Ore $orarioPrevisto"
        } else {
            "$dosaggio · ore $orarioPrevisto"
        }

        val notifica = NotificationCompat.Builder(context, ASSUNZIONE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_pill)
            .setContentTitle("È ora di prendere $nomeMedicinale")
            .setContentText(testoContenuto)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(idAssunzionePrevista, notifica)
    }
}
