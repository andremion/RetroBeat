import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        DIKt.doInitDI(audioPlayer: AudioPlayerImpl())
        Logging_iosKt.doInitLogging()
    }
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
