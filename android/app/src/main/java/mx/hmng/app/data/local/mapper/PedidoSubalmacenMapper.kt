package mx.hmng.app.data.local.mapper

import mx.hmng.app.data.dto.PedidoSubalmacenDto
import mx.hmng.app.data.local.entity.PedidoSubalmacenEntity
import mx.hmng.app.domain.model.PedidoSubalmacen

fun PedidoSubalmacenDto.toEntity(): PedidoSubalmacenEntity = PedidoSubalmacenEntity(
    id = id,
    estado = estado,
    observaciones = observaciones,
    servicioNombre = servicioNombre,
    solicitanteNombre = solicitanteNombre,
    totalItems = totalItems,
    fechaPedido = fechaPedido
)

fun PedidoSubalmacenEntity.toDomain(): PedidoSubalmacen = PedidoSubalmacen(
    id = id,
    estado = estado,
    observaciones = observaciones,
    servicioNombre = servicioNombre,
    solicitanteNombre = solicitanteNombre,
    totalItems = totalItems,
    fechaPedido = fechaPedido
)
