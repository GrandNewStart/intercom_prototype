package dev.bluelemonade.peerapp

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.Executors

/**
 * A client for connecting to the RelayServer.
 *
 * This client handles sending and receiving UDP packets to and from the server.
 *
 * @param serverIp The IP address of the device running the RelayServer.
 * @param serverPort The port number the RelayServer is listening on (default is 8888).
 * @param onMessageReceived A callback function that is invoked with the message
 *                          received from another peer via the server.
 */
class PeerClient(
    private val serverIp: String,
    private val serverPort: Int = 8888,
    private val onMessageReceived: (String) -> Unit
) {
    private var socket: DatagramSocket? = null
    private val executor = Executors.newSingleThreadExecutor()
    private var isRunning = false

    /**
     * Starts the client.
     *
     * This initializes the socket, starts a listener thread for incoming messages,
     * and sends an initial packet to register with the server.
     */
    fun start() {
        if (isRunning) {
            Log.w("PeerClient", "Client is already running.")
            return
        }
        isRunning = true

        executor.execute {
            try {
                // Use a random available local port
                socket = DatagramSocket()
                val serverAddress = InetAddress.getByName(serverIp)
                Log.d("PeerClient", "Client started. Server: $serverIp:$serverPort")

                // Start a listener thread for messages from the server
                Thread { listenForMessages() }.start()

                // Send an initial packet to get registered on the server
                val initialMessage = "Hello from ${socket?.localPort}".toByteArray()
                val initialPacket = DatagramPacket(initialMessage, initialMessage.size, serverAddress, serverPort)
                socket?.send(initialPacket)

            } catch (e: Exception) {
                Log.e("PeerClient", "Error starting client", e)
                isRunning = false
            }
        }
    }

    private fun listenForMessages() {
        Log.d("PeerClient", "Listener thread started.")
        while (isRunning) {
            try {
                val buffer = ByteArray(2048) // Buffer for incoming data
                val packet = DatagramPacket(buffer, buffer.size)
                socket?.receive(packet) // This call blocks until a packet is received

                if (packet.length > 0) {
                    val message = String(packet.data, 0, packet.length)
                    Log.d("PeerClient", "Message received: $message")
                    onMessageReceived(message)
                }
            } catch (e: Exception) {
                if (isRunning) {
                    Log.e("PeerClient", "Error in listener thread", e)
                }
            }
        }
        Log.d("PeerClient", "Listener thread stopped.")
    }

    /**
     * Sends a message to the server, which will broadcast it to other peers.
     *
     * @param message The message to send. This can be any string, but for a real
     *                voice call app, you would send encoded audio data.
     */
    fun sendMessage(message: String) {
        if (!isRunning) {
            Log.w("PeerClient", "Cannot send message, client is not running.")
            return
        }
        executor.execute {
            try {
                val serverAddress = InetAddress.getByName(serverIp)
                val data = message.toByteArray()
                val packet = DatagramPacket(data, data.size, serverAddress, serverPort)
                socket?.send(packet)
                Log.d("PeerClient", "Message sent: $message")
            } catch (e: Exception) {
                Log.e("PeerClient", "Error sending message", e)
            }
        }
    }

    /**
     * Stops the client and closes the socket.
     */
    fun stop() {
        if (!isRunning) return
        isRunning = false
        executor.shutdown()
        socket?.close()
        socket = null
        Log.d("PeerClient", "Client stopped.")
    }
}