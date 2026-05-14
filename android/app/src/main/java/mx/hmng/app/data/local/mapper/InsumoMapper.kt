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
    estadoCaducidad = estadoCaducidad,
    estadoStock = estadoStock,
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
