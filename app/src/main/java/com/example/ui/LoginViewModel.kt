package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {
    private val auth = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null // Fallback if Firebase not configured
    }

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = LoginUiState.Error("Email and password cannot be empty")
            return
        }
        val currentAuth = try { FirebaseAuth.getInstance() } catch(e: Exception) { null }
        if (currentAuth == null || currentAuth.app.options.projectId == "MY_FIREBASE_PROJECT_ID") {
            _uiState.value = LoginUiState.Error("Firebase is not configured. Please add your credentials in Settings -> Secrets.")
            return
        }
        
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            try {
                currentAuth.signInWithEmailAndPassword(email, pass).await()
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                val msg = when (e) {
                    is FirebaseAuthInvalidUserException -> "User not found. Would you like to sign up?"
                    is FirebaseAuthInvalidCredentialsException -> "Invalid credentials."
                    else -> e.localizedMessage ?: "Login failed"
                }
                _uiState.value = LoginUiState.Error(msg)
            }
        }
    }

    fun loginWithGoogle() {
        _uiState.value = LoginUiState.Error("Google Sign-In requires SHA-1 and Web Client ID configuration in Firebase Console.")
    }

    fun signUp(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = LoginUiState.Error("Email and password cannot be empty")
            return
        }
        val currentAuth = try { FirebaseAuth.getInstance() } catch(e: Exception) { null }
        if (currentAuth == null || currentAuth.app.options.projectId == "MY_FIREBASE_PROJECT_ID") {
            _uiState.value = LoginUiState.Error("Firebase is not configured. Please add your credentials in Settings -> Secrets.")
            return
        }

        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            try {
                currentAuth.createUserWithEmailAndPassword(email, pass).await()
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.localizedMessage ?: "Sign up failed")
            }
        }
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
