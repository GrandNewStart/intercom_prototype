package dev.bluelemonade.peerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.bluelemonade.peerapp.ui.theme.PeerAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PeerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PeerScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun PeerScreen(modifier: Modifier = Modifier, viewModel: MainViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    val peers by viewModel.peers.collectAsState()

    // Replace with the actual IP of your RelayServer device
    val serverIp = "192.168.21.164"

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Connection Status
        val (statusText, statusColor) = when (connectionState) {
            ConnectionState.DISCONNECTED -> "Disconnected" to Color.Red
            ConnectionState.CONNECTING -> "Connecting" to Color.Yellow
            ConnectionState.CONNECTED -> "Connected" to Color.Green
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(statusColor)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = statusText)
        }

        // Peer List
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(peers) { peer ->
                Text(
                    text = peer,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Connect/Disconnect Button
        Button(
            onClick = {
                if (connectionState == ConnectionState.DISCONNECTED) {
                    viewModel.connect(serverIp)
                } else {
                    viewModel.disconnect()
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            val buttonText = if (connectionState == ConnectionState.DISCONNECTED) {
                "Connect"
            } else {
                "Disconnect"
            }
            Text(text = buttonText)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PeerScreenPreview() {
    PeerAppTheme {
        PeerScreen(viewModel = MainViewModel())
    }
}
