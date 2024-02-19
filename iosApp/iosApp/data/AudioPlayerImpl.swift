import shared
import AVKit

class AudioPlayerImpl: AbstractAudioPlayer {
    
    //private var session = AVAudioSession.sharedInstance()

    private var player: AVPlayer? = nil
    private var currentItemIndex: Int = 0
    private var tracks: Array<AudioPlayerTrack> = Array()
    private var timeObserverToken: Any? = nil
    
    override func initialize(onInitialized: @escaping () -> Void) {
        onInitialized()
    }
    
    override func setTracks(tracks: [AudioPlayerTrack]) {
        self.tracks = tracks
        setCurrentItem(index: 0)
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
    
    override func play(trackIndex: Int32) {
        setCurrentItem(index: Int(trackIndex))
        play()
    }
    
    override func updateProgress() {
        Task.init {
            do {
                if let player = player {
                    if let currentItem = player.currentItem {
                        let duration = try await currentItem.asset.load(.duration)
                        updateState { state in
                            state.copy(
                                position: Float(player.currentTime().seconds / duration.seconds),
                                time: DurationKt
                                    .toDuration(milliseconds: player.currentTime().seconds.rounded() * 1_000),
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
        if (currentItemIndex > 0) {
            setCurrentItem(index: currentItemIndex - 1)
        }
    }

    override func skipToNext() {
        if (currentItemIndex < tracks.count - 1) {
            setCurrentItem(index: currentItemIndex + 1)
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
            player.replaceCurrentItem(with: playerItem)
        } else {
            player = AVPlayer(playerItem: playerItem)
        }
        
        /*Task.init {
            do {
                let duration = try await playerItem.asset.load(.duration)
                let interval = CMTimeMultiplyByFloat64(duration, multiplier: 1.0)
                NSLog("duration=\(duration), interval=\(interval)")
                timeObserverToken = player?.addBoundaryTimeObserver(
                    forTimes: [NSValue(time: interval)],
                    queue: .main
                ) { [weak self] in
                    NSLog("Here")
                    self?.skipToNext()
                }
            } catch {
                NSLog("Error on fetching player item duration")
            }
        }*/
        
        updateProgress()
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
            seekBackIncrement: self.seekBackIncrement,
            seekForwardIncrement: self.seekForwardIncrement,
            isPlaying: isPlaying,
            position: self.position,
            time: self.time,
            duration: self.duration,
            repeatMode: self.repeatMode,
            isShuffleModeOn: self.isShuffleModeOn
        )
    }

    func copy(position: Float, time: Int64, duration: Int64) -> AudioPlayerState {
        AudioPlayerState(
            seekBackIncrement: self.seekBackIncrement,
            seekForwardIncrement: self.seekForwardIncrement,
            isPlaying: self.isPlaying,
            position: position,
            time: time,
            duration: duration,
            repeatMode: self.repeatMode,
            isShuffleModeOn: self.isShuffleModeOn
        )
    }
}
