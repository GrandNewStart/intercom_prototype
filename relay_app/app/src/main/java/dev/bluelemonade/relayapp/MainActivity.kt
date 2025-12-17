package dev.bluelemonade.relayapp

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var peerList: RecyclerView
    private lateinit var startStopButton: Button
    private lateinit var logView: TextView
    private lateinit var peerListAdapter: PeerListAdapter

    private var relayServer: RelayServer? = null
    private val maxLogLines = 100
    private val logBuffer = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        peerList = findViewById(R.id.peer_list)
        startStopButton = findViewById(R.id.start_stop_button)
        logView = findViewById(R.id.log_view)
        logView.movementMethod = ScrollingMovementMethod()

        peerListAdapter = PeerListAdapter()
        peerList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = peerListAdapter
        }

        startStopButton.setOnClickListener {
            if (relayServer == null) {
                relayServer = RelayServer().apply {
                    startServer()
                    onClientsChanged = { runOnUiThread { peerListAdapter.updatePeers(it) } }
                    onLog = { runOnUiThread { appendLog(it) } }
                }
                startStopButton.text = getString(R.string.stop_button_label)
            } else {
                relayServer?.stopServer()
                relayServer = null
                startStopButton.text = getString(R.string.start_button_label)
                clearLogs()
            }
        }
    }

    private fun appendLog(message: String) {
        if (logBuffer.lines().size >= maxLogLines) {
            val firstNewline = logBuffer.indexOf('\n')
            if (firstNewline != -1) {
                logBuffer.delete(0, firstNewline + 1)
            }
        }
        logBuffer.append(message).append("\n")
        logView.text = logBuffer.toString()

        // Auto-scroll to the bottom
        val scrollAmount = logView.layout.getLineTop(logView.lineCount) - logView.height
        if (scrollAmount > 0) {
            logView.scrollTo(0, scrollAmount)
        } else {
            logView.scrollTo(0, 0)
        }
    }

    private fun clearLogs() {
        logBuffer.clear()
        logView.text = ""
    }

    override fun onDestroy() {
        super.onDestroy()
        relayServer?.stopServer()
    }
}

class PeerListAdapter : RecyclerView.Adapter<PeerListAdapter.PeerViewHolder>() {

    private val peers = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_peer, parent, false)
        return PeerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PeerViewHolder, position: Int) {
        holder.bind(peers[position])
    }

    override fun getItemCount(): Int = peers.size

    fun updatePeers(newPeers: List<String>) {
        peers.clear()
        peers.addAll(newPeers)
        notifyDataSetChanged()
    }

    class PeerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val peerName: TextView = itemView.findViewById(R.id.peer_name)

        fun bind(peer: String) {
            peerName.text = peer
        }
    }
}