//
//  peerappApp.swift
//  peerapp
//
//  Created by emblock on 12/17/25.
//

import SwiftUI

@main
struct PeerApp: App {
    var body: some Scene {
        WindowGroup {
            // Set PeerScreen as the root view of the application
            PeerScreen()
        }
    }
}

struct PeerScreen: View {
    // @StateObject creates and keeps a single instance of the ViewModel for this view.
    @StateObject private var viewModel = MainViewModel()
    
    // Replace with the actual IP of your RelayServer device
    private let serverIp = "192.168.222.74"
    
    var body: some View {
        // VStack arranges its children vertically, similar to Compose's Column.
        VStack(spacing: 0) {
            // Connection Status
            statusView
            
            // Peer List
            // List is SwiftUI's equivalent to LazyColumn for displaying rows of data.
            List(viewModel.peers, id: \.self) { peer in
                Text(peer)
            }
            
            // Connect/Disconnect Button
            connectButton
                .padding()
        }
    }
    
    // A computed property to build the status view.
    @ViewBuilder
    private var statusView: some View {
        let (statusText, statusColor): (String, Color) = {
            switch viewModel.connectionState {
            case .disconnected:
                return ("Disconnected", .red)
            case .connecting:
                return ("Connecting", .yellow)
            case .connected:
                return ("Connected", .green)
            }
        }()
        
        Text(statusText)
            .frame(maxWidth: .infinity) // Fills the width
            .padding(8)
            .background(statusColor)
    }
    
    // A computed property to build the button view.
    @ViewBuilder
    private var connectButton: some View {
        Button(action: {
            if viewModel.connectionState == .disconnected {
                viewModel.connect(serverIp: serverIp)
            } else {
                viewModel.disconnect()
            }
        }) {
            let buttonText = (viewModel.connectionState == .disconnected) ? "Connect" : "Disconnect"
            Text(buttonText)
                .frame(maxWidth: .infinity)
        }
        .buttonStyle(.borderedProminent)
    }
}

#Preview {
    PeerScreen()
}
