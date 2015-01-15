package com.netease.service.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 消息数据库表的名称及字段定义
 * @author echo_chen
 * @since  2014-05-20
 */

public class MsgDBTables {
    public static final String AUTHORITY = MsgDBProvider.AUTHORITY;
    public static final String[] TableNames = new String[]{
        MsgTable.TABLE_NAME,
        LastMsgTable.TABLE_NAME
    };
    
    
    /********************************************************************
     * 消息表
     *******************************************************************/
    public static interface MsgTable {

        public static final String TABLE_NAME = "msg";
        public static final Uri CONTENT_URI = Uri
                .parse("content://" + AUTHORITY + "/" + TABLE_NAME);

        /** ===================数据库字段================== */
        // long 消息id
        public static final String C_MSGID = "msgId";
        // long 聊天对像用户id
        public static final String C_ANOTHERID = "anotherId";
        // long 发出消息用户id
        public static final String C_FROMID = "fromId";
        // int 消息类型 0：文本； 1：私照 ；2：本地照片；3：音频；4：视频；5：礼物；6：系统消息 7:表情
        public static final String C_TYPE = "type";
        // string 消息文本内容或者是视频截图url
        public static final String C_CONTENT = "content";
        // int 消息状态（发送失败、已下载、未下载等状态）
        public static final String C_STATUS = "status";
        // long 消息时间
        public static final String C_TIME  = "time"; 
        // string 图片、音频、视频、礼物、私照地址
        public static final String C_MEDIA_URL = "mediaUrl";
        // int 时长
        public static final String C_DURATION = "duration";
        // long 消息扩展id，如礼物id
        public static final String C_EXTRA_ID  = "extraId"; 
        // string 聊天扩展内容,MsgExtra类型，使用时解析
        public static final String C_EXTRA = "extra";
        // int 收礼物或送礼物后增加的魅力值或是豪气值
        public static final String C_USERCP = "usercp";
        // string 附加内容（可组合为jsonstring存入，音视频图片下载保存地址、文件名等）
        public static final String C_ATTACH = "attach";
        
        // 预留字段
        // string
        // C_RESERVED1已被使用。用于标记 收到的 音频 信息是否已经播放过。   
        // 1或者空值（空值是为了兼容以前版本的数据库）:已播放    0:未播放
        public static final String C_RESERVED1 = "reserved1";  
        // string
        // C_RESERVED2已被使用。
        // 当消息类型是表情时，用于记录faceId的值；
        // 当消息类型是礼物时，用于记录animat的值；
        public static final String C_RESERVED2 = "reserved2";
        // int
        // C_RESERVED3已被使用，用于标记
        // (isCameraPhoto << 8) | (0xFF | sendType) 
        public static final String C_RESERVED3 = "reserved3";
        // long
        public static final String C_RESERVED4 = "reserved4";
    }
    /********************************************************************
     * 最新消息列表（聊天对象列表），共用消息列表全部字段，其余新增字段如下
     *******************************************************************/
    public static final class LastMsgTable implements BaseColumns,MsgTable{
       
        // int 新消息数
        public static final String C_UNREADNUM = "unreadNum";
        // int 是否vip 0:不是，1：是
        public static final String C_ISVIP = "isVip";
        // int 是否新用户 0:不是，1：是
        public static final String C_ISNEW = "isNew";
        // string 对方昵称
        public static final String C_NICK = "nick";
        // string 对方头像
        public static final String C_AVATAR = "avatar";
        // int 当前戴的皇冠的id(女性用户才有)
        public static final String C_CROWNID = "crownId";
        //豪气值
        public static final String C_RICH = "rich" ;
        //亲密度
        public static final String C_INTIMACY = "intimacy";
        
        
        public static final String TABLE_NAME  = "lastMsg";      // 表名
        public static final Uri CONTENT_URI   = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    }

}
