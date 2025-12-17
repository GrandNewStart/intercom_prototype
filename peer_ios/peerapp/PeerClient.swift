//
//  PeerClient.swift
//  peerapp
//
//  Created by emblock on 12/17/25.
//
import Foundation
import Network

class PeerClient {
    private let serverIp: String
    private let serverPort: UInt16
    private let onMessageReceived: (String) -> Void
    
    private var connection: NWConnection?
    private let queue = DispatchQueue(label: "PeerClientQueue")

    init(serverIp: String, serverPort: UInt16 = 8888, onMessageReceived: @escaping (String) -> Void) {
        self.serverIp = serverIp
        self.serverPort = serverPort
        self.onMessageReceived = onMessageReceived
    }

    func start() {
        guard connection == nil else {
            print("PeerClient is already running.")
            return
        }

        let host = NWEndpoint.Host(serverIp)
        let port = NWEndpoint.Port(rawValue: serverPort)!
        
        connection = NWConnection(host: host, port: port, using: .udp)
        
        connection?.stateUpdateHandler = { newState in
            switch newState {
            case .ready:
                print("PeerClient started. Server: \(self.serverIp):\(self.serverPort)")
                self.listenForMessages()
                // Send an initial packet to get registered on the server
                self.sendMessage("Hello from Swift")
            case .failed(let error):
                print("PeerClient error starting: \(error)")
                self.connection = nil
            default:
                break
            }
        }
        
        connection?.start(queue: queue)
    }

    private func listenForMessages() {
        connection?.receiveMessage { [weak self] (content, context, isComplete, error) in
            if let content = content, !content.isEmpty {
                let message = String(data: content, encoding: .utf8) ?? ""
                print("Message received: \(message)")
                self?.onMessageReceived(message)
            }
            
            if error == nil {
                // Listen for the next message
                self?.listenForMessages()
            }
        }
    }

    func sendMessage(_ message: String) {
        guard let connection = connection else {
            print("Cannot send message, client is not running.")
            return
        }
        
        let data = message.data(using: .utf8)
        connection.send(content: data, completion: .contentProcessed { error in
            if let error = error {
                print("Error sending message: \(error)")
            } else {
                print("Message sent: \(message)")
            }
        })
    }

    func stop() {
        guard let connection = connection else { return }
        connection.cancel()
        self.connection = nil
        print("PeerClient stopped.")
    }
}
