package micheli.giorgio.pillsoclock.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import micheli.giorgio.pillsoclock.PillsOClockApp
import micheli.giorgio.pillsoclock.data.workers.generaAssunzioniEPianificaPromemoria

/**
 * Riceve l'allarme esatto di mezzanotte e genera le assunzioni del nuovo giorno.
 * La generazione viene eseguita direttamente qui (con goAsync) e non tramite
 * WorkManager: durante la notte il dispositivo è in Doze e i job di WorkManager
 * verrebbero rimandati, anche di ore, lasciando senza promemoria le dosi del mattino.
 */
class GenerazioneGiornalieraReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as PillsOClockApp
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                generaAssunzioniEPianificaPromemoria(
                    app.medicinaleRepository,
                    app.assunzioneRepository,
                    app.utenteRepository,
                    app.promemoriaRepository
                )
            } catch (e: Exception) {
                // L'allarme della notte successiva viene comunque riarmato,
                // e all'apertura dell'app la generazione viene ripetuta
            } finally {
                pendingResult.finish()
            }
        }
    }
}
