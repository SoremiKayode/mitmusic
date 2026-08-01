# CodeIgniteMITMusic

CodeIgniteMITMusic is a professional MIT App Inventor extension template that automatically builds a polished, two-screen music-player interface inspired by modern commercial players.

The extension intentionally does **not** implement playback, playlist persistence, storage scanning, permissions, or sharing backends. It supplies a complete GUI and exposes blocks/events so students can implement the business logic themselves.

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

All interaction blocks dispatch events for App Inventor students to handle in blocks.
