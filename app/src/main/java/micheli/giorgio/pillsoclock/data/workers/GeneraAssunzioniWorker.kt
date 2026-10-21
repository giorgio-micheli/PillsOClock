package micheli.giorgio.pillsoclock.data.workers

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository
import micheli.giorgio.pillsoclock.domain.repository.PromemoriaRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository

class GeneraAssunzioniWorker(
    context: Context,
    params: WorkerParameters,
    private val medicinaleRepository: MedicinaleRepository,
    private val assunzioneRepository: AssunzioneRepository,
    private val utenteRepository: UtenteRepository,
    private val promemoriaRepository: PromemoriaRepository
) : CoroutineWorker(context, params) {

    @SuppressLint("RestrictedApi")
    override suspend fun doWork(): Result {
        return try {
            val generato = generaAssunzioniEPianificaPromemoria(
                medicinaleRepository,
                assunzioneRepository,
                utenteRepository,
                promemoriaRepository
            )
            if (generato) Result.Success() else Result.failure()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
