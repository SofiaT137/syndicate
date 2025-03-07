package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;


@LambdaHandler(lambdaName = "sqs_handler",
		roleName = "sqs_handler-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SqsTriggerEventSource(
		targetQueue = "async_queue",
		batchSize = 10
)
@DependsOn(
		name = "async_queue",
		resourceType = ResourceType.SQS_QUEUE
)
public class SqsHandler implements RequestHandler<Object, String> {

	public String handleRequest(Object request, Context context) {
		System.out.println(request.toString());
		return "";
	}
}
