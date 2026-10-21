package micheli.giorgio.pillsoclock.domain.repository

import java.time.LocalDate
import java.time.LocalTime

interface PromemoriaRepository {

    fun pianifica(
        idAssunzionePrevista: Int,
        data: LocalDate,
        orarioPrevisto: LocalTime,
        nomeMedicinale: String,
        dosaggio: String?
    )

    fun cancella(idAssunzionePrevista: Int)

    fun puoPianificareAllarmiEsatti(): Boolean

    /**
     * Pianifica (o ripianifica) l'allarme di mezzanotte che genera le
     * assunzioni del nuovo giorno e i relativi promemoria.
     */
    fun pianificaGenerazioneMezzanotte()
}
