package com.example.waterandvitamintracker

import com.example.waterandvitamintracker.models.WsMessage
import com.example.waterandvitamintracker.network.MockSocketManager
import com.example.waterandvitamintracker.network.SocketState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SocketManagerTest {

    private lateinit var manager: MockSocketManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        manager = MockSocketManager(testDispatcher)
    }

    @After
    fun tearDown() {
        manager.disconnect()
        manager.destroy()
        Dispatchers.resetMain()
    }

    private fun executeLifecycleSafeTest(testBody: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        try {
            testBody()
        } finally {
            manager.destroy()
        }
    }

    @Test
    fun testInitialStateIsDisconnected() = executeLifecycleSafeTest {
        assertEquals(SocketState.Disconnected, manager.socketState.value)
    }

    @Test
    fun testConnectEventuallyChangesToConnected() = executeLifecycleSafeTest {
        manager.connect("url")
        advanceTimeBy(1500)
        runCurrent()
        assertEquals(SocketState.Connected, manager.socketState.value)
    }

    @Test
    fun testDisconnectChangesStateToDisconnected() = executeLifecycleSafeTest {
        manager.connect("url")
        advanceTimeBy(1500)
        manager.disconnect()
        runCurrent()
        assertEquals(SocketState.Disconnected, manager.socketState.value)
    }

    @Test
    fun testReconnectChangesStateToReconnecting() = executeLifecycleSafeTest {
        manager.connect("url")
        advanceTimeBy(1500)
        manager.simulateDisconnectForReconnect()
        runCurrent()
        assertEquals(SocketState.Reconnecting, manager.socketState.value)
    }

    @Test
    fun testReconnectEventuallyChangesToConnected() = executeLifecycleSafeTest {
        manager.connect("url")
        advanceTimeBy(1500)
        manager.simulateDisconnectForReconnect()
        advanceTimeBy(2500)
        runCurrent()
        assertEquals(SocketState.Connected, manager.socketState.value)
    }

    @Test
    fun testMessageIsReceivedAfterConnection() = executeLifecycleSafeTest {
        var received = false
        manager.onMessage { received = true }
        manager.connect("url")
        advanceTimeBy(5500)
        runCurrent()
        assertTrue(received)
    }

    @Test
    fun testNoMessagesWhenDisconnected() = executeLifecycleSafeTest {
        var receivedCount = 0
        manager.onMessage { receivedCount++ }
        manager.connect("url")
        advanceTimeBy(500)
        manager.disconnect()
        advanceTimeBy(10000)
        runCurrent()
        assertEquals(0, receivedCount)
    }

    @Test
    fun testSendDoesNotCrash() = executeLifecycleSafeTest {
        manager.connect("url")
        manager.send("test")
        assertTrue(true)
    }

    @Test
    fun testMultipleConnectCallsIgnored() = executeLifecycleSafeTest {
        manager.connect("url")
        runCurrent()
        manager.connect("url2")
        runCurrent()
        assertEquals(SocketState.Connecting, manager.socketState.value)
    }

    @Test
    fun testMessageParsingStructure() = executeLifecycleSafeTest {
        var msg: WsMessage? = null
        manager.onMessage { msg = it }
        manager.connect("url")
        advanceTimeBy(5500)
        runCurrent()
        assertEquals("reminder", msg?.type)
        assertEquals("Time to drink water!", msg?.text)
    }

    @Test
    fun testOnMessageHandlerCanBeReassigned() = executeLifecycleSafeTest {
        var count = 0
        manager.onMessage { count += 1 }
        manager.connect("url")
        advanceTimeBy(5500)
        manager.onMessage { count += 10 }
        advanceTimeBy(4500)
        runCurrent()
        assertEquals(11, count)
    }

    @Test
    fun testDisconnectCancelsMessageLoop() = executeLifecycleSafeTest {
        var count = 0
        manager.onMessage { count++ }
        manager.connect("url")
        advanceTimeBy(5500)
        manager.disconnect()
        advanceTimeBy(10000)
        runCurrent()
        assertEquals(1, count)
    }

    @Test
    fun testReconnectResumesMessageLoop() = executeLifecycleSafeTest {
        var count = 0
        manager.onMessage { count++ }
        manager.connect("url")
        advanceTimeBy(5500)
        manager.simulateDisconnectForReconnect()
        advanceTimeBy(6500)
        runCurrent()
        assertEquals(2, count)
    }

    @Test
    fun testConnectWhileConnectedIgnored() = executeLifecycleSafeTest {
        manager.connect("url")
        advanceTimeBy(1500)
        runCurrent()
        assertEquals(SocketState.Connected, manager.socketState.value)
        manager.connect("url2")
        runCurrent()
        assertEquals(SocketState.Connected, manager.socketState.value)
    }

    @Test
    fun testMultipleMessagesReceivedOverTime() = executeLifecycleSafeTest {
        var count = 0
        manager.onMessage { count++ }
        manager.connect("url")
        advanceTimeBy(10000)
        runCurrent()
        assertEquals(2, count)
    }
}