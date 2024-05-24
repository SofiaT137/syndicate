package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.Map;

@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
@LambdaHandler(lambdaName = "hello_world",
	roleName = "hello_world-role",
	isPublishVersion = true,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)

public class HelloWorld implements RequestHandler<Map<String, Object>, String> {

	@Override
	public String handleRequest(Map<String, Object> input, Context context) {
		String path = (String) input.get("rawPath");
		Map<String, Object> requestContext = (Map<String, Object>) input.get("requestContext");
		String method = (String) ((Map<String, Object>) requestContext.get("http")).get("method");
		if ("/hello".equals(path)) {
			return "{\"statusCode\": 200, \"message\": \"Hello from Lambda\"}";
		} else {
			return "{\"statusCode\": 400, \"message\": \"Bad request syntax or unsupported method. Request path: " + path + ". HTTP method: " + method + "\"}";
		}
	}
}
