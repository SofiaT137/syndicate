package com.task09;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "processor",
	roleName = "processor-role",
	isPublishVersion = false,
	layers = {"sdk-layer"},
	runtime = DeploymentRuntime.JAVA11,
	architecture = Architecture.ARM64,
	tracingMode = TracingMode.Active,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaLayer(
		layerName = "sdk-layer",
		libraries = {"lib/open_meteo_api-0.0.1.jar"},
		runtime = DeploymentRuntime.JAVA11,
		architectures = {Architecture.ARM64},
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@DependsOn(
		name = "Weather",
		resourceType = ResourceType.DYNAMODB_TABLE
)
public class Processor implements RequestHandler<Object, Map<String, Object>> {
	private static final DynamoDB DYNAMO_DB = DynamoDBClient.getInstance();
	private static final String DYNAMODB_TABLE_NAME = "cmtr-401608dd-Weather-test";

	public Map<String, Object> handleRequest(Object request, Context context) {
		Map<String, Object> resultMap = new HashMap<>();
		var item = getItem();
		var table = DYNAMO_DB.getTable(DYNAMODB_TABLE_NAME);
		table.putItem(item);

		resultMap.put("statusCode", 200);
		resultMap.put("item", item.asMap());

		return resultMap;
	}

	private UUID getRandomUUID() {
		return UUID.randomUUID();
	}

	private String getWeather() {
		var weatherService = new WeatherService();
        try {
            var forecast = weatherService.getWeatherForecast();
			return forecast.replaceAll("\\\"", "\"");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

	private Item getItem() {
		var item = new Item();
		item.withString("id", getRandomUUID().toString());
		var weather = getWeather();
		item.withJSON("forecast", weather);
		return item;
	}
}
