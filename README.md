# CodeIgniteMITMusic

CodeIgniteMITMusic is a professional MIT App Inventor extension template that automatically builds a polished, two-screen music-player interface inspired by modern commercial players.

The extension includes in-memory playlist, favorites, queue, and recently-played behavior plus MediaStore-based music scanning blocks. Playback and platform sharing are exposed as events so apps can connect them to their preferred player/share logic.

## Included UI

- Music Library screen with top app bar, search bar, scrollable song list, floating update button, navigation drawer, playlist drawer, bottom navigation, dropdown search results, and empty state.
- Now Playing screen with album art, song title, artist, seek bar, time labels, playback controls, favorite, share, playlist/queue, and back-style controls.
- Dark and light theme blocks plus color customization blocks.

## Data format

`LoadMusicIntoList` accepts a JSON array of song objects:

```json
[
  {
    "path": "/storage/emulated/0/Music/song.mp3",
    "title": "Song Name",
    "artist": "Artist",
    "duration": "3:45",
    "album": "Album",
    "albumArt": "album.png"
  }
]
```

## How to use the extension in MIT App Inventor

### 1. Import and place the extension

1. Build or download the extension `.aix` file.
2. Open your MIT App Inventor project.
3. In the **Palette**, open **Extension**.
4. Click **Import extension** and upload the `.aix` file.
5. Drag **CodeIgniteMITMusic** into the Viewer. The extension is a visible component, so place it where you want the music interface to appear.
6. For best results, put the extension inside a full-width/full-height Vertical Arrangement or Screen area. If needed, call `AddToArrangement` with your Arrangement component to move the generated interface into that container.

### 2. Request audio permission before scanning music

Call `RequestAudioPermission` or `RequestStoragePermission` before calling `LoadAllMusic`.

Recommended startup flow:

1. In `Screen.Initialize`, call `CodeIgniteMITMusic.RequestAudioPermission`.
2. In the `PermissionGranted` event, call `CodeIgniteMITMusic.LoadAllMusic` when the granted permission is the audio/storage permission.
3. In the `PermissionDenied` event, show a message explaining that the app needs permission to scan local music.

Permission behavior:

- Android 13 and newer use `android.permission.READ_MEDIA_AUDIO`.
- Older Android versions use `android.permission.READ_EXTERNAL_STORAGE`.
- You can call `CheckPermission` to test whether permission is already granted. Pass an empty string to check the permission required for the current Android version.

### 3. Load music into the library

Use one of these blocks depending on your app design:

- `LoadAllMusic`: scans the device MediaStore for all readable music/audio files.
- `LoadMusicFromFolder(folder)`: scans MediaStore and only displays songs whose file path contains the supplied folder path or folder name.
- `LoadMusicFromPath(path)`: displays a single path. If the path is already in the catalog, its known metadata is used; otherwise the title is inferred from the file name.
- `LoadMusicIntoList(json)`: loads a custom JSON array into the UI without scanning the device.
- `DisplayMusicList`: redraws the current in-memory music list.
- `ClearMusicList`: clears the displayed list.
- `RefreshMusicList`: redraws the list after your app changes data.

After a scan or load, the `MusicDatabaseUpdated(count, resultList)` event fires. `count` is the number of songs and `resultList` is the JSON array of songs.

### 4. Respond when a song is selected

When the user taps a song, the extension raises:

- `MusicClicked(musicIndex, musicPath, songName, artist, duration, album)`
- `MusicSelected(musicIndex, musicPath)`

The extension does not play audio by itself. Connect these events to your preferred player component, such as MIT App Inventor's Player component:

1. In `MusicClicked`, set `Player.Source` to `musicPath`.
2. Call `Player.Start`.
3. Call `SetSongTitle(songName)` and `SetArtist(artist)` to update the Now Playing screen.
4. Call `OpenNowPlayingScreen` if you want to switch screens automatically.

### 5. Wire playback controls

The Now Playing screen dispatches button events for your blocks to handle:

- `PlayClicked`: call your player start/resume block.
- `PauseClicked`: pause playback.
- `NextClicked`: play the next song in your own playlist or queue logic.
- `PreviousClicked`: play the previous song.
- `ShuffleClicked`: enable or toggle shuffle in your app logic.
- `RepeatClicked`: enable or toggle repeat in your app logic.
- `SeekChanged(position)`: seek your player to the selected position if your player supports seeking.
- `ControlButtonClicked(name)`: generic event for any control icon that was tapped.

Update the Now Playing progress with:

- `SetCurrentPosition(position)`
- `SetDuration(duration)`
- `UpdateSeekbar(position, duration)`
- `SetCurrentTime(text)`
- `SetDurationTime(text)`

### 6. Use search

Search is built into the UI and can also be controlled from blocks:

- `ShowSearchBar` / `HideSearchBar`: show or hide the search field.
- `SearchMusic(query)`: searches title, artist, and album in the current list.
- `SearchCompleted(resultList)`: fires with matching results as JSON.
- `UpdateDropdown(json)`: displays search results in the dropdown.
- `DisplaySearchResult(json)`: updates and shows the dropdown.
- `HideSearchResult` and `ClearSearchResult`: hide or clear dropdown results.
- `SearchItemClicked(musicPath, songName)`: fires when a dropdown result is tapped.

### 7. Manage playlists, favorites, recently played, and queue

The extension stores these lists in memory while the app is running:

- Playlists: `CreatePlaylist`, `DeletePlaylist`, `RenamePlaylist`, `AddSongToPlaylist`, `RemoveSongFromPlaylist`, `LoadPlaylist`, and `DisplayPlaylist`.
- Favorites: `AddToFavorites`, `RemoveFromFavorites`, `LoadFavorites`, and `DisplayFavorites`.
- Recently played: `AddRecentlyPlayed`, `LoadRecentlyPlayed`, and `ClearRecentlyPlayed`.
- Queue: `AddToQueue`, `RemoveFromQueue`, `MoveQueueItem`, `LoadQueue`, and `ClearQueue`.

Important: playlist, favorite, recently-played, and queue data is not persisted automatically. If you want the data to survive app restarts, save your own JSON or list data with TinyDB and reload it when the screen initializes.

### 8. Handle share and action icons

The extension emits events for sharing and action buttons, but your app should perform the actual platform share:

- `ShareIconClicked(path)` fires from a song row share icon.
- `ShareClicked(path)` fires from share controls and share helper functions.
- `PlaylistIconClicked(path)` fires when the user taps the add-to-playlist icon.
- `FavoriteClicked(path)` fires when the user taps the favorite icon.

Use these events to call MIT App Inventor sharing blocks, Activity Starter, or your own custom sharing logic.

### 9. Customize appearance

Theme and color helpers:

- `DarkTheme` and `LightTheme`
- `SetPrimaryColor(color)`
- `SetBackgroundColor(color)` or `SetBackground(color)`
- `SetCardColor(color)`
- `SetAccentColor(color)`
- `SetTextColor(color)`
- `SetButtonColor(color)`
- `Hint`, `HintColor`, `TextColor`, and `Radius` for the search bar

UI text helpers:

- `SetLogo(value)` changes the logo text.
- `SetAppTitle(value)` changes the app title.
- `SetTitleColor(color)` changes the title color.
- `SetEmptyStateText(text)` changes the empty-library message.

### 10. Suggested block workflow

A simple complete app flow is:

1. `Screen.Initialize` → call `RequestAudioPermission`.
2. `PermissionGranted` → call `LoadAllMusic`.
3. `MusicDatabaseUpdated` → optionally store `resultList` in TinyDB.
4. `MusicClicked` → set your Player source to `musicPath`, start playback, update Now Playing labels, and call `OpenNowPlayingScreen`.
5. `PauseClicked` → pause your Player.
6. `PlayClicked` → start or resume your Player.
7. `NextClicked` / `PreviousClicked` → choose another path from your saved list and play it.
8. Clock timer while playing → call `UpdateSeekbar`, `SetCurrentTime`, and `SetDurationTime`.

## Notes and limitations

- The extension builds the music-player interface and dispatches events. It intentionally does not own audio playback.
- Device music scanning depends on Android storage/audio permission and the files indexed by Android MediaStore.
- In-memory lists are reset when the app process restarts unless your app saves them externally.
- Several icon/image setter blocks are placeholders for app-level customization flows and do not load image assets directly yet.
