package net.masonapps.mediaplayervr.media;

import android.net.Uri;

/**
 * Created by Bob on 12/24/2016.
 */

public class SongDetails extends MediaDetails {
    public long id;
    public Uri uri;
    public ArtistDetails artistDetails;
    public AlbumDetails albumDetails;

    public SongDetails() {
        artistDetails = new ArtistDetails();
        albumDetails = new AlbumDetails();
    }
}
