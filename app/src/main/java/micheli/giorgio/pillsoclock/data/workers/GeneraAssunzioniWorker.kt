package micheli.giorgio.pillsoclock.data.workers

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.firstOrNull
import micheli.giorgio.pillsoclock.data.local.entity.StatoAssunzione
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository
import micheli.giorgio.pillsoclock.domain.repository.PromemoriaRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository
import java.time.LocalDate

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
            val utente = utenteRepository.getUtente().firstOrNull()
                ?: return Result.failure()

            val oggi = LocalDate.now()

            assunzioneRepository.segnaVecchieComeSaltate(utente.id)

            val medicinali = medicinaleRepository
                .getMedicinaliAttiviConPianoEOrari(utente.id)
                .firstOrNull() ?: emptyList()

            assunzioneRepository.generaAssunzioniPerGiorno(utente.id, oggi, medicinali)

            val assunzioniOggi = assunzioneRepository.getAssunzioniGiornaliere(utente.id, oggi).firstOrNull()
                ?: emptyList()
            assunzioniOggi
                .filter { it.assunzionePrevista.stato == StatoAssunzione.IN_ATTESA }
                .forEach {
                    promemoriaRepository.pianifica(
                        it.assunzionePrevista.id,
                        it.assunzionePrevista.data,
                        it.assunzionePrevista.orarioPrevisto,
                        it.nomeMedicinale,
                        it.dosaggio
                    )
                }

            Result.Success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}