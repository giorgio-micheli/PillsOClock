package micheli.giorgio.pillsoclock.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import micheli.giorgio.pillsoclock.data.workers.GeneraAssunzioniWorker

/**
 * Gli allarmi esatti pianificati con AlarmManager vengono cancellati dal
 * sistema a ogni riavvio del device: qui rigeneriamo le AssunzionePrevista
 * di oggi (che, con GeneraAssunzioniWorker esteso, ripianifica anche i
 * relativi promemoria) invece di affidarci implicitamente al fatto che
 * ricevere questo broadcast risvegli comunque il processo e quindi
 * PillsOClockApp.onCreate().
 * Lo stesso vale dopo un aggiornamento dell'app, un cambio di ora/fuso orario
 * (gli allarmi RTC andrebbero ricalcolati) o la concessione del permesso
 * per gli allarmi esatti.
 */
class PromemoriaBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in AZIONI_GESTITE) return

        val request = OneTimeWorkRequestBuilder<GeneraAssunzioniWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "genera_assunzioni_dopo_riavvio",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    companion object {
        private val AZIONI_GESTITE = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        )
    }
}
