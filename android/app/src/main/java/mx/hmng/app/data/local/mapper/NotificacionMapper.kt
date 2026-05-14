package mx.hmng.app.data.local.mapper

import mx.hmng.app.data.dto.NotificacionDto
import mx.hmng.app.data.local.entity.NotificacionEntity
import mx.hmng.app.domain.model.Notificacion

fun NotificacionDto.toEntity(): NotificacionEntity = NotificacionEntity(
    id = id,
    tipo = tipo,
    mensaje = mensaje,
    referenciaId = referenciaId,
    leida = leida,
    createdAt = createdAt
)

fun NotificacionEntity.toDomain(): Notificacion = Notificacion(
    id = id,
    tipo = tipo,
    mensaje = mensaje,
    referenciaId = referenciaId,
    leida = leida,
    createdAt = createdAt
)
