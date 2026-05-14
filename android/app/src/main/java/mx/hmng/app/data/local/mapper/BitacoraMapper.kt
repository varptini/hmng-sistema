package mx.hmng.app.data.local.mapper

import mx.hmng.app.data.dto.BitacoraDto
import mx.hmng.app.data.local.entity.BitacoraEntity
import mx.hmng.app.domain.model.BitacoraMovimiento

fun BitacoraDto.toEntity(): BitacoraEntity = BitacoraEntity(
    id = id,
    tipo = tipo,
    insumoNombre = insumoNombre,
    unidadMedida = unidadMedida,
    cantidad = cantidad,
    existenciaAnterior = existenciaAnterior,
    existenciaNueva = existenciaNueva,
    usuarioNombre = usuarioNombre,
    fecha = fecha,
    referenciaTipo = referenciaTipo
)

fun BitacoraEntity.toDomain(): BitacoraMovimiento = BitacoraMovimiento(
    id = id,
    tipo = tipo,
    insumoNombre = insumoNombre,
    unidadMedida = unidadMedida,
    cantidad = cantidad,
    existenciaAnterior = existenciaAnterior,
    existenciaNueva = existenciaNueva,
    usuarioNombre = usuarioNombre,
    fecha = fecha,
    referenciaTipo = referenciaTipo
)

fun BitacoraDto.toDomain(): BitacoraMovimiento = BitacoraMovimiento(
    id = id,
    tipo = tipo,
    insumoNombre = insumoNombre,
    unidadMedida = unidadMedida,
    cantidad = cantidad,
    existenciaAnterior = existenciaAnterior,
    existenciaNueva = existenciaNueva,
    usuarioNombre = usuarioNombre,
    fecha = fecha,
    referenciaTipo = referenciaTipo
)
