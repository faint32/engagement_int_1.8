package com.netease.service.transactions;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.netease.common.http.THttpRequest;
import com.netease.common.http.cache.HttpCache;
import com.netease.common.http.cache.HttpCacheManager;
import com.netease.common.task.AsyncTransaction;
import com.netease.common.task.NotifyTransaction;
import com.netease.common.task.TransTypeCode;
import com.netease.common.task.Transaction;
import com.netease.common.task.example.StringAsyncTransaction;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.BaseData;
import com.netease.service.protocol.meta.DebugData;
/**
 * 定义各协议transaction的id，进行一些返回数据外层的解析及通用错误的处理
 * @author echo_chen
 * @since  2014-03-17
 */

public abstract class EgmBaseTransaction extends StringAsyncTransaction {
    
    private boolean TestData = false;
    private String TestFileName = null;
    
    private Object mRequest;
    /**标志该协议请求是否需要登录过才能执行，是的话在有未完成登录前需要加入等待执行队列*/
    private boolean mNeedLogin = false;
    private EgmBaseTransaction mCurrentTx;

    public EgmBaseTransaction(int type) {
        super(type);
        mCurrentTx = this;
    }
    public void setNeedLogin(boolean needlogin){
        if(mNeedLogin != needlogin){
            mNeedLogin = needlogin;
        }
    }
    public boolean isNeedLogin(){
        return mNeedLogin;
    }
    
    /**
     * 用于界面内部传递数据
     */
    public static final int  TRANSACTION_UI_BROADCAST = 1;
    
    /**
     * echo_chen的协议id段
     */
    private static final int TRANSACTION_TYPE_ECHO_BASE = 0x100;
    /** 获取用户资料 */
    public static final int TRANSACTION_TYPE_GET_USER_INFO = TRANSACTION_TYPE_ECHO_BASE + 1;
    /** 处理push消息 */
    public static final int TRANSACTION_TYPE_HANDLE_PUSH_MSG = TRANSACTION_TYPE_ECHO_BASE + 2;
    /** 反馈 */
    public static final int TRANSACTION_TYPE_FEEDBACK = TRANSACTION_TYPE_ECHO_BASE + 3;
    /** 检查版本更新 */
    public static final int TRANSACTION_TYPE_CHECKVERSION = TRANSACTION_TYPE_ECHO_BASE + 4;
    /** 读取第三方登录用户资料接口 */
    public static final int TRANSACTION_TYPE_GET_OAUTHUSERINFO = TRANSACTION_TYPE_ECHO_BASE + 5;
    /** 读取用户防骚扰配置 */
    public static final int TRANSACTION_TYPE_GET_ANTIHARASSMENT = TRANSACTION_TYPE_ECHO_BASE + 6;
    /** 更新用户防骚扰配置 */
    public static final int TRANSACTION_TYPE_SET_ANTIHARASSMENT = TRANSACTION_TYPE_ECHO_BASE + 7;
    
    
    /* hzchenlk的协议id段 */    
    private static final int TRANSACTION_TYPE_BYRON_BASE = TRANSACTION_TYPE_ECHO_BASE + 200;
    /** 初始化URS */
    public static final int TRANSACTION_TYPE_INIT_URS = TRANSACTION_TYPE_BYRON_BASE + 1;
    /** 登录URS */
    public static final int TRANSACTION_TYPE_LOGIN_URS = TRANSACTION_TYPE_BYRON_BASE + 2;
    /** 获取手机号注册验证码 */
    public static final int TRANSACTION_TYPE_MOBILE_VERIFY_CODE = TRANSACTION_TYPE_BYRON_BASE + 3;
    /** 手机号注册 */
    public static final int TRANSACTION_TYPE_MOBILE_REGISTER = TRANSACTION_TYPE_BYRON_BASE + 4;
    /** 判断是否是同城约会1.0的帐号 */
    public static final int TRANSACTION_TYPE_IS_YUEHUI_ACCOUNT = TRANSACTION_TYPE_BYRON_BASE + 5;
    /** 手机号注册——补全用户资料 */
    public static final int TRANSACTION_TYPE_FILL_USER_INFO = TRANSACTION_TYPE_BYRON_BASE + 6;
    /** 注册——绑定手机号 */
    public static final int TRANSACTION_TYPE_BIND_MOBILE = TRANSACTION_TYPE_BYRON_BASE + 7;
    /** 登录——上传地理位置并获取用户资料 */
    public static final int TRANSACTION_TYPE_LOGIN_GET_USER_INFO = TRANSACTION_TYPE_BYRON_BASE + 8;
    /** 上传地理位置 */
    public static final int TRANSACTION_TYPE_UPLOAD_LOCATION = TRANSACTION_TYPE_BYRON_BASE + 9;
    /** 退出登录 */
    public static final int TRANSACTION_TYPE_LOGOUT = TRANSACTION_TYPE_BYRON_BASE + 10;
    /** 获取push所需相关数据 */
    public static final int TRANSACTION_TYPE_GET_PUSH_PARAMS = TRANSACTION_TYPE_BYRON_BASE + 11;
    /** 获取推荐列表 */
    public static final int TRANSACTION_TYPE_GET_RECOMMEND = TRANSACTION_TYPE_BYRON_BASE + 12;
    /** 获取排行榜 */
    public static final int TRANSACTION_TYPE_GET_RANK_LIST = TRANSACTION_TYPE_BYRON_BASE + 13;
    /** 获取碰缘分数据 */
    public static final int TRANSACTION_TYPE_GET_YUANFEN_INFO = TRANSACTION_TYPE_BYRON_BASE + 14;
    /** 通知碰缘分开关状态 */
    public static final int TRANSACTION_TYPE_INFORM_YUANFEN_SWITCHER = TRANSACTION_TYPE_BYRON_BASE + 15;
    /** 通知碰缘分类型 */
    public static final int TRANSACTION_TYPE_INFORM_YUANFEN_TYPE = TRANSACTION_TYPE_BYRON_BASE + 16;
    /** 上传碰缘分的群发语音或者帮我写文字 */
    public static final int TRANSACTION_TYPE_SEND_YUANFEN = TRANSACTION_TYPE_BYRON_BASE + 17;
    /** 搜索 */
    public static final int TRANSACTION_TYPE_SEARCH = TRANSACTION_TYPE_BYRON_BASE + 18;
    /** 排行榜背景图片 */
    public static final int TRANSACTION_TYPE_RANK_PICTURE = TRANSACTION_TYPE_BYRON_BASE + 19;
    /** 获取个人中心女性账户信息 */
    public static final int TRANSACTION_TYPE_GET_MONEY_ACCOUNT_INFO = TRANSACTION_TYPE_BYRON_BASE + 20;
    /** 个人中心女性申请提现 */
    public static final int TRANSACTION_TYPE_APPLY_WITHDRAW = TRANSACTION_TYPE_BYRON_BASE + 21;
    /** 验证身份证 */
    public static final int TRANSACTION_TYPE_AUTH_IDENTITY = TRANSACTION_TYPE_BYRON_BASE + 22;
    /** 使用Token置换登录ticket */
    public static final int TRANSACTION_TYPE_EXCHANGE_TICKET = TRANSACTION_TYPE_BYRON_BASE + 23;
    /** 获取女性现金收支记录 */
    public static final int TRANSACTION_TYPE_GET_MONEY_HISTORY = TRANSACTION_TYPE_BYRON_BASE + 24;
    /** 添加易信好友 */
    public static final int TRANSACTION_TYPE_YIXIN_ADD_FRIEND = TRANSACTION_TYPE_BYRON_BASE + 25;
    /** 易信注册 */
    public static final int TRANSACTION_TYPE_YIXIN_REGISTER = TRANSACTION_TYPE_BYRON_BASE + 26;
    /** 查看易信消息 */
    public static final int TRANSACTION_TYPE_YIXIN_CHECK = TRANSACTION_TYPE_BYRON_BASE + 27;
    /**绑定易信 */
    public static final int TRANSACTION_TYPE_YIXIN_BIND = TRANSACTION_TYPE_BYRON_BASE + 28;
    
    /**
     * hzchenxiaoxiong的协议id段
     */    
    private static final int TRANSACTION_TYPE_CXX_BASE = TRANSACTION_TYPE_BYRON_BASE + 200;
    /** 获取用户详细信息，包含私照公开照等 */
    public static final int TARNSACTION_TYPE_GET_USERINFO_DETAIL = TRANSACTION_TYPE_CXX_BASE + 1 ;
    /** 获取个人资料配置项数据 */
    public static final int TARNSACTION_TYPE_GET_USERINFO_CONGFIG = TRANSACTION_TYPE_CXX_BASE + 2 ;
    /** 获取礼物列表*/
    public static final int TARNSACTION_TYPE_GET_GIFT_LIST = TRANSACTION_TYPE_CXX_BASE + 3 ;
    /** 修改会员详细资料*/
    public static final int TARNSACTION_TYPE_MODIFY_DETAIL_INFO = TRANSACTION_TYPE_CXX_BASE + 4 ;
    /** 修改会员头像*/
    public static final int TARNSACTION_TYPE_MODIFY_PROFILE = TRANSACTION_TYPE_CXX_BASE + 5 ;
    /** 上传语音自我介绍*/
    public static final int TARNSACTION_TYPE_UPDATE_AUDIO = TRANSACTION_TYPE_CXX_BASE + 6 ;
    /** 删除语音自我介绍*/
    public static final int TARNSACTION_DEL_AUDIO_INTR = TRANSACTION_TYPE_CXX_BASE + 7 ;
    /** 获取表情配置数据*/
    public static final int TARNSACTION_GET_EMOT_CONFIG = TRANSACTION_TYPE_CXX_BASE + 8 ;
    /** 获取礼物配置数据*/
    public static final int TARNSACTION_GET_GIFT_CONFIG = TRANSACTION_TYPE_CXX_BASE + 9 ;
    /** 获取个人中心数据*/
    public static final int TARNSACTION_GET_PRIVATE_DATA = TRANSACTION_TYPE_CXX_BASE + 10 ;
    /** 获取照片列表*/
    public static final int TARNSACTION_GET_PICTURE_LIST = TRANSACTION_TYPE_CXX_BASE + 11 ;
    /** 赠送礼物*/
    public static final int TARNSACTION_SEND_GIFT = TRANSACTION_TYPE_CXX_BASE + 12 ;
    /** 修改自我介绍 */
    public static final int TRANSACTION_MODIFY_INTRODUCE = TRANSACTION_TYPE_CXX_BASE + 13;
    /** 上传照片 */
    public static final int TRANSACTION_UPLOAD_PIC = TRANSACTION_TYPE_CXX_BASE + 14;
    /** 删除照片 */
    public static final int TRANSACTION_DELETE_PIC = TRANSACTION_TYPE_CXX_BASE + 15;
    /** 赞私照*/
    public static final int TRANSACTION_PRAISE_PIC = TRANSACTION_TYPE_CXX_BASE + 16;
    /** 获取聊天列表*/
    public static final int TRANSACTION_GET_CHAT_LIST = TRANSACTION_TYPE_CXX_BASE + 17;
    /** 删除聊天列表*/
    public static final int TRANSACTION_DEL_CHAT_LIST = TRANSACTION_TYPE_CXX_BASE + 18;
    /** 聊天列表排序*/
    public static final int TRANSACTION_SORT_CHAT_LIST = TRANSACTION_TYPE_CXX_BASE + 19;
    /** 删除某条消息*/
    public static final int TRANSACTION_DEL_MSG = TRANSACTION_TYPE_CXX_BASE + 20;
    /** 发送消息*/
    public static final int TRANSACTION_SEND_MSG = TRANSACTION_TYPE_CXX_BASE + 21;
    /** 获取聊天及碰缘份文字模板*/
    public static final int TRANSACTION_GET_TOPIC_DATA = TRANSACTION_TYPE_CXX_BASE + 22;
    /** 获取消息列表*/
    public static final int TRANSACTION_GET_MSG_LIST = TRANSACTION_TYPE_CXX_BASE + 23;
    /** 举报*/
    public static final int TRANSACTION_COMPLAIN = TRANSACTION_TYPE_CXX_BASE + 24;
    /** 加黑*/
    public static final int TRANSACTION_BLOCK = TRANSACTION_TYPE_CXX_BASE + 25;
    /** 将消息设置为已读*/
    public static final int TRANSACTION_SET_MSG_READ = TRANSACTION_TYPE_CXX_BASE + 26;
    /** 获取单张私照*/
    public static final int TRANSACTION_GET_SINGLE_PRI_IMAGE = TRANSACTION_TYPE_CXX_BASE + 27;
    /** 男性在小爱聊天界面查询邀请者*/
    public static final int TRANSACTION_QUERY_INVITOR = TRANSACTION_TYPE_CXX_BASE + 28;
    /** 获取手机绑定的通行证帐号*/
    public static final int TRANSACTION_QUERY_ACCOUNT = TRANSACTION_TYPE_CXX_BASE + 29;
    /** 踩私照*/
    public static final int TRANSACTION_UNLIKE_PIC = TRANSACTION_TYPE_CXX_BASE + 30;
    /** 获取系统头像列表 */
    public static final int TARNSACTION_TYPE_GET_SYSPORTRAIT_LIST = TRANSACTION_TYPE_CXX_BASE + 31;
    /** 设置系统头像 */
    public static final int TARNSACTION_TYPE_UPDATE_SYSPORTRAIT = TRANSACTION_TYPE_CXX_BASE + 32;
    /** 获取已解锁私照列表*/
    public static final int TARNSACTION_GET_UNLOCK_PICTURE_LIST = TRANSACTION_TYPE_CXX_BASE + 33;
    /** 上传视频自我介绍*/
    public static final int TARNSACTION_TYPE_UPDATE_VIDEO = TRANSACTION_TYPE_CXX_BASE + 34 ;
    
    /**
     * dingding的协议id段
     */
    private static final int TRANSACTION_TYPE_DD_BASE = TRANSACTION_TYPE_BYRON_BASE + 300;
    public static final int TRANSACTION_DOWNLOAD_MSG_RES = TRANSACTION_TYPE_DD_BASE + 1;
    public static final int TRANSACTION_DOWNLOAD_RES = TRANSACTION_TYPE_DD_BASE + 2;
    
    /**
     * lichangjie的协议id段
     */
    private static final int TRANSACTION_TYPE_LCJ_BASE = TRANSACTION_TYPE_BYRON_BASE + 400;
    public static final int TRANSACTION_GET_FIRE_MESSAGE_MEDIA_URL = TRANSACTION_TYPE_LCJ_BASE + 1;
    public static final int TRANSACTION_DELETE_FIRE_MESSAGE = TRANSACTION_TYPE_LCJ_BASE + 2;
    public static final int TRANSACTION_GET_CHAT_SKILLS_LIST = TRANSACTION_TYPE_LCJ_BASE + 3;
    public static final int TRANSACTION_UPDATE_CHAT_SKILLS_LIST = TRANSACTION_TYPE_LCJ_BASE + 4;
    
    /**
     * lishang的协议id段
     */    
    private static final int TRANSACTION_TYPE_LS_BASE = TRANSACTION_TYPE_BYRON_BASE + 500;
    public static final int TRANSACTION_GET_COMPETITION_LIST = TRANSACTION_TYPE_LS_BASE+1;
    public static final int TRANSACTION_GET_RANK_LIST_INFO_IN_HOME = TRANSACTION_TYPE_LS_BASE+2;
    public static final int TRANSACTION_SWITCH_AUDIO_VIDEO = TRANSACTION_TYPE_LS_BASE+3; 
    public static final int TRANSACTION_GET_AUDIO_VIDEO = TRANSACTION_TYPE_LS_BASE+4; 
    protected class XoneNotifyTransaction extends NotifyTransaction {

        public XoneNotifyTransaction(AsyncTransaction tran, Object data,
                int type, int code) {
            super(tran, data, type, code);
        }

        public XoneNotifyTransaction(List<AsyncTransaction> trans, Object data,
                int type, int code) {
            super(trans, data, type, code);
        }

        @Override
        public void doBeforeTransact() {
            if (isSuccessNotify()) {
                Object data = getData();
                if (data != null) {
                    try {
                        do {
                            BaseData base = null;
                            Type type = new TypeToken<BaseData>(){}.getType();
                            if (data instanceof String) {
                                base = gson.fromJson((String)data, type);
                            } else if (data instanceof HttpCache) {
                                String str = readString(null, ((HttpCache) data).LocalFile
                                        .openInputStream(), ((HttpCache) data).Charset);
                                base = gson.fromJson(str, type);
                            } else {
                                setNotifyTypeAndCode(NOTIFY_TYPE_ERROR, 0);
                                break;
                            }
                            
                            int code = base.code;
                            if(code == EgmServiceCode.PROTOCOL_CODE_SUCCESS){  
                                resetData(base.data);
                            } 
                            // 特殊处理，易信加好友，未注册，需要返回数据
                            // 因关键词过滤导致消息发送失败，需要返回内容
                            else if(code == EgmServiceCode.TRANSACTION_YIXIN_NO_REGISTER
                            		|| code == EgmServiceCode.TRANSACTION_CHAT_KEYWORDS_BLOCKED){
                                resetData(base.data);
                                setNotifyTypeAndCode(NOTIFY_TYPE_SUCCESS, code);
                            }
                            else {
                                resetData(base);
                                setNotifyTypeAndCode(NOTIFY_TYPE_ERROR, code);
                            }
                            
                        } while (false);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        if (mRequest != null && mRequest instanceof THttpRequest) {
                            THttpRequest httpRequest = (THttpRequest) mRequest;
                            HttpCacheManager.deleteHttpCache(httpRequest);
                        }

                        setNotifyTypeAndCode(NOTIFY_TYPE_ERROR,
                                TransTypeCode.ERR_CODE_DATA_PARSE_EXCEPTION);
                    }
                } else {
                    setNotifyTypeAndCode(NOTIFY_TYPE_ERROR, 0);
                }
            }
        }

    }

    private static class JsonElementJsonDeserializer implements JsonDeserializer<JsonElement> {
        @Override
        public JsonElement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return json;
        }
    }
    
    private final static Gson gson = new GsonBuilder()
    .registerTypeAdapter(JsonElement.class, new JsonElementJsonDeserializer())
    .create();
    
    @Override
    public NotifyTransaction createNotifyTransaction(Object data,
            int notifyType, int code) {
        return new XoneNotifyTransaction(this, data, notifyType, code);
    }

    @Override
    public NotifyTransaction createNotifyTransaction(
            List<AsyncTransaction> trans, Object data, int notifyType, int code) {
        return new XoneNotifyTransaction(trans, data, notifyType, code);
    }
    protected void setTestFileName(String name) {
        TestData = true;
        TestFileName = name;
    }
    
    @Override
    protected void sendRequest(Object obj) {
        if(TestData){
            try {
                String path = DebugData.getTestDataPath() + TestFileName;
                File file = new File(path);
                if (file != null && file.exists()) {
                    FileInputStream inputStream = new FileInputStream(file);
                    String str = readString(null, inputStream, null);
                    Transaction t = createNotifyTransaction(str,
                            NotifyTransaction.NOTIFY_TYPE_SUCCESS, 0);
                    getTransactionEngine().beginTransaction(t);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                notifyError(0, null);
                doEnd();
                return;
            }
        }else{
            mRequest = obj;
            super.sendRequest(obj);
        }
    }
   
    @Override
    protected void sendRequest(Object obj, AsyncTransaction trans) {
        mRequest = obj;
        super.sendRequest(obj, trans);
    }

    @Override
    protected final void onTransactionSuccess(int code, Object obj) {
        onEgmTransactionSuccess(code, obj);
    }

    @Override
    protected final void onTransactionError(int errCode, Object obj) {
    	if (obj != null && obj instanceof BaseData) {
    		BaseData data = (BaseData) obj;
    		if (! onEgmTransactionError(errCode, data)) {
    			onEgmTransactionError(errCode, data.message);
    		}
    	}
    	else {
    		onEgmTransactionError(errCode, obj);
    	}
    }

    protected void onEgmTransactionSuccess(int code, Object obj) {

    }
    
    /**
     * 
     * @param errCode
     * @param data
     * @return true 表示已处理，不再执行onEgmTransactionError(int errCode, Object obj)
     */
    protected boolean onEgmTransactionError(int errCode, BaseData data) {
    	return false;
    }

    protected void onEgmTransactionError(int errCode, Object obj) {
        notifyError(errCode, obj);
    }
    
    /** 通知数据解析错误 */
    protected void notifyDataParseError(){
        notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                ErrorToString.getString(EgmServiceCode.TRANSACTION_FAIL));
    }
}
