package android.backport.webp;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

/**
 * Factory to encode and decode WebP images into Android Bitmap
 * @author Alexey Pelykh
 */
public final class WebPFactory {
	
	private static boolean mLoaded;
	
	// Load library at class loading
	static {
		try {
			System.loadLibrary("webpbackport");
			mLoaded = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	};
	
	public static Bitmap decode(File file) {
		Bitmap bitmap = null;
		
		if (Build.VERSION.SDK_INT < 17 && mLoaded) {
			int length = (int) file.length();
			DataInputStream is = null;
			try {
				byte[] data = new byte[length];
				is = new DataInputStream(new FileInputStream(file));
				is.readFully(data);
				is.close();
				
				if (isWebP(data)) {
					bitmap = nativeDecodeByteArray(data, null);
				}
			} catch (Exception e) {
			} catch (Error e) {
			} finally {
				try {
					if (is != null) {
						is.close();
					}
				} catch (Exception e) {
				}
			}
		}
		
		return bitmap;
	}
	
    /**
     * Verify bitmap's format
     * 
     * @param data
     * @return
     */
    public static boolean isWebP(byte[] data) {
        return data != null && data.length > 12 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P';
    }

	/**
	 * Decodes byte array to bitmap 
	 * @param data Byte array with WebP bitmap data
	 * @param opts Options to control decoding. Accepts null
	 * @return Decoded bitmap
	 */
	public static native Bitmap nativeDecodeByteArray(byte[] data, BitmapFactory.Options options);
	
	/**
	 * Encodes bitmap into byte array
	 * @param bitmap Bitmap
	 * @param quality Quality, should be between 0 and 100
	 * @return Encoded byte array
	 */
	public static native byte[] nativeEncodeBitmap(Bitmap bitmap, int quality);
	
}
