package micheli.giorgio.pillsoclock.notifications

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
 */
class PromemoriaBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val request = OneTimeWorkRequestBuilder<GeneraAssunzioniWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "genera_assunzioni_dopo_riavvio",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
