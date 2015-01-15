package com.netease.android.entity;

import java.io.Serializable;

public class PostInfo implements Serializable{
	private static final long serialVersionUID = -4373478798767966978L;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getBlogId() {
		return blogId;
	}
	public void setBlogId(long blogId) {
		this.blogId = blogId;
	}
	public long getPublisherUserId() {
		return publisherUserId;
	}
	public void setPublisherUserId(long publisherUserId) {
		this.publisherUserId = publisherUserId;
	}
	public boolean isContribute() {
		return isContribute;
	}
	public void setContribute(boolean isContribute) {
		this.isContribute = isContribute;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public long getPublishTime() {
		return publishTime;
	}
	public void setPublishTime(long publishTime) {
		this.publishTime = publishTime;
	}
	public long getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(long modifyTime) {
		this.modifyTime = modifyTime;
	}
	public boolean isPublished() {
		return isPublished;
	}
	public void setPublished(boolean isPublished) {
		this.isPublished = isPublished;
	}
	public int getAllowView() {
		return allowView;
	}
	public void setAllowView(int allowView) {
		this.allowView = allowView;
	}
	public int getValid() {
		return valid;
	}
	public void setValid(int valid) {
		this.valid = valid;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getFirstImageUrl() {
		return firstImageUrl;
	}
	public void setFirstImageUrl(String firstImageUrl) {
		this.firstImageUrl = firstImageUrl;
	}
	public String getPermalink() {
		return permalink;
	}
	public void setPermalink(String permalink) {
		this.permalink = permalink;
	}
	public String getEmbed() {
		return embed;
	}
	public void setEmbed(String embed) {
		this.embed = embed;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getPhotoLinks() {
		return photoLinks;
	}
	public void setPhotoLinks(String photoLinks) {
		this.photoLinks = photoLinks;
	}
	public int getCctype() {
		return cctype;
	}
	public void setCctype(int cctype) {
		this.cctype = cctype;
	}
	
	//发布日志类型定义
	public static final int TEXT = 1;
	public static final int PHOTO = 2;
	public static final int MUSIC = 3;
	public static final int VIDEO = 4;
	public static final int QUESTION = 5;
	public static final int QUEUE = 99;
	//发布日志类型名称对应
	public static final String POST_TYPE_TEXT = "Text";
	public static final String POST_TYPE_PHOTO = "Photo";
	public static final String POST_TYPE_MUSIC = "Music";
	public static final String POST_TYPE_VIDEO = "Video";
	public static final String POST_TYPE_QUESTION = "Ask";
	//站点同步变量定义
	public final static int SITE_TSINA = 1;
    public final static int SITE_T163 = 2;
    public final static int SITE_QQ = 3;
    public final static int SITE_DOUBAN = 4;
    public final static int SITE_RENREN = 5;
    public final static int SITE_TQQ = 6;
    public final static int SITE_TWITTER = 7;
    public final static int SITE_FACEBOOK = 8;
    public final static int SITE_GOOGLE = 9;
    public final static int SITE_FLICKR = 10;
    public final static int SITE_500PX = 11;
    //发布类型定义
    public static final String OPTTYPE_EDIT = "edit";//编辑
    public static final String OPTTYPE_REBLOG = "reblog";//转载
    public static final String OPTTYPE_NEW = "new";//新发布
    public static final String OPTTYPE_ANSWER_ONLY = "answerOnly";//仅回答
    public static final String OPTTYPE_ANSWER_PUBLIC = "answerPublic";//回答并发布
    public static final String OPTTYPE_SINA_SHARE = "sinaShare";//新浪分享
    
    
	private long  id;//日志id   key
	private long blogId;//博客的Id  均衡字段
	private long publisherUserId;//发布者的userId
	private boolean isContribute;//非投稿为0
	private String title;//日志标题(编辑时作为问答类型 提问内容用)
	private String content;//日志内容(编辑时作为问答类型 回答内容用)
	private long publishTime;//发布时间
	private long modifyTime;//修改时间
	private boolean isPublished;//发布状态
	private int allowView;//0公开，私人等
	private int valid;//日志状态,如被反垃圾或已经删除
	private int rank;// 0或1, 1表示置顶
	private String tag;//标签，用英文或中文逗号分隔
	private int type;//日志类型
	private String photoLinks;  //照片地址列表　json格式[{small:"",middle:"",orign:"","ow":1024,"oh":768},{small:"",middle:"",orign:"","ow":1024,"oh":768}]
	private String firstImageUrl;//json格式的数组。第一张图片的链接,先方图(小图)，后中图，以逗号分隔。提取不到方图时，方图的链接与中图相同
	private String permalink;//日志链接
	private int cctype = -100;
    private String digest;
	public String getDigest() {
        return digest;
    }

    /**
	 * 音乐、视频
	 */
	private String embed;
	private String caption; 
	
}
