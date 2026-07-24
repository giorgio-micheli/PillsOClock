package micheli.giorgio.pillsoclock

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import micheli.giorgio.pillsoclock.data.local.AppDatabase
import micheli.giorgio.pillsoclock.data.repository.AssunzioneRepositoryImpl
import micheli.giorgio.pillsoclock.data.repository.ImpostazioniRepositoryImpl
import micheli.giorgio.pillsoclock.data.repository.MedicinaleRepositoryImpl
import micheli.giorgio.pillsoclock.data.repository.PromemoriaRepositoryImpl
import micheli.giorgio.pillsoclock.data.repository.UtenteRepositoryImpl
import micheli.giorgio.pillsoclock.data.workers.GeneraAssunzioniWorker
import micheli.giorgio.pillsoclock.data.workers.GeneraAssunzioniWorkerFactory
import micheli.giorgio.pillsoclock.domain.model.Utente
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.ImpostazioniRepository
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository
import micheli.giorgio.pillsoclock.domain.repository.PromemoriaRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository
import micheli.giorgio.pillsoclock.notifications.AssunzioneNotificationHelper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "impostazioni")

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

// Estende Application e implementa Configuration.Provider
class PillsOClockApp : Application(), Configuration.Provider {

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

    val impostazioniRepository: ImpostazioniRepository by lazy {
        ImpostazioniRepositoryImpl(dataStore)
    }

    val promemoriaRepository: PromemoriaRepository by lazy {
        PromemoriaRepositoryImpl(this)
    }

    // Configuration.Provider richiede di sovrascrivere workManagerConfiguration
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(
                GeneraAssunzioniWorkerFactory(
                    medicinaleRepository,
                    assunzioneRepository,
                    utenteRepository,
                    promemoriaRepository
                )
            )
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        inizializzaUtente()
        pianificaGenerazioneGiornaliera()
        eseguiGenerazioneImmediata()
    }

    private fun inizializzaUtente() {
        CoroutineScope(Dispatchers.IO).launch {
            val utente = database.utentiDao().getUtente().firstOrNull()
            if (utente == null) {
                utenteRepository.inserisciUtente(
                    Utente(0, "", "", "", LocalDate.now())
                )
            }
        }
    }

    private fun pianificaGenerazioneGiornaliera() {
        // calcola i minuti mancanti alla mezzanotte
        val adesso = LocalDateTime.now()
        val mezzanotte = adesso.toLocalDate().plusDays(1).atStartOfDay()
        val minutiAllaMezzanotte = ChronoUnit.MINUTES.between(adesso, mezzanotte)

        val requestPeriodica = PeriodicWorkRequestBuilder<GeneraAssunzioniWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(minutiAllaMezzanotte, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "genera_assunzioni_giornaliere",
            ExistingPeriodicWorkPolicy.UPDATE,
            requestPeriodica
        )
    }

    private fun eseguiGenerazioneImmediata() {
        val requestImmediata = OneTimeWorkRequestBuilder<GeneraAssunzioniWorker>()
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "genera_assunzioni_immediata",
            ExistingWorkPolicy.KEEP, // se è già in coda o in esecuzione non la riesegue
            requestImmediata
        )
    }

    /**
     * Crea l'unico notification channel per le notifiche di questa app
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            AssunzioneNotificationHelper.ASSUNZIONE_CHANNEL_ID,
            "Assunzione",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = AssunzioneNotificationHelper.ASSUNZIONE_CHANNEL_DESCRIPTION

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}