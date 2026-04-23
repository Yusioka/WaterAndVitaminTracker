package com.example.waterandvitamintracker.security

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AuthState {
    Idle, Authenticating, Success, Failed, Unavailable
}

enum class SensorType {
    FACE_ID, FINGERPRINT, NONE
}

interface AppBiometricManager {
    val authState: StateFlow<AuthState>
    fun checkAvailability(): SensorType
    fun authenticate(activity: FragmentActivity, reason: String)
    fun isEnabledByUser(): Boolean
    fun setEnabledByUser(enabled: Boolean)
    fun getLockTimeoutSeconds(): Long
    fun setLockTimeoutSeconds(seconds: Long)
    fun resetState()
}

class RealBiometricManager(private val context: Context) : AppBiometricManager {
    private val _authState = MutableStateFlow(AuthState.Idle)
    override val authState: StateFlow<AuthState> = _authState

    private val prefs: SharedPreferences = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    override fun checkAvailability(): SensorType {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val packageManager = context.packageManager
            return when {
                packageManager.hasSystemFeature(PackageManager.FEATURE_FACE) -> SensorType.FACE_ID
                packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) -> SensorType.FINGERPRINT
                else -> SensorType.FINGERPRINT
            }
        }
        return SensorType.NONE
    }

    override fun authenticate(activity: FragmentActivity, reason: String) {
        if (checkAvailability() == SensorType.NONE) {
            _authState.value = AuthState.Unavailable
            return
        }

        _authState.value = AuthState.Authenticating

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    _authState.value = AuthState.Failed
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    _authState.value = AuthState.Success
                }

                override fun onAuthenticationFailed() {
                    _authState.value = AuthState.Failed
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authentication Required")
            .setSubtitle(reason)
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    override fun isEnabledByUser(): Boolean {
        return prefs.getBoolean("biometrics_enabled", false)
    }

    override fun setEnabledByUser(enabled: Boolean) {
        prefs.edit().putBoolean("biometrics_enabled", enabled).apply()
    }

    override fun getLockTimeoutSeconds(): Long {
        return prefs.getLong("lock_timeout", 5L)
    }

    override fun setLockTimeoutSeconds(seconds: Long) {
        prefs.edit().putLong("lock_timeout", seconds).apply()
    }

    override fun resetState() {
        _authState.value = AuthState.Idle
    }
}

class MockBiometricManager : AppBiometricManager {
    private val _authState = MutableStateFlow(AuthState.Idle)
    override val authState: StateFlow<AuthState> = _authState

    var mockSensorType = SensorType.FINGERPRINT
    var mockAuthResult = AuthState.Success
    private var isEnabled = false
    private var timeout = 5L

    override fun checkAvailability(): SensorType = mockSensorType

    override fun authenticate(activity: FragmentActivity, reason: String) {
        _authState.value = AuthState.Authenticating
        if (mockSensorType == SensorType.NONE) {
            _authState.value = AuthState.Unavailable
        } else {
            _authState.value = mockAuthResult
        }
    }

    override fun isEnabledByUser(): Boolean = isEnabled

    override fun setEnabledByUser(enabled: Boolean) {
        isEnabled = enabled
    }

    override fun getLockTimeoutSeconds(): Long = timeout

    override fun setLockTimeoutSeconds(seconds: Long) {
        timeout = seconds
    }

    override fun resetState() {
        _authState.value = AuthState.Idle
    }
}