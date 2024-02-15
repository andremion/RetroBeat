import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        DIKt.doInitDI(audioPlayer: AudioPlayerImpl())
    }
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
