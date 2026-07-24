package micheli.giorgio.pillsoclock.data.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository
import micheli.giorgio.pillsoclock.domain.repository.PromemoriaRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository

class GeneraAssunzioniWorkerFactory(
    private val medicinaleRepository: MedicinaleRepository,
    private val assunzioneRepository: AssunzioneRepository,
    private val utenteRepository: UtenteRepository,
    private val promemoriaRepository: PromemoriaRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            GeneraAssunzioniWorker::class.java.name -> GeneraAssunzioniWorker(
                appContext,
                workerParameters,
                medicinaleRepository,
                assunzioneRepository,
                utenteRepository,
                promemoriaRepository
            )
            else -> null // lascia che WorkManager usi la factory di default per altri Worker
        }
    }
}