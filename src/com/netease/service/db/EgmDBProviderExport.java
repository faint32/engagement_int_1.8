
package com.netease.service.db;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.netease.common.cache.CacheManager;

/**
 * Provider的方式来管理拍照、录音等文件，解决文件路径方式无法在没有sd卡的设备上使用的问题
 */

public class EgmDBProviderExport extends ContentProvider {

    public static final String AUTHORITY_EXPORT = "com.netease.date.export";

    private static final int MATCH_PHOTO_CAPTURE = 1 ;
    private static final int MATCH_RECORD_AUDIO = 2 ;
    
    private static final UriMatcher sUriMatcher;
    
    public static final int TYPE_BASE = 0;
    public static final int TYPE_PREVIEW  = TYPE_BASE;
    public static final int TYPE_CAMERA   = TYPE_BASE + 1;
    public static final int TYPE_RECORDER_AUDIO = TYPE_BASE + 2;

    public static final class CapturePhoto {
        public static String PATH = "path";
        public static final String TABLE_NAME = "photocapture";
    }
    
    public static final class RecordAudio {
        public static String PATH = "path";
        public static final String TABLE_NAME = "recordaudio";
    }
    
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY_EXPORT, CapturePhoto.TABLE_NAME + "/#", MATCH_PHOTO_CAPTURE);
        sUriMatcher.addURI(AUTHORITY_EXPORT, RecordAudio.TABLE_NAME + "/#", MATCH_RECORD_AUDIO);
    }

    public static Uri getUri(int type, String id) {
    	switch (type) {
			case TYPE_CAMERA:
				return Uri.parse("content://" + AUTHORITY_EXPORT + "/" + CapturePhoto.TABLE_NAME + "/" + id);
			case MATCH_RECORD_AUDIO:
				return Uri.parse("content://" + AUTHORITY_EXPORT + "/" + RecordAudio.TABLE_NAME + "/" + id);
		}
    	return null;
    }

    public static String getPathFromUri(int type, Uri uri) {
        String path = null;
        String id = uri.getLastPathSegment();
        path = getFilePathByType(type, id);
        return path;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        int match = sUriMatcher.match(url);
        switch (match) {
            case MATCH_PHOTO_CAPTURE:
                File photo = new File(getPathFromUri(TYPE_CAMERA, url));
                if (photo.exists())
                    photo.delete();
                break;
            default: {
                throw new UnsupportedOperationException("Cannot delete that URL: " + url);
            }
        }

        return 0;
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        return null;
    }

    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection, String[] selectionArgs,
            String sort) {
        int match = sUriMatcher.match(url);
        MatrixCursor mc = null;
        switch (match) {
            case MATCH_PHOTO_CAPTURE: {
                String[] tableCursor = new String[] {
                    CapturePhoto.PATH
                };
                mc = new MatrixCursor(tableCursor);
                mc.addRow(new Object[] {
                    getPathFromUri(TYPE_CAMERA, url)
                });
                return mc;
            }
            
            case MATCH_RECORD_AUDIO: {
                String[] tableCursor = new String[] {
                    CapturePhoto.PATH
                };
                mc = new MatrixCursor(tableCursor);
                mc.addRow(new Object[] {
                    getPathFromUri(TYPE_RECORDER_AUDIO, url)
                });
                return mc;
            }
            default:
                return null;
        }

    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MATCH_PHOTO_CAPTURE:
                return openPhotoCaptureFile(uri, mode);
            case MATCH_RECORD_AUDIO:
                return openRecordFile(uri, mode);
            default:
                break;
        }

        return null;
    }

    private ParcelFileDescriptor openPhotoCaptureFile(Uri uri, String mode) {
        try {
            int bitMode = modeToMode(uri, mode);
            return ParcelFileDescriptor.open(new File(getPathFromUri(TYPE_CAMERA, uri)), bitMode);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private ParcelFileDescriptor openRecordFile(Uri uri, String mode) {
        try {
            int bitMode = modeToMode(uri, mode);
            return ParcelFileDescriptor.open(new File(getPathFromUri(TYPE_RECORDER_AUDIO, uri)), bitMode);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int modeToMode(Uri uri, String mode) throws FileNotFoundException {
        int modeBits;
        if ("r".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
        } else if ("w".equals(mode) || "wt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else if ("wa".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_APPEND;
        } else if ("rw".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE;
        } else if ("rwt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else {
            throw new FileNotFoundException("Bad mode for " + uri + ": " + mode);
        }
        
        return modeBits;
    }
    
    public static String getFilePathByType(int type, String id) {
        String filePath = null;
        File file = null; 
        String path = null;
        path = CacheManager.getRoot();
        file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        path = file.toString() + File.separator;
        if (!new File(path).exists()) {
            new File(path).mkdirs();
        }
        
        if (TYPE_PREVIEW == type) {
            filePath = path + "image_preview_"+ id + ".png";
        } else if (TYPE_CAMERA == type) {
            filePath = path + "pic_camera_"+ id + ".png";
        } else if (TYPE_RECORDER_AUDIO == type) {
            filePath = path + "media_recorder_"+ id + ".amr";
        }
        return filePath;
    }
}
