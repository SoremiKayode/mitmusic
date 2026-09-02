package chatinputbox.chatinputboxnew;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.EditText;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.PermissionResultHandler;
import com.google.appinventor.components.runtime.util.MediaUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Random;

public class CodeIgniteMITMusic extends AndroidViewComponent {
    private final ComponentContainer container;
    private final FrameLayout root;
    private final LinearLayout shell;
    private final LinearLayout appBar;
    private final TextView logoView;
    private final TextView titleView;
    private final EditText searchBar;
    private final FrameLayout contentFrame;
    private final LinearLayout libraryScreen;
    private final LinearLayout nowPlayingScreen;
    private LinearLayout songList;
    private LinearLayout dropdownList;
    private ScrollView searchDropdown;
    private final LinearLayout playlistDrawer;
    private final LinearLayout sidebarDrawer;
    private final View drawerScrim;
    private TextView emptyStateText;
    private final TextView fab;
    private ImageView albumArt;
    private TextView nowTitle;
    private TextView nowArtist;
    private SeekBar seekBar;
    private TextView currentTime;
    private TextView durationTime;
    private final LinearLayout bottomNav;

    private final ArrayList<Song> songs = new ArrayList<Song>();
    private final ArrayList<Song> searchResults = new ArrayList<Song>();
    private final HashSet<String> favorites = new HashSet<String>();
    private final ArrayList<String> recentlyPlayed = new ArrayList<String>();
    private final ArrayList<String> queue = new ArrayList<String>();
    private final LinkedHashMap<String, ArrayList<String>> playlists = new LinkedHashMap<String, ArrayList<String>>();
    private final LinkedHashMap<String, Song> catalog = new LinkedHashMap<String, Song>();
    private int primaryColor = Color.rgb(18, 18, 18);
    private int accentColor = Color.rgb(30, 215, 96);
    private int backgroundColor = Color.rgb(12, 12, 12);
    private int cardColor = Color.rgb(32, 32, 32);
    private int textColor = Color.WHITE;
    private int hintColor = Color.rgb(175, 175, 175);
    private int buttonColor = Color.WHITE;
    private int searchRadius = 24;
    private boolean darkTheme = true;
    private boolean animationsEnabled = true;
    private int animationSpeed = 180;
    private String currentScreen = "Library";
    private String sharePath = "";
    private String defaultAlbumArtPath = "";
    private MediaPlayer mediaPlayer;
    private int internalCurrentIndex = 0;
    private boolean internalShuffle = false;
    private boolean internalRepeat = false;
    private float playerVolume = 1f;
    private String pendingPlaylistPath = "";
    private final Random random = new Random();
    private final HashMap<String, ArrayList<TextView>> iconViews = new HashMap<String, ArrayList<TextView>>();
    private final HashMap<String, String> iconFallbacks = new HashMap<String, String>();
    private final HashMap<String, String> iconImagePaths = new HashMap<String, String>();
    private final HashSet<String> dispatchingEvents = new HashSet<String>();

    public CodeIgniteMITMusic(ComponentContainer container) {
        super(container);
        this.container = container;
        root = new FrameLayout(container.$context());
        initIconFallbacks();
        shell = new LinearLayout(container.$context());
        shell.setOrientation(LinearLayout.VERTICAL);

        appBar = new LinearLayout(container.$context());
        appBar.setGravity(Gravity.CENTER_VERTICAL);
        appBar.setPadding(dp(16), dp(10), dp(10), dp(8));
        logoView = label("♫", 28, true);
        titleView = label("CodeIgniteMITMusic", 20, true);
        appBar.addView(logoView, new LinearLayout.LayoutParams(dp(42), dp(42)));
        appBar.addView(titleView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        appBar.addView(icon("⌕", new View.OnClickListener(){ public void onClick(View v){ ShowSearchBar(); }}));
        appBar.addView(icon("☰", new View.OnClickListener(){ public void onClick(View v){ OpenPlaylistDrawer(); }}));
        appBar.addView(icon("⚙", new View.OnClickListener(){ public void onClick(View v){ SidebarItemClicked("Settings"); }}));

        searchBar = new EditText(container.$context());
        searchBar.setSingleLine(true);
        searchBar.setHint("Search songs, artists, albums");
        searchBar.setPadding(dp(18), 0, dp(18), 0);
        searchBar.setVisibility(View.GONE);
        searchBar.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { SearchTextChanged(s.toString()); }
            public void afterTextChanged(Editable s) {}
        });

        contentFrame = new FrameLayout(container.$context());
        libraryScreen = buildLibraryScreen();
        nowPlayingScreen = buildNowPlayingScreen();
        contentFrame.addView(libraryScreen);
        contentFrame.addView(nowPlayingScreen);
        nowPlayingScreen.setVisibility(View.GONE);

        bottomNav = new LinearLayout(container.$context());
        bottomNav.setGravity(Gravity.CENTER);
        bottomNav.addView(nav("Library"));
        bottomNav.addView(nav("Now Playing"));

        fab = label("↻", 28, true);
        registerIcon("↻",fab);
        fab.setGravity(Gravity.CENTER);
        fab.setContentDescription("Update music database");
        fab.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ FABClicked(); LoadAllMusic(); }});
        FrameLayout.LayoutParams fabLp = new FrameLayout.LayoutParams(dp(64), dp(64), Gravity.BOTTOM | Gravity.RIGHT);
        fabLp.setMargins(0,0,dp(18),dp(78));

        playlistDrawer = playlistDrawer();
        sidebarDrawer = drawer("Menu", new String[]{"Library","Playlists","Favorites","Recently Played","Settings"});
        playlistDrawer.setVisibility(View.GONE); sidebarDrawer.setVisibility(View.GONE);
        drawerScrim = new View(container.$context());
        drawerScrim.setBackgroundColor(Color.argb(150,0,0,0));
        drawerScrim.setVisibility(View.GONE);
        drawerScrim.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ if(playlistDrawer.getVisibility()==View.VISIBLE)ClosePlaylistDrawer(); if(sidebarDrawer.getVisibility()==View.VISIBLE)CloseSidebar(); }});

        shell.addView(appBar);
        shell.addView(searchBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        shell.addView(contentFrame, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        shell.addView(bottomNav, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64)));
        root.addView(shell);
        root.addView(fab, fabLp);
        root.addView(searchDropdown, dropdownParams());
        root.addView(drawerScrim, new FrameLayout.LayoutParams(-1,-1));
        root.addView(playlistDrawer, drawerParams(Gravity.RIGHT));
        root.addView(sidebarDrawer, drawerParams(Gravity.LEFT));
        applyTheme();
        Width(ViewGroup.LayoutParams.MATCH_PARENT); Height(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override public View getView() { return root; }
    @SimpleFunction(description="Adds the complete music interface to a container. Required parameter: arrangement (component): the Arrangement that will contain the interface.") public void AddToArrangement(AndroidViewComponent arrangement){ ViewGroup p=(ViewGroup)arrangement.getView(); if(root.getParent()!=null)((ViewGroup)root.getParent()).removeView(root); p.addView(root,new ViewGroup.LayoutParams(-1,-1)); }

    private LinearLayout buildLibraryScreen(){ LinearLayout page=new LinearLayout(container.$context()); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(14), dp(8), dp(14), dp(8)); dropdownList=new LinearLayout(container.$context()); dropdownList.setOrientation(LinearLayout.VERTICAL); searchDropdown=new ScrollView(container.$context()); searchDropdown.setFillViewport(false); searchDropdown.setBackground(round(cardColor,18)); searchDropdown.setElevation(dp(12)); searchDropdown.setVisibility(View.GONE); searchDropdown.addView(dropdownList); emptyStateText=label("No songs yet\nTap the update button or load a list from blocks.",16,false); emptyStateText.setGravity(Gravity.CENTER); ScrollView scroll=new ScrollView(container.$context()); songList=new LinearLayout(container.$context()); songList.setOrientation(LinearLayout.VERTICAL); scroll.addView(songList); page.addView(emptyStateText,new LinearLayout.LayoutParams(-1,dp(140))); page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); return page; }
    private LinearLayout buildNowPlayingScreen(){ LinearLayout p=new LinearLayout(container.$context()); p.setOrientation(LinearLayout.VERTICAL); p.setGravity(Gravity.CENTER_HORIZONTAL); p.setPadding(dp(24),dp(18),dp(24),dp(18)); albumArt=new ImageView(container.$context()); albumArt.setBackground(round(cardColor,28)); nowTitle=titleLabel("Song Title",24); nowArtist=label("Artist",16,false); seekBar=new SeekBar(container.$context()); seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ public void onProgressChanged(SeekBar b,int progress,boolean fromUser){ if(fromUser) SeekChanged(progress);} public void onStartTrackingTouch(SeekBar b){} public void onStopTrackingTouch(SeekBar b){} }); LinearLayout times=new LinearLayout(container.$context()); times.setGravity(Gravity.CENTER); currentTime=label("0:00",12,false); durationTime=label("0:00",12,false); times.addView(currentTime,new LinearLayout.LayoutParams(0,-2,1)); times.addView(durationTime); p.addView(albumArt,new LinearLayout.LayoutParams(dp(280),dp(280))); p.addView(nowTitle); p.addView(nowArtist); p.addView(seekBar,new LinearLayout.LayoutParams(-1,-2)); p.addView(times,new LinearLayout.LayoutParams(-1,-2)); p.addView(controlRow(new String[]{"↩","⇄","◀","▶","Ⅱ","▶▶","♡","↗","≡","⤴"})); return p; }
    private LinearLayout controlRow(String[] names){ LinearLayout r=new LinearLayout(container.$context()); r.setGravity(Gravity.CENTER); for(final String n:names) r.addView(icon(n,new View.OnClickListener(){ public void onClick(View v){ control(n); }})); return r; }
    private void control(String n){ if(n.equals("▶")){ PlayClicked(); ResumeInternalPlayer(); } else if(n.equals("Ⅱ")){ PauseClicked(); PauseInternalPlayer(); } else if(n.equals("▶▶")){ NextClicked(); PlayNextSong(); } else if(n.equals("◀")){ PreviousClicked(); PlayPreviousSong(); } else if(n.equals("⇄")){ internalShuffle=!internalShuffle; ShuffleClicked(); } else if(n.equals("↩")){ internalRepeat=!internalRepeat; RepeatClicked(); } else if(n.equals("♡")) FavoriteClicked(sharePath); else if(n.equals("≡")) QueueClicked(); else if(n.equals("↗")||n.equals("⤴")) ShareClicked(sharePath); ControlButtonClicked(n); }

    private void renderSongs(ArrayList<Song> data){
        songList.removeAllViews();
        emptyStateText.setVisibility(data.size()==0?View.VISIBLE:View.GONE);
        for(int i=0;i<data.size();i++){
            final Song song=data.get(i); final int index=i+1;
            LinearLayout row=new LinearLayout(container.$context()); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(12),dp(10),dp(8),dp(10)); row.setBackground(round(cardColor,18));
            TextView art=label("♫",26,true); art.setGravity(Gravity.CENTER);
            LinearLayout details=new LinearLayout(container.$context()); details.setOrientation(LinearLayout.VERTICAL);
            TextView title=titleLabel(song.title,15); details.addView(title,new LinearLayout.LayoutParams(-1,-2));
            String secondary=joinMetadata(song.artist,song.duration);
            if(secondary.length()>0){ TextView meta=label(secondary,13,false); meta.setTextColor(hintColor); details.addView(meta); }
            row.addView(art,new LinearLayout.LayoutParams(dp(54),dp(54))); row.addView(details,new LinearLayout.LayoutParams(0,-2,1));
            row.addView(icon("↗",new View.OnClickListener(){ public void onClick(View v){ ShareIconClicked(song.path); }}));
            row.addView(icon("＋",new View.OnClickListener(){ public void onClick(View v){ PlaylistIconClicked(song.path); ShowPlaylistPopup(song.path); }}));
            row.addView(icon("♡",new View.OnClickListener(){ public void onClick(View v){ FavoriteClicked(song.path); }}));
            row.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ MusicClicked(index,song.path,song.title,song.artist,song.duration,song.album); }});
            row.setOnLongClickListener(new View.OnLongClickListener(){ public boolean onLongClick(View v){ MusicLongPressed(index,song.path); return true; }});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,dp(12)); songList.addView(row,lp);
        }
    }

    @SimpleFunction(description="Loads and displays a custom song list. Required parameter: json (text): a JSON array of objects with path, title, artist, duration, album, and albumArt fields.") public void LoadMusicIntoList(String json){ songs.clear(); try{ JSONArray a=new JSONArray(json); for(int i=0;i<a.length();i++){ JSONObject o=a.optJSONObject(i); if(o!=null){ Song song=new Song(o.optString("path"),cleanTitle(o.optString("title"),o.optString("path")),cleanMetadata(o.optString("artist")),o.optString("duration","0:00"),o.optString("album",""),o.optString("albumArt","")); songs.add(song); catalog.put(song.path,song); } }}catch(Exception e){} renderSongs(songs); }
    @SimpleFunction(description="Displays music list.") public void DisplayMusicList(){ renderSongs(songs); } @SimpleFunction(description="Clears music list.") public void ClearMusicList(){ songs.clear(); renderSongs(songs); } @SimpleFunction(description="Refreshes music list.") public void RefreshMusicList(){ renderSongs(songs); }
    @SimpleFunction(description="Searches music. Required parameter: query (text): Text to match against song titles, artists, and albums.") public void SearchMusic(String query){ searchResults.clear(); String q=query.toLowerCase(); for(Song s:songs) if(s.title.toLowerCase().contains(q)||s.artist.toLowerCase().contains(q)||s.album.toLowerCase().contains(q)) searchResults.add(s); SearchCompleted(toJson(searchResults)); UpdateDropdown(toJson(searchResults)); }
    @SimpleFunction(description="Displays search result. Required parameter: json (text): JSON text containing an array of song objects.") public void DisplaySearchResult(String json){ UpdateDropdown(json); ShowDropdown(); } @SimpleFunction(description="Hides search result.") public void HideSearchResult(){ HideDropdown(); } @SimpleFunction(description="Clears search result.") public void ClearSearchResult(){ searchResults.clear(); dropdownList.removeAllViews(); }
    @SimpleFunction(description="Shows dropdown.") public void ShowDropdown(){ searchDropdown.setVisibility(dropdownList.getChildCount()>0?View.VISIBLE:View.GONE); } @SimpleFunction(description="Hides dropdown.") public void HideDropdown(){ searchDropdown.setVisibility(View.GONE); } @SimpleFunction(description="Updates dropdown. Required parameter: json (text): JSON text containing an array of song objects.") public void UpdateDropdown(String json){
        dropdownList.removeAllViews();
        try{ JSONArray a=new JSONArray(json); for(int i=0;i<a.length();i++){ JSONObject o=a.getJSONObject(i); final String path=o.optString("path"), title=cleanTitle(o.optString("title"),path), artist=cleanMetadata(o.optString("artist")); LinearLayout row=new LinearLayout(container.$context()); row.setOrientation(LinearLayout.VERTICAL); row.setPadding(dp(14),dp(10),dp(14),dp(10)); TextView heading=titleLabel(title,15); row.addView(heading); if(artist.length()>0){ TextView subtitle=label(artist,13,false); subtitle.setTextColor(hintColor); row.addView(subtitle); } row.setBackground(round(cardColor,14)); row.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ HideDropdown(); SearchItemClicked(path,title); }}); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(dp(8),dp(5),dp(8),dp(5)); dropdownList.addView(row,lp); }}catch(Exception e){} ShowDropdown(); }


    @SimpleFunction(description="Shows search bar.") public void ShowSearchBar(){ searchBar.setVisibility(View.VISIBLE); updateDropdownPosition(); searchBar.requestFocus(); } @SimpleFunction(description="Hides search bar.") public void HideSearchBar(){ searchBar.setVisibility(View.GONE); HideDropdown(); } @SimpleFunction(description="Sets search hint. Required parameter: hint (text): Text displayed when the search field is empty.") public void SetSearchHint(String hint){ searchBar.setHint(hint); } @SimpleFunction(description="Clears search.") public void ClearSearch(){ searchBar.setText(""); }
    @SimpleProperty(description="Returns the current hint property.") public String Hint(){ return searchBar.getHint().toString(); } @DesignerProperty(editorType="string", defaultValue="Search songs, artists, albums") @SimpleProperty(description="Sets the hint property. Required parameter: v (text): New property value.") public void Hint(String v){ searchBar.setHint(v); } @SimpleProperty(description="Sets the hint color property. Required parameter: c (number): App Inventor color integer.") public void HintColor(int c){ hintColor=c; applyTheme(); } @SimpleProperty(description="Sets the text color property. Required parameter: c (number): App Inventor color integer.") public void TextColor(int c){ textColor=c; applyTheme(); } @SimpleProperty(description="Sets the radius property. Required parameter: r (number): Corner radius in density-independent pixels.") public void Radius(int r){ searchRadius=r; applyTheme(); }
    @SimpleFunction(description="Shows a playlist chooser for a song. Required parameter: path (text): song path to add when a playlist is selected.")
    public void ShowPlaylistPopup(String path){ pendingPlaylistPath=path==null?"":path; showPlaylistChooser(); }

    @SimpleFunction(description="Opens playlist drawer.") public void OpenPlaylistDrawer(){ HideDropdown(); drawerScrim.setVisibility(View.VISIBLE); playlistDrawer.setVisibility(View.VISIBLE); PlaylistOpened(); } @SimpleFunction(description="Closes playlist drawer.") public void ClosePlaylistDrawer(){ playlistDrawer.setVisibility(View.GONE); if(sidebarDrawer.getVisibility()!=View.VISIBLE)drawerScrim.setVisibility(View.GONE); PlaylistClosed(); } @SimpleFunction(description="Opens sidebar.") public void OpenSidebar(){ HideDropdown(); drawerScrim.setVisibility(View.VISIBLE); sidebarDrawer.setVisibility(View.VISIBLE); DrawerOpened(); } @SimpleFunction(description="Closes sidebar.") public void CloseSidebar(){ sidebarDrawer.setVisibility(View.GONE); if(playlistDrawer.getVisibility()!=View.VISIBLE)drawerScrim.setVisibility(View.GONE); DrawerClosed(); } @SimpleFunction(description="Toggles sidebar.") public void ToggleSidebar(){ if(sidebarDrawer.getVisibility()==View.VISIBLE)CloseSidebar(); else OpenSidebar(); }
    @SimpleFunction(description="Opens library screen.") public void OpenLibraryScreen(){ NavigateTo("Library"); } @SimpleFunction(description="Opens now playing screen.") public void OpenNowPlayingScreen(){ NavigateTo("NowPlaying"); } @SimpleFunction(description="Navigates to. Required parameter: screen (text): Screen name; use Library or NowPlaying.") public void NavigateTo(String screen){ currentScreen=screen; boolean now=screen.toLowerCase().contains("now"); libraryScreen.setVisibility(now?View.GONE:View.VISIBLE); nowPlayingScreen.setVisibility(now?View.VISIBLE:View.GONE); ScreenChanged(screen); } @SimpleFunction(description="Runs the go back operation.") public void GoBack(){ OpenLibraryScreen(); }

    @SimpleFunction(description="Sets album art. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void SetAlbumArt(String path){ Drawable d=loadDrawable(path); if(d==null&&defaultAlbumArtPath.length()>0)d=loadDrawable(defaultAlbumArtPath); if(d!=null)albumArt.setImageDrawable(d); } @SimpleFunction(description="Sets song title. Required parameter: title (text): Song title text.") public void SetSongTitle(String title){ nowTitle.setText(title); nowTitle.setTranslationX(0); nowTitle.post(new Runnable(){ public void run(){ animateOverflowingTitle(nowTitle); }}); } @SimpleFunction(description="Sets artist. Required parameter: artist (text): Artist name text.") public void SetArtist(String artist){ nowArtist.setText(artist); } @SimpleFunction(description="Sets background. Required parameter: c (number): App Inventor color integer.") public void SetBackground(int c){ backgroundColor=c; applyTheme(); } @SimpleFunction(description="Sets accent color. Required parameter: c (number): App Inventor color integer.") public void SetAccentColor(int c){ accentColor=c; applyTheme(); } @SimpleFunction(description="Sets button color. Required parameter: c (number): App Inventor color integer.") public void SetButtonColor(int c){ buttonColor=c; applyTheme(); }

    @SimpleFunction(description="Sets the current seek-bar progress. Required parameter: position (number): the playback position in milliseconds.")
    public void SetCurrentPosition(int position){
        seekBar.setProgress(position);
    }

    @SimpleFunction(description="Sets the seek-bar maximum. Required parameter: duration (number): the total duration in milliseconds.")
    public void SetDuration(int duration){
        seekBar.setMax(duration);
    }

    @SimpleFunction(description="Updates the complete seek-bar state. Required parameters: position (number): playback position in milliseconds; duration (number): total duration in milliseconds.")
    public void UpdateSeekbar(int position,int duration){
        seekBar.setMax(duration);
        seekBar.setProgress(position);
    }

    @SimpleFunction(description="Sets the elapsed-time label. Required parameter: text (text): formatted elapsed time, for example 1:25.")
    public void SetCurrentTime(String text){
        currentTime.setText(text);
    }

    @SimpleFunction(description="Sets the total-time label. Required parameter: text (text): formatted total time, for example 3:45.")
    public void SetDurationTime(String text){
        durationTime.setText(text);
    }

    @SimpleFunction(description="Returns music count.") public int GetMusicCount(){ return songs.size(); } @SimpleFunction(description="Returns music at index. Required parameter: i (number): 1-based song index.") public String GetMusicAtIndex(int i){ return i>0&&i<=songs.size()?songs.get(i-1).toJson().toString():""; } @SimpleFunction(description="Returns album art. Required parameter: i (number): 1-based song index.") public String GetAlbumArt(int i){ return get(i).albumArt; } @SimpleFunction(description="Returns artist. Required parameter: i (number): 1-based song index.") public String GetArtist(int i){ return get(i).artist; } @SimpleFunction(description="Returns title. Required parameter: i (number): 1-based song index.") public String GetTitle(int i){ return get(i).title; } @SimpleFunction(description="Returns duration. Required parameter: i (number): 1-based song index.") public String GetDuration(int i){ return get(i).duration; } @SimpleFunction(description="Returns album. Required parameter: i (number): 1-based song index.") public String GetAlbum(int i){ return get(i).album; } @SimpleFunction(description="Returns path. Required parameter: i (number): 1-based song index.") public String GetPath(int i){ return get(i).path; }

    @SimpleFunction(description="Runs the dark theme operation.") public void DarkTheme(){ darkTheme=true; primaryColor=Color.rgb(18,18,18); backgroundColor=Color.rgb(12,12,12); cardColor=Color.rgb(32,32,32); textColor=Color.WHITE; applyTheme(); } @SimpleFunction(description="Runs the light theme operation.") public void LightTheme(){ darkTheme=false; primaryColor=Color.WHITE; backgroundColor=Color.rgb(246,246,246); cardColor=Color.WHITE; textColor=Color.rgb(25,25,25); applyTheme(); } @SimpleFunction(description="Sets primary color. Required parameter: c (number): App Inventor color integer.") public void SetPrimaryColor(int c){ primaryColor=c; applyTheme(); } @SimpleFunction(description="Sets background color. Required parameter: c (number): App Inventor color integer.") public void SetBackgroundColor(int c){ backgroundColor=c; applyTheme(); } @SimpleFunction(description="Sets card color. Required parameter: c (number): App Inventor color integer.") public void SetCardColor(int c){ cardColor=c; applyTheme(); } @SimpleFunction(description="Sets text color. Required parameter: c (number): App Inventor color integer.") public void SetTextColor(int c){ textColor=c; applyTheme(); }
    @SimpleFunction(description="Enables animation.") public void EnableAnimation(){ animationsEnabled=true; } @SimpleFunction(description="Disables animation.") public void DisableAnimation(){ animationsEnabled=false; } @SimpleFunction(description="Sets animation speed. Required parameter: ms (number): Animation duration in milliseconds.") public void SetAnimationSpeed(int ms){ animationSpeed=ms; }

    // Data blocks manage in-memory state and can scan device audio through Android MediaStore.
    @SimpleFunction(description="Runs the first load music operation.") public void FirstLoadMusic(){ if(songs.size()==0) LoadAllMusic(); else DisplayMusicList(); } @SimpleFunction(description="Updates music database.") public void UpdateMusicDatabase(){ LoadAllMusic(); } @SimpleFunction(description="Returns stored music.") public String GetStoredMusic(){ return toJson(songs); } @SimpleFunction(description="Clears database.") public void ClearDatabase(){ ClearMusicList(); catalog.clear(); playlists.clear(); favorites.clear(); recentlyPlayed.clear(); queue.clear(); QueueUpdated(); } @SimpleFunction(description="Runs the database exists operation.") public boolean DatabaseExists(){ return songs.size()>0; }
    @SimpleFunction(description="Scans the phone MediaStore for every music/audio file the app has permission to read, then displays the results.") public void LoadAllMusic(){ loadMusicFromMediaStore(null); }
    @SimpleFunction(description="Scans and displays MediaStore songs matching a folder. Required parameter: folder (text): the full folder path or folder name to match.") public void LoadMusicFromFolder(String folder){ loadMusicFromMediaStore(folder); }
    @SimpleFunction(description="Loads music from path. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void LoadMusicFromPath(String path){ songs.clear(); Song found=findSong(path); if(found.path.length()>0)songs.add(found); else songs.add(new Song(path,titleFromPath(path),"","0:00","","")); renderSongs(songs); MusicDatabaseUpdated(songs.size(),toJson(songs)); }
    @SimpleFunction(description="Creates playlist. Required parameter: name (text): Playlist, screen, control, or item name.") public void CreatePlaylist(String name){ if(!playlists.containsKey(name))playlists.put(name,new ArrayList<String>()); PlaylistCreated(name); } @SimpleFunction(description="Deletes playlist. Required parameter: name (text): Playlist, screen, control, or item name.") public void DeletePlaylist(String name){ playlists.remove(name); PlaylistDeleted(name); } @SimpleFunction(description="Renames playlist. Required parameters: oldName (text): Existing playlist name; newName (text): Replacement playlist name.") public void RenamePlaylist(String oldName,String newName){ ArrayList<String> items=playlists.remove(oldName); if(items==null)items=new ArrayList<String>(); playlists.put(newName,items); PlaylistRenamed(oldName,newName); } @SimpleFunction(description="Adds song to playlist. Required parameters: playlist (text): Playlist name; path (text): Song content URI, file path, or packaged asset path as appropriate.") public void AddSongToPlaylist(String playlist,String path){ if(!playlists.containsKey(playlist))playlists.put(playlist,new ArrayList<String>()); ArrayList<String> items=playlists.get(playlist); if(!items.contains(path))items.add(path); SongAdded(playlist,path); } @SimpleFunction(description="Removes song from playlist. Required parameters: playlist (text): Playlist name; path (text): Song content URI, file path, or packaged asset path as appropriate.") public void RemoveSongFromPlaylist(String playlist,String path){ if(playlists.containsKey(playlist))playlists.get(playlist).remove(path); SongRemoved(playlist,path); } @SimpleFunction(description="Loads playlist. Required parameter: name (text): Playlist, screen, control, or item name.") public void LoadPlaylist(String name){ songs.clear(); if(playlists.containsKey(name)) for(String path:playlists.get(name)) songs.add(findSong(path)); renderSongs(songs); PlaylistSelected(name); } @SimpleFunction(description="Displays playlist. Required parameter: json (text): JSON text containing an array of song objects.") public void DisplayPlaylist(String json){ LoadMusicIntoList(json); }
    // Playback blocks are kept alphabetically ordered so they are easy to find in the Blocks editor.
    @SimpleFunction(description="Returns the built-in MediaPlayer duration in milliseconds, or 0 if no internal player is active.")
    public int CurrentDuration(){ return mediaPlayer!=null?mediaPlayer.getDuration():0; }

    @SimpleFunction(description="Returns the built-in MediaPlayer current position in milliseconds, or 0 if no internal player is active.")
    public int CurrentPosition(){ return mediaPlayer!=null?mediaPlayer.getCurrentPosition():0; }

    @SimpleFunction(description="Selects and plays the next song in the current library. Shuffle mode is honored and the list wraps at the end.")
    public void NextMusic(){ NextClicked(); PlayNextSong(); }

    @SimpleFunction(description="Pauses the extension's built-in MediaPlayer. Call ResumeMusic to continue playback.")
    public void PauseMusic(){ PauseClicked(); PauseInternalPlayer(); }

    @SimpleFunction(description="Starts or resumes the extension's built-in MediaPlayer for the selected song.")
    public void PlayMusic(){ PlayClicked(); ResumeInternalPlayer(); }

    @SimpleFunction(description="Plays the next song in the current library. Shuffle mode is honored and the list wraps at the end.")
    public void PlayNextSong(){
        int count=songs.size();
        if(count==0)return;
        int next=internalCurrentIndex<=0?1:internalCurrentIndex+1;
        if(internalShuffle&&count>1){ do{ next=random.nextInt(count)+1; }while(next==internalCurrentIndex); }
        if(next>count)next=1;
        playSongAtIndex(next);
    }

    @SimpleFunction(description="Plays the previous song from the current library list and wraps to the last song from the start.")
    public void PlayPreviousSong(){
        int count=songs.size();
        if(count==0)return;
        int previous=internalCurrentIndex<=1?count:internalCurrentIndex-1;
        playSongAtIndex(previous);
    }

    @SimpleFunction(description="Plays a library song, updates Now Playing, and opens that screen. Required parameter: index (number): a 1-based library index; invalid indexes are ignored.")
    public void PlaySongAtIndex(int index){ playSongAtIndex(index); }

    @SimpleFunction(description="Selects and plays the previous song in the current library, wrapping to the last song when necessary.")
    public void PreviousMusic(){ PreviousClicked(); PlayPreviousSong(); }

    @SimpleFunction(description="Toggles repeat mode for the built-in player. When enabled, the current song restarts after it finishes.")
    public void RepeatMusic(){ internalRepeat=!internalRepeat; RepeatClicked(); }

    @SimpleFunction(description="Resumes paused playback, or starts the currently selected song when the player has not been created yet.")
    public void ResumeMusic(){ PlayClicked(); ResumeInternalPlayer(); }

    @SimpleFunction(description="Seeks the built-in player and updates the seek bar. Required parameter: position (number): destination in milliseconds. For an external player, handle SeekChanged and pass its position to that player.")
    public void SeekTo(int position){
        if(mediaPlayer!=null)mediaPlayer.seekTo(Math.max(0,position));
        SetCurrentPosition(Math.max(0,position));
        SetCurrentTime(formatDuration(Math.max(0,position)));
    }

    @SimpleFunction(description="Alias for SeekTo. Required parameter: position (number): destination in milliseconds.")
    public void SetPosition(int position){ SeekTo(position); }

    @SimpleFunction(description="Toggles shuffle mode for next-song selection.")
    public void ShuffleMusic(){ internalShuffle=!internalShuffle; ShuffleClicked(); }

    @SimpleFunction(description="Stops playback and releases the extension's built-in MediaPlayer.")
    public void StopMusic(){ StopInternalPlayer(); ControlButtonClicked("Stop"); }

    @SimpleFunction(description="Sets built-in playback volume from 0 to 100. Required parameter: volume (number): desired percentage.")
    public void SetVolume(int volume){ playerVolume=Math.max(0,Math.min(100,volume))/100f; if(mediaPlayer!=null)mediaPlayer.setVolume(playerVolume,playerVolume); VolumeChanged(Math.round(playerVolume*100)); }
    @SimpleFunction(description="Returns the built-in playback volume as a percentage from 0 to 100.") public int Volume(){ return Math.round(playerVolume*100); }
    @SimpleFunction(description="Notifies this component that another player started.") public void NotifyOtherPlayerStarted(){ OtherPlayerStarted(); }
    @SimpleFunction(description="Notifies this component that another player ended.") public void NotifyOtherPlayerEnd(){ OtherPlayerEnd(); }
    @SimpleFunction(description="Reports an external or built-in player error. Required parameter: message (text): error detail.") public void NotifyPlayerError(String message){ PlayerError(message); }

    @SimpleFunction(description="Requests sharing for music. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void ShareMusic(String path){ sharePath=path; ShareClicked(path); } @SimpleFunction(description="Requests sharing for to whats app. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void ShareToWhatsApp(String path){ ShareClicked(path); } @SimpleFunction(description="Requests sharing for to facebook. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void ShareToFacebook(String path){ ShareClicked(path); } @SimpleFunction(description="Requests sharing for to instagram. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void ShareToInstagram(String path){ ShareClicked(path); } @SimpleFunction(description="Requests sharing for to telegram. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void ShareToTelegram(String path){ ShareClicked(path); } @SimpleFunction(description="Requests sharing for to messenger. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void ShareToMessenger(String path){ ShareClicked(path); } @SimpleFunction(description="Requests sharing for to system. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void ShareToSystem(String path){ ShareClicked(path); }
    @SimpleFunction(description="Adds to favorites. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void AddToFavorites(String path){ favorites.add(path); FavoriteAdded(path); } @SimpleFunction(description="Removes from favorites. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void RemoveFromFavorites(String path){ favorites.remove(path); FavoriteRemoved(path); } @SimpleFunction(description="Loads favorites.") public void LoadFavorites(){ renderSongs(pathsToSongs(new ArrayList<String>(favorites))); } @SimpleFunction(description="Displays favorites. Required parameter: json (text): JSON text containing an array of song objects.") public void DisplayFavorites(String json){ LoadMusicIntoList(json); } @SimpleFunction(description="Adds recently played. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void AddRecentlyPlayed(String path){ recentlyPlayed.remove(path); recentlyPlayed.add(0,path); } @SimpleFunction(description="Loads recently played.") public void LoadRecentlyPlayed(){ renderSongs(pathsToSongs(recentlyPlayed)); } @SimpleFunction(description="Clears recently played.") public void ClearRecentlyPlayed(){ recentlyPlayed.clear(); } @SimpleFunction(description="Adds to queue. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void AddToQueue(String path){ queue.add(path); QueueUpdated(); } @SimpleFunction(description="Removes from queue. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void RemoveFromQueue(String path){ queue.remove(path); QueueUpdated(); } @SimpleFunction(description="Moves queue item. Required parameters: from (number): Current 1-based queue position; to (number): Destination 1-based queue position.") public void MoveQueueItem(int from,int to){ if(from>0&&from<=queue.size()&&to>0&&to<=queue.size()){ String item=queue.remove(from-1); queue.add(to-1,item); } QueueUpdated(); } @SimpleFunction(description="Loads queue.") public void LoadQueue(){ renderSongs(pathsToSongs(queue)); } @SimpleFunction(description="Clears queue.") public void ClearQueue(){ queue.clear(); QueueUpdated(); } @SimpleFunction(description="Sets default album art. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void SetDefaultAlbumArt(String path){ defaultAlbumArtPath=path; SetAlbumArt(path); } @SimpleFunction(description="Loads album art. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void LoadAlbumArt(String path){ SetAlbumArt(path); }
    @SimpleFunction(description="Sets the floating action button icon. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void SetFABIcon(String path){ setIconImage("fab",path); } @SimpleFunction(description="Sets the floating action button color. Required parameter: c (number): App Inventor color integer.") public void SetFABColor(int c){ fab.setBackground(round(c,32)); } @SimpleFunction(description="Shows empty state.") public void ShowEmptyState(){ emptyStateText.setVisibility(View.VISIBLE); } @SimpleFunction(description="Hides empty state.") public void HideEmptyState(){ emptyStateText.setVisibility(View.GONE); } @SimpleFunction(description="Sets empty state image. Required parameter: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void SetEmptyStateImage(String path){} @SimpleFunction(description="Sets empty state text. Required parameter: text (text): Text to display.") public void SetEmptyStateText(String text){ emptyStateText.setText(text); }
    @SimpleFunction(description="Requests storage permission.") public void RequestStoragePermission(){ requestPermission(requiredAudioPermission()); } @SimpleFunction(description="Requests audio permission.") public void RequestAudioPermission(){ requestPermission(requiredAudioPermission()); } @SimpleFunction(description="Checks permission. Required parameter: permission (text): Android permission name, or empty text to use the required audio permission.") public boolean CheckPermission(String permission){ String p=permission!=null&&permission.length()>0?permission:requiredAudioPermission(); return container.$context().checkCallingOrSelfPermission(p)==PackageManager.PERMISSION_GRANTED; } @SimpleFunction(description="Runs the permission granted operation. Required parameter: permission (text): Android permission name, or empty text to use the required audio permission.") public void PermissionGranted(String permission){ EventDispatcher.dispatchEvent(this,"PermissionGranted",permission); } @SimpleFunction(description="Runs the permission denied operation. Required parameter: permission (text): Android permission name, or empty text to use the required audio permission.") public void PermissionDenied(String permission){ EventDispatcher.dispatchEvent(this,"PermissionDenied",permission); }
    @SimpleFunction(description="Sets logo. Required parameter: value (text): Text to display.") public void SetLogo(String value){ logoView.setText(value); } @SimpleFunction(description="Sets app title. Required parameter: value (text): Text to display.") public void SetAppTitle(String value){ titleView.setText(value); } @SimpleFunction(description="Sets title color. Required parameter: c (number): App Inventor color integer.") public void SetTitleColor(int c){ titleView.setTextColor(c); } @SimpleFunction(description="Sets elevation. Required parameter: e (number): Elevation in density-independent pixels.") public void SetElevation(float e){ appBar.setElevation(e); }
    @SimpleFunction(description="Sets play icon. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetPlayIcon(String p){ setIconImage("play",p); } @SimpleFunction(description="Sets pause icon. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetPauseIcon(String p){ setIconImage("pause",p); } @SimpleFunction(description="Sets next icon. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetNextIcon(String p){ setIconImage("next",p); } @SimpleFunction(description="Sets previous icon. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetPreviousIcon(String p){ setIconImage("previous",p); } @SimpleFunction(description="Sets shuffle icon. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetShuffleIcon(String p){ setIconImage("shuffle",p); } @SimpleFunction(description="Sets repeat icon. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetRepeatIcon(String p){ setIconImage("repeat",p); } @SimpleFunction(description="Sets favorite icon. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetFavoriteIcon(String p){ setIconImage("favorite",p); } @SimpleFunction(description="Sets playlist icon. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetPlaylistIcon(String p){ setIconImage("playlist",p); } @SimpleFunction(description="Sets search icon. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetSearchIcon(String p){ setIconImage("search",p); } @SimpleFunction(description="Sets sidebar icon. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetSidebarIcon(String p){ setIconImage("sidebar",p); } @SimpleFunction(description="Sets album placeholder. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetAlbumPlaceholder(String p){ defaultAlbumArtPath=p; SetAlbumArt(p); } @SimpleFunction(description="Sets share icon. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetShareIcon(String p){ setIconImage("share",p); } @SimpleFunction(description="Sets search icon image. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetSearchIconImage(String p){ SetSearchIcon(p); } @SimpleFunction(description="Sets clear icon. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetClearIcon(String p){} @SimpleFunction(description="Sets share button image. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetShareButtonImage(String p){ SetShareIcon(p); }
    @SimpleFunction(description="Sets play button. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetPlayButton(String p){ SetPlayIcon(p); } @SimpleFunction(description="Sets pause button. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetPauseButton(String p){ SetPauseIcon(p); } @SimpleFunction(description="Sets previous button. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetPreviousButton(String p){ SetPreviousIcon(p); } @SimpleFunction(description="Sets next button. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetNextButton(String p){ SetNextIcon(p); } @SimpleFunction(description="Sets shuffle button. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetShuffleButton(String p){ SetShuffleIcon(p); } @SimpleFunction(description="Sets repeat button. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetRepeatButton(String p){ SetRepeatIcon(p); } @SimpleFunction(description="Sets favorite button. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetFavoriteButton(String p){ SetFavoriteIcon(p); } @SimpleFunction(description="Sets queue button. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetQueueButton(String p){ setIconImage("queue",p); } @SimpleFunction(description="Sets share button. Required parameter: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void SetShareButton(String p){ SetShareIcon(p); }

    @SimpleEvent(description="Triggered when search text changed occurs. Provides: searchText (text): Current search-field text.") public void SearchTextChanged(String searchText){ if(dispatchEventOnce("SearchTextChanged",searchText)) SearchMusic(searchText); } @SimpleEvent(description="Triggered when search completed occurs. Provides: resultList (text): JSON array text containing matching songs.") public void SearchCompleted(String resultList){ dispatchEventOnce("SearchCompleted",resultList); } @SimpleEvent(description="Triggered when search item clicked occurs. Provides: musicPath (text): Selected song content URI or file path; songName (text): Selected song title.") public void SearchItemClicked(String musicPath,String songName){ dispatchEventOnce("SearchItemClicked",musicPath,songName); }
    @SimpleEvent(description="Triggered when music clicked occurs. Provides: musicIndex (number): 1-based selected song index; musicPath (text): Selected song content URI or file path; songName (text): Selected song title; artist (text): Artist name text; duration (text): Formatted song duration text, usually m:ss; album (text): Album name.") public void MusicClicked(int musicIndex,String musicPath,String songName,String artist,String duration,String album){ sharePath=musicPath; dispatchEventOnce("MusicClicked",musicIndex,musicPath,songName,artist,duration,album); dispatchEventOnce("MusicSelected",musicIndex,musicPath); } @SimpleEvent(description="Triggered when music long pressed occurs. Provides: index (number): 1-based song index; path (text): Song content URI, file path, or packaged asset path as appropriate.") public void MusicLongPressed(int index,String path){ dispatchEventOnce("MusicLongPressed",index,path); } @SimpleEvent(description="Triggered when share icon clicked occurs. Provides: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void ShareIconClicked(String path){ dispatchEventOnce("ShareIconClicked",path); } @SimpleEvent(description="Triggered when playlist icon clicked occurs. Provides: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void PlaylistIconClicked(String path){ dispatchEventOnce("PlaylistIconClicked",path); } @SimpleEvent(description="Triggered when favorite clicked occurs. Provides: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void FavoriteClicked(String path){ dispatchEventOnce("FavoriteClicked",path); }
    @SimpleEvent(description="Triggered when playlist opened occurs.") public void PlaylistOpened(){ EventDispatcher.dispatchEvent(this,"PlaylistOpened"); } @SimpleEvent(description="Triggered when playlist closed occurs.") public void PlaylistClosed(){ EventDispatcher.dispatchEvent(this,"PlaylistClosed"); } @SimpleEvent(description="Triggered when drawer opened occurs.") public void DrawerOpened(){ EventDispatcher.dispatchEvent(this,"DrawerOpened"); } @SimpleEvent(description="Triggered when drawer closed occurs.") public void DrawerClosed(){ EventDispatcher.dispatchEvent(this,"DrawerClosed"); } @SimpleEvent(description="Triggered when sidebar item clicked occurs. Provides: item (text): Selected sidebar item name.") public void SidebarItemClicked(String item){ EventDispatcher.dispatchEvent(this,"SidebarItemClicked",item); }
    @SimpleEvent(description="Triggered when playlist created occurs. Provides: n (text): Playlist name or control symbol.") public void PlaylistCreated(String n){ EventDispatcher.dispatchEvent(this,"PlaylistCreated",n); } @SimpleEvent(description="Triggered when playlist deleted occurs. Provides: n (text): Playlist name or control symbol.") public void PlaylistDeleted(String n){ EventDispatcher.dispatchEvent(this,"PlaylistDeleted",n); } @SimpleEvent(description="Triggered when song added occurs. Provides: p (text): Song path, playlist name, or image asset/file path as indicated by the block name; path (text): Song content URI, file path, or packaged asset path as appropriate.") public void SongAdded(String p,String path){ EventDispatcher.dispatchEvent(this,"SongAdded",p,path); } @SimpleEvent(description="Triggered when song removed occurs. Provides: p (text): Song path, playlist name, or image asset/file path as indicated by the block name; path (text): Song content URI, file path, or packaged asset path as appropriate.") public void SongRemoved(String p,String path){ EventDispatcher.dispatchEvent(this,"SongRemoved",p,path); } @SimpleEvent(description="Triggered when playlist selected occurs. Provides: n (text): Playlist name or control symbol.") public void PlaylistSelected(String n){ EventDispatcher.dispatchEvent(this,"PlaylistSelected",n); }
    @SimpleEvent(description="Triggered when play clicked occurs.") public void PlayClicked(){ dispatchEventOnce("PlayClicked"); } @SimpleEvent(description="Triggered when pause clicked occurs.") public void PauseClicked(){ dispatchEventOnce("PauseClicked"); } @SimpleEvent(description="Triggered when next clicked occurs.") public void NextClicked(){ dispatchEventOnce("NextClicked"); } @SimpleEvent(description="Triggered when previous clicked occurs.") public void PreviousClicked(){ dispatchEventOnce("PreviousClicked"); } @SimpleEvent(description="Triggered when shuffle clicked occurs.") public void ShuffleClicked(){ dispatchEventOnce("ShuffleClicked"); } @SimpleEvent(description="Triggered when repeat clicked occurs.") public void RepeatClicked(){ dispatchEventOnce("RepeatClicked"); } @SimpleEvent(description="Triggered when queue clicked occurs.") public void QueueClicked(){ dispatchEventOnce("QueueClicked"); } @SimpleEvent(description="Triggered when share clicked occurs. Provides: path (text): Song content URI, file path, or packaged asset path as appropriate.") public void ShareClicked(String path){ dispatchEventOnce("ShareClicked",path); } @SimpleEvent(description="Triggered when control button clicked occurs. Provides: name (text): Playlist, screen, control, or item name.") public void ControlButtonClicked(String name){ dispatchEventOnce("ControlButtonClicked",name); } @SimpleEvent(description="Triggered when seek changed occurs. Provides: position (number): Playback or seek-bar position in milliseconds.") public void SeekChanged(int position){ dispatchEventOnce("SeekChanged",position); } @SimpleEvent(description="Triggered when screen changed occurs. Provides: screen (text): Screen name; use Library or NowPlaying.") public void ScreenChanged(String screen){ dispatchEventOnce("ScreenChanged",screen); } @SimpleEvent(description="Triggered when the floating action button is clicked.") public void FABClicked(){ dispatchEventOnce("FABClicked"); } @SimpleEvent(description="Triggered when favorite added occurs. Provides: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void FavoriteAdded(String p){ dispatchEventOnce("FavoriteAdded",p); } @SimpleEvent(description="Triggered when favorite removed occurs. Provides: p (text): Song path, playlist name, or image asset/file path as indicated by the block name.") public void FavoriteRemoved(String p){ dispatchEventOnce("FavoriteRemoved",p); } @SimpleEvent(description="Triggered when queue updated occurs.") public void QueueUpdated(){ dispatchEventOnce("QueueUpdated"); }

    @SimpleEvent(description="Triggered when music database updated occurs. Provides: count (number): Number of songs found; resultList (text): JSON array text containing matching songs.") public void MusicDatabaseUpdated(int count,String resultList){ EventDispatcher.dispatchEvent(this,"MusicDatabaseUpdated",count,resultList); }
    @SimpleEvent(description="Triggered when playback started occurs. Provides: index (number): 1-based song index; path (text): Song content URI, file path, or packaged asset path as appropriate.") public void PlaybackStarted(int index,String path){ EventDispatcher.dispatchEvent(this,"PlaybackStarted",index,path); }
    @SimpleEvent(description="Triggered when playback completed occurs.") public void PlaybackCompleted(){ EventDispatcher.dispatchEvent(this,"PlaybackCompleted"); }
    @SimpleEvent(description="Triggered when playback error occurs. Provides: message (text): Playback error message.") public void PlaybackError(String message){ EventDispatcher.dispatchEvent(this,"PlaybackError",message); }
    @SimpleEvent(description="Triggered when any player completes so clocks and timers can be restarted or reset.") public void PlayerCompleted(){ EventDispatcher.dispatchEvent(this,"PlayerCompleted"); }
    @SimpleEvent(description="Triggered when another audio player is reported as started.") public void OtherPlayerStarted(){ EventDispatcher.dispatchEvent(this,"OtherPlayerStarted"); }
    @SimpleEvent(description="Triggered when another audio player is reported as ended.") public void OtherPlayerEnd(){ EventDispatcher.dispatchEvent(this,"OtherPlayerEnd"); }
    @SimpleEvent(description="Triggered when a player error is reported. Provides: message (text): error detail.") public void PlayerError(String message){ EventDispatcher.dispatchEvent(this,"PlayerError",message); }
    @SimpleEvent(description="Triggered after the built-in volume changes. Provides: volume (number): percentage from 0 to 100.") public void VolumeChanged(int volume){ EventDispatcher.dispatchEvent(this,"VolumeChanged",volume); }
    @SimpleEvent(description="Triggered when a playlist drawer action is clicked. Provides: action (text): Create Playlist, Rename Playlist, or Delete Playlist.") public void PlaylistActionClicked(String action){ EventDispatcher.dispatchEvent(this,"PlaylistActionClicked",action); }

    @SimpleEvent(description="Triggered when playlist renamed occurs. Provides: oldName (text): Existing playlist name; newName (text): Replacement playlist name.") public void PlaylistRenamed(String oldName,String newName){ EventDispatcher.dispatchEvent(this,"PlaylistRenamed",oldName,newName); }

    private boolean dispatchEventOnce(String eventName,Object... args){
        if(dispatchingEvents.contains(eventName))return false;
        dispatchingEvents.add(eventName);
        try{
            EventDispatcher.dispatchEvent(this,eventName,args);
            return true;
        }finally{
            dispatchingEvents.remove(eventName);
        }
    }


    private void startInternalPlayer(final Song song){
        StopInternalPlayer();
        mediaPlayer=new MediaPlayer();
        try{
            if(song.path.startsWith("content://"))mediaPlayer.setDataSource(container.$context(),Uri.parse(song.path)); else mediaPlayer.setDataSource(song.path);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){ public void onCompletion(MediaPlayer mp){ PlaybackCompleted(); PlayerCompleted(); if(internalRepeat)playSongAtIndex(internalCurrentIndex); else PlayNextSong(); }});
            mediaPlayer.prepare();
            mediaPlayer.setVolume(playerVolume,playerVolume);
            mediaPlayer.start();
            PlaybackStarted(internalCurrentIndex,song.path);
        }catch(Exception e){ String message=e.getMessage()==null?"Unable to play audio":e.getMessage(); PlaybackError(message); PlayerError(message); StopInternalPlayer(); }
    }
    private void ResumeInternalPlayer(){ if(mediaPlayer!=null)mediaPlayer.start(); else if(internalCurrentIndex>0)playSongAtIndex(internalCurrentIndex); }
    private void PauseInternalPlayer(){ if(mediaPlayer!=null&&mediaPlayer.isPlaying())mediaPlayer.pause(); }
    private void StopInternalPlayer(){ if(mediaPlayer!=null){ try{ mediaPlayer.stop(); }catch(Exception e){} mediaPlayer.release(); mediaPlayer=null; }}
    private void playSongAtIndex(int index){ if(index<1||index>songs.size())return; Song song=songs.get(index-1); internalCurrentIndex=index; sharePath=song.path; startInternalPlayer(song); updateNowPlaying(song,0); OpenNowPlayingScreen(); AddRecentlyPlayed(song.path); }
    private void updateNowPlaying(Song song,int position){ SetSongTitle(song.title); SetArtist(song.artist); SetAlbumArt(song.albumArt); int duration=durationTextToMillis(song.duration); if(mediaPlayer!=null)duration=mediaPlayer.getDuration(); SetDuration(duration); SetCurrentPosition(position); SetCurrentTime(formatDuration(position)); SetDurationTime(formatDuration(duration)); }
    private int durationTextToMillis(String durationText){ if(durationText==null)return 0; String[] parts=durationText.split(":"); try{ if(parts.length==2)return ((Integer.parseInt(parts[0])*60)+Integer.parseInt(parts[1]))*1000; if(parts.length==3)return ((Integer.parseInt(parts[0])*3600)+(Integer.parseInt(parts[1])*60)+Integer.parseInt(parts[2]))*1000; }catch(Exception e){} return 0; }

    private void loadMusicFromMediaStore(String folder){ songs.clear(); if(folder==null||folder.length()==0)catalog.clear(); Uri uri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI; String[] projection=new String[]{MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.DURATION,MediaStore.Audio.Media.ALBUM}; String selection=MediaStore.Audio.Media.IS_MUSIC+"!=0"; Cursor c=null; try{ c=container.$context().getContentResolver().query(uri,projection,selection,null,MediaStore.Audio.Media.TITLE+" ASC"); if(c!=null){ while(c.moveToNext()){ long id=c.getLong(0); String filePath=c.getString(1); if(filePath==null)filePath=""; if(folder!=null&&folder.length()>0&&!filePath.toLowerCase().contains(folder.toLowerCase()))continue; String path=ContentUris.withAppendedId(uri,id).toString(); Song song=new Song(path,nonEmpty(c.getString(2),titleFromPath(filePath)),cleanMetadata(c.getString(3)),formatDuration(c.getLong(4)),nonEmpty(c.getString(5),""),""); songs.add(song); catalog.put(path,song); } } }catch(Exception e){ PermissionDenied(requiredAudioPermission()); } finally{ if(c!=null)c.close(); } renderSongs(songs); MusicDatabaseUpdated(songs.size(),toJson(songs)); }
    private ArrayList<Song> pathsToSongs(ArrayList<String> paths){ ArrayList<Song> result=new ArrayList<Song>(); for(String path:paths)result.add(findSong(path)); return result; }
    private Song findSong(String path){ if(catalog.containsKey(path))return catalog.get(path); for(Song s:songs)if(s.path.equals(path))return s; return new Song(path,titleFromPath(path),"","0:00","",""); }
    private String titleFromPath(String path){ int slash=path.lastIndexOf('/'); String name=slash>=0?path.substring(slash+1):path; int dot=name.lastIndexOf('.'); return dot>0?name.substring(0,dot):name; }
    private String nonEmpty(String value,String fallback){ return value!=null&&value.length()>0?value:fallback; }
    private String formatDuration(long ms){ long total=ms/1000; return (total/60)+":"+(total%60<10?"0":"")+(total%60); }
    private String requiredAudioPermission(){ return Build.VERSION.SDK_INT>=33?"android.permission.READ_MEDIA_AUDIO":Manifest.permission.READ_EXTERNAL_STORAGE; }
    private void requestPermission(final String permission){ if(CheckPermission(permission)){ PermissionGranted(permission); return; } container.$form().askPermission(permission,new PermissionResultHandler(){ public void HandlePermissionResponse(String p,boolean granted){ if(granted)PermissionGranted(p); else PermissionDenied(p); }}); }

    private String cleanMetadata(String value){ if(value==null)return ""; String v=value.trim(); return v.equalsIgnoreCase("unknown")||v.equalsIgnoreCase("unknown artist")||v.equalsIgnoreCase("<unknown>")?"":v; }
    private String cleanTitle(String title,String path){ String value=cleanMetadata(title); return value.length()>0?value:titleFromPath(path); }
    private String joinMetadata(String artist,String duration){ String a=cleanMetadata(artist), d=cleanMetadata(duration); if(d.equals("0:00"))d=""; return a.length()>0&&d.length()>0?a+" • "+d:(a.length()>0?a:d); }
    private TextView titleLabel(String text,int size){ final TextView title=label(text,size,true); title.setMaxLines(3); title.setEllipsize(TextUtils.TruncateAt.END); title.post(new Runnable(){ public void run(){ animateOverflowingTitle(title); }}); return title; }
    private void animateOverflowingTitle(final TextView title){ if(!animationsEnabled||title.getLayout()==null)return; int last=title.getLayout().getLineCount()-1; if(last<0||title.getLayout().getEllipsisCount(last)==0)return; float distance=Math.max(dp(28),title.getWidth()/3); ObjectAnimator animator=ObjectAnimator.ofFloat(title,"translationX",0,-distance); animator.setDuration(Math.max(900,animationSpeed*8)); animator.setStartDelay(800); animator.setRepeatMode(ObjectAnimator.REVERSE); animator.setRepeatCount(ObjectAnimator.INFINITE); animator.start(); }
    private FrameLayout.LayoutParams dropdownParams(){ FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,dp(280),Gravity.TOP); lp.setMargins(dp(14),dp(106),dp(14),0); return lp; }
    private void updateDropdownPosition(){ FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams)searchDropdown.getLayoutParams(); lp.topMargin=dp(106); searchDropdown.setLayoutParams(lp); searchDropdown.bringToFront(); }

    private TextView label(String t,int sp,boolean bold){ TextView v=new TextView(container.$context()); v.setText(t); v.setTextSize(sp); v.setTextColor(textColor); if(bold)v.setTypeface(Typeface.DEFAULT_BOLD); return v; }
    private TextView icon(String t, View.OnClickListener l){ TextView b=label(t,22,true); b.setGravity(Gravity.CENTER); b.setMinWidth(dp(44)); b.setMinHeight(dp(44)); b.setPadding(dp(8),dp(6),dp(8),dp(6)); b.setBackground(round(darkTheme?Color.rgb(48,48,48):Color.rgb(232,232,232),22)); b.setElevation(dp(2)); b.setContentDescription(t); b.setOnClickListener(l); registerIcon(t,b); return b; }
    private void initIconFallbacks(){ iconFallbacks.put("play","▶"); iconFallbacks.put("pause","Ⅱ"); iconFallbacks.put("next","▶▶"); iconFallbacks.put("previous","◀"); iconFallbacks.put("shuffle","⇄"); iconFallbacks.put("repeat","↩"); iconFallbacks.put("favorite","♡"); iconFallbacks.put("playlist","＋"); iconFallbacks.put("search","⌕"); iconFallbacks.put("sidebar","☰"); iconFallbacks.put("share","↗"); iconFallbacks.put("queue","≡"); iconFallbacks.put("fab","↻"); }
    private void registerIcon(String symbol,TextView view){ String key=keyForSymbol(symbol); if(key.length()==0)return; if(!iconViews.containsKey(key))iconViews.put(key,new ArrayList<TextView>()); iconViews.get(key).add(view); if(iconImagePaths.containsKey(key))applyIconImageToView(key,view,loadDrawable(iconImagePaths.get(key))); }
    private String keyForSymbol(String symbol){ if(symbol.equals("▶"))return "play"; if(symbol.equals("Ⅱ"))return "pause"; if(symbol.equals("▶▶"))return "next"; if(symbol.equals("◀"))return "previous"; if(symbol.equals("⇄"))return "shuffle"; if(symbol.equals("↩"))return "repeat"; if(symbol.equals("♡"))return "favorite"; if(symbol.equals("＋"))return "playlist"; if(symbol.equals("⌕"))return "search"; if(symbol.equals("☰"))return "sidebar"; if(symbol.equals("↗")||symbol.equals("⤴"))return "share"; if(symbol.equals("≡"))return "queue"; if(symbol.equals("↻"))return "fab"; return ""; }
    private void setIconImage(String key,String path){ iconImagePaths.put(key,path); ArrayList<TextView> views=iconViews.get(key); if(views==null)return; Drawable drawable=loadDrawable(path); for(TextView v:views)applyIconImageToView(key,v,drawable); }
    private void applyIconImageToView(String key,TextView v,Drawable drawable){ if(drawable!=null){ Drawable copy=drawable.getConstantState()!=null?drawable.getConstantState().newDrawable():drawable; copy.setBounds(0,0,dp(28),dp(28)); v.setText(""); v.setCompoundDrawables(null,copy,null,null); v.setContentDescription(key); } else { v.setCompoundDrawables(null,null,null,null); v.setText(iconFallbacks.containsKey(key)?iconFallbacks.get(key):""); } }
    private Drawable loadDrawable(String path){ if(path==null||path.length()==0)return null; try{ return MediaUtil.getBitmapDrawable(container.$form(),path); }catch(Exception ignored){} String asset=path; if(asset.startsWith("file:///android_asset/"))asset=asset.substring(22); if(asset.startsWith("//"))asset=asset.substring(2); while(asset.startsWith("/"))asset=asset.substring(1); try{ return Drawable.createFromStream(container.$context().getAssets().open(asset),null); }catch(Exception e){} int slash=asset.lastIndexOf('/'); if(slash>=0){ try{ return Drawable.createFromStream(container.$context().getAssets().open(asset.substring(slash+1)),null); }catch(Exception e){} } try{ return Drawable.createFromPath(path); }catch(Exception e){} if(path.startsWith("file://")){ try{ return Drawable.createFromPath(path.substring(7)); }catch(Exception e){} } return null; }
    private TextView nav(final String t){ TextView v=label(t,14,true); v.setGravity(Gravity.CENTER); v.setOnClickListener(new View.OnClickListener(){ public void onClick(View view){ NavigateTo(t); }}); v.setLayoutParams(new LinearLayout.LayoutParams(0,-1,1)); return v; }
    private LinearLayout playlistDrawer(){
        LinearLayout drawer=new LinearLayout(container.$context()); drawer.setOrientation(LinearLayout.VERTICAL); drawer.setPadding(dp(16),dp(18),dp(16),dp(18)); drawer.setBackground(round(cardColor,0));
        LinearLayout header=new LinearLayout(container.$context()); header.setGravity(Gravity.CENTER_VERTICAL); header.addView(label("Playlists",20,true),new LinearLayout.LayoutParams(0,-2,1)); TextView close=icon("×",new View.OnClickListener(){ public void onClick(View v){ ClosePlaylistDrawer(); }}); close.setContentDescription("Close playlist drawer"); header.addView(close); drawer.addView(header);
        String[] actions={"Create Playlist","Rename Playlist","Delete Playlist"};
        for(final String action:actions){ TextView row=label(action,15,false); row.setPadding(dp(12),dp(14),dp(12),dp(14)); row.setBackground(round(darkTheme?Color.rgb(45,45,45):Color.rgb(240,240,240),12)); row.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ PlaylistActionClicked(action); showPlaylistAction(action); }}); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,dp(10),0,0); drawer.addView(row,lp); }
        return drawer;
    }
    private void showPlaylistChooser(){
        final ArrayList<String> names=new ArrayList<String>(playlists.keySet());
        if(names.size()==0){ new AlertDialog.Builder(container.$context()).setTitle("No playlists yet").setMessage("Create a playlist before adding this song.").setPositiveButton("Create",new DialogInterface.OnClickListener(){ public void onClick(DialogInterface dialog,int which){ PlaylistActionClicked("Create Playlist"); showPlaylistAction("Create Playlist"); }}).setNegativeButton("Cancel",null).show(); return; }
        final String[] choices=names.toArray(new String[names.size()]);
        new AlertDialog.Builder(container.$context()).setTitle("Add to playlist").setItems(choices,new DialogInterface.OnClickListener(){ public void onClick(DialogInterface dialog,int which){ String name=choices[which]; if(pendingPlaylistPath.length()>0)AddSongToPlaylist(name,pendingPlaylistPath); PlaylistSelected(name); }}).setNegativeButton("Cancel",null).show();
    }
    private void showPlaylistAction(final String action){
        final ArrayList<String> names=new ArrayList<String>(playlists.keySet());
        if(action.equals("Create Playlist")){ showTextDialog("Create playlist","Playlist name",new TextResult(){ public void accept(String value){ if(value.length()>0)CreatePlaylist(value); }}); return; }
        if(names.size()==0){ new AlertDialog.Builder(container.$context()).setTitle("No playlists").setMessage("There are no playlists to "+action.toLowerCase()+".").setPositiveButton("OK",null).show(); return; }
        final String[] choices=names.toArray(new String[names.size()]);
        new AlertDialog.Builder(container.$context()).setTitle(action).setItems(choices,new DialogInterface.OnClickListener(){ public void onClick(DialogInterface dialog,int which){ final String selected=choices[which]; if(action.equals("Delete Playlist"))DeletePlaylist(selected); else showTextDialog("Rename playlist","New name",new TextResult(){ public void accept(String value){ if(value.length()>0)RenamePlaylist(selected,value); }}); }}).setNegativeButton("Cancel",null).show();
    }
    private interface TextResult{ void accept(String value); }
    private void showTextDialog(String title,String hint,final TextResult result){ final EditText input=new EditText(container.$context()); input.setHint(hint); int pad=dp(20); FrameLayout holder=new FrameLayout(container.$context()); holder.setPadding(pad,0,pad,0); holder.addView(input,new FrameLayout.LayoutParams(-1,-2)); new AlertDialog.Builder(container.$context()).setTitle(title).setView(holder).setPositiveButton("Save",new DialogInterface.OnClickListener(){ public void onClick(DialogInterface dialog,int which){ result.accept(input.getText().toString().trim()); }}).setNegativeButton("Cancel",null).show(); }

    private LinearLayout drawer(String title,String[] items){ LinearLayout d=new LinearLayout(container.$context()); d.setOrientation(LinearLayout.VERTICAL); d.setPadding(dp(16),dp(18),dp(16),dp(18)); d.setBackground(round(cardColor,0)); d.addView(label(title,20,true)); for(final String item:items){ TextView row=label(item,15,false); row.setPadding(0,dp(14),0,dp(14)); row.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ SidebarItemClicked(item); }}); d.addView(row); } return d; }
    private FrameLayout.LayoutParams drawerParams(int gravity){ FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(dp(280),-1,gravity); return lp; }
    private GradientDrawable round(int color,int radius){ GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(radius)); return g; }
    private void applyTheme(){ root.setBackgroundColor(backgroundColor); shell.setBackgroundColor(backgroundColor); appBar.setBackgroundColor(primaryColor); titleView.setTextColor(textColor); logoView.setTextColor(accentColor); searchBar.setTextColor(textColor); searchBar.setHintTextColor(hintColor); searchBar.setBackground(round(cardColor,searchRadius)); bottomNav.setBackgroundColor(primaryColor); fab.setBackground(round(accentColor,32)); for(ArrayList<TextView> views:iconViews.values())for(TextView v:views){ v.setTextColor(buttonColor); v.setBackground(round(darkTheme?Color.rgb(48,48,48):Color.rgb(232,232,232),22)); } }
    private int dp(int v){ return (int)(v*container.$context().getResources().getDisplayMetrics().density+0.5f); }
    private Song get(int i){ return i>0&&i<=songs.size()?songs.get(i-1):new Song("","","","","",""); }
    private String toJson(ArrayList<Song> list){ JSONArray a=new JSONArray(); for(Song s:list)a.put(s.toJson()); return a.toString(); }
    private static class Song{ String path,title,artist,duration,album,albumArt; Song(String p,String t,String ar,String d,String al,String aa){path=p;title=t;artist=ar;duration=d;album=al;albumArt=aa;} JSONObject toJson(){ JSONObject o=new JSONObject(); try{o.put("path",path);o.put("title",title);o.put("artist",artist);o.put("duration",duration);o.put("album",album);o.put("albumArt",albumArt);}catch(Exception e){} return o; }}
}
