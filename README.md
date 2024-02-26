<img alt="RetroBeat icon" src="https://github.com/andremion/RetroBeat/blob/10eeb0bf743224c36f43d753af785d0b89eea893/androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png" width=100 align=left>

# RetroBeat

A music player app with the vibe of good old vinyl players.

</br>

<a href=https://medium.com/@andremion/music-player-3a85864d6df7 target=_blank><img align=left width="480" alt="Music Player: From UI proposal to code" src="https://github.com/andremion/RetroBeat/assets/12762356/14b3cd4a-ba02-4eab-b983-0cd3d68cb928"></a>

In 2016, I published an [article](https://medium.com/@andremion/music-player-3a85864d6df7) about how to code a design proposal.

I noticed that a lot of people had difficulty building motion designs.

**RetroBeat** is an updated version of that proposal that uses modern tools and frameworks and now also targets iOS. 🚀

</br>

<div align=center>

https://github.com/andremion/RetroBeat/assets/12762356/91988a95-48cc-438a-a261-b261ff853afd
    
</div>

## Music source

Currently fetching playlists from [Deezer API](https://developers.deezer.com/api/explorer) where those songs are previews of 30 seconds.

The solution is flexible enough to support another source.

## Dependencies

- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform): A declarative framework based on Jetpack Compose and developed by JetBrains and open-source contributors for sharing UIs across multiple platforms with Kotlin.
- [Exoplayer](https://github.com/google/ExoPlayer): An extensible media player for Android.
- [PreCompose](https://github.com/Tlaster/PreCompose): Supports navigation and view models providing similar APIs to Jetpack ones.
- [Compottie](https://github.com/alexzhirkevich/compottie): A port of [Lottie Compose](https://github.com/airbnb/lottie/blob/master/android-compose.md).
- [Koin](https://github.com/InsertKoinIO/koin): A pragmatic lightweight dependency injection framework.
- [Ktor Client](https://github.com/ktorio/ktor): A library for fetching data from the internet and written in Kotlin from the ground up.

## TODO

- [ ] Cache the selected playlist
- [ ] Move hardcoded strings to resources
- [ ] Navigate directly to the selected playlist from the launcher
- [ ] Add integration tests
- [ ] Add better transition from the playlist to the player screen
- [ ] Handle loading errors

## References

- [Quick guide to Animations in Compose](https://developer.android.com/jetpack/compose/animation/quick-guide)
- [Introducing LookaheadLayout](https://newsletter.jorgecastillo.dev/p/introducing-lookaheadlayout)
- [LookaheadScope](https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/LookaheadScope)
- [Compose Animation Core](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/animation/animation-core/src/commonMain/kotlin/androidx/compose/animation/core/;bpv=0)
- [onPlaced Modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/package-summary#(androidx.compose.ui.Modifier).onPlaced(kotlin.Function1))
- [Create a basic media player app using Media3 ExoPlayer](https://developer.android.com/media/implement/playback-app)
- [Background playback with a MediaSessionService](https://developer.android.com/media/media3/session/background-playback)
- [Media controls](https://developer.android.com/media/implement/surfaces/mobile)

## License

    Copyright 2024 André Luiz Oliveira Rêgo
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
           http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
