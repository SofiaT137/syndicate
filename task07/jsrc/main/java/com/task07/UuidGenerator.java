package com.task07;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "uuid_generator",
		roleName = "uuid_generator-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@RuleEventSource(
		targetRule = "uuid_trigger"
)
@DependsOn(
		name = "uuid_trigger",
		resourceType = ResourceType.CLOUDWATCH_RULE
)
@DependsOn(
		name = "uuid-storage",
		resourceType = ResourceType.S3_BUCKET
)
public class UuidGenerator implements RequestHandler<Object, Map<String, Object>> {
	private static final Regions REGION = Regions.EU_CENTRAL_1;
	private static final String BUCKET_NAME = "cmtr-401608dd-uuid-storage-test";

	public Map<String, Object> handleRequest(Object request, Context context) {
		var uuids = generateUUIDs(10);
        var jsonObject = new JSONObject();
		var jsonArray = new JSONArray();
		jsonArray.addAll(uuids);
		jsonObject.put("ids", jsonArray);

		var jsonLength = jsonObject.toString();
		var amazonS3Client = getAmazonS3Client();
		var date = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT );

		try(InputStream inputStream = new ByteArrayInputStream(jsonLength.getBytes(StandardCharsets.UTF_8))) {
			var metadata = new ObjectMetadata();
			metadata.setContentLength(jsonLength.length());
			var putRequest = new PutObjectRequest(BUCKET_NAME, date, inputStream, metadata);
			amazonS3Client.putObject(putRequest);
		} catch (IOException e) {
			System.out.println(e);
		}

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("statusCode", 200);
		resultMap.put("date", date);
		return resultMap;
	}

	private static List<String> generateUUIDs(int count) {
		List<String> uuids = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			uuids.add(UUID.randomUUID().toString());
		}
		return uuids;
	}

	private AmazonS3 getAmazonS3Client() {
		return AmazonS3ClientBuilder.standard().withRegion(REGION).build();
	}
}
