package enhanced

import config.Db
import enhanced.dao.OrderDao
import enhanced.entities.Order
import enhanced.entities.OrderItem
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import java.util.UUID

fun uuid(): UUID = UUID.randomUUID()

val orderId = uuid()

val orderItem1 = OrderItem(
    orderId = orderId,
    orderItemId = uuid(),
    itemName = "Pringles",
    itemPrice = 10
)

val orderItem2 = OrderItem(
    orderId = orderId,
    orderItemId = uuid(),
    itemName = "Coke",
    itemPrice = 15
)

val order = Order(
    orderId = orderId,
    orderCustomerName = "Vitor",
    orderItems = listOf(orderItem1, orderItem2)
)

val db = Db()

fun main() {
    val orderDao = OrderDao(db.config, db.enhancedClient)

    orderDao.create(order)

    val myOrder = orderDao.get(orderId)
    println(myOrder)
}
