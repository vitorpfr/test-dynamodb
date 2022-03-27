package enhanced.entities

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import java.util.UUID

@DynamoDbBean
class Order constructor(
    orderId: UUID = UUID.randomUUID(),
    orderCustomerName: String? = null,
    orderItems: List<OrderItem> = emptyList()
) {
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("PK")
    var partitionKey: String = toPk(orderId)

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("SK")
    var sortKey: String = toSk(orderId)

    @get:DynamoDbAttribute("orderId")
    var orderId: UUID

    @get:DynamoDbAttribute("orderCustomerName")
    var orderCustomerName: String?

    // not mapped because it is not a DynamoDB attribute
    var orderItems: List<OrderItem>

    init {
        this.orderId = orderId
        this.orderCustomerName = orderCustomerName
        this.orderItems = orderItems
    }

    fun toPk(id: UUID) = "OR#${id}"
    fun toSk(id: UUID) = "OR#${id}"
    override fun toString(): String {
        return "Order(partitionKey='$partitionKey', sortKey='$sortKey', orderId=$orderId, orderCustomerName=$orderCustomerName, orderItems=$orderItems)"
    }
}
