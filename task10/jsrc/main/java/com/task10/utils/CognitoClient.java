package com.task10.utils;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

public final class CognitoClient {

    private static final CognitoIdentityProviderClient INSTANCE = createInstance();

    private CognitoClient() {

    }

    public static CognitoIdentityProviderClient getInstance() {
        return INSTANCE;
    }

    private static CognitoIdentityProviderClient createInstance() {
        return CognitoIdentityProviderClient.create();
    }
}
