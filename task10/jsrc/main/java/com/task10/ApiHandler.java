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

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private static final String POOL_NAME = "simple-booking-userpool-test";
	private static final String TABLES_DB_NAME = "Tables-test";
	private static final String RESERVATION_DB_NAME = "Reservations-test";

    private final TablesService tablesService;
	private final ReservationService reservationService;
	private final AccessControlService accessControlService;

	public ApiHandler() {
		accessControlService = new AccessControlService();
        var tablesRepository = new TablesRepository();
        var reservationRepository = new ReservationRepository();
		this.tablesService = new TablesService(tablesRepository);
		this.reservationService = new ReservationService(reservationRepository, tablesRepository);
	}

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		Map<String, Object> resultMap = new HashMap<>();
		System.out.println("BodyRequest: " + request.getBody());
		System.out.println("Context: " + context);
		try {
			var path = request.getPath();
			var body = request.getBody();
			var parameters = request.getPathParameters();
			var httpMethod = request.getHttpMethod();
			resultMap = handlePath(path, body, "", parameters, httpMethod);

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

	private Map<String, Object> handlePath(String path, String body, String poolName, Map<String, String> parameters, String method) {
		switch (path) {
			case "/signup":
				return signUp(body, poolName);
			case "/signin":
				return signIn(body, poolName);
			case "/tables":
				return handleTableRequests(method, body, parameters);
			case "/reservations":
				return handleReservationRequests(method, body);
			default:
				throw new IllegalStateException("Unexpected value: " + path);
		}
	}

	private Map<String, Object> signIn(String body, String poolName) {
		return accessControlService.signIn(body, poolName);
	}

	private Map<String, Object> signUp(String body, String poolName) {
		return accessControlService.signUp(body, poolName);
	}

	private Map<String, Object> handleTableRequests(String httpMethod, String body, Map<String, String> pathParameters) {
		switch (httpMethod) {
			case "GET":
				if (pathParameters != null && pathParameters.containsKey("id")) {
					int tableId = Integer.parseInt(pathParameters.get("id"));
					return tablesService.get(tableId);
				} else {
					return tablesService.getAll();
				}
			case "POST":
				return tablesService.create(body);
			default:
				throw new UnsupportedOperationException("HTTP method " + httpMethod + " is not supported for tables");
		}
	}

	private Map<String, Object> handleReservationRequests(String httpMethod, String body) {
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
