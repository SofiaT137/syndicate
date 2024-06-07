package com.task10.utils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

public final class DynamoDBClient {

    private static final DynamoDB INSTANCE = createInstance();

    private DynamoDBClient() {

    }

    public static DynamoDB getInstance() {
        return INSTANCE;
    }

    private static DynamoDB createInstance() {
        AmazonDynamoDB CLIENT = AmazonDynamoDBClientBuilder.defaultClient();
        return new DynamoDB(CLIENT);
    }
}
