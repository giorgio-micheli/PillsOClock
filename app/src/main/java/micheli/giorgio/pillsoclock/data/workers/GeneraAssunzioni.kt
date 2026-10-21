package micheli.giorgio.pillsoclock.data.workers

import kotlinx.coroutines.flow.firstOrNull
import micheli.giorgio.pillsoclock.data.local.entity.StatoAssunzione
import micheli.giorgio.pillsoclock.domain.repository.AssunzioneRepository
import micheli.giorgio.pillsoclock.domain.repository.MedicinaleRepository
import micheli.giorgio.pillsoclock.domain.repository.PromemoriaRepository
import micheli.giorgio.pillsoclock.domain.repository.UtenteRepository
import java.time.LocalDate

/**
 * Genera le AssunzionePrevista di oggi e pianifica i relativi promemoria.
 * Viene usata sia da GeneraAssunzioniWorker sia da GenerazioneGiornalieraReceiver
 * (allarme esatto di mezzanotte, che a differenza di WorkManager non viene
 * rimandato dal Doze durante la notte).
 *
 * Al termine riarma sempre l'allarme della mezzanotte successiva.
 *
 * @return false se non esiste ancora un utente
 */
suspend fun generaAssunzioniEPianificaPromemoria(
    medicinaleRepository: MedicinaleRepository,
    assunzioneRepository: AssunzioneRepository,
    utenteRepository: UtenteRepository,
    promemoriaRepository: PromemoriaRepository
): Boolean {
    try {
        val utente = utenteRepository.getUtente().firstOrNull() ?: return false

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

        return true
    } finally {
        promemoriaRepository.pianificaGenerazioneMezzanotte()
    }
}
