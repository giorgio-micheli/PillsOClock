package micheli.giorgio.pillsoclock

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import micheli.giorgio.pillsoclock.data.local.AppDatabase
import micheli.giorgio.pillsoclock.data.repository.AssunzioneRepositoryImpl
import micheli.giorgio.pillsoclock.data.repository.MedicinaleRepositoryImpl
import micheli.giorgio.pillsoclock.data.repository.UtenteRepositoryImpl
import micheli.giorgio.pillsoclock.domain.model.Utente
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository
import java.time.LocalDate

/**
 * Application è una classe base di Android che rappresenta lo stato globale dell'app.
 * Ogni app android ne ha automaticamente un'istanza, creata dal sistema operativo, anche
 * se non la andiamo a estendere esplicitamente.
 * La differenza rispetto a una Activity risiede nel suo ciclo di vita. L'oggetto Application
 * viene creato quando il processo dell'app viene avviato (prima ancora che venga mostrata la prima
 * schermata) e viene distrutta solo quando il processo dell'app viene terminato dal sistema.
 * Una Activity può essere invece creata e distrutta molte volte durante la stessa sessione d'uso.
 * Istanziando l'oggetto per interagire con il database in questa classe siamo sicuri che ne esista
 * solo un'istanza per tutta la vita del processo, indipendentemente da quante activity vengono
 * create e distrutte.
 *
 * La riga che abbiamo aggiunto al manifest serve per dire al sistema operativo "quando devi
 * istanziare la classe Application per questa app, non usare quella di default, usa la mia
 * sottoclasse personalizzata". Senza questa riga il sistema operativo Android continuerebbe a
 * utilizzare la classe Application di default.
 *
 * Il motivo per cui da ogni Activity o Composable possiamo accedere all'oggetto Application è che
 * ogni Context in Android mantiene un riferimento al Context dell'applicazione che lo contiene.
 */

class PillsOClockApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val utenteRepository: UtenteRepository by lazy {
        UtenteRepositoryImpl(database.utentiDao())
    }

    val medicinaleRepository: MedicinaleRepository by lazy {
        MedicinaleRepositoryImpl(
            database.medicinaliDao(),
            database.pianiAssunzioniDao(),
            database.orariAssunzioniDao()
        )
    }

    val assunzioneRepository: AssunzioneRepository by lazy {
        AssunzioneRepositoryImpl(
            database.assunzioniPrevisteDao(),
            database.assunzioniEffettuateDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        inizializzaUtente()
    }

    private fun inizializzaUtente() {
        CoroutineScope(Dispatchers.IO).launch {
            val utente = database.utentiDao().getUtente().firstOrNull()
            if (utente == null) {
                utenteRepository.inserisciUtente(
                    Utente(0, "", "", LocalDate.now())
                )
            }
        }
    }
}