package chatinputbox.chatinputboxnew;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.EditText;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

@DesignerComponent(
    version = 1,
    description = "CodeIgniteMITMusic builds a polished two-screen music-player GUI for MIT App Inventor. It exposes UI blocks and events only; students implement playback, storage, playlists, permissions, and sharing logic.",
    category = ComponentCategory.EXTENSION,
    nonVisible = false,
    iconName = "icon.png")
@SimpleObject(external = true)
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
    private final LinearLayout songList;
    private final LinearLayout dropdownList;
    private final LinearLayout playlistDrawer;
    private final LinearLayout sidebarDrawer;
    private final TextView emptyStateText;
    private final ImageButton fab;
    private final ImageView albumArt;
    private final TextView nowTitle;
    private final TextView nowArtist;
    private final SeekBar seekBar;
    private final TextView currentTime;
    private final TextView durationTime;
    private final LinearLayout bottomNav;

    private final ArrayList<Song> songs = new ArrayList<Song>();
    private final ArrayList<Song> searchResults = new ArrayList<Song>();
    private final HashSet<String> favorites = new HashSet<String>();
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

    public CodeIgniteMITMusic(ComponentContainer container) {
        super(container);
        this.container = container;
        root = new FrameLayout(container.$context());
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

        fab = new ImageButton(container.$context());
        fab.setImageDrawable(null); fab.setContentDescription("Update music database");
        fab.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ FABClicked(); }});
        FrameLayout.LayoutParams fabLp = new FrameLayout.LayoutParams(dp(64), dp(64), Gravity.BOTTOM | Gravity.RIGHT);
        fabLp.setMargins(0,0,dp(18),dp(78));

        playlistDrawer = drawer("Playlists", new String[]{"Create Playlist","Rename Playlist","Delete Playlist"});
        sidebarDrawer = drawer("Menu", new String[]{"Library","Playlists","Favorites","Recently Played","Settings"});
        playlistDrawer.setVisibility(View.GONE); sidebarDrawer.setVisibility(View.GONE);

        shell.addView(appBar);
        shell.addView(searchBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        shell.addView(contentFrame, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        shell.addView(bottomNav, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64)));
        root.addView(shell);
        root.addView(fab, fabLp);
        root.addView(playlistDrawer, drawerParams(Gravity.RIGHT));
        root.addView(sidebarDrawer, drawerParams(Gravity.LEFT));
        applyTheme();
        Width(ViewGroup.LayoutParams.MATCH_PARENT); Height(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override public View getView() { return root; }
    @SimpleFunction(description="Adds the complete CodeIgniteMITMusic interface to an Arrangement.") public void AddToArrangement(AndroidViewComponent arrangement){ ViewGroup p=(ViewGroup)arrangement.getView(); if(root.getParent()!=null)((ViewGroup)root.getParent()).removeView(root); p.addView(root,new ViewGroup.LayoutParams(-1,-1)); }

    private LinearLayout buildLibraryScreen(){ LinearLayout page=new LinearLayout(container.$context()); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(14), dp(8), dp(14), dp(8)); dropdownList=new LinearLayout(container.$context()); dropdownList.setOrientation(LinearLayout.VERTICAL); dropdownList.setVisibility(View.GONE); emptyStateText=label("No songs yet\nTap the update button or load a list from blocks.",16,false); emptyStateText.setGravity(Gravity.CENTER); ScrollView scroll=new ScrollView(container.$context()); songList=new LinearLayout(container.$context()); songList.setOrientation(LinearLayout.VERTICAL); scroll.addView(songList); page.addView(dropdownList); page.addView(emptyStateText,new LinearLayout.LayoutParams(-1,dp(140))); page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); return page; }
    private LinearLayout buildNowPlayingScreen(){ LinearLayout p=new LinearLayout(container.$context()); p.setOrientation(LinearLayout.VERTICAL); p.setGravity(Gravity.CENTER_HORIZONTAL); p.setPadding(dp(24),dp(18),dp(24),dp(18)); albumArt=new ImageView(container.$context()); albumArt.setBackground(round(cardColor,28)); nowTitle=label("Song Title",24,true); nowArtist=label("Artist",16,false); seekBar=new SeekBar(container.$context()); seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ public void onProgressChanged(SeekBar b,int progress,boolean fromUser){ if(fromUser) SeekChanged(progress);} public void onStartTrackingTouch(SeekBar b){} public void onStopTrackingTouch(SeekBar b){} }); LinearLayout times=new LinearLayout(container.$context()); times.setGravity(Gravity.CENTER); currentTime=label("0:00",12,false); durationTime=label("0:00",12,false); times.addView(currentTime,new LinearLayout.LayoutParams(0,-2,1)); times.addView(durationTime); p.addView(albumArt,new LinearLayout.LayoutParams(dp(280),dp(280))); p.addView(nowTitle); p.addView(nowArtist); p.addView(seekBar,new LinearLayout.LayoutParams(-1,-2)); p.addView(times,new LinearLayout.LayoutParams(-1,-2)); p.addView(controlRow(new String[]{"↩","⇄","◀","▶","Ⅱ","▶▶","♡","↗","≡","⤴"})); return p; }
    private LinearLayout controlRow(String[] names){ LinearLayout r=new LinearLayout(container.$context()); r.setGravity(Gravity.CENTER); for(final String n:names) r.addView(icon(n,new View.OnClickListener(){ public void onClick(View v){ control(n); }})); return r; }
    private void control(String n){ if(n.equals("▶")) PlayClicked(); else if(n.equals("Ⅱ")) PauseClicked(); else if(n.equals("▶▶")) NextClicked(); else if(n.equals("◀")) PreviousClicked(); else if(n.equals("⇄")) ShuffleClicked(); else if(n.equals("↩")) RepeatClicked(); else if(n.equals("♡")) FavoriteClicked(sharePath); else if(n.equals("≡")) QueueClicked(); else if(n.equals("↗")||n.equals("⤴")) ShareClicked(sharePath); ControlButtonClicked(n); }

    private void renderSongs(ArrayList<Song> data){ songList.removeAllViews(); emptyStateText.setVisibility(data.size()==0?View.VISIBLE:View.GONE); for(int i=0;i<data.size();i++){ final Song s=data.get(i); final int index=i+1; LinearLayout row=new LinearLayout(container.$context()); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(12),dp(10),dp(8),dp(10)); row.setBackground(round(cardColor,18)); TextView art=label("♪",28,true); TextView meta=label(s.title+"\n"+s.artist+" • "+s.duration,15,true); row.addView(art,new LinearLayout.LayoutParams(dp(54),dp(54))); row.addView(meta,new LinearLayout.LayoutParams(0,-2,1)); row.addView(icon("↗", new View.OnClickListener(){ public void onClick(View v){ ShareIconClicked(s.path); }})); row.addView(icon("＋", new View.OnClickListener(){ public void onClick(View v){ PlaylistIconClicked(s.path); }})); row.addView(icon("♡", new View.OnClickListener(){ public void onClick(View v){ FavoriteClicked(s.path); }})); row.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ MusicClicked(index,s.path,s.title,s.artist,s.duration,s.album); }}); row.setOnLongClickListener(new View.OnLongClickListener(){ public boolean onLongClick(View v){ MusicLongPressed(index,s.path); return true; }}); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,dp(10)); songList.addView(row,lp);} }

    @SimpleFunction(description="Loads a JSON array of songs with path,title,artist,duration,album,albumArt fields into the designed list.") public void LoadMusicIntoList(String json){ songs.clear(); try{ JSONArray a=new JSONArray(json); for(int i=0;i<a.length();i++){ JSONObject o=a.optJSONObject(i); if(o!=null)songs.add(new Song(o.optString("path"),o.optString("title","Unknown Title"),o.optString("artist","Unknown Artist"),o.optString("duration","0:00"),o.optString("album",""),o.optString("albumArt",""))); }}catch(Exception e){} renderSongs(songs); }
    @SimpleFunction public void DisplayMusicList(){ renderSongs(songs); } @SimpleFunction public void ClearMusicList(){ songs.clear(); renderSongs(songs); } @SimpleFunction public void RefreshMusicList(){ renderSongs(songs); }
    @SimpleFunction public void SearchMusic(String query){ searchResults.clear(); String q=query.toLowerCase(); for(Song s:songs) if(s.title.toLowerCase().contains(q)||s.artist.toLowerCase().contains(q)||s.album.toLowerCase().contains(q)) searchResults.add(s); SearchCompleted(toJson(searchResults)); UpdateDropdown(toJson(searchResults)); }
    @SimpleFunction public void DisplaySearchResult(String json){ UpdateDropdown(json); ShowDropdown(); } @SimpleFunction public void HideSearchResult(){ HideDropdown(); } @SimpleFunction public void ClearSearchResult(){ searchResults.clear(); dropdownList.removeAllViews(); }
    @SimpleFunction public void ShowDropdown(){ dropdownList.setVisibility(View.VISIBLE); } @SimpleFunction public void HideDropdown(){ dropdownList.setVisibility(View.GONE); } @SimpleFunction public void UpdateDropdown(String json){ dropdownList.removeAllViews(); try{ JSONArray a=new JSONArray(json); for(int i=0;i<a.length();i++){ JSONObject o=a.getJSONObject(i); final String path=o.optString("path"), title=o.optString("title"), artist=o.optString("artist"); TextView row=label("♪  "+title+"\n"+artist,14,false); row.setPadding(dp(14),dp(8),dp(14),dp(8)); row.setBackground(round(cardColor,16)); row.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ SearchItemClicked(path,title); }}); dropdownList.addView(row,new LinearLayout.LayoutParams(-1,-2)); }}catch(Exception e){} ShowDropdown(); }

    @SimpleFunction public void ShowSearchBar(){ searchBar.setVisibility(View.VISIBLE); } @SimpleFunction public void HideSearchBar(){ searchBar.setVisibility(View.GONE); } @SimpleFunction public void SetSearchHint(String hint){ searchBar.setHint(hint); } @SimpleFunction public void ClearSearch(){ searchBar.setText(""); }
    @SimpleProperty public String Hint(){ return searchBar.getHint().toString(); } @DesignerProperty(editorType="string", defaultValue="Search songs, artists, albums") @SimpleProperty public void Hint(String v){ searchBar.setHint(v); } @SimpleProperty public void HintColor(int c){ hintColor=c; applyTheme(); } @SimpleProperty public void TextColor(int c){ textColor=c; applyTheme(); } @SimpleProperty public void Radius(int r){ searchRadius=r; applyTheme(); }
    @SimpleFunction public void OpenPlaylistDrawer(){ playlistDrawer.setVisibility(View.VISIBLE); PlaylistOpened(); } @SimpleFunction public void ClosePlaylistDrawer(){ playlistDrawer.setVisibility(View.GONE); PlaylistClosed(); } @SimpleFunction public void OpenSidebar(){ sidebarDrawer.setVisibility(View.VISIBLE); DrawerOpened(); } @SimpleFunction public void CloseSidebar(){ sidebarDrawer.setVisibility(View.GONE); DrawerClosed(); } @SimpleFunction public void ToggleSidebar(){ if(sidebarDrawer.getVisibility()==View.VISIBLE)CloseSidebar(); else OpenSidebar(); }
    @SimpleFunction public void OpenLibraryScreen(){ NavigateTo("Library"); } @SimpleFunction public void OpenNowPlayingScreen(){ NavigateTo("NowPlaying"); } @SimpleFunction public void NavigateTo(String screen){ currentScreen=screen; boolean now=screen.toLowerCase().contains("now"); libraryScreen.setVisibility(now?View.GONE:View.VISIBLE); nowPlayingScreen.setVisibility(now?View.VISIBLE:View.GONE); ScreenChanged(screen); } @SimpleFunction public void GoBack(){ OpenLibraryScreen(); }

    @SimpleFunction public void SetAlbumArt(String path){ } @SimpleFunction public void SetSongTitle(String title){ nowTitle.setText(title); } @SimpleFunction public void SetArtist(String artist){ nowArtist.setText(artist); } @SimpleFunction public void SetBackground(int c){ backgroundColor=c; applyTheme(); } @SimpleFunction public void SetAccentColor(int c){ accentColor=c; applyTheme(); } @SimpleFunction public void SetButtonColor(int c){ buttonColor=c; applyTheme(); }
    @SimpleFunction public void SetCurrentPosition(int p){ seekBar.setProgress(p); } @SimpleFunction public void SetDuration(int d){ seekBar.setMax(d); } @SimpleFunction public void UpdateSeekbar(int p,int d){ seekBar.setMax(d); seekBar.setProgress(p); } @SimpleFunction public void SetCurrentTime(String t){ currentTime.setText(t); } @SimpleFunction public void SetDurationTime(String t){ durationTime.setText(t); }
    @SimpleFunction public int GetMusicCount(){ return songs.size(); } @SimpleFunction public String GetMusicAtIndex(int i){ return i>0&&i<=songs.size()?songs.get(i-1).toJson().toString():""; } @SimpleFunction public String GetAlbumArt(int i){ return get(i).albumArt; } @SimpleFunction public String GetArtist(int i){ return get(i).artist; } @SimpleFunction public String GetTitle(int i){ return get(i).title; } @SimpleFunction public String GetDuration(int i){ return get(i).duration; } @SimpleFunction public String GetAlbum(int i){ return get(i).album; } @SimpleFunction public String GetPath(int i){ return get(i).path; }

    @SimpleFunction public void DarkTheme(){ darkTheme=true; primaryColor=Color.rgb(18,18,18); backgroundColor=Color.rgb(12,12,12); cardColor=Color.rgb(32,32,32); textColor=Color.WHITE; applyTheme(); } @SimpleFunction public void LightTheme(){ darkTheme=false; primaryColor=Color.WHITE; backgroundColor=Color.rgb(246,246,246); cardColor=Color.WHITE; textColor=Color.rgb(25,25,25); applyTheme(); } @SimpleFunction public void SetPrimaryColor(int c){ primaryColor=c; applyTheme(); } @SimpleFunction public void SetBackgroundColor(int c){ backgroundColor=c; applyTheme(); } @SimpleFunction public void SetCardColor(int c){ cardColor=c; applyTheme(); } @SimpleFunction public void SetTextColor(int c){ textColor=c; applyTheme(); }
    @SimpleFunction public void EnableAnimation(){ animationsEnabled=true; } @SimpleFunction public void DisableAnimation(){ animationsEnabled=false; } @SimpleFunction public void SetAnimationSpeed(int ms){ animationSpeed=ms; }

    // UI-only teaching blocks: intentionally dispatch events or update visible state, never perform backend playback/storage/share work.
    @SimpleFunction public void FirstLoadMusic(){ } @SimpleFunction public void UpdateMusicDatabase(){ } @SimpleFunction public String GetStoredMusic(){ return toJson(songs); } @SimpleFunction public void ClearDatabase(){ ClearMusicList(); } @SimpleFunction public boolean DatabaseExists(){ return songs.size()>0; }
    @SimpleFunction public void LoadAllMusic(){ } @SimpleFunction public void LoadMusicFromFolder(String folder){ } @SimpleFunction public void LoadMusicFromPath(String path){ }
    @SimpleFunction public void CreatePlaylist(String name){ PlaylistCreated(name); } @SimpleFunction public void DeletePlaylist(String name){ PlaylistDeleted(name); } @SimpleFunction public void RenamePlaylist(String oldName,String newName){ } @SimpleFunction public void AddSongToPlaylist(String playlist,String path){ SongAdded(playlist,path); } @SimpleFunction public void RemoveSongFromPlaylist(String playlist,String path){ SongRemoved(playlist,path); } @SimpleFunction public void LoadPlaylist(String name){ PlaylistSelected(name); } @SimpleFunction public void DisplayPlaylist(String json){ LoadMusicIntoList(json); }
    @SimpleFunction public void PlayMusic(){ PlayClicked(); } @SimpleFunction public void PauseMusic(){ PauseClicked(); } @SimpleFunction public void ResumeMusic(){ PlayClicked(); } @SimpleFunction public void StopMusic(){ ControlButtonClicked("Stop"); } @SimpleFunction public void NextMusic(){ NextClicked(); } @SimpleFunction public void PreviousMusic(){ PreviousClicked(); } @SimpleFunction public void ShuffleMusic(){ ShuffleClicked(); } @SimpleFunction public void RepeatMusic(){ RepeatClicked(); }
    @SimpleFunction public void ShareMusic(String path){ sharePath=path; ShareClicked(path); } @SimpleFunction public void ShareToWhatsApp(String path){ ShareClicked(path); } @SimpleFunction public void ShareToFacebook(String path){ ShareClicked(path); } @SimpleFunction public void ShareToInstagram(String path){ ShareClicked(path); } @SimpleFunction public void ShareToTelegram(String path){ ShareClicked(path); } @SimpleFunction public void ShareToMessenger(String path){ ShareClicked(path); } @SimpleFunction public void ShareToSystem(String path){ ShareClicked(path); }
    @SimpleFunction public void AddToFavorites(String path){ favorites.add(path); FavoriteAdded(path); } @SimpleFunction public void RemoveFromFavorites(String path){ favorites.remove(path); FavoriteRemoved(path); } @SimpleFunction public void LoadFavorites(){ } @SimpleFunction public void DisplayFavorites(String json){ LoadMusicIntoList(json); } @SimpleFunction public void AddRecentlyPlayed(String path){ } @SimpleFunction public void LoadRecentlyPlayed(){ } @SimpleFunction public void ClearRecentlyPlayed(){ } @SimpleFunction public void AddToQueue(String path){ QueueUpdated(); } @SimpleFunction public void RemoveFromQueue(String path){ QueueUpdated(); } @SimpleFunction public void MoveQueueItem(int from,int to){ QueueUpdated(); } @SimpleFunction public void LoadQueue(){ } @SimpleFunction public void ClearQueue(){ QueueUpdated(); } @SimpleFunction public void SetDefaultAlbumArt(String path){} @SimpleFunction public void LoadAlbumArt(String path){}
    @SimpleFunction public void SetFABIcon(String path){} @SimpleFunction public void SetFABColor(int c){ fab.setBackground(round(c,32)); } @SimpleFunction public void ShowEmptyState(){ emptyStateText.setVisibility(View.VISIBLE); } @SimpleFunction public void HideEmptyState(){ emptyStateText.setVisibility(View.GONE); } @SimpleFunction public void SetEmptyStateImage(String path){} @SimpleFunction public void SetEmptyStateText(String text){ emptyStateText.setText(text); }
    @SimpleFunction public void RequestStoragePermission(){ } @SimpleFunction public void RequestAudioPermission(){ } @SimpleFunction public boolean CheckPermission(String permission){ return false; } @SimpleFunction public void PermissionGranted(String permission){ } @SimpleFunction public void PermissionDenied(String permission){ }
    @SimpleFunction public void SetLogo(String value){ logoView.setText(value); } @SimpleFunction public void SetAppTitle(String value){ titleView.setText(value); } @SimpleFunction public void SetTitleColor(int c){ titleView.setTextColor(c); } @SimpleFunction public void SetElevation(float e){ appBar.setElevation(e); }
    @SimpleFunction public void SetPlayIcon(String p){} @SimpleFunction public void SetPauseIcon(String p){} @SimpleFunction public void SetNextIcon(String p){} @SimpleFunction public void SetPreviousIcon(String p){} @SimpleFunction public void SetShuffleIcon(String p){} @SimpleFunction public void SetRepeatIcon(String p){} @SimpleFunction public void SetFavoriteIcon(String p){} @SimpleFunction public void SetPlaylistIcon(String p){} @SimpleFunction public void SetSearchIcon(String p){} @SimpleFunction public void SetSidebarIcon(String p){} @SimpleFunction public void SetAlbumPlaceholder(String p){} @SimpleFunction public void SetShareIcon(String p){} @SimpleFunction public void SetSearchIconImage(String p){} @SimpleFunction public void SetClearIcon(String p){} @SimpleFunction public void SetShareButtonImage(String p){}
    @SimpleFunction public void SetPlayButton(String p){} @SimpleFunction public void SetPauseButton(String p){} @SimpleFunction public void SetPreviousButton(String p){} @SimpleFunction public void SetNextButton(String p){} @SimpleFunction public void SetShuffleButton(String p){} @SimpleFunction public void SetRepeatButton(String p){} @SimpleFunction public void SetFavoriteButton(String p){} @SimpleFunction public void SetQueueButton(String p){} @SimpleFunction public void SetShareButton(String p){}

    @SimpleEvent public void SearchTextChanged(String searchText){ EventDispatcher.dispatchEvent(this,"SearchTextChanged",searchText); SearchMusic(searchText); } @SimpleEvent public void SearchCompleted(String resultList){ EventDispatcher.dispatchEvent(this,"SearchCompleted",resultList); } @SimpleEvent public void SearchItemClicked(String musicPath,String songName){ EventDispatcher.dispatchEvent(this,"SearchItemClicked",musicPath,songName); }
    @SimpleEvent public void MusicClicked(int musicIndex,String musicPath,String songName,String artist,String duration,String album){ sharePath=musicPath; EventDispatcher.dispatchEvent(this,"MusicClicked",musicIndex,musicPath,songName,artist,duration,album); EventDispatcher.dispatchEvent(this,"MusicSelected",musicIndex,musicPath); } @SimpleEvent public void MusicLongPressed(int index,String path){ EventDispatcher.dispatchEvent(this,"MusicLongPressed",index,path); } @SimpleEvent public void ShareIconClicked(String path){ EventDispatcher.dispatchEvent(this,"ShareIconClicked",path); } @SimpleEvent public void PlaylistIconClicked(String path){ EventDispatcher.dispatchEvent(this,"PlaylistIconClicked",path); } @SimpleEvent public void FavoriteClicked(String path){ EventDispatcher.dispatchEvent(this,"FavoriteClicked",path); }
    @SimpleEvent public void PlaylistOpened(){ EventDispatcher.dispatchEvent(this,"PlaylistOpened"); } @SimpleEvent public void PlaylistClosed(){ EventDispatcher.dispatchEvent(this,"PlaylistClosed"); } @SimpleEvent public void DrawerOpened(){ EventDispatcher.dispatchEvent(this,"DrawerOpened"); } @SimpleEvent public void DrawerClosed(){ EventDispatcher.dispatchEvent(this,"DrawerClosed"); } @SimpleEvent public void SidebarItemClicked(String item){ EventDispatcher.dispatchEvent(this,"SidebarItemClicked",item); }
    @SimpleEvent public void PlaylistCreated(String n){ EventDispatcher.dispatchEvent(this,"PlaylistCreated",n); } @SimpleEvent public void PlaylistDeleted(String n){ EventDispatcher.dispatchEvent(this,"PlaylistDeleted",n); } @SimpleEvent public void SongAdded(String p,String path){ EventDispatcher.dispatchEvent(this,"SongAdded",p,path); } @SimpleEvent public void SongRemoved(String p,String path){ EventDispatcher.dispatchEvent(this,"SongRemoved",p,path); } @SimpleEvent public void PlaylistSelected(String n){ EventDispatcher.dispatchEvent(this,"PlaylistSelected",n); }
    @SimpleEvent public void PlayClicked(){ EventDispatcher.dispatchEvent(this,"PlayClicked"); } @SimpleEvent public void PauseClicked(){ EventDispatcher.dispatchEvent(this,"PauseClicked"); } @SimpleEvent public void NextClicked(){ EventDispatcher.dispatchEvent(this,"NextClicked"); } @SimpleEvent public void PreviousClicked(){ EventDispatcher.dispatchEvent(this,"PreviousClicked"); } @SimpleEvent public void ShuffleClicked(){ EventDispatcher.dispatchEvent(this,"ShuffleClicked"); } @SimpleEvent public void RepeatClicked(){ EventDispatcher.dispatchEvent(this,"RepeatClicked"); } @SimpleEvent public void QueueClicked(){ EventDispatcher.dispatchEvent(this,"QueueClicked"); } @SimpleEvent public void ShareClicked(String path){ EventDispatcher.dispatchEvent(this,"ShareClicked",path); } @SimpleEvent public void ControlButtonClicked(String name){ EventDispatcher.dispatchEvent(this,"ControlButtonClicked",name); } @SimpleEvent public void SeekChanged(int position){ EventDispatcher.dispatchEvent(this,"SeekChanged",position); } @SimpleEvent public void ScreenChanged(String screen){ EventDispatcher.dispatchEvent(this,"ScreenChanged",screen); } @SimpleEvent public void FABClicked(){ EventDispatcher.dispatchEvent(this,"FABClicked"); } @SimpleEvent public void FavoriteAdded(String p){ EventDispatcher.dispatchEvent(this,"FavoriteAdded",p); } @SimpleEvent public void FavoriteRemoved(String p){ EventDispatcher.dispatchEvent(this,"FavoriteRemoved",p); } @SimpleEvent public void QueueUpdated(){ EventDispatcher.dispatchEvent(this,"QueueUpdated"); }

    private TextView label(String t,int sp,boolean bold){ TextView v=new TextView(container.$context()); v.setText(t); v.setTextSize(sp); v.setTextColor(textColor); if(bold)v.setTypeface(Typeface.DEFAULT_BOLD); return v; }
    private ImageButton icon(String t, View.OnClickListener l){ ImageButton b=new ImageButton(container.$context()); b.setBackground(round(Color.TRANSPARENT,24)); b.setContentDescription(t); b.setOnClickListener(l); return b; }
    private TextView nav(final String t){ TextView v=label(t,14,true); v.setGravity(Gravity.CENTER); v.setOnClickListener(new View.OnClickListener(){ public void onClick(View view){ NavigateTo(t); }}); v.setLayoutParams(new LinearLayout.LayoutParams(0,-1,1)); return v; }
    private LinearLayout drawer(String title,String[] items){ LinearLayout d=new LinearLayout(container.$context()); d.setOrientation(LinearLayout.VERTICAL); d.setPadding(dp(16),dp(18),dp(16),dp(18)); d.setBackground(round(cardColor,0)); d.addView(label(title,20,true)); for(final String item:items){ TextView row=label(item,15,false); row.setPadding(0,dp(14),0,dp(14)); row.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ SidebarItemClicked(item); }}); d.addView(row); } return d; }
    private FrameLayout.LayoutParams drawerParams(int gravity){ FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(dp(280),-1,gravity); return lp; }
    private GradientDrawable round(int color,int radius){ GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(radius)); return g; }
    private void applyTheme(){ root.setBackgroundColor(backgroundColor); shell.setBackgroundColor(backgroundColor); appBar.setBackgroundColor(primaryColor); titleView.setTextColor(textColor); logoView.setTextColor(accentColor); searchBar.setTextColor(textColor); searchBar.setHintTextColor(hintColor); searchBar.setBackground(round(cardColor,searchRadius)); bottomNav.setBackgroundColor(primaryColor); fab.setBackground(round(accentColor,32)); }
    private int dp(int v){ return (int)(v*container.$context().getResources().getDisplayMetrics().density+0.5f); }
    private Song get(int i){ return i>0&&i<=songs.size()?songs.get(i-1):new Song("","","","","",""); }
    private String toJson(ArrayList<Song> list){ JSONArray a=new JSONArray(); for(Song s:list)a.put(s.toJson()); return a.toString(); }
    private static class Song{ String path,title,artist,duration,album,albumArt; Song(String p,String t,String ar,String d,String al,String aa){path=p;title=t;artist=ar;duration=d;album=al;albumArt=aa;} JSONObject toJson(){ JSONObject o=new JSONObject(); try{o.put("path",path);o.put("title",title);o.put("artist",artist);o.put("duration",duration);o.put("album",album);o.put("albumArt",albumArt);}catch(Exception e){} return o; }}
}
