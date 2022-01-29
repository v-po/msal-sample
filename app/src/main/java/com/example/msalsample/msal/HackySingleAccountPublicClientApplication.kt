package com.example.msalsample.msal

import com.microsoft.identity.client.PublicClientApplicationConfiguration
import com.microsoft.identity.client.SingleAccountPublicClientApplication

/**
 * The MSAL library does not allow creating an instance of
 * [com.microsoft.identity.client.IPublicClientApplication] on the main thread,
 * making it difficult to create a Dagger Provider for it.
 * This class extends [SingleAccountPublicClientApplication] in order to access its protected
 * constructor
 */
class HackySingleAccountPublicClientApplication(
    config: PublicClientApplicationConfiguration
) : SingleAccountPublicClientApplication(config)
