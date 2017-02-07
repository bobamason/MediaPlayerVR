package net.masonapps.mediaplayervr.utils;

import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by Bob on 4/29/2016.
 */
public class Constants {

    public static final String EXTRA_MUSIC_URI = "musicUri";
    public static final Uri MUSIC_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    public static final Uri ARTIST_URI = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
    public static final Uri ALBUMS_URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
    public static final Uri ALBUM_ART_URI = Uri
            .parse("content://media/external/audio/albumart");
    public static final String EXTRA_SONG_ACTIVITY_BUNDLE = "songBundle";
    public static final String KEY_QUERY_SELECTION = "query";
    public static final String KEY_TITLE = "title";
    public static final String KEY_ALBUM_COVER_URI = "albumCover";
    public static final float LIST_COLUMN_WIDTH = 140f;
}
