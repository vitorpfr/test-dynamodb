package ddbenhancedclient.dao

import config.DbConfig
import ddbenhancedclient.entities.OrderEntity
import ddbenhancedclient.entities.OrderItemCollection
import ddbenhancedclient.entities.OrderItemEntity
import ddbenhancedclient.orderEntity
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Expression
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.QueryResponse
import java.util.Map
import java.util.UUID

class OrderDao constructor(
    dbConfig: DbConfig,
    client: DynamoDbClient,
    enhancedClient: DynamoDbEnhancedClient
) {
    private val dbConfig: DbConfig
    private val client: DynamoDbClient
    private val enhancedClient: DynamoDbEnhancedClient

    init {
        this.dbConfig = dbConfig
        this.client = client
        this.enhancedClient = enhancedClient
    }

    private val orderEntityTable: DynamoDbTable<OrderEntity> = enhancedClient.table(dbConfig.tableName, TableSchema.fromClass(OrderEntity::class.java))
    private val orderItemEntityTable: DynamoDbTable<OrderItemEntity> = enhancedClient.table(dbConfig.tableName, TableSchema.fromClass(OrderItemEntity::class.java))

    fun getOrderEntity(id: UUID): OrderEntity? = orderEntityTable.getItem(
        Key.builder()
            .partitionValue(toPk(id))
            .sortValue(toSk(id))
            .build()
    )

    fun getOrderItemEntity(orderId: UUID, orderItemId: UUID): OrderItemEntity? = orderItemEntityTable.getItem(
        Key.builder()
            .partitionValue(toPk(orderId))
            .sortValue(toItemSk(orderItemId))
            .build()
    )

    fun createOrderEntity(orderEntity: OrderEntity): OrderEntity {
        val itemNotExistsExpression = Expression.builder()
            .expression("attribute_not_exists(#pk)")
            .putExpressionName("#pk", dbConfig.pk)
            .build()

        val request = UpdateItemEnhancedRequest.builder(OrderEntity::class.java)
            .item(orderEntity)
            .conditionExpression(itemNotExistsExpression)
            .build()

        return orderEntityTable.updateItem(request)
    }

    fun createOrderItemEntity(orderItemEntity: OrderItemEntity): OrderItemEntity {
        val itemNotExistsExpression = Expression.builder()
            .expression("attribute_not_exists(#pk)")
            .putExpressionName("#pk", dbConfig.pk)
            .build()

        val request = UpdateItemEnhancedRequest.builder(OrderItemEntity::class.java)
            .item(orderItemEntity)
            .conditionExpression(itemNotExistsExpression)
            .build()

        return orderItemEntityTable.updateItem(request)
    }

    // option 1: makes two calls (one get and one query), using the high-level API, and re-builds the object from the two separate results
    fun getOrderItemCollection1(id: UUID): OrderItemCollection {
        val orderEntity: OrderEntity = getOrderEntity(id) ?: OrderEntity()
        val orderItemEntities: List<OrderItemEntity> = orderItemEntityTable.query(
                QueryEnhancedRequest.builder()
                    .queryConditional(
                        QueryConditional.keyEqualTo(
                            Key.builder()
                                .partitionValue(toPk(id))
                                .build()))
                    .queryConditional(
                        QueryConditional.sortBeginsWith(
                            Key.builder()
                                .partitionValue(toPk(id))
                                .sortValue("ORI#")
                                .build()))
                    .build())
            .items()
            .toList()

        return OrderItemCollection(orderEntity = orderEntity, orderItemEntities = orderItemEntities)
    }

    // option 2: makes only one query, using the regular API, and re-builds the object from the raw result
    fun getOrderItemCollection2(id: UUID): OrderItemCollection {
        val orderPk = AttributeValue.builder()
            .s(toPk(id))
            .build()

        val queryRequest = QueryRequest.builder()
            .tableName(dbConfig.tableName)
            .keyConditionExpression("#pk = :pk") // Define aliases for the Attribute, '#pk' and the value, ':pk'
            .expressionAttributeNames(Map.of("#pk", dbConfig.pk)) // '#pk' refers to the Attribute 'PK'
            .expressionAttributeValues(Map.of(":pk", orderPk)) // ':pk' refers to the customer PK of interest
            .scanIndexForward(false) // Search from "bottom to top"
            .build()

        // Use the DynamoDbClient directly rather than the
        // DynamoDbEnhancedClient or DynamoDbTable
        val queryResponse: QueryResponse = client.query(queryRequest)

        // The result is a list of items in a "DynamoDB JSON map"
        val items = queryResponse.items()

        return OrderItemCollection(
            orderEntity = items
                .filter { !it.contains("orderItemId") }
                .map { TableSchema.fromClass(OrderEntity::class.java).mapToItem(it) }[0],
            orderItemEntities = items
                .filter { it.contains("orderItemId") }
                .map { TableSchema.fromClass(OrderItemEntity::class.java).mapToItem(it) }
        )
    }

}

// duplicating code here just to make things easier
fun toPk(id: UUID) = "OR#${id}"
fun toSk(id: UUID) = "OR#${id}"
fun toItemSk(id: UUID) = "ORI#${id}"
