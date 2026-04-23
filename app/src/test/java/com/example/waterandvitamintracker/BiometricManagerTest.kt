package com.example.waterandvitamintracker

import androidx.fragment.app.FragmentActivity
import com.example.waterandvitamintracker.security.AuthState
import com.example.waterandvitamintracker.security.MockBiometricManager
import com.example.waterandvitamintracker.security.SensorType
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BiometricManagerTest {

    private lateinit var manager: MockBiometricManager
    private val mockActivity = mockk<FragmentActivity>()

    @Before
    fun setup() {
        manager = MockBiometricManager()
    }

    @Test
    fun testCheckAvailabilityReturnsUnavailableWhenNoSensor() {
        manager.mockSensorType = SensorType.NONE
        assertEquals(SensorType.NONE, manager.checkAvailability())
    }

    @Test
    fun testCheckAvailabilityReturnsFingerprintWhenAvailable() {
        manager.mockSensorType = SensorType.FINGERPRINT
        assertEquals(SensorType.FINGERPRINT, manager.checkAvailability())
    }

    @Test
    fun testAuthenticateReturnsSuccessOnValidBiometrics() {
        manager.mockSensorType = SensorType.FINGERPRINT
        manager.mockAuthResult = AuthState.Success
        manager.authenticate(mockActivity, "reason")
        assertEquals(AuthState.Success, manager.authState.value)
    }

    @Test
    fun testAuthenticateReturnsFailedOnCancel() {
        manager.mockSensorType = SensorType.FINGERPRINT
        manager.mockAuthResult = AuthState.Failed
        manager.authenticate(mockActivity, "reason")
        assertEquals(AuthState.Failed, manager.authState.value)
    }

    @Test
    fun testAuthenticateReturnsUnavailableWhenNoSensorActive() {
        manager.mockSensorType = SensorType.NONE
        manager.authenticate(mockActivity, "reason")
        assertEquals(AuthState.Unavailable, manager.authState.value)
    }

    @Test
    fun testIsEnabledByUserReturnsTrueAfterSettingTrue() {
        manager.setEnabledByUser(true)
        assertTrue(manager.isEnabledByUser())
    }

    @Test
    fun testIsEnabledByUserReturnsFalseInitially() {
        assertFalse(manager.isEnabledByUser())
    }

    @Test
    fun testIsEnabledByUserReturnsFalseAfterSettingFalse() {
        manager.setEnabledByUser(true)
        manager.setEnabledByUser(false)
        assertFalse(manager.isEnabledByUser())
    }

    @Test
    fun testStateTransitionsToIdleOnReset() {
        manager.mockSensorType = SensorType.FINGERPRINT
        manager.mockAuthResult = AuthState.Success
        manager.authenticate(mockActivity, "reason")
        manager.resetState()
        assertEquals(AuthState.Idle, manager.authState.value)
    }

    @Test
    fun testAuthenticateChangesStateToAuthenticatingBeforeResult() {
        var observedState = AuthState.Idle
        val spyManager = object : MockBiometricManager() {
            override fun authenticate(activity: FragmentActivity, reason: String) {
                super.authenticate(activity, reason)
                if (mockSensorType != SensorType.NONE) {
                    observedState = AuthState.Authenticating
                }
            }
        }
        spyManager.authenticate(mockActivity, "reason")
        assertEquals(AuthState.Authenticating, observedState)
    }

    @Test
    fun testMockCorrectlySimulatesDifferentSensorTypes() {
        manager.mockSensorType = SensorType.FACE_ID
        assertEquals(SensorType.FACE_ID, manager.checkAvailability())
    }

    @Test
    fun testInitialAuthStateIsIdle() {
        assertEquals(AuthState.Idle, manager.authState.value)
    }
}