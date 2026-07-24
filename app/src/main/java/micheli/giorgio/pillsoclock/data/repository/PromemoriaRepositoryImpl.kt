package micheli.giorgio.pillsoclock.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import micheli.giorgio.pillsoclock.domain.repository.PromemoriaRepository
import micheli.giorgio.pillsoclock.notifications.PromemoriaReceiver
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class PromemoriaRepositoryImpl(private val context: Context) : PromemoriaRepository {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun puoPianificareAllarmiEsatti(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true

    override fun pianifica(
        idAssunzionePrevista: Int,
        data: LocalDate,
        orarioPrevisto: LocalTime,
        nomeMedicinale: String,
        dosaggio: String?
    ) {
        val triggerAtMillis = data.atTime(orarioPrevisto)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Un istante già trascorso farebbe scattare l'allarme immediatamente:
        // non ha senso pianificarlo (es. dopo l'annullamento di una dose in ritardo).
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val pendingIntent = creaPendingIntent(idAssunzionePrevista, nomeMedicinale, dosaggio, orarioPrevisto)

        try {
            if (puoPianificareAllarmiEsatti()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    override fun cancella(idAssunzionePrevista: Int) {
        val intent = Intent(context, PromemoriaReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            idAssunzionePrevista,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun creaPendingIntent(
        id: Int,
        nomeMedicinale: String,
        dosaggio: String?,
        orarioPrevisto: LocalTime
    ): PendingIntent {
        val intent = Intent(context, PromemoriaReceiver::class.java).apply {
            putExtra(PromemoriaReceiver.EXTRA_ID_ASSUNZIONE_PREVISTA, id)
            putExtra(PromemoriaReceiver.EXTRA_NOME_MEDICINALE, nomeMedicinale)
            putExtra(PromemoriaReceiver.EXTRA_DOSAGGIO, dosaggio)
            putExtra(PromemoriaReceiver.EXTRA_ORARIO_PREVISTO, orarioPrevisto.toString())
        }
        return PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
