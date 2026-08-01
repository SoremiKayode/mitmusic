# CodeIgniteMITMusic

CodeIgniteMITMusic is a professional MIT App Inventor extension template that automatically builds a polished, two-screen music-player interface inspired by modern commercial players.

The extension now includes in-memory playlist, favorites, queue, and recently-played behavior plus MediaStore-based music scanning blocks. Playback and platform sharing are still exposed as events so apps can connect them to their preferred player/share logic.

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

Use `RequestAudioPermission` or `RequestStoragePermission`, then call `LoadAllMusic` to scan the device MediaStore for readable audio files and populate the library. Interaction blocks continue to dispatch events for App Inventor students to handle in blocks.
