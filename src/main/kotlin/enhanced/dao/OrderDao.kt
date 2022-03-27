package enhanced.dao

import config.DbConfig
import enhanced.entities.Order
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Expression
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest
import java.util.UUID

class OrderDao constructor(
    dbConfig: DbConfig,
    enhancedClient: DynamoDbEnhancedClient
) {
    val dbConfig: DbConfig
    val enhancedClient: DynamoDbEnhancedClient

    init {
        this.dbConfig = dbConfig
        this.enhancedClient = enhancedClient
    }

    private val orderTable: DynamoDbTable<Order> = enhancedClient.table(
        dbConfig.tableName,
        TableSchema.fromBean(Order::class.java)
    )

    fun get(id: UUID): Order? = orderTable.getItem(
        Key.builder()
            .partitionValue(toPk(id))
            .sortValue(toSk(id))
            .build()
    )

    fun create(order: Order): Order {
        val itemNotExistsExpression = Expression.builder()
            .expression("attribute_not_exists(#pk)")
            .putExpressionName("#pk", dbConfig.pk)
            .build()

        val request = UpdateItemEnhancedRequest.builder(Order::class.java)
            .item(order)
            .conditionExpression(itemNotExistsExpression)
            .build()

        return orderTable.updateItem(request)
    }
}

// duplicating code here just to make things easier
fun toPk(id: UUID) = "OR#${id}"
fun toSk(id: UUID) = "OR#${id}"
fun toItemSk(id: UUID) = "ORI#${id}"

// inline fun <reified T : Any> DynamoDbEnhancedClient.table(prefix: String): DynamoDbTable<T> = table(
//     "$prefix-${T::class.java.getAnnotation(DynamoDbTableName::class.java).tableName}",
//     TableSchema.fromClass(T::class.java)
// )
