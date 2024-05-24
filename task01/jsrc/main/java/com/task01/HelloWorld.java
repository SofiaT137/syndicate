package com.task01;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import java.util.LinkedHashMap;
import java.util.Map;

@LambdaHandler(
	lambdaName = "hello_world",
	roleName = "hello_world-role",
	isPublishVersion = true,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class HelloWorld implements RequestHandler<Object, String> {

	public String handleRequest(Object request, Context context) {
		System.out.println("Hello from lambda");
		Map<String, Object> resultMap = new LinkedHashMap<>();
		return "{\"statusCode\": 200, \"message\": \"Hello from Lambda\"}";
	}
}
