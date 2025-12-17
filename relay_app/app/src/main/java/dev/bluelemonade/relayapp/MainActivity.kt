package dev.bluelemonade.relayapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {

    private var relayServer: RelayServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                var isServerRunning by remember { mutableStateOf(false) }
                val logText = remember { mutableStateOf("") }
                var peers by remember { mutableStateOf<List<String>>(emptyList()) }

                MainScreen(
                    isServerRunning = isServerRunning,
                    logText = logText.value,
                    peers = peers,
                    onStartStopClick = {
                        if (isServerRunning) {
                            relayServer?.stopServer()
                            relayServer = null
                            isServerRunning = false
                            logText.value = ""
                            peers = emptyList()
                        } else {
                            relayServer = RelayServer().apply {
                                startServer()
                                onClientsChanged = { newPeers ->
                                    runOnUiThread { peers = newPeers }
                                }
                                onLog = { logMessage ->
                                    runOnUiThread { logText.value += "$logMessage\n" }
                                }
                            }
                            isServerRunning = true
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        relayServer?.stopServer()
    }
}

@Composable
fun MainScreen(
    isServerRunning: Boolean,
    logText: String,
    peers: List<String>,
    onStartStopClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(logText) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            Text(
                text = logText,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .verticalScroll(scrollState)
                    .padding(bottom = 8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(peers) { peer ->
                    Text(peer, modifier = Modifier.padding(8.dp))
                }
            }
            Button(
                onClick = onStartStopClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isServerRunning) "Stop Server" else "Start Server")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme {
        MainScreen(
            isServerRunning = false,
            logText = "Server logs...\nAnother log...",
            peers = listOf("192.168.1.1:8888", "192.168.1.2:8888"),
            onStartStopClick = {}
        )
    }
}
