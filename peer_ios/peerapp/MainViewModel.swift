//
//  MainViewModel.swift
//  peerapp
//
//  Created by emblock on 12/17/25.
//

import Foundation
import Combine

// The same enum as in the Android project
enum ConnectionState {
    case disconnected
    case connecting
    case connected
}

class MainViewModel: ObservableObject {

    @Published var connectionState: ConnectionState = .disconnected
    @Published var peers: [String] = []

    private var peerClient: PeerClient?

    func connect(serverIp: String) {
        if peerClient == nil {
            connectionState = .connecting
            peerClient = PeerClient(
                serverIp: serverIp,
                onMessageReceived: { [weak self] message in
                    // The onMessageReceived callback is on a background thread,
                    // so we must dispatch UI updates to the main thread.
                    DispatchQueue.main.async {
                        // Assuming the message is a comma-separated list of peers
                        let newPeers = message.split(separator: ",").map(String.init)
                        self?.peers = newPeers
                        self?.connectionState = .connected
                    }
                }
            )
            peerClient?.start()
        }
    }

    func disconnect() {
        peerClient?.stop()
        peerClient = nil
        connectionState = .disconnected
        peers = []
    }
    
    // This is called automatically when the ViewModel is deallocated.
    deinit {
        peerClient?.stop()
    }
}
