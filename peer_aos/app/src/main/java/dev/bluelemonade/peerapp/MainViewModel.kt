package dev.bluelemonade.peerapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

class MainViewModel : ViewModel() {

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _peers = MutableStateFlow<List<String>>(emptyList())
    val peers: StateFlow<List<String>> = _peers

    private var peerClient: PeerClient? = null

    fun connect(serverIp: String) {
        if (peerClient == null) {
            _connectionState.value = ConnectionState.CONNECTING
            peerClient = PeerClient(
                serverIp = serverIp,
                onMessageReceived = { message ->
                    // This is called from a background thread
                    viewModelScope.launch {
                        // A more robust solution would parse the message
                        // to differentiate between peer list updates and regular messages.
                        // For now, we'll assume any message is a peer update.
                        val newPeers = message.split(",")
                        _peers.value = newPeers
                        _connectionState.value = ConnectionState.CONNECTED
                    }
                }
            )
            peerClient?.start()
        }
    }

    fun disconnect() {
        peerClient?.stop()
        peerClient = null
        _connectionState.value = ConnectionState.DISCONNECTED
        _peers.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        peerClient?.stop()
    }
}
