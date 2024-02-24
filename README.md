<img alt="Icon" src="https://github.com/andremion/RetroBeat/blob/10eeb0bf743224c36f43d753af785d0b89eea893/androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png" width=100>

# RetroBeat

Android|iOS
-|-
![retrobeat_android-ezgif com-video-to-gif-converter](https://github.com/andremion/RetroBeat/assets/12762356/f163378b-38b9-45c6-ab47-8095f5780a32)|![retrobeat_ios-ezgif com-video-to-gif-converter](https://github.com/andremion/RetroBeat/assets/12762356/ddc6c154-16d3-4932-8ccd-1a6f5e518972)

## Data source

Currently fetching playlists from [Deezer API](https://developers.deezer.com/api/explorer) but flexible enough to change to another one.

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
