package dev.bluelemonade.relayapp

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RelayServer(val port: Int = 8888) {
    private val socket = DatagramSocket(port)
    private val clients = mutableSetOf<Pair<InetAddress, Int>>() // Tracks connected IPs and Ports
    private var isRunning = true

    var onClientsChanged: ((List<String>) -> Unit)? = null
    var onLog: ((String) -> Unit)? = null

    private fun log(message: String) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timestamp = sdf.format(Date())
        onLog?.invoke("[$timestamp] $message")
    }

    fun startServer() {
        // Receiver Thread
        Thread {
            val buffer = ByteArray(2048)
            log("Relay Server started on port $port")

            while (isRunning) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet) // Blocks until data arrives

                    val clientAddress = packet.address
                    val clientPort = packet.port
                    val clientKey = Pair(clientAddress, clientPort)

                    if (!clients.contains(clientKey)) {
                        clients.add(clientKey)
                        log("New client connected: ${clientAddress.hostAddress}:$clientPort")
                        val clientStrings = clients.map { "${it.first.hostAddress}:${it.second}" }
                        onClientsChanged?.invoke(clientStrings)
                    }

                    val data = packet.data.copyOfRange(0, packet.length)
                    broadcast(data, clientKey)
                } catch (e: Exception) {
                    if (isRunning) {
                        log("Error receiving packet: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        }.start()

        // Logging Thread
        Thread {
            while(isRunning) {
                try {
                    log("Current clients: ${clients.size}")
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    // Thread interrupted, likely shutting down
                }
            }
        }.start()
    }

    private fun broadcast(data: ByteArray, senderKey: Pair<InetAddress, Int>) {
        clients.forEach { client ->
            if (client != senderKey) {
                try {
                    val sendPacket = DatagramPacket(data, data.size, client.first, client.second)
                    socket.send(sendPacket)
                } catch (e: Exception) {
                    log("Error sending packet to ${client.first.hostAddress}:${client.second}: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    fun stopServer() {
        log("Stopping server...")
        isRunning = false
        socket.close()
        clients.clear()
        onClientsChanged?.invoke(emptyList())
        log("Server stopped.")
    }
}