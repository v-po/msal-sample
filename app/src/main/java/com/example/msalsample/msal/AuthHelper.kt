package com.example.msalsample.msal

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import com.example.msalsample.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class AuthHelper(
    @ApplicationContext context: Context,
) : Application.ActivityLifecycleCallbacks {

    private val scopes = arrayOf("User.Read", "Files.ReadWrite.AppFolder")

    private val pca: ISingleAccountPublicClientApplication

    private var startedActivities = ArrayList<String>()
    private var currentActivity: Activity? = null
    private var isActivityResumed = false

    init {
        pca = HackySingleAccountPublicClientApplication(
            PublicClientApplicationConfigurationFactory.initializeConfiguration(
                context, R.raw.msal_config
            )
        )
    }

    suspend fun acquireTokenInteractively(): Result<IAuthenticationResult> {
        val activity = currentActivity
        if (activity == null) {
            Log.e(TAG, "Unable to acquire token interactively: currentActivity is null")
            return Result.failure(Exception())
        }
        return withContext(Dispatchers.IO) {
            suspendCoroutine { continuation: Continuation<Result<IAuthenticationResult>> ->
                pca.signIn(activity, null, scopes, object : AuthenticationCallback {
                    override fun onSuccess(authenticationResult: IAuthenticationResult) {
                        Log.d(TAG, "[MSAL] Authentication Successful")
                        continuation.resume(Result.success(authenticationResult))
                    }

                    override fun onError(exception: MsalException) {
                        Log.e(TAG, "[MSAL] Authentication Error", exception)
                        continuation.resume(Result.failure(exception))
                    }

                    override fun onCancel() {
                        Log.d(TAG, "[MSAL] Authentication Cancelled")
                        continuation.resume(Result.failure(Exception()))
                    }
                })
            }
        }
    }

    suspend fun getAccount(): Result<IAccount> {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { continuation: Continuation<Result<IAccount>> ->
                try {
                    val account = pca.currentAccount.currentAccount
                    if (account != null) {
                        continuation.resume(Result.success(account))
                    } else {
                        continuation.resume(Result.failure(Exception()))
                    }
                } catch (exception: MsalException) {
                    Log.e(TAG, "[MSAL] Error retrieving the current account", exception)
                    continuation.resume(Result.failure(exception))
                }
            }
        }
    }

    suspend fun signOut(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { continuation: Continuation<Result<Unit>> ->
                pca.signOut(object :
                    ISingleAccountPublicClientApplication.SignOutCallback {
                    override fun onSignOut() {
                        Log.d(TAG, "[MSAL] Signed out")
                        continuation.resume(Result.success(Unit))
                    }

                    override fun onError(exception: MsalException) {
                        Log.e(TAG, "[MSAL] Error signing out", exception)
                        continuation.resume(Result.failure(exception))
                    }
                })
            }
        }
    }

    /**
     * [Application.ActivityLifecycleCallbacks]
     */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = activity
        log(activity, "Created")
    }

    override fun onActivityStarted(activity: Activity) {
        startedActivities.add(activity.localClassName)
        currentActivity = activity
        log(activity, "Started")
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        isActivityResumed = true
        log(activity, "Resumed")
    }

    override fun onActivityPaused(activity: Activity) {
        isActivityResumed = false
        log(activity, "SaveInstanceState")
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivities.remove(activity.localClassName)
        log(activity, "Stopped")
        if (startedActivities.isEmpty()) {
            // all activities are stopped
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, savedInstanceState: Bundle) {
        log(activity, "SaveInstanceState")
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
        log(activity, "Destroyed")
    }

    private fun log(activity: Activity, lifecycle: String) {
        val message = "Activity $lifecycle ${activity.javaClass.simpleName}"
        Log.d(TAG, message)
    }

    companion object {
        val TAG = AuthHelper::class.simpleName
    }
}
