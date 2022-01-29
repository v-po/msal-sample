package com.example.msalsample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.msalsample.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels<MainViewModelDefault>()

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLayout()
        setupObservers()
        viewModel.onViewCreated()
    }

    private fun setupLayout() {
        binding.btnSignIn.setOnClickListener {
            viewModel.onSignInClicked()
        }
        binding.btnSignOut.setOnClickListener {
            viewModel.onSignOutClicked()
        }
    }

    private fun setupObservers() {
        viewModel.viewState.observe(this) { viewState: MainViewModel.ViewState ->
            render(viewState)
        }
    }

    private fun render(viewState: MainViewModel.ViewState) {
        binding.btnSignIn.isVisible = viewState !is MainViewModel.ViewState.LoggedIn
        binding.btnSignOut.isVisible = viewState is MainViewModel.ViewState.LoggedIn
        binding.tvUserName.isVisible = viewState is MainViewModel.ViewState.LoggedIn
        binding.progressBar.isVisible = viewState is MainViewModel.ViewState.Loading

        when (viewState) {
            is MainViewModel.ViewState.LoggedIn -> {
                binding.tvUserName.text = viewState.account.username
            }
            is MainViewModel.ViewState.Error -> {
                Toast.makeText(this, viewState.message, Toast.LENGTH_LONG).show()
            }
            MainViewModel.ViewState.LoggedOut -> {
            }
            MainViewModel.ViewState.Loading -> {
            }
        }
    }

}
