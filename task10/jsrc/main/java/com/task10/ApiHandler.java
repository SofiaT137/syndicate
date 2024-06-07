package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.task10.repository.ReservationRepository;
import com.task10.repository.TablesRepository;
import com.task10.service.AccessControlService;
import com.task10.service.ReservationService;
import com.task10.service.TablesService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final AccessControlService accessControlService;

	public ApiHandler() {
		accessControlService = new AccessControlService();
	}

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		Map<String, Object> resultMap = new HashMap<>();
		System.out.println("Request: " + request.toString() + " " + "request body: " + request.getBody());
		System.out.println("Context: " + context.getFunctionName());
		try {

			String poolName = context.getFunctionName().replace("api_handler", "simple-booking-userpool");
			System.out.println("Poolname: " + poolName);

			String tablesName = context.getFunctionName().replace("api_handler", "Tables");
			System.out.println("tablesName: " + tablesName);
			if (Objects.isNull(tablesName) || tablesName.isBlank()) {
				tablesName = "Tables";
			}
			var tablesRepository = new TablesRepository(tablesName);
			var tableService = new TablesService(tablesRepository);

			String reservationName = context.getFunctionName().replace("api_handler", "Reservations");
			System.out.println("reservationName: " + reservationName);
			if (Objects.isNull(reservationName) || reservationName.isBlank()) {
				reservationName = "Reservations";
			}
			var reservationRepository = new ReservationRepository(reservationName);
			var reservationService = new ReservationService(reservationRepository, tablesRepository);

			var pathParameters = request.getPathParameters();
			var path = request.getPath();
			var httpMethod = request.getHttpMethod();
			var body = request.getBody();

			resultMap = handlePath(path, body, poolName, pathParameters, httpMethod, tableService, reservationService);

			return new APIGatewayProxyResponseEvent()
					.withStatusCode(200)
					.withBody(new Gson().toJson(resultMap))
					.withHeaders(Map.of("Content-Type", "application/json"));

		} catch (Exception exception) {
			resultMap.put("exception: ", exception.getMessage());
			return new APIGatewayProxyResponseEvent()
					.withStatusCode(400)
					.withBody(new Gson().toJson(resultMap.toString()));
		}
	}

	private Map<String, Object> handlePath(String path, String body, String poolName, Map<String, String> parameters,
										   String method, TablesService tablesService,
										   ReservationService reservationService) {
		var id = parameters.get("id");

		if (path.equals("/signup")) {
			return signUp(body, poolName);
		} else if (path.equals("/signin")) {
			return signIn(body, poolName);
		} else if (path.startsWith("/tables") && parameters != null && id != null ) {
			return tablesService.get(Integer.parseInt(id));
		} else if (path.equals("/tables")) {
			return handleTableRequests(method, body, tablesService);
		} else if (path.equals("/reservations")){
			return handleReservationRequests(method, body, reservationService);
		}
		throw new IllegalStateException("Unexpected value: " + path);
	}

	private Map<String, Object> signIn(String body, String poolName) {
		return accessControlService.signIn(body, poolName);
	}

	private Map<String, Object> signUp(String body, String poolName) {
		return accessControlService.signUp(body, poolName);
	}

	private Map<String, Object> handleTableRequests(String httpMethod, String body, TablesService tablesService) {
		switch (httpMethod) {
			case "GET":
				return tablesService.getAll();
			case "POST":
				return tablesService.create(body);
			default:
				throw new UnsupportedOperationException("HTTP method " + httpMethod + " is not supported for tables");
		}
	}

	private Map<String, Object> handleReservationRequests(String httpMethod, String body,
														  ReservationService reservationService) {
		switch (httpMethod) {
			case "GET":
				return reservationService.getAll();
			case "POST":
				return reservationService.create(body);
			default:
				throw new UnsupportedOperationException("HTTP method " + httpMethod + " is not supported for reservations");
		}
	}
}
