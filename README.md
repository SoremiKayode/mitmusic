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

You can connect these events to your preferred player component, such as MIT App Inventor's Player component:

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
- `SeekChanged(position)`: seek your player to the selected position if your player supports seeking. If your audio player has `SeekTo` / `SetPosition`, use this block pattern: `when CodeIgniteMITMusic.SeekChanged position` → `call YourPlayer.SeekTo(position)` (or `call YourPlayer.SetPosition(position)`).
- `ControlButtonClicked(name)`: generic event for any control icon that was tapped.

If your App Inventor player component cannot seek, the extension also includes optional built-in MediaPlayer blocks. They are listed alphabetically below and include descriptions in the Blocks editor:

- `CurrentDuration`: gets the current song duration in milliseconds.
- `CurrentPosition`: gets the current playback position in milliseconds.
- `NextMusic`: raises `NextClicked` and plays the next song.
- `PauseMusic`: pauses built-in playback.
- `PlayMusic`: starts or resumes the selected song.
- `PlayNextSong`: plays the next song, honoring shuffle mode.
- `PlayPreviousSong`: plays the previous song.
- `PlaySongAtIndex(index)`: plays a song using a 1-based library index; invalid indexes are ignored.
- `PreviousMusic`: raises `PreviousClicked` and plays the previous song.
- `RepeatMusic`: toggles repeat mode.
- `ResumeMusic`: resumes paused playback.
- `SeekTo(position)`: seeks to a millisecond position.
- `SetPosition(position)`: alias for `SeekTo(position)`.
- `ShuffleMusic`: toggles shuffle mode.
- `StopMusic`: stops and releases built-in playback.

The ambiguous `PlayIndex` block and duplicate lowercase `playNextSong` block are no longer exposed. Use `PlaySongAtIndex` and `PlayNextSong`, respectively. You can either keep using events with your external player or let these built-in blocks own playback and seeking.

Update the Now Playing progress with:

- `SetCurrentPosition(position)`
- `SetDuration(duration)`
- `UpdateSeekbar(position, duration)`
- `SetCurrentTime(text)`
- `SetDurationTime(text)`

Important MIT App Inventor Player note: the built-in Player component does not expose reliable `CurrentPosition` and `Duration` getter blocks in all App Inventor distributions. If your Player drawer does not contain those blocks, do not use `Player1.CurrentPosition` or `Player1.Duration`. Instead, keep your own progress variables with a Clock:

1. Create global variables:
   - `currentPositionMs` = `0`
   - `durationMs` = `0`
   - `isPlaying` = `false`
2. When `MusicClicked` fires, use the `duration` text supplied by the extension, convert it to milliseconds with the `durationTextToMillis` procedure below, set `currentPositionMs` to `0`, start `Player1`, set `isPlaying` to `true`, and enable the Clock.
3. On every Clock timer tick, add the Clock interval to `currentPositionMs` while `isPlaying` is true, then call `UpdateSeekbar(currentPositionMs, durationMs)`, `SetCurrentTime(formatMillis(currentPositionMs))`, and `SetDurationTime(formatMillis(durationMs))`.
4. When `SeekChanged(position)` fires, set `currentPositionMs` to `position`. If your player component has a seek block, seek the player too; if not, only the UI seek bar can be updated.
5. When pause/stop/completed happens, set `isPlaying` to `false` and disable or stop incrementing the Clock.

Example `formatMillis` procedure for App Inventor blocks:

```text
to formatMillis(ms)
    set totalSeconds to floor(ms / 1000)
    set minutes to floor(totalSeconds / 60)
    set seconds to totalSeconds mod 60

    if seconds < 10
        return join minutes ":0" seconds
    else
        return join minutes ":" seconds
```

Example `durationTextToMillis` procedure for the `duration` text received from `MusicClicked`, such as `3:45`:

```text
to durationTextToMillis(durationText)
    set parts to split durationText at ":"
    if length of list parts = 2
        set minutes to number item 1 of parts
        set seconds to number item 2 of parts
        return ((minutes * 60) + seconds) * 1000
    else
        return 0
```

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
8. Clock timer while playing → if your player component exposes position/duration getters, call `UpdateSeekbar`, `SetCurrentTime`, and `SetDurationTime` from those values. If you use the built-in Player and those getters are unavailable, update `currentPositionMs` yourself from the Clock interval and use the `formatMillis` procedure above.

## Notes and limitations

- The extension builds the music-player interface and dispatches events. It intentionally does not own audio playback.
- Device music scanning depends on Android storage/audio permission and the files indexed by Android MediaStore.
- In-memory lists are reset when the app process restarts unless your app saves them externally.
- Icon setter blocks now load image assets from packaged app assets or absolute file paths. Music scanned from MediaStore is returned as a content URI so App Inventor Player blocks can open it more reliably on modern Android versions.

## Detailed block reference

Use this section as a quick explanation of what each visible App Inventor block does, how to connect it, and what parameter type it expects.

### Layout and screen blocks

| Block | Parameters | What it does | How to use it |
| --- | --- | --- | --- |
| `AddToArrangement(arrangement)` | `arrangement`: a Vertical/Horizontal Arrangement component | Moves the generated music UI into the Arrangement you provide. | Call once from `Screen.Initialize` if you want the player interface inside a specific container instead of the extension's default position. |
| `OpenLibraryScreen()` | None | Shows the Library screen and hides Now Playing. | Use from a Back button, bottom navigation logic, or after clearing Now Playing. |
| `OpenNowPlayingScreen()` | None | Shows the Now Playing screen and hides the Library. | Call after `MusicClicked` when you start playback. |
| `NavigateTo(screen)` | `screen`: text, usually `Library` or `NowPlaying` | Switches screens by name. Any value containing `now` opens Now Playing; other values open Library. | Use when you store the destination in a variable. |
| `GoBack()` | None | Returns to the Library screen. | Connect to a custom back button. |

### Music loading and database blocks

| Block | Parameters | What it does | How to use it |
| --- | --- | --- | --- |
| `RequestAudioPermission()` | None | Requests the correct Android audio permission for the device version. | Call before scanning music; then call `LoadAllMusic` from `PermissionGranted`. |
| `RequestStoragePermission()` | None | Same as `RequestAudioPermission`; provided for older app naming. | Use if your blocks already refer to storage permission. |
| `CheckPermission(permission)` | `permission`: text, or empty text for the default audio permission | Returns true if the app already has that permission. | Use before scanning to avoid showing a permission dialog unnecessarily. |
| `FirstLoadMusic()` | None | Loads all music if the internal list is empty; otherwise redraws the current list. | Good for `Screen.Initialize` after permission is granted. |
| `UpdateMusicDatabase()` | None | Re-scans all music through MediaStore. | Connect to the floating update button or a manual refresh button. |
| `LoadAllMusic()` | None | Scans Android MediaStore for every readable music/audio file and displays the results. | Requires audio/storage permission first. |
| `LoadMusicFromFolder(folder)` | `folder`: text folder path or folder name | Scans MediaStore and keeps songs whose file path contains the supplied text. | Pass values such as `/storage/emulated/0/Music` or `Downloads`. |
| `LoadMusicFromPath(path)` | `path`: text file path | Displays one song path. Uses known metadata if that path was already scanned. | Use when another picker or file component gives you a single audio path. |
| `LoadMusicIntoList(json)` | `json`: text JSON array of song objects | Replaces the visible list with your supplied song data. | Use TinyDB, Web, or custom data. Each object can include `path`, `title`, `artist`, `duration`, `album`, and `albumArt`. |
| `DisplayMusicList()` | None | Redraws the current in-memory song list. | Call after changing UI colors or after returning to the Library. |
| `RefreshMusicList()` | None | Same redraw behavior as `DisplayMusicList`. | Use as a clearer name for refresh buttons. |
| `ClearMusicList()` | None | Clears only the currently displayed song list. | Use when showing an empty state or before loading a replacement list. |
| `GetStoredMusic()` | None | Returns the current song list as JSON text. | Store this in TinyDB if you want to restore the displayed list later. |
| `ClearDatabase()` | None | Clears songs, catalog metadata, playlists, favorites, recently played, and queue. | Use for a reset button; save anything important before calling it. |
| `DatabaseExists()` | None | Returns true when the internal song list has at least one item. | Use to decide whether to scan or simply display existing data. |

### Song metadata getter blocks

All index-based getter blocks use a 1-based `index` number, matching App Inventor list numbering. If the index is out of range, they return empty text.

| Block | Parameters | Returns |
| --- | --- | --- |
| `GetMusicCount()` | None | Number of songs currently in the main list. |
| `GetMusicAtIndex(index)` | `index`: number | One song object as JSON text. |
| `GetPath(index)` | `index`: number | Song file path. |
| `GetTitle(index)` | `index`: number | Song title. |
| `GetArtist(index)` | `index`: number | Artist name. |
| `GetDuration(index)` | `index`: number | Duration text, for example `3:45`. |
| `GetAlbum(index)` | `index`: number | Album name. |
| `GetAlbumArt(index)` | `index`: number | Album art path/text if supplied in JSON. |

### Search blocks

| Block | Parameters | What it does | How to use it |
| --- | --- | --- | --- |
| `ShowSearchBar()` / `HideSearchBar()` | None | Shows or hides the search text box. | Use with custom search buttons or menu items. |
| `SetSearchHint(hint)` | `hint`: text | Changes the placeholder text in the search box. | Example: `Search by song or artist`. |
| `ClearSearch()` | None | Clears the search box text. | Call when closing search or resetting filters. |
| `SearchMusic(query)` | `query`: text | Searches title, artist, and album in the current list, updates dropdown results, and raises `SearchCompleted`. | Call manually if you have your own search input. |
| `DisplaySearchResult(json)` | `json`: song JSON array text | Updates and shows the dropdown with supplied results. | Use when results come from your own data source. |
| `UpdateDropdown(json)` | `json`: song JSON array text | Rebuilds the dropdown list from JSON and shows it. | Use to provide custom dropdown rows. |
| `ShowDropdown()` / `HideDropdown()` | None | Shows or hides the dropdown area. | Use to control result visibility. |
| `HideSearchResult()` | None | Hides the dropdown result area. | Use after a result is selected. |
| `ClearSearchResult()` | None | Clears stored search results and removes dropdown rows. | Use before a new custom search. |

### Playback and Now Playing blocks

The extension does not play audio directly. These blocks update the UI or raise events so your App Inventor Player, TaifunPlayer, or other audio component can do the actual playback.

| Block | Parameters | What it does | How to use it |
| --- | --- | --- | --- |
| `PlayMusic()` / `ResumeMusic()` | None | Raises the `PlayClicked` event. This block does not start audio by itself. | In `PlayClicked`, call your actual audio player start/resume block and set your `isPlaying` variable to `true`. |
| `PauseMusic()` | None | Raises the `PauseClicked` event. This block does not pause audio by itself. | In `PauseClicked`, call your actual audio player pause block and set `isPlaying` to `false` so the Clock stops advancing progress. |
| `StopMusic()` | None | Raises `ControlButtonClicked("Stop")`. | In `ControlButtonClicked`, if `name` is `Stop`, stop your player, set `isPlaying` to `false`, reset `currentPositionMs` to `0`, and call `SetCurrentPosition(0)`. |
| `NextMusic()` / `PreviousMusic()` | None | Raises the `NextClicked` or `PreviousClicked` event and also advances the optional built-in MediaPlayer when you are using internal playback. | With an external player, keep a `CurrentIndex` variable. For next, add 1 and wrap to 1 after `GetMusicCount`; for previous, subtract 1 and wrap to `GetMusicCount`. Then use `GetPath(CurrentIndex)` as the new player source. |
| `PlayIndex(index)` / `PlaySongAtIndex(index)` | `index`: 1-based song index | Starts the optional built-in MediaPlayer for that library item, updates Now Playing metadata, resets progress, opens Now Playing, and adds the path to Recently Played. | Use this instead of `Player1.Source`/`Player1.Start` if your project needs this extension to own audio playback. |
| `PlayNextSong()` / `playNextSong()` | None | Selects and plays the next song with the optional built-in MediaPlayer, wrapping to the first song. The lowercase alias exists so App Inventor users can create/call a block named exactly `playNextSong`. | Connect `NextClicked` and player completion logic to this block when you want extension-managed next-track behavior. |
| `PlayPreviousSong()` | None | Selects and plays the previous song with the optional built-in MediaPlayer, wrapping to the last song. | Connect `PreviousClicked` to this block when using extension-managed playback. |
| `ShuffleMusic()` / `RepeatMusic()` | None | Toggles internal shuffle/repeat for the optional built-in MediaPlayer and raises the corresponding events. | With an external player, toggle your own Boolean variables, such as `shuffleEnabled` and `repeatEnabled`, and use them in `Player.Completed`, `NextClicked`, and `PreviousClicked`. |
| `SetSongTitle(title)` | `title`: text. Use the `songName` value from `MusicClicked` or `GetTitle(index)`. | Updates only the Now Playing song title label. It does not change the song file being played. | Call immediately after setting your player source so the Now Playing screen matches the audio. |
| `SetArtist(artist)` | `artist`: text. Use the `artist` value from `MusicClicked` or `GetArtist(index)`. | Updates only the Now Playing artist label. | Call with `Unknown Artist` or blank text if your song has no artist metadata. |
| `SetAlbumArt(path)` | `path`: text asset name, `file:///android_asset/...`, or absolute image file path. | Loads the supplied image into the Now Playing album-art view if it can be found. | Package a placeholder image in your app assets and pass its name, such as `album_placeholder.png`; if a song has no art, use `SetDefaultAlbumArt` or `SetAlbumPlaceholder`. |
| `SetCurrentPosition(position)` | `position`: number. This is the current playback location. Use milliseconds if your duration is also milliseconds. If App Inventor shows this socket as `p`, `p` means `position`. | Sets only the seek bar progress/current thumb position. It does not seek the real audio player. | With the built-in Player, set `position` from your own `currentPositionMs` Clock variable. If your player has a real current-position getter, pass that value instead. |
| `SetDuration(duration)` | `duration`: number. This is the total song length. Use the same unit as `position`. If App Inventor shows this socket as `d`, `d` means `duration`. | Sets only the seek bar maximum/end value. It does not change the audio file duration. | If your player exposes a duration getter, pass it. Otherwise convert the `duration` text from `MusicClicked`, for example `3:45`, with `durationTextToMillis(duration)`. |
| `UpdateSeekbar(position, duration)` | `position`: number current playback location; `duration`: number total song length. In some block views these may appear as short names like `p` and `d`; `p` = progress/position and `d` = duration. Both values must use the same unit, preferably milliseconds. | Sets both the seek bar maximum and current progress at the same time. It only updates the extension UI. It does not read from or control `Player1`. | On each Clock tick, call `UpdateSeekbar(currentPositionMs, durationMs)`. If using another player extension with getters, call `UpdateSeekbar(realCurrentPosition, realDuration)`. If the bar jumps or fills instantly, your `position` and `duration` are probably using different units. |
| `SeekTo(position)` / `SetPosition(position)` | `position`: number in milliseconds | Seeks the optional built-in MediaPlayer and updates the visual seek bar/current time. | In `SeekChanged(position)`, call `YourPlayer.SeekTo(position)` for an external player that supports seeking, or call this extension's `SeekTo(position)` when using built-in playback. |
| `CurrentPosition()` / `CurrentDuration()` | None | Returns the optional built-in MediaPlayer's current position or duration in milliseconds. | Use these from a Clock when the extension owns playback and you want exact progress rather than estimated progress. |
| `SetCurrentTime(text)` | `text`: formatted display text for the elapsed time, such as `0:07`, `1:23`, or `12:05`. | Updates the left time label next to the seek bar. It expects text, not milliseconds. | Pass `formatMillis(currentPositionMs)`, not the raw number, unless you want the label to show an unformatted number. |
| `SetDurationTime(text)` | `text`: formatted display text for the total song length, such as `3:45`. | Updates the right time label next to the seek bar. It expects text, not milliseconds. | Pass the original `duration` text from `MusicClicked`, or pass `formatMillis(durationMs)` after converting duration to milliseconds. |

Parameter troubleshooting for the Now Playing blocks:

- If a block socket is named `p`, treat it as **position/progress**: the current place in the song.
- If a block socket is named `d`, treat it as **duration**: the total length of the song.
- `position` and `duration` must use the same units. Recommended: milliseconds. Example: a 3 minute 45 second song is `225000` milliseconds.
- `SetCurrentTime` and `SetDurationTime` need already-formatted text. Use `formatMillis(225000)` to display `3:45`.
- These UI blocks do not fix missing Player features. If the built-in Player cannot report position/duration or seek, maintain progress with Clock variables or use a player extension that provides current-position, duration, and seek blocks.

### Playlist, favorites, recently played, and queue blocks

These blocks keep data in memory only. Save important lists in TinyDB if you need them after the app closes.

| Block | Parameters | What it does |
| --- | --- | --- |
| `CreatePlaylist(name)` | `name`: text | Creates an empty playlist if it does not already exist. |
| `DeletePlaylist(name)` | `name`: text | Removes a playlist from memory. |
| `RenamePlaylist(oldName, newName)` | both text | Renames a playlist and keeps its song paths. |
| `AddSongToPlaylist(playlist, path)` | playlist name text, song path text | Adds a path to the named playlist. |
| `RemoveSongFromPlaylist(playlist, path)` | playlist name text, song path text | Removes a path from the named playlist. |
| `LoadPlaylist(name)` | `name`: text | Displays songs from that playlist and raises `PlaylistSelected`. |
| `DisplayPlaylist(json)` | `json`: song JSON array text | Displays a playlist that your app supplies as JSON. |
| `AddToFavorites(path)` / `RemoveFromFavorites(path)` | `path`: text | Adds or removes a song path in the favorites set. |
| `LoadFavorites()` | None | Displays favorite songs. |
| `DisplayFavorites(json)` | `json`: song JSON array text | Displays favorites supplied by your app. |
| `AddRecentlyPlayed(path)` | `path`: text | Moves a path to the top of the recently played list. |
| `LoadRecentlyPlayed()` | None | Displays the recently played list. |
| `ClearRecentlyPlayed()` | None | Clears the recently played list. |
| `AddToQueue(path)` / `RemoveFromQueue(path)` | `path`: text | Adds or removes a song path in the queue. |
| `MoveQueueItem(from, to)` | `from`: number, `to`: number | Moves a queue item using 1-based positions. |
| `LoadQueue()` | None | Displays queued songs. |
| `ClearQueue()` | None | Clears the queue. |

### Sharing and action icon blocks

| Block | Parameters | What it does | How to use it |
| --- | --- | --- | --- |
| `ShareMusic(path)` | `path`: text | Stores the path as the current share target and raises `ShareClicked`. | Handle `ShareClicked` with Sharing or Activity Starter blocks. |
| `ShareToWhatsApp(path)`, `ShareToFacebook(path)`, `ShareToInstagram(path)`, `ShareToTelegram(path)`, `ShareToMessenger(path)`, `ShareToSystem(path)` | `path`: text | Convenience share blocks that raise `ShareClicked(path)`. | Use the event to implement platform-specific sharing in your app. |
| `SetFABColor(color)` | `color`: App Inventor color number | Changes the floating update button color. | Use with App Inventor color blocks. |
| `ShowEmptyState()` / `HideEmptyState()` | None | Shows or hides the no-songs message. | Use while loading or after clearing the list. |
| `SetEmptyStateText(text)` | `text`: text | Changes the no-songs message. | Example: `No music found in Downloads`. |

### Theme, color, text, and placeholder customization blocks

| Block | Parameters | What it does |
| --- | --- | --- |
| `DarkTheme()` / `LightTheme()` | None | Applies built-in dark or light colors. |
| `SetPrimaryColor(color)` | color number | Changes the app bar and bottom navigation color. |
| `SetBackgroundColor(color)` / `SetBackground(color)` | color number | Changes the main background color. |
| `SetCardColor(color)` | color number | Changes song row/card surfaces. |
| `SetTextColor(color)` | color number | Changes primary text color. |
| `SetButtonColor(color)` | color number | Stores the requested button color for app-level styling. |
| `SetAccentColor(color)` | color number | Changes accent elements such as the logo and floating button. |
| `EnableAnimation()` / `DisableAnimation()` | None | Stores whether animations should be enabled. |
| `SetAnimationSpeed(ms)` | `ms`: number | Stores the preferred animation duration in milliseconds. |
| `SetLogo(value)` | `value`: text | Changes the logo text; you can pass an emoji or short label. |
| `SetAppTitle(value)` | `value`: text | Changes the title in the app bar. |
| `SetTitleColor(color)` | color number | Changes only the app title color. |
| `SetElevation(e)` | `e`: number | Changes the app bar elevation/shadow. |
| `Hint` property | text | Gets or sets the search bar placeholder text. |
| `HintColor` property | color number | Changes the search hint color. |
| `TextColor` property | color number | Changes the extension text color. |
| `Radius` property | number | Changes the search bar corner radius in density-independent pixels. |

The icon/image setter blocks such as `SetPlayIcon(path)`, `SetPauseIcon(path)`, `SetNextIcon(path)`, `SetPreviousIcon(path)`, `SetShuffleIcon(path)`, `SetRepeatIcon(path)`, `SetFavoriteIcon(path)`, `SetPlaylistIcon(path)`, `SetSearchIcon(path)`, `SetSidebarIcon(path)`, `SetShareIcon(path)`, `SetSearchIconImage(path)`, `SetShareButtonImage(path)`, `SetPlayButton(path)`, `SetPauseButton(path)`, `SetPreviousButton(path)`, `SetNextButton(path)`, `SetShuffleButton(path)`, `SetRepeatButton(path)`, `SetFavoriteButton(path)`, `SetQueueButton(path)`, `SetShareButton(path)`, and `SetFABIcon(path)` now replace matching built-in symbols with the image found at the supplied packaged asset name or absolute file path. `SetAlbumPlaceholder(path)`, `SetDefaultAlbumArt(path)`, and `LoadAlbumArt(path)` update the Now Playing album-art image. If an image cannot be loaded, the UI falls back to the default text symbol.

### Event blocks

| Event | Parameters | When it fires and what to do |
| --- | --- | --- |
| `PermissionGranted(permission)` / `PermissionDenied(permission)` | permission text | Fires after permission requests. Start scanning from `PermissionGranted`. |
| `MusicDatabaseUpdated(count, resultList)` | number, JSON text | Fires after loading/scanning. Store `resultList` if you need persistence. |
| `MusicClicked(musicIndex, musicPath, songName, artist, duration, album)` | index number and song metadata text | Fires when a library row is tapped. Set your player source to `musicPath`. |
| `MusicSelected(musicIndex, musicPath)` | index number, path text | Secondary selection event for simpler apps. |
| `MusicLongPressed(index, path)` | index number, path text | Fires on long press; use for context menus. |
| `PlayClicked`, `PauseClicked`, `NextClicked`, `PreviousClicked`, `ShuffleClicked`, `RepeatClicked` | None | Fires when matching controls are tapped; connect them to your audio component logic. |
| `SeekChanged(position)` | number | Fires when the user drags the seek bar; seek your player to this value. |
| `ControlButtonClicked(name)` | text | Fires for any control icon; useful for one generic handler. |
| `SearchTextChanged(searchText)` | text | Fires as the built-in search field changes and automatically runs search. |
| `SearchCompleted(resultList)` | JSON text | Fires after `SearchMusic`. |
| `SearchItemClicked(musicPath, songName)` | path text, title text | Fires when a dropdown result is tapped. |
| `ShareIconClicked(path)`, `ShareClicked(path)` | path text | Fires from row or control share actions; run your sharing blocks here. |
| `PlaylistIconClicked(path)` / `FavoriteClicked(path)` | path text | Fires from row action icons. |
| `PlaylistOpened`, `PlaylistClosed`, `DrawerOpened`, `DrawerClosed` | None | Fires when drawers are opened or closed. |
| `SidebarItemClicked(item)` | text | Fires when a drawer/menu item is tapped. |
| `PlaylistCreated(name)`, `PlaylistDeleted(name)`, `PlaylistRenamed(oldName, newName)`, `PlaylistSelected(name)` | playlist names | Fires after playlist operations. |
| `SongAdded(playlist, path)`, `SongRemoved(playlist, path)` | playlist text, path text | Fires after changing playlist contents. |
| `FavoriteAdded(path)`, `FavoriteRemoved(path)` | path text | Fires after favorite changes. |
| `QueueUpdated()` | None | Fires after queue changes. |
| `ScreenChanged(screen)` | text | Fires after screen navigation. |
| `FABClicked()` | None | Fires when the floating update button is tapped, before it scans music. |
