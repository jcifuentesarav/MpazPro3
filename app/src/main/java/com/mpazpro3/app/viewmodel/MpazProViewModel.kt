package com.mpazpro3.app.viewmodel

import androidx.lifecycle.ViewModel
import com.mpazpro3.app.data.MockData
import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.EstadoAvance
import com.mpazpro3.app.model.EstadoValidacion
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.RespuestaEstudiante
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MpazProViewModel : ViewModel() {

private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

private val _errorLogin = MutableStateFlow<String?>(null)
val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

private val _contenidos = MutableStateFlow<List<Contenido>>(MockData.contenidos)
val contenidos: StateFlow<List<Contenido>> = _contenidos.asStateFlow()

private val _recursos = MutableStateFlow<List<RecursoDidactico>>(MockData.recursos)
val recursos: StateFlow<List<RecursoDidactico>> = _recursos.asStateFlow()

private val _actividades = MutableStateFlow<List<Actividad>>(MockData.actividades)
val actividades: StateFlow<List<Actividad>> = _actividades.asStateFlow()

private val _asignaciones = MutableStateFlow<List<Asignacion>>(MockData.asignaciones)
val asignaciones: StateFlow<List<Asignacion>> = _asignaciones.asStateFlow()

private val _estudiantes = MutableStateFlow<List<Estudiante>>(MockData.estudiantes)
val estudiantes: StateFlow<List<Estudiante>> = _estudiantes.asStateFlow()

private val _seguimientos = MutableStateFlow<List<SeguimientoAvance>>(MockData.seguimientos)
val seguimientos: StateFlow<List<SeguimientoAvance>> = _seguimientos.asStateFlow()

private val _informes = MutableStateFlow<List<InformeDocente>>(emptyList())
val informes: StateFlow<List<InformeDocente>> = _informes.asStateFlow()

private val _contenidoSeleccionadoId = MutableStateFlow<String?>(null)
val contenidoSeleccionadoId: StateFlow<String?> = _contenidoSeleccionadoId.asStateFlow()

private val _estudianteSeleccionadoId = MutableStateFlow<String?>(null)
val estudianteSeleccionadoId: StateFlow<String?> = _estudianteSeleccionadoId.asStateFlow()

fun login(usuarioStr: String, claveStr: String): Boolean {
val encontrado = MockData.usuarios.find {
it.usuario.equals(usuarioStr, ignoreCase = true) && it.clave == claveStr
}
return if (encontrado != null) {
_usuarioActivo.value = encontrado
_errorLogin.value = null
true
} else {
_errorLogin.value = "Usuario o clave incorrecta"
false
}
}

fun entrarModoDesarrollo(perfil: Perfil) {
val usuarioPrueba = MockData.usuarios.find { it.perfil == perfil }
?: Usuario("DEV", "Usuario ${perfil.etiqueta}", perfil.name.lowercase(), "1234", perfil, "3° básico A")
_usuarioActivo.value = usuarioPrueba
_errorLogin.value = null
}

fun logout() {
_usuarioActivo.value = null
_errorLogin.value = null
_contenidoSeleccionadoId.value = null
_estudianteSeleccionadoId.value = null
}

fun seleccionarContenido(id: String) {
_contenidoSeleccionadoId.value = id
}

fun seleccionarEstudiante(id: String) {
_estudianteSeleccionadoId.value = id
}

fun estudiantePorId(id: String?): Estudiante? {
return _estudiantes.value.find { it.id == id }
}

fun contenidoPorId(id: String?): Contenido? {
return _contenidos.value.find { it.id == id }
}

fun actividadPorId(id: String): Actividad? {
return _actividades.value.find { it.id == id }
}

fun recursosDe(contenidoId: String?): List<RecursoDidactico> {
return _recursos.value.filter { it.contenidoId == contenidoId }
}

fun contenidosFiltrados(asignatura: Asignatura?, estado: EstadoAprobacion?): List<Contenido> {
return _contenidos.value.filter { c ->
(asignatura == null || c.asignatura == asignatura) &&
        (estado == null || c.estado == estado)
}
}

fun aprobarContenido(contenidoId: String, observacion: String) {
_contenidos.value = _contenidos.value.map { c ->
if (c.id == contenidoId) {
    c.copy(estado = EstadoAprobacion.APROBADO, observacionUtp = observacion)
} else c
}
}

fun rechazarContenido(contenidoId: String, observacion: String) {
_contenidos.value = _contenidos.value.map { c ->
if (c.id == contenidoId) {
    c.copy(estado = EstadoAprobacion.RECHAZADO, observacionUtp = observacion)
} else c
}
}

fun asignacionesDelCurso(curso: String): List<Asignacion> {
return _asignaciones.value.filter { it.curso == curso }
}

fun contenidosDisponiblesParaAsignar(): List<Contenido> {
return _contenidos.value.filter { it.estado == EstadoAprobacion.APROBADO }
}

fun asignarAlCurso(curso: String, contenidoIds: List<String>, maxIntentos: Int) {
val nuevas = contenidoIds.map { id ->
Asignacion(
    id = UUID.randomUUID().toString(),
    curso = curso,
    contenidoId = id,
    maxIntentosEvaluacion = maxIntentos
)
}
_asignaciones.value = _asignaciones.value + nuevas
}

fun descargarResultados(curso: String) {
// Método de demostración
println("Descargando resultados para el curso $curso")
}

fun progresoGeneral(estudianteId: String): Float {
val contenidosEstudiante = _seguimientos.value.filter { it.estudianteId == estudianteId }
if (contenidosEstudiante.isEmpty()) return 0f
val finalizados = contenidosEstudiante.count { it.estado == EstadoAvance.FINALIZADO }
return finalizados.toFloat() / contenidosEstudiante.size.toFloat()
}

fun contenidosDelEstudiante(usuario: Usuario?): List<Contenido> {
val curso = usuario?.curso
return if (curso.isNull_or_empty_or_blank()) {
_contenidos.value.filter { it.estado == EstadoAprobacion.APROBADO }
} else {
val asignacionesCurso = _asignaciones.value.filter { it.curso == curso }.map { it.contenidoId }
_contenidos.value.filter { it.id in asignacionesCurso }
}
}

private fun String?.isNull_or_empty_or_blank(): Boolean {
return this == null || this.trim().isEmpty()
}

fun progreso(estudianteId: String, contenidoId: String): Float {
val seguimiento = _seguimientos.value.find { it.estudianteId == estudianteId && it.contenidoId == contenidoId }
return when (seguimiento?.estado) {
EstadoAvance.FINALIZADO -> 1f
EstadoAvance.EN_PROGRESO -> 0.5f
else -> 0f
}
}

fun confirmarMaterialRevisado(estudianteId: String, contenidoId: String) {
val seguimiento = _seguimientos.value.find { it.estudianteId == estudianteId && it.contenidoId == contenidoId }
if (seguimiento == null) {
val nuevo = SeguimientoAvance(
    estudianteId = estudianteId,
    contenidoId = contenidoId,
    estado = EstadoAvance.EN_PROGRESO
)
_seguimientos.value = _seguimientos.value + nuevo
}
}

fun desafiosDe(contenidoId: String?): List<Actividad> {
return _actividades.value.filter {
it.contenidoId == contenidoId && !it.esEvaluacionFinal
}.sortedBy { it.orden }
}

fun responderDesafio(estudianteId: String, actividad: Actividad, alternativaIndex: Int): Boolean {
val alternativa = actividad.alternativas.getOrNull(alternativaIndex)
val esCorrecta = alternativa?.esCorrecta == true

val seguimiento = _seguimientos.value.find { it.estudianteId == estudianteId && it.contenidoId == actividad.contenidoId }
?: SeguimientoAvance(estudianteId = estudianteId, contenidoId = actividad.contenidoId, estado = EstadoAvance.EN_PROGRESO)

val respuesta = RespuestaEstudiante(
actividadId = actividad.id,
alternativaElegida = alternativaIndex,
esCorrecta = esCorrecta,
numeroIntento = 1
)

val respuestasActualizadas = seguimiento.respuestas.filterNot { it.actividadId == actividad.id } + respuesta
val seguimientoActualizado = seguimiento.copy(
respuestas = respuestasActualizadas,
estado = EstadoAvance.EN_PROGRESO
)

_seguimientos.value = _seguimientos.value.filterNot {
it.estudianteId == estudianteId && it.contenidoId == actividad.contenidoId
} + seguimientoActualizado

return esCorrecta
}

fun evaluacionFinalDe(contenidoId: String?): List<Actividad> {
return _actividades.value.filter {
it.contenidoId == contenidoId && it.esEvaluacionFinal
}.sortedBy { it.orden }
}

fun intentosRestantes(estudianteId: String, contenidoId: String): Int? {
val usuario = _usuarioActivo.value
val curso = usuario?.curso ?: "3° básico A"
val asignacion = _asignaciones.value.find { it.curso == curso && it.contenidoId == contenidoId }
?: _asignaciones.value.find { it.contenidoId == contenidoId }
val maxIntentos = asignacion?.maxIntentosEvaluacion ?: 2

val seguimiento = _seguimientos.value.find { it.estudianteId == estudianteId && it.contenidoId == contenidoId }
val intentosUsados = seguimiento?.intentosEvaluacionUsados ?: 0

return (maxIntentos - intentosUsados).coerceAtLeast(0)
}

fun enviarEvaluacion(estudianteId: String, contenidoId: String, respuestasMap: Map<String, Int>) {
val seguimiento = _seguimientos.value.find { it.estudianteId == estudianteId && it.contenidoId == contenidoId }
?: SeguimientoAvance(estudianteId = estudianteId, contenidoId = contenidoId)

val nuevasRespuestas = respuestasMap.mapNotNull { (actividadId, altIndex) ->
val actividad = actividadPorId(actividadId) ?: return@mapNotNull null
val esCorrecta = actividad.alternativas.getOrNull(altIndex)?.esCorrecta == true
RespuestaEstudiante(
    actividadId = actividadId,
    alternativaElegida = altIndex,
    esCorrecta = esCorrecta,
    numeroIntento = seguimiento.intentosEvaluacionUsados + 1
)
}

val seguimientoActualizado = seguimiento.copy(
respuestas = seguimiento.respuestas.filterNot { r -> respuestasMap.containsKey(r.actividadId) } + nuevasRespuestas,
intentosEvaluacionUsados = seguimiento.intentosEvaluacionUsados + 1,
estado = EstadoAvance.FINALIZADO
)

_seguimientos.value = _seguimientos.value.filterNot {
it.estudianteId == estudianteId && it.contenidoId == contenidoId
} + seguimientoActualizado
}


fun respuestasDe(estudianteId: String, contenidoId: String): List<RespuestaEstudiante> {
return _seguimientos.value.find {
it.estudianteId == estudianteId && it.contenidoId == contenidoId
}?.respuestas.orEmpty()
}
fun generarInforme(estudianteId: String) {
val usuario = _usuarioActivo.value
if (usuario?.perfil != Perfil.DOCENTE) {
println("Error: Solo el docente puede generar informes.")
return
}

val seguimiento = _seguimientos.value.find { it.estudianteId == estudianteId }
val contenidoId = seguimiento?.contenidoId ?: "C2"
val respuestas = seguimiento?.respuestas ?: emptyList()

val informeExistente = _informes.value.find {
    it.estudianteId == estudianteId && it.contenidoId == contenidoId
}

if (informeExistente?.estado == EstadoValidacion.VALIDADO) {
    println("Aviso: Este informe ya fue validado y no se puede regenerar.")
    return
}

val totalRespuestas = respuestas.size
val correctas = respuestas.count { it.esCorrecta }
val incorrectas = totalRespuestas - correctas
val porcentaje = if (totalRespuestas > 0) (correctas * 100) / totalRespuestas else 0

val rendimientoStr = "$correctas de $totalRespuestas correctas ($porcentaje%)"

val fortalezas = mutableListOf<String>()
val aspectosAReforzar = mutableListOf<String>()


