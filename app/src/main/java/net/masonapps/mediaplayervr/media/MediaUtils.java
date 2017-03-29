package net.masonapps.mediaplayervr.media;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 1/12/2017.
 */

public class MediaUtils {

    public static final int SAMPLE_SIZE = 1;

    public static List<VideoDetails> getVideoList(Context context) {
        ArrayList<VideoDetails> list = new ArrayList<>();
        final String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.WIDTH, MediaStore.Video.Media.HEIGHT, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.TAGS};
        Cursor c = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Video.Media.DISPLAY_NAME + " ASC");
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    do {
                        final VideoDetails videoDetails = new VideoDetails();
                        videoDetails.id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                        videoDetails.uri = Uri.parse(c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
                        videoDetails.title = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                        videoDetails.width = c.getInt(c.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH));
                        videoDetails.height = c.getInt(c.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT));
                        videoDetails.duration = c.getInt(c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                        videoDetails.tags = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.TAGS));
                        list.add(videoDetails);
                    } while (c.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }
        return list;
    }

    public static List<ImageDetails> getImageList(Context context) {
        ArrayList<ImageDetails> list = new ArrayList<>();
        final String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT, MediaStore.Images.Media.DATE_TAKEN};
        Cursor c = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Video.Media.DATE_TAKEN + " DSC");
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    do {
                        final ImageDetails imageDetails = new ImageDetails();
                        imageDetails.id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                        imageDetails.uri = Uri.parse(c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
                        imageDetails.title = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                        imageDetails.width = c.getInt(c.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH));
                        imageDetails.height = c.getInt(c.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT));
                        list.add(imageDetails);
                    } while (c.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }
        return list;
    }

    public static Bitmap getVideoThumbnailBitmap(Context context, long id) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = SAMPLE_SIZE;
        return MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Video.Thumbnails.MINI_KIND, options);
    }

    public static Bitmap getImageThumbnailBitmap(Context context, long id) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = SAMPLE_SIZE;
        return MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, options);
    }

    public static List<SongDetails> getSongList(Context context, String selection) {
        ArrayList<SongDetails> list = new ArrayList<>();
        final String[] projection = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID};
        Cursor c = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, MediaStore.Audio.Media.TITLE + " ASC");
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    do {
                        final SongDetails songDetails = new SongDetails();
                        songDetails.id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                        songDetails.albumDetails.title = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                        songDetails.albumDetails.albumId = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                        songDetails.artistDetails.title = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        songDetails.artistDetails.artistId = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
                        songDetails.title = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        songDetails.uri = Uri.parse(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
//                        songDetails.thumbnailPath = getAlbumThumbnailPath(context, songDetails.albumDetails.albumId);
                        list.add(songDetails);
                    } while (c.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }
        return list;
    }

    public static List<ArtistDetails> getArtistList(Context context) {
        ArrayList<ArtistDetails> list = new ArrayList<>();
        final String[] projection = {MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST};
        Cursor c = context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Audio.Artists.ARTIST + " ASC");
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    do {
                        final ArtistDetails artistDetails = new ArtistDetails();
                        artistDetails.artistId = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID));
                        artistDetails.title = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST));
//                        artistDetails.thumbnailPath = getArtistThumbnailPath(context, artistDetails.artistId);
                        list.add(artistDetails);
                    } while (c.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }
        return list;
    }

    public static List<AlbumDetails> getAlbumList(Context context) {
        ArrayList<AlbumDetails> list = new ArrayList<>();
        final String[] projection = {MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ALBUM_ART};
        Cursor c = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Audio.Albums.ALBUM + " ASC");
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    do {
                        final AlbumDetails albumDetails = new AlbumDetails();
                        albumDetails.albumId = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID));
                        albumDetails.title = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
                        albumDetails.thumbnailPath = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                        list.add(albumDetails);
                    } while (c.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }
        return list;
    }

    @Nullable
    public static String getVideoThumbnailPath(Context context, long id) {
        String path = null;
        Cursor c = context.getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Video.Thumbnails.VIDEO_ID, MediaStore.Video.Thumbnails.DATA}, MediaStore.Video.Thumbnails.VIDEO_ID + "=" + id, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    path = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }
        Log.d(MediaUtils.class.getSimpleName(), "getVideoThumbnailPath -> " + (path == null ? "null" : path));
        return path;
    }

    @Nullable
    public static String getAlbumThumbnailPath(Context context, long albumId) {
        String path = null;
        Cursor c = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Albums.ALBUM_ART, MediaStore.Audio.Albums._ID}, MediaStore.Audio.Albums._ID + " = " + albumId, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }
        return path;
    }

    @Nullable
    public static String getArtistThumbnailPath(Context context, long artistId) {
        long albumId = -1;
        Cursor c = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ARTIST_ID}, MediaStore.Audio.Media.ARTIST_ID + " = " + artistId, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    albumId = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }
        if (albumId == -1)
            return null;
        else
            return getAlbumThumbnailPath(context, albumId);
    }
}
