package config

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.URI

class Db {
    var client: DynamoDbClient
    var config: DbConfig = DbConfig()
    var enhancedClient: DynamoDbEnhancedClient

    init {
        this.client = DynamoDbClient.builder()
            .configure(this.config)
            .build()

        this.enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(this.client)
            .build()
    }

    fun <B, C> B.configure(config: DbConfig): B where B : AwsClientBuilder<B, C> = apply {
        endpointOverride(URI.create(config.url))
        region(Region.of(config.region))
        credentialsProvider { AwsBasicCredentials.create("fake-access-key-id", "fake-access-key") }
    }
}
