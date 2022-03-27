package config

data class DbConfig(
    val url: String = "http://localhost:8000",
    val region: String = "us-east-1",
    val tableName: String = "test-orders-table",
    val pk: String = "PK",
    val sk: String = "SK",
)
