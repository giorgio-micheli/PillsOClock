package micheli.giorgio.pillsoclock.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PromemoriaReceiver : BroadcastReceiver() {

override fun onReceive(context: Context, intent: Intent) {
        val idAssunzionePrevista = intent.getIntExtra(EXTRA_ID_ASSUNZIONE_PREVISTA, -1)
        if (idAssunzionePrevista == -1) return

        val nomeMedicinale = intent.getStringExtra(EXTRA_NOME_MEDICINALE) ?: return
        val dosaggio = intent.getStringExtra(EXTRA_DOSAGGIO)
        val orarioPrevisto = intent.getStringExtra(EXTRA_ORARIO_PREVISTO) ?: return

        AssunzioneNotificationHelper.mostraNotifica(
            context = context,
            idAssunzionePrevista = idAssunzionePrevista,
            nomeMedicinale = nomeMedicinale,
            dosaggio = dosaggio,
            orarioPrevisto = orarioPrevisto
        )
    }

    companion object {
        const val EXTRA_ID_ASSUNZIONE_PREVISTA = "id_assunzione_prevista"
        const val EXTRA_NOME_MEDICINALE = "nome_medicinale"
        const val EXTRA_DOSAGGIO = "dosaggio"
        const val EXTRA_ORARIO_PREVISTO = "orario_previsto"
    }
}
