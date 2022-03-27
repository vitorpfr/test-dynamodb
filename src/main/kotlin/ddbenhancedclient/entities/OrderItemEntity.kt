package ddbenhancedclient.entities

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey
import java.util.UUID

@DynamoDbBean
class OrderItemEntity constructor(
    orderId: UUID = UUID.randomUUID(),
    orderItemId: UUID = UUID.randomUUID(),
    itemName: String? = null,
    itemPrice: Int? = null
) {
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute("PK")
    var partitionKey: String = toPk(orderId)

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute("SK")
    var sortKey: String = toSk(orderItemId)

    @get:DynamoDbAttribute("orderId")
    var orderId: UUID

    @get:DynamoDbAttribute("orderItemId")
    var orderItemId: UUID

    @get:DynamoDbAttribute("itemName")
    var itemName: String?

    @get:DynamoDbAttribute("itemPrice")
    var itemPrice: Int?

    init {
        this.orderId = orderId
        this.orderItemId = orderItemId
        this.itemName = itemName
        this.itemPrice = itemPrice
    }

    fun toPk(id: UUID) = "OR#${id}"
    fun toSk(id: UUID) = "ORI#${id}"
    override fun toString(): String {
        return "OrderItem(partitionKey='$partitionKey', sortKey='$sortKey', orderId=$orderId, orderItemId=$orderItemId, itemName=$itemName, itemPrice=$itemPrice)"
    }
}
