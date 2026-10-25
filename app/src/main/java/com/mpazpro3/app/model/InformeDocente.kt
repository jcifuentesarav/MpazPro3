package com.mpazpro3.app.model

enum class EstadoValidacion(val etiqueta: String) {
    BORRADOR("Borrador (sin validar)"),
    VALIDADO("Validado por el docente")
}

data class InformeDocente(
    val id: String,
    val estudianteId: String,
    val contenidoId: String,
    val rendimiento: String,
    val fortalezas: List<String>,
    val aspectosAReforzar: List<String>,
    val fecha: String,
    val estado: EstadoValidacion = EstadoValidacion.BORRADOR
) // <-- Aquí se cierra la clase correctamente.