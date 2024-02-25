import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        Logging_iosKt.doInitLogging()
        DIKt.doInitDI()
    }
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
