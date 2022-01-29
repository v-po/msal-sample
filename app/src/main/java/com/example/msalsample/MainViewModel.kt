package com.example.msalsample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.msalsample.msal.AuthHelper
import com.microsoft.identity.client.IAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

interface MainViewModel {
    val viewState: LiveData<ViewState>

    sealed class ViewState {
        class LoggedIn(val account: IAccount) : ViewState()
        object LoggedOut : ViewState()
        object Loading : ViewState()
        class Error(val message: String) : ViewState()
    }

    fun onViewCreated()
    fun onSignInClicked()
    fun onSignOutClicked()
}

@HiltViewModel
@JvmSuppressWildcards
class MainViewModelDefault @Inject constructor(
    private val authHelper: AuthHelper
) : ViewModel(), MainViewModel {

    override val viewState = MutableLiveData<MainViewModel.ViewState>()

    override fun onViewCreated() {
        viewState.value = MainViewModel.ViewState.Loading
        viewModelScope.launch {
            authHelper.getAccount().fold(
                onSuccess = { account ->
                    viewState.value = MainViewModel.ViewState.LoggedIn(account)
                },
                onFailure = {
                    viewState.value = MainViewModel.ViewState.LoggedOut
                }
            )
        }
    }

    override fun onSignInClicked() {
        viewState.value = MainViewModel.ViewState.Loading
        viewModelScope.launch {
            authHelper.acquireTokenInteractively().fold(
                onSuccess = {
                    viewState.value = MainViewModel.ViewState.LoggedIn(it.account)
                },
                onFailure = {
                    viewState.value = MainViewModel.ViewState.Error(it.message ?: "Error")
                }
            )
        }
    }

    override fun onSignOutClicked() {
        viewModelScope.launch {
            authHelper.signOut().fold(
                onSuccess = {
                    viewState.value = MainViewModel.ViewState.LoggedOut
                },
                onFailure = {
                    viewState.value = MainViewModel.ViewState.LoggedOut
                }
            )
        }
    }
}
