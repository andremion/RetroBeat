import shared
import AVKit

class AudioPlayerImpl: AbstractAudioPlayer {
    
    //private var session = AVAudioSession.sharedInstance()

    private var player: AVPlayer? = nil
    private var tracks: Array<AudioPlayerTrack> = Array()
    
    override func initialize(onInitialized: @escaping () -> Void) {
        onInitialized()
    }
    
    override func setTracks(tracks: [AudioPlayerTrack]) {
        self.tracks = tracks
        let currentTrack = tracks[0]
        let url = URL(string: currentTrack.uri)
        let playerItem = AVPlayerItem(url: url!)
        player = AVPlayer(playerItem: playerItem)
        updateTrack { _ in currentTrack }
        updateProgress()
    }

    override func play() {
        if let player = player {
            player.play()
            updateState { state in
                state.copy(isPlaying: true)
            }
            updateProgress()
        }
    }

    override func updateProgress() {
        if let player = player {
            if let currentItem = player.currentItem {
                updateState { state in
                    state.copy(
                        position: Float(player.currentTime().seconds / currentItem.duration.seconds),
                        time: player.currentTime().toString(),
                        duration: currentItem.duration.toString()
                    )
                }
            }
        }
    }

    override func pause() {
        if let player = player {
            player.pause()
            updateState { state in
                state.copy(isPlaying: false)
            }
            updateProgress()
        }
    }

    override func skipToPrevious() {

    }

    override func skipToNext() {

    }

    override func seekBackward() {

    }

    override func seekForward() {

    }
    
    override func toggleRepeatMode() {

    }
    
    override func toggleShuffleMode() {

    }

    override func releasePlayer() {
        player?.pause()
        player = nil
    }
    
    
    /*private func activateSession() {
        do {
            try session.setCategory(
                .playback,
                mode: .default,
                options: []
            )
        } catch _ {}
        
        do {
            try session.setActive(true, options: .notifyOthersOnDeactivation)
        } catch _ {}
        
        do {
            try session.overrideOutputAudioPort(.speaker)
        } catch _ {}
    }
    
    private func deactivateSession() {
        do {
            try session.setActive(false, options: .notifyOthersOnDeactivation)
        } catch let error as NSError {
            print("Failed to deactivate audio session: \(error.localizedDescription)")
        }
    }*/
}

private extension AudioPlayerState {
    func copy(isPlaying: Bool) -> AudioPlayerState {
        AudioPlayerState(
            isPlaying: isPlaying,
            position: self.position,
            time: self.time,
            duration: self.duration,
            repeatMode: self.repeatMode,
            isShuffleModeOn: self.isShuffleModeOn
        )
    }
    func copy(position: Float, time: String, duration: String) -> AudioPlayerState {
        AudioPlayerState(
            isPlaying: self.isPlaying,
            position: position,
            time: time,
            duration: duration,
            repeatMode: self.repeatMode,
            isShuffleModeOn: self.isShuffleModeOn
        )
    }
}

private extension CMTime {
    func toString() -> String {
        let roundedSeconds = seconds.rounded()
        var hours = Int(roundedSeconds / 3600)
        var minutes = Int((roundedSeconds / 60).truncatingRemainder(dividingBy: 60))
        var seconds = Int(roundedSeconds.truncatingRemainder(dividingBy: 60))
        return hours > 0 ?
            String(format: "%d:%02d:%02d", hours, minutes, seconds) :
            String(format: "%02d:%02d", minutes, seconds)
    }
}
