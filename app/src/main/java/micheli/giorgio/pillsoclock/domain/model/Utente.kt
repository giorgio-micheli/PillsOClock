package micheli.giorgio.pillsoclock.domain.model

import java.time.LocalDate

data class Utente(
    val id: Int,
    val nome: String,
    val email: String,
    val dataRegistrazione: LocalDate
)