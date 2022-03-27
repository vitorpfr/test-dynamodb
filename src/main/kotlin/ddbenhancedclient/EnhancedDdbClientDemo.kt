package ddbenhancedclient

import config.Db
import ddbenhancedclient.dao.OrderDao
import ddbenhancedclient.entities.OrderEntity
import ddbenhancedclient.entities.OrderItemEntity
import java.util.UUID

fun uuid(): UUID = UUID.randomUUID()

val orderId = uuid()

val orderItemEntity1 = OrderItemEntity(
    orderId = orderId,
    orderItemId = uuid(),
    itemName = "Pringles",
    itemPrice = 10
)

val orderItemEntity2 = OrderItemEntity(
    orderId = orderId,
    orderItemId = uuid(),
    itemName = "Coke",
    itemPrice = 15
)

val orderEntity = OrderEntity(
    orderId = orderId,
    orderCustomerName = "Vitor"
)
//
// val order = OrderItemCollection(
//     orderEntity = orderEntity,
//     orderItemEntities = listOf(orderItemEntity1, orderItemEntity2)
// )

val db = Db()

fun main() {
    val orderDao = OrderDao(
        dbConfig = db.config,
        client = db.client,
        enhancedClient = db.enhancedClient
    )

    orderDao.createOrderEntity(orderEntity)
    orderDao.createOrderItemEntity(orderItemEntity1)
    orderDao.createOrderItemEntity(orderItemEntity2)

    val myOrder1 = orderDao.getOrderItemCollection1(orderId)
    println(myOrder1)

    val myOrder2 = orderDao.getOrderItemCollection2(orderId)
    println(myOrder2)
}
