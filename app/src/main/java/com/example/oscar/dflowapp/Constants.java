

/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.example.oscar.dflowapp;


/*
These constant values configure the client app to use OAuth2 and open id connect
to authenticate with Azure and authorize the app to access the specified scopes.
Read more about scopes: https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-v2-scopes
 */
interface Constants {
    String AUTHORITY_URL = "https://login.microsoftonline.com/common";
    // Update these two constants with the values for your application:
    String CLIENT_ID = "7266e346-eb83-441c-be6d-6bfc8e3eaf61";
    String REDIRECT_URI = "https://login.microsoftonline.com/common/oauth2/nativeclient";
    String MICROSOFT_GRAPH_API_ENDPOINT_RESOURCE_ID = "https://graph.microsoft.com/";
    String SCOPES = "openid profile User.Read Mail.Send offline_access";

}
