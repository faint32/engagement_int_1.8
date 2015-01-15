package com.netease.android.video.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.netease.android.util.PhotoPickUtils;

public class VideoEditUtil {
	public static final int THOUSAND_INT = 1000;

	public static float stitchMovies(File[] files) {
		float lengthInSeconds = 0.0F;
		try {
			Movie[] inMovies = new Movie[files.length - 1];
			for (int i = 0; i < files.length - 1; i++) {
				inMovies[i] = MovieCreator.build(files[i].getAbsolutePath());
			}

			List<Track> videoTracks = new LinkedList<Track>();
			List<Track> audioTracks = new LinkedList<Track>();

			for (Movie m : inMovies) {
				for (Track t : m.getTracks()) {
					if (t.getHandler().equals("soun")) {
						audioTracks.add(t);
					}
					if (t.getHandler().equals("vide")) {
						videoTracks.add(t);
					}
				}
			}

			Movie result = new Movie();

			if (audioTracks.size() > 0) {
				result.addTrack(new AppendTrack(audioTracks
						.toArray(new Track[audioTracks.size()])));
			}

			if (videoTracks.size() > 0) {
				result.addTrack(new AppendTrack(videoTracks
						.toArray(new Track[videoTracks.size()])));
			}

			Container out = new DefaultMp4Builder().build(result);

			FileChannel fc = new RandomAccessFile(files[(files.length - 1)],
					"rw").getChannel();
			out.writeContainer(fc);
			fc.close();
			IsoFile isoFile = new IsoFile(
					files[(files.length - 1)].getAbsolutePath());
			long duration = isoFile.getMovieBox().getMovieHeaderBox()
					.getDuration();
			long timescale = isoFile.getMovieBox().getMovieHeaderBox()
					.getTimescale();
			if ((duration > 0L) && (timescale > 0L)) {
				lengthInSeconds = (float) duration / (float) timescale;
			}
			isoFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lengthInSeconds;
	}

	/**
	 * 截取视频(长度，时间)
	 * 
	 * @param srcPath
	 * @param dstPath
	 * @param startMs
	 * @param endMs
	 * @throws IOException
	 */
	public static void shorten(String srcPath, String dstPath, int startMs,
			int endMs) throws IOException {

		Movie movie = MovieCreator.build(srcPath);

		// remove all tracks we will create new tracks from the old
		List<Track> tracks = movie.getTracks();
		movie.setTracks(new LinkedList<Track>());
		// for (Track track : tracks) {
		// printTime(track);
		// }

		double startTime = startMs / 1000;
		double endTime = endMs / 1000;

		boolean timeCorrected = false;

		// Here we try to find a track that has sync samples. Since we can only
		// start decoding
		// at such a sample we SHOULD make sure that the start of the new
		// fragment is exactly
		// such a frame
		for (Track track : tracks) {
			if (track.getSyncSamples() != null
					&& track.getSyncSamples().length > 0) {
				if (timeCorrected) {
					// This exception here could be a false positive in case we
					// have multiple tracks
					// with sync samples at exactly the same positions. E.g. a
					// single movie containing
					// multiple qualities of the same video (Microsoft Smooth
					// Streaming file)

					throw new RuntimeException(
							"The startTime has already been corrected by another track with SyncSample. Not Supported.");
				}
				startTime = correctTimeToSyncSample(track, startTime, false);// true
				endTime = correctTimeToSyncSample(track, endTime, true);// false
				timeCorrected = true;
			}
		}
		// System.out.println("trim startTime-->" + startTime);
		// System.out.println("trim endTime-->" + endTime);
		// int x = 0;
		for (Track track : tracks) {
			long currentSample = 0;
			double currentTime = 0;
			long startSample = -1;
			long endSample = -1;
			// x++;
			for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
				TimeToSampleBox.Entry entry = track.getDecodingTimeEntries()
						.get(i);
				for (int j = 0; j < entry.getCount(); j++) {
					// entry.getDelta() is the amount of time the current sample
					// covers.

					if (currentTime <= startTime) {
						// current sample is still before the new starttime
						startSample = currentSample;
					}
					if (currentTime <= endTime) {
						// current sample is after the new start time and still
						// before the new endtime
						endSample = currentSample;
					} else {
						// current sample is after the end of the cropped video
						break;
					}
					currentTime += (double) entry.getDelta()
							/ (double) track.getTrackMetaData().getTimescale();
					currentSample++;
				}
			}

			// System.out.println("trim startSample-->" + startSample);
			// System.out.println("trim endSample-->" + endSample);
			movie.addTrack(new CroppedTrack(track, startSample, endSample));
			break;
		}
		// movie.addTrack(new CroppedTrack(track, startSample, endSample));

		// IsoFile out = (IsoFile) new DefaultMp4Builder().build(movie);
		Container container = new DefaultMp4Builder().build(movie);

		File dst = new File(dstPath);
		if (!dst.exists()) {
			dst.createNewFile();
		}

		FileOutputStream fos = new FileOutputStream(dst);
		FileChannel fc = fos.getChannel();
		// out.getBox(fc); // This one build up the memory.
		container.writeContainer(fc);

		fc.close();
		fos.close();
		// randomAccessFile.close();
	}

	private static double correctTimeToSyncSample(Track track, double cutHere,
			boolean next) {
		double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
		long currentSample = 0;
		double currentTime = 0;
		for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
			TimeToSampleBox.Entry entry = track.getDecodingTimeEntries().get(i);
			for (int j = 0; j < entry.getCount(); j++) {
				if (Arrays.binarySearch(track.getSyncSamples(),
						currentSample + 1) >= 0) {
					// samples always start with 1 but we start with zero
					// therefore +1
					timeOfSyncSamples[Arrays.binarySearch(
							track.getSyncSamples(), currentSample + 1)] = currentTime;
				}
				currentTime += (double) entry.getDelta()
						/ (double) track.getTrackMetaData().getTimescale();
				currentSample++;
			}
		}
		double previous = 0;
		for (double timeOfSyncSample : timeOfSyncSamples) {
			if (timeOfSyncSample > cutHere) {
				if (next) {
					return timeOfSyncSample;
				} else {
					return previous;
				}
			}
			previous = timeOfSyncSample;
		}
		return timeOfSyncSamples[timeOfSyncSamples.length - 1];
	}

	/**
	 * 获取视频截图
	 * 
	 * @param position
	 * @param border
	 * @return
	 */
	public static Bitmap getScaleVideoFrame(String path, long position,
			int height, int width) {
		Bitmap bitmap = getVideoFrame(position, path);
		bitmap = PhotoPickUtils.scaleCrop(bitmap, height, width, false);
		return bitmap;
	}

	/**
	 * 获取视频截图,要自己释放retriever
	 * 
	 * @param time
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	public static Bitmap getVideoFrame(long position, String filePath,MediaMetadataRetriever retriever) {
		Bitmap bitmap = null;
		try {
			retriever.setDataSource(filePath);
			if (position <= 0) {
				bitmap = retriever.getFrameAtTime(MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
				if (bitmap == null) {
					bitmap = retriever.getFrameAtTime(MediaMetadataRetriever.OPTION_NEXT_SYNC);
				}
				if (bitmap == null) {
					bitmap = retriever.getFrameAtTime(MediaMetadataRetriever.OPTION_PREVIOUS_SYNC);
				}
			} else {
				bitmap = retriever.getFrameAtTime(position,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
				if (bitmap == null) {
					bitmap = retriever.getFrameAtTime(position,MediaMetadataRetriever.OPTION_NEXT_SYNC);
				}
				if (bitmap == null) {
					bitmap = retriever.getFrameAtTime(position,MediaMetadataRetriever.OPTION_PREVIOUS_SYNC);
				}
			}
			
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
		}
		
		return bitmap;
	}
	/**
	 * 获取视频截图 
	 * 
	 * @param time
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	public static Bitmap getVideoFrame(long position, String filePath) {
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		Bitmap bitmap = getVideoFrame(position, filePath, retriever);
		try {
			retriever.release();
		} catch (Exception e) {
		}		
		return bitmap;
	}

	/**
	 * 获得视频总时间
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	 @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	public static long getDuration(String path) {
		MediaMetadataRetriever mmRetriever = new MediaMetadataRetriever();
		mmRetriever.setDataSource(path);
		String duration = mmRetriever
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		mmRetriever.release();
		
		return Long.valueOf(duration);
	}
}