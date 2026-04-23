package com.example.waterandvitamintracker.network

import com.example.waterandvitamintracker.models.WsMessage
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class SocketState {
    Disconnected, Connecting, Connected, Reconnecting
}

class MockSocketManager(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    private val _socketState = MutableStateFlow(SocketState.Disconnected)
    val socketState: StateFlow<SocketState> = _socketState

    private var scope = CoroutineScope(dispatcher + SupervisorJob())
    private var connectJob: Job? = null
    private var messageJob: Job? = null
    private var onMessageHandler: ((WsMessage) -> Unit)? = null
    private val gson = Gson()

    fun connect(url: String) {
        if (_socketState.value == SocketState.Connected || _socketState.value == SocketState.Connecting) return

        connectJob?.cancel()
        connectJob = scope.launch {
            try {
                _socketState.value = SocketState.Connecting
                delay(1000)
                _socketState.value = SocketState.Connected
                startMessageLoop()
            } finally {
            }
        }
    }

    private fun startMessageLoop() {
        messageJob?.cancel()
        messageJob = scope.launch {
            try {
                while (isActive) {
                    delay(4000)
                    val json = """{"type":"reminder","text":"Time to drink water!","timestamp":${System.currentTimeMillis()}}"""
                    val msg = gson.fromJson(json, WsMessage::class.java)
                    onMessageHandler?.invoke(msg)
                }
            } finally {
            }
        }
    }

    fun disconnect() {
        try {
            connectJob?.cancel()
            messageJob?.cancel()
            _socketState.value = SocketState.Disconnected
        } finally {
        }
    }

    fun send(message: String) {
    }

    fun onMessage(handler: (WsMessage) -> Unit) {
        onMessageHandler = handler
    }

    fun simulateDisconnectForReconnect() {
        connectJob?.cancel()
        connectJob = scope.launch {
            try {
                messageJob?.cancel()
                _socketState.value = SocketState.Reconnecting
                delay(2000)
                _socketState.value = SocketState.Connected
                startMessageLoop()
            } finally {
            }
        }
    }

    fun destroy() {
        scope.cancel()
    }
}