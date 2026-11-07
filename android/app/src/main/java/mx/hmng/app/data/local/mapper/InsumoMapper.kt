package mx.hmng.app.data.local.mapper

import mx.hmng.app.data.dto.InsumoDto
import mx.hmng.app.data.local.entity.InsumoEntity
import mx.hmng.app.domain.model.Insumo

fun InsumoDto.toEntity(): InsumoEntity = InsumoEntity(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    unidadMedida = unidadMedida,
    existencia = existencia,
    cantidadMinima = cantidadMinima,
    lote = lote,
    fechaCaducidad = fechaCaducidad,
    codigoBarras = codigoBarras,
    // El backend envía estado_caducidad/estado_stock en minúsculas (p. ej. "por_caducar"),
    // mientras que el resto de la app (AlertBadge, InsumoCard, Insumo.estaCaducado) compara
    // contra constantes en mayúsculas ("POR_CADUCAR"). Normalizamos aquí, en el único punto
    // de entrada de datos del backend hacia Room/dominio.
    estadoCaducidad = estadoCaducidad.uppercase(),
    estadoStock = estadoStock.uppercase(),
    activo = activo,
    updatedAt = updatedAt
)

fun InsumoEntity.toDomain(): Insumo = Insumo(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    unidadMedida = unidadMedida,
    existencia = existencia,
    cantidadMinima = cantidadMinima,
    lote = lote,
    fechaCaducidad = fechaCaducidad,
    codigoBarras = codigoBarras,
    estadoCaducidad = estadoCaducidad,
    estadoStock = estadoStock,
    activo = activo,
    updatedAt = updatedAt
)
