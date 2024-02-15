import shared
import AVKit

class AudioPlayerImpl: AbstractAudioPlayer {
    
    //private var session = AVAudioSession.sharedInstance()

    private var player: AVPlayer? = nil
    
    override func initialize(onInitialized: @escaping () -> Void) {
        onInitialized()
    }
    
    override func setTracks(tracks: [AudioPlayerTrack]) {
        let currentTrack = tracks[0]
        let url = URL(string: currentTrack.uri)
        let playerItem = AVPlayerItem(url: url!)
        player = AVPlayer(playerItem: playerItem)
        updateTrack { track in
            AudioPlayerTrack(
                id: currentTrack.id,
                uri: currentTrack.uri,
                metadata: AudioPlayerTrack.Metadata(
                    title: currentTrack.metadata.title,
                    artist: currentTrack.metadata.artist,
                    albumTitle: currentTrack.metadata.albumTitle,
                    artworkUri: currentTrack.metadata.artworkUri
                )
            )
        }
    }

    override func play() {
        if let player = player {
            player.play()
            updateState { state in
                AudioPlayerState(
                    isPlaying: true,
                    position: state.position,
                    time: state.time,
                    duration: state.duration,
                    repeatMode: state.repeatMode,
                    isShuffleModeOn: state.isShuffleModeOn
                )
            }
        }
    }

    override func updateProgress() {
        if let player = player {
            updateState { state in
                AudioPlayerState(
                    isPlaying: true,
                    position: 0.2,
                    time: player.currentTime().seconds.description,
                    duration: player.currentItem?.duration.seconds.description ?? "duration",
                    repeatMode: state.repeatMode,
                    isShuffleModeOn: state.isShuffleModeOn
                )
            }
        }
    }

    override func pause() {
        if let player = player {
            player.pause()
            updateState { state in
                AudioPlayerState(
                    isPlaying: false,
                    position: state.position,
                    time: state.time,
                    duration: state.duration,
                    repeatMode: state.repeatMode,
                    isShuffleModeOn: state.isShuffleModeOn
                )
            }
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
