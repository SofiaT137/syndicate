package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "audit_producer",
	roleName = "audit_producer-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DynamoDbTriggerEventSource(
		targetTable = "Configuration",
		batchSize = 1
)
@DependsOn(
		name = "Configuration",
		resourceType = ResourceType.DYNAMODB_TABLE
)
@DependsOn(
		name = "Audit",
		resourceType = ResourceType.DYNAMODB_TABLE
)
public class AuditProducer implements RequestHandler<DynamodbEvent, Map<String, Object>> {
	private static final DynamoDB DYNAMO_DB = DynamoDBClient.getInstance();

	private static final String DYNAMODB_TABLE_NAME = "cmtr-401608dd-Audit-test";

	public Map<String, Object> handleRequest(DynamodbEvent request, Context context) {
		Map<String, Object> resultMap = new HashMap<>();
		for (DynamodbEvent.DynamodbStreamRecord record : request.getRecords()) {

			var item = getItem(record);

			var table = DYNAMO_DB.getTable(DYNAMODB_TABLE_NAME);
			table.putItem(item);

			resultMap.put("statusCode", 200);
			resultMap.put("item", item.asMap());
		}

		return resultMap;
	}

	private Item getItem(DynamodbEvent.DynamodbStreamRecord record) {
		var item = new Item();
		var itemKey = getItemKey(record, item);
		if ("INSERT".equals(record.getEventName())) {
			String json = "{\"key\": \"" + itemKey + "\", \"value\": " + record
					.getDynamodb()
					.getNewImage()
					.get("value")
					.getN() + "}";
			item.withJSON("newValue", json);
		} else {
			item.withString("updatedAttribute", "value");
			item.withInt("oldValue", Integer.parseInt(record.getDynamodb().getOldImage().get("value").getN()));
			item.withInt("newValue", Integer.parseInt(record.getDynamodb().getNewImage().get("value").getN()));
		}
		return item;
	}

	private String getItemKey(DynamodbEvent.DynamodbStreamRecord record, Item item) {
		UUID uuid = getRandomUUID();
		var itemKey = record.getDynamodb().getNewImage().get("key").getS();
		item.withString("id", uuid.toString());
		item.withString("itemKey", itemKey);
		item.withString("modificationTime", ZonedDateTime.now( ZoneOffset.UTC )
				.format( DateTimeFormatter.ISO_INSTANT ));
		return itemKey;
	}

	private UUID getRandomUUID() {
		return UUID.randomUUID();
	}
}
