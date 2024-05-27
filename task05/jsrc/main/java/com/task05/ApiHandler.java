package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = true,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<Request, Map<String, Object>> {

	private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
	private final DynamoDB dynamoDB = new DynamoDB(client);

	public Map<String, Object> handleRequest(Request request, Context context) {
		try {
			UUID uuid = UUID.randomUUID();
			Item item = new Item();
			var principal = request.getPrincipalId();
			item.withString("id", uuid.toString());
			item.withInt("principalId", principal);
			var content =  request.getContent();
			item.withMap("body", content);
			context.getLogger().log("Content: " + content);
			item.withString("createdAt", ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT ));
			var result = item.asMap();
			Table table = dynamoDB.getTable("cmtr-401608dd-Events");
			table.putItem(item);
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("statusCode", 201);
			resultMap.put("event", result);
			return resultMap;

		} catch (Exception e) {
			context.getLogger().log(e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
