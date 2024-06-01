package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	layers = {"sdk-layer"},
	runtime = DeploymentRuntime.JAVA11,
	architecture = Architecture.ARM64,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaLayer(
		layerName = "sdk-layer",
		libraries = {"lib/commons-lang3-3.14.0.jar", "lib/gson-2.10.1.jar"},
		runtime = DeploymentRuntime.JAVA11,
		architectures = {Architecture.ARM64},
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<Object, String> {
	private static final HttpClient httpClient = HttpClient.newHttpClient();
	private static final String OPEN_METEO_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=40.1792&longitude=44.4991&hourly=temperature_2m";

	public String handleRequest(Object request, Context context) {
		Map<String, Object> result = new HashMap<>();
		try {
			HttpRequest httpRequest = HttpRequest.newBuilder()
					.GET()
					.uri(URI.create(OPEN_METEO_API_URL))
					.build();
			HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			return httpResponse.body().replaceAll("\\\"", "\"");
		} catch (IOException | InterruptedException exception) {
			result.put("statusCode", 400);
			result.put("body", exception.getMessage());
		}
		return "";
	}
}
