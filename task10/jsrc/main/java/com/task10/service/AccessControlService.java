package com.task10.service;

import com.google.gson.Gson;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.task10.request.SignInRequest;
import com.task10.request.SignUpRequest;
import com.task10.utils.CognitoClient;
import java.util.Map;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

public class AccessControlService {

    private final CognitoIdentityProviderClient cognitoClient;

    public AccessControlService() {
        cognitoClient = CognitoClient.getInstance();
    }

    public Map<String, Object> signIn(String body, String poolName) {
        var request = new Gson().fromJson(body, SignInRequest.class);
        var userPoolId = getUserPoolId(poolName);
        var clientId = getClientId(userPoolId);
        var authenticationParams = Map.of(
                "USERNAME", request.getEmail(),
                "PASSWORD", request.getPassword()
        );

        var authenticationRequest = AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .userPoolId(userPoolId)
                .clientId(clientId)
                .authParameters(authenticationParams)
                .build();
        var authenticationResponse = cognitoClient.adminInitiateAuth(authenticationRequest);
        System.out.println("authenticationResponse: " + authenticationResponse);
        return Map.of("accessToken", authenticationResponse.authenticationResult().idToken());
    }

    public Map<String, Object> signUp(String body, String poolName) {
        var request = new Gson().fromJson(body, SignUpRequest.class);
        var userPoolId = getUserPoolId(poolName);

        var createUserRequest = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .temporaryPassword(request.getPassword())
                .username(request.getEmail())
                .messageAction("SUPPRESS")
                .build();


        var setUserPasswordRequest = AdminSetUserPasswordRequest.builder()
                .userPoolId(userPoolId)
                .username(request.getEmail())
                .password(request.getPassword())
                .permanent(true)
                .build();

       var createUserResult = cognitoClient.adminCreateUser(createUserRequest);
       var adminSetUserPasswordResult = cognitoClient.adminSetUserPassword(setUserPasswordRequest);
       System.out.println("createUserResult: " + createUserResult);
       System.out.println("adminSetUserPasswordResult: " + adminSetUserPasswordResult);
       return Map.of("response", 200);
    }


    private String getUserPoolId(String poolName) {
        var request = ListUserPoolsRequest.builder()
                .build();
        var response = cognitoClient.listUserPools(request);
        return response.userPools().stream()
                .filter(userPool -> userPool.name().equals(poolName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find userPool with name " + poolName))
                .id();
    }

    public String getClientId(String userPoolId) {
        var request = ListUserPoolClientsRequest.builder()
                .userPoolId(userPoolId)
                .build();
        var response = cognitoClient.listUserPoolClients(request);
        var clientId = response.userPoolClients().get(0).clientId();
        if (clientId.isBlank()) {
            throw new RuntimeException("Cannot find client with id " + userPoolId);
        }
        return clientId;
    }

}
