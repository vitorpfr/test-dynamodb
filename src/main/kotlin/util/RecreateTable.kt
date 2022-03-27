package util

import config.Db
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.KeyType
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter
import kotlin.system.exitProcess

val db = Db()

fun deleteTableIfExists() {
    val request = DeleteTableRequest.builder()
        .tableName(db.config.tableName)
        .build()

    try {
        db.client.deleteTable(request)
    } catch (e: DynamoDbException) {
        println(e.message)
    }
}

fun createTable() {
    val dbWaiter: DynamoDbWaiter = db.client.waiter()
    val request: CreateTableRequest = CreateTableRequest.builder()
        .attributeDefinitions(
            AttributeDefinition.builder()
                .attributeName(db.config.pk)
                .attributeType(ScalarAttributeType.S)
                .build(),
            AttributeDefinition.builder()
                .attributeName(db.config.sk)
                .attributeType(ScalarAttributeType.S)
                .build()
        )
        .keySchema(
            KeySchemaElement.builder()
                .attributeName(db.config.pk)
                .keyType(KeyType.HASH)
                .build(),
            KeySchemaElement.builder()
                .attributeName(db.config.sk)
                .keyType(KeyType.RANGE)
                .build()
        )
        .provisionedThroughput(
            ProvisionedThroughput.builder()
                .readCapacityUnits(10)
                .writeCapacityUnits(10)
                .build()
        )
        .tableName(db.config.tableName)
        .build()

    var newTable = ""

    try {
        val response: CreateTableResponse = db.client.createTable(request)
        val tableRequest = DescribeTableRequest.builder()
            .tableName(db.config.tableName)
            .build()

        // Wait until the Amazon DynamoDB table is created
        val waiterResponse = dbWaiter.waitUntilTableExists(tableRequest)
        waiterResponse.matched().response().ifPresent { x: DescribeTableResponse? ->
            println(x)
        }
        newTable = response.tableDescription().tableName()
        println(newTable)
    } catch (e: DynamoDbException) {
        System.err.println(e.message)
        exitProcess(1)
    }
}

fun main() {
    deleteTableIfExists()
    createTable()
}
