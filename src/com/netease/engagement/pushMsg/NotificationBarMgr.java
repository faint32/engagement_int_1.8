package com.netease.engagement.pushMsg;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityHome;
import com.netease.engagement.activity.ActivityPrivateSession;
import com.netease.engagement.app.EgmConstants;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmProtocolConstants.MSG_TYPE;
import com.netease.service.protocol.meta.ChatItemUserInfo;
import com.netease.service.protocol.meta.MsgExtra;
import com.netease.util.MediaPlayerSystemTone;

public class NotificationBarMgr {
	public static final int NOTIFICATION_TYPE_CHAT = 100;// 聊天消息及活动之外的系统消息
	public static final int NOTIFICATION_ACTIVITIES = 101; // 系统消息：产品活动（特殊交互，单独提出来）

	private static NotificationBarMgr sInstance;

	public static NotificationBarMgr getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new NotificationBarMgr(context);
		}

		return sInstance;
	}

	private Context mContext;
	private NotificationManager mNotificationManager;

	private NotificationBarMgr(Context context) {
		mContext = context;
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	/**
	 * 阅后即焚截屏系统消息
	 * 点击notification后，会直接跳到对应人的聊天页面，不是跳聊天列表页面
	 * @param anotherUserInfo
	 * 		对方个人信息
	 */
	public void showPushScreenShot(ChatItemUserInfo anotherUserInfo) {
		if (anotherUserInfo != null) {
			// 使用uid作为notification的id   long强制转换成int，在实际使用中，两个不同的long值转换后变成同一个int值的概率不大
			int uid = (int) anotherUserInfo.uid; 
			mNotificationManager.cancel(uid);
			Intent sendIntent = new Intent();
			sendIntent.setClass(mContext, ActivityPrivateSession.class);
			sendIntent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_ITEM_USER_INFO, anotherUserInfo);
			sendIntent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_FROM_SCREEN_SHOT_NOTIFICATION, true);
			sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			String content = "系统监测到" + anotherUserInfo.nick + "对你的[阅后即焚]消息截屏";
			String tickerText = content;
			String contentTitle = mContext.getResources().getString(R.string.app_name);
			notification(contentIntent, contentTitle, content, tickerText, EgmPrefHelper.getSoundOn(mContext), EgmPrefHelper.getShockOn(mContext), uid, System.currentTimeMillis());
		
			EgmPrefHelper.putSessionHasScreenShoteFlag(mContext, uid, true);
		}
	}

	/**
	 * 产品活动（属于系统消息，因特殊交互，单独提出来）
	 * 
	 * @param content
	 *            活动内容
	 * @param extra
	 *            产品活动的扩展内容
	 */
	public void showPushActivities(String content, MsgExtra extra) {
		if (extra != null) {
			// 把之前的cancel掉，这样才能更新tickerText
			mNotificationManager.cancel(NOTIFICATION_ACTIVITIES);
			Intent sendIntent = new Intent();
			sendIntent.setClass(mContext, ActivityHome.class);
			sendIntent.setAction(String.valueOf(EgmConstants.NOTIFICATION_ACTIVITIES));
			sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			sendIntent.putExtra(EgmConstants.BUNDLE_KEY.PUSH_ACTIVITY_CONTENT, content);
			sendIntent.putExtra(EgmConstants.BUNDLE_KEY.PUSH_ACTIVITY_EXTRA, extra);

			PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			String tickerText = extra.title;
			String contentTitle = mContext.getResources().getString(R.string.app_name);
			notification(contentIntent, contentTitle, content, tickerText, EgmPrefHelper.getSoundOn(mContext), EgmPrefHelper.getShockOn(mContext), NOTIFICATION_ACTIVITIES, System.currentTimeMillis());
		}
	}

	/**
	 * 聊天消息及产品活动之外的系统消息
	 * 
	 * @param user
	 *            最后一条消息发送者信息
	 * @param msgType
	 *            最后一条消息类型
	 * @param chatCount
	 *            拥有未读消息的会话数（人数）
	 * @param unreadCount
	 *            未读消息总数
	 * @param extra
	 *            附加内容（系统消息时直接显示该内容）
	 */
	public void showPushMessage(ChatItemUserInfo user, int msgType, int chatCount,
			int unreadCount, String extra, boolean fire) {
		String content = null;
		String contentTitle = null;
		int chatType = 0;
		if (chatCount > 1) {
			chatType = EgmConstants.NOTIFICATION_CHAT_TYPE_MULTI;
			contentTitle = mContext.getResources().getString(R.string.app_name);
			content = mContext.getResources().getString(R.string.rec_msg_multi, chatCount, unreadCount);
		} else {
			chatType = EgmConstants.NOTIFICATION_CHAT_TYPE_ONE;
			contentTitle = user.nick;
			if (unreadCount > 1) {
				content = mContext.getResources().getString(R.string.rec_msg_count, unreadCount);
			} else {
				int contentResId = 0;
				switch (msgType) {
				case MSG_TYPE.MSG_TYPE_TEXT:
					contentResId = R.string.rec_msg_type_text;
					break;
				case MSG_TYPE.MSG_TYPE_AUDIO:
					contentResId = R.string.rec_msg_type_audio;
					break;
				case MSG_TYPE.MSG_TYPE_VIDEO:
					contentResId = R.string.rec_msg_type_video;
					break;
				case MSG_TYPE.MSG_TYPE_GIFT:
					contentResId = R.string.rec_msg_type__gift;
					break;
				case MSG_TYPE.MSG_TYPE_LOCAL_PIC:
					contentResId = R.string.rec_msg_type_local_pic;
					break;
				case MSG_TYPE.MSG_TYPE_PRIVATE_PIC:
					contentResId = R.string.rec_msg_type_private_pic;
					break;
				case MSG_TYPE.MSG_TYPE_FACE:
					contentResId = R.string.rec_msg_type_face;
					break;
				case MSG_TYPE.MSG_TYPE_SYS:
					content = extra;
					break;
				}

				if (fire) {
					content = mContext.getResources().getString(
							R.string.rec_msg_fire);
				}
				else if (null == content && contentResId > 0) {
					content = mContext.getResources().getString(contentResId);
				}
			}
		}

		// 把之前的cancel掉，这样才能更新tickerText
		mNotificationManager.cancel(NOTIFICATION_TYPE_CHAT);
		Intent sendIntent = new Intent();
		sendIntent.setClass(mContext, ActivityHome.class);
		sendIntent.setAction(String.valueOf(chatType));
		if (chatType == EgmConstants.NOTIFICATION_CHAT_TYPE_ONE) {
			sendIntent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_ITEM_USER_INFO, user);
		}
		sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		String tickerText = user.nick + mContext.getResources().getString(R.string.rec_msg_count, 1);
		notification(contentIntent, contentTitle, content, tickerText, EgmPrefHelper.getSoundOn(mContext), EgmPrefHelper.getShockOn(mContext), NOTIFICATION_TYPE_CHAT, System.currentTimeMillis());
	}

	private void notification(PendingIntent pendingIntent, String title, String content, String tickerText, boolean ring, boolean vibrate, int id, long when) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
		mBuilder.setContentTitle(title).setContentText(content).setSmallIcon(R.drawable.icon_statusbar).setAutoCancel(true).setLights(0x0000FF00, 500, 500).setContentIntent(pendingIntent).setTicker(tickerText).setWhen(when);

		if (ring) {
			// int defaults = Notification.DEFAULT_SOUND;
			// mBuilder.setDefaults(defaults);
			MediaPlayerSystemTone.instance(mContext).playWelecomTone("date_push.mp3");
		}
		if (vibrate) {
			long[] pattern = { 0, 120 };
			mBuilder.setVibrate(pattern);
		}

		mNotificationManager.notify(id, mBuilder.build());
	}

	public void cancleAll() {
		mNotificationManager.cancelAll();
	}

	public void cancelPushAll() {
		mNotificationManager.cancel(NOTIFICATION_TYPE_CHAT);
		mNotificationManager.cancel(NOTIFICATION_ACTIVITIES);
	}

	public void cancelPushChat() {
		mNotificationManager.cancel(NOTIFICATION_TYPE_CHAT);
	}
	
	public void cancelScreenShot(int uid) {
		mNotificationManager.cancel(uid);
	}
}
