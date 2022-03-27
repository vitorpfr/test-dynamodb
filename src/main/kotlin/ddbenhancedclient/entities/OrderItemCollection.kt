package ddbenhancedclient.entities

// references for this:
// https://www.alexdebrie.com/posts/dynamodb-one-to-many/
// https://www.davidagood.com/dynamodb-enhanced-client-java-heterogeneous-item-collections/
class OrderItemCollection(
    val orderEntity: OrderEntity? = null,
    val orderItemEntities: List<OrderItemEntity> = emptyList()
) {

}
