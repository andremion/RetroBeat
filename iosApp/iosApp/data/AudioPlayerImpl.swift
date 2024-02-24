import shared
import AVKit

class AudioPlayerImpl: AbstractAudioPlayer {
    
    //private var session = AVAudioSession.sharedInstance()

    private var player: AVPlayer? = nil
    private var currentItemIndex: Int = 0
    private var tracks: Array<AudioPlayerTrack> = Array()
    private var timeObserverToken: Any? = nil
    
    override var seekBackIncrementInSeconds: Int32 { 5 }
    override var seekForwardIncrementInSeconds: Int32 { 15 }
    
    override func initialize(onInitialized: @escaping () -> Void) {
        // iOS doesn't need to asynchronously initialize anything so far
        onInitialized()
    }
    
    override func setTracks(tracks: [AudioPlayerTrack]) {
        self.tracks = tracks
        setCurrentItem(index: 0)
    }
    
    override func playPause() {
        if let state = state.value as? AudioPlayerState {
            if (state.isPlaying) {
                pause()
            } else {
                play()
            }
        }
    }
    
    private func play() {
        if let player = player {
            player.play()
            updateState { state in
                state.copy(isPlaying: true)
            }
            updateProgress()
        }
    }
    
    override func play(trackIndex: Int32) {
        setCurrentItem(index: Int(trackIndex))
        play()
    }
    
    override func updateProgress() {
        if let player = player {
            if let currentItem = player.currentItem {
                updateState { state in
                    NSLog("currentTime: \(player.currentTime().seconds.rounded()), duration: \(currentItem.duration.seconds.rounded())")
                    return state.copy(
                        position: Float(player.currentTime().seconds / currentItem.duration.seconds),
                        time: DurationKt
                            .toDuration(milliseconds: player.currentTime().seconds.rounded() * 1_000)
                    )
                }
            }
        }
    }
    
    private func pause() {
        if let player = player {
            player.pause()
            updateState { state in
                state.copy(isPlaying: false)
            }
            updateProgress()
        }
    }

    override func skipToPrevious() {
        if (currentItemIndex > 0) {
            play(trackIndex: Int32(currentItemIndex - 1))
        }
    }

    override func skipToNext() {
        if (currentItemIndex < tracks.count - 1) {
            play(trackIndex: Int32(currentItemIndex + 1))
        }
    }

    override func seekBackward() {
        // TODO seekBackward
    }

    override func seekForward() {
        // TODO seekForward
    }
    
    override func toggleRepeatMode() {
        // TODO toggleRepeatMode
    }
    
    override func toggleShuffleMode() {
        // TODO toggleShuffleMode
    }

    override func releasePlayer() {
        player?.pause()
        player = nil
    }
    
    private func setCurrentItem(index: Int) {
        currentItemIndex = index

        let currentTrack = tracks[index]
        updateTrack { track in currentTrack }

        let url = URL(string: currentTrack.uri)
        let playerItem = AVPlayerItem(url: url!)
        if let timeObserverToken = timeObserverToken {
            player?.removeTimeObserver(timeObserverToken)
            self.timeObserverToken = nil
        }
        if let player = player {
            player.pause()
            player.replaceCurrentItem(with: playerItem)
        } else {
            player = AVPlayer(playerItem: playerItem)
        }
        
        updateDuration()
        
        updateProgress()
    }
    
    private func updateDuration() {
        Task.init {
            do {
                if let player = player {
                    if let currentItem = player.currentItem {
                        let duration = try await currentItem.asset.load(.duration)
                        let interval = CMTimeMultiplyByFloat64(duration, multiplier: 1.0)
                        NSLog("duration=\(duration.seconds.rounded()), interval=\(interval.seconds.rounded())")
                        timeObserverToken = player.addBoundaryTimeObserver(
                            forTimes: [NSValue(time: interval)],
                            queue: .main
                        ) { [weak self] in
                            self?.skipToNext()
                        }
                        updateState { state in
                            state.copy(
                                duration: DurationKt
                                    .toDuration(milliseconds: duration.seconds.rounded() * 1_000)
                            )
                        }
                    }
                }
            } catch {
                NSLog("Error on fetching player item duration")
            }
        }
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
    
    func copy(duration: Int64) -> AudioPlayerState {
        AudioPlayerState(
            isPlaying: self.isPlaying,
            position: self.position,
            time: self.time,
            duration: duration,
            repeatMode: self.repeatMode,
            isShuffleModeOn: self.isShuffleModeOn
        )
    }
    
    func copy(position: Float, time: Int64) -> AudioPlayerState {
        AudioPlayerState(
            isPlaying: self.isPlaying,
            position: position,
            time: time,
            duration: self.duration,
            repeatMode: self.repeatMode,
            isShuffleModeOn: self.isShuffleModeOn
        )
    }
}
