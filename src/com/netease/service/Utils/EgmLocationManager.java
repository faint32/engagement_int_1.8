package com.netease.service.Utils;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

/**
 * 地理位置管理类。
 * <p>可能需要的权限：
 * <br>1、android.permission.ACCESS_COARSE_LOCATION（网络模糊位置）；
 * <br>2、android.permission.ACCESS_FINE_LOCATION（GPS精确位置）；
 * <br>3、android.permission.INTERNET（网络访问）
 * 
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class EgmLocationManager {
    public double mLatitude;
    public double mLongtitude;
    public String mProvince;
    public int mProvinceCode;
    public String mCity;
    public int mCityCode;
    public String mDistrict;
    public int mDistrictCode;
    
    private Context mContext;
    private LocationClient mLocationClient = null;

    public EgmLocationManager(Context context){
        mContext = context;
        initClient();
        mLocationClient.start();
    }

    private void initClient() {
        mLocationClient = new LocationClient(mContext);     //声明LocationClient类
        mLocationClient.registerLocationListener(mBDListener);    //注册监听函数
        
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Battery_Saving);//设置定位模式
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度，默认值gcj02
//        option.setScanSpan(5000);//.setScanType(5000);//设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);//返回的定位结果包含地址信息
//        option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
        mLocationClient.setLocOption(option);
    }

    public void requestLocation(){
        mLocationClient.requestLocation();
    }
    
    /** 停止定位工作 */
    public void stop(){
        mLocationClient.unRegisterLocationListener(mBDListener);
        mLocationClient.stop();
    }
    
    private BDLocationListener mBDListener = new BDLocationListener(){
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return ;
            
            mLatitude = location.getLatitude();
            mLongtitude = location.getLongitude();
            
            mProvince = location.getProvince();
            mCity = location.getCity();
            mDistrict = location.getDistrict();
            
            if(!TextUtils.isEmpty(mProvince)){
                mProvinceCode = AreaTable.getProvinceIdByName(mContext, mProvince);
                
                if(mProvinceCode > 0 && !TextUtils.isEmpty(mCity)){
                    mCityCode = AreaTable.getCityIdByName(mContext, mProvinceCode, mCity);
                    
                    if(mCityCode > 0 && !TextUtils.isEmpty(mDistrict)){
                        mDistrictCode = AreaTable.getDistrictIdByName(mContext, mProvinceCode, mCityCode, mDistrict);
                    }
                }
            }
        }

        @Override
        public void onReceivePoi(BDLocation arg0) {
            
        }
    };
    
    /**
     * 获取当前所处的区域信息。
     * <br><b>该方法需要耗时的联网，可能会阻塞UI。
     * @param context
     * @return 返回数组的规则：
     *      <p>null，没有任何数据;<br>
     *         lenght == 2，只有经纬度数据;<br>
     *         length == 4，只有省份数据;<br>
     *         length == 6，只有省份和市的数据;<br>
     *         length == 8，有省份、市、县\区数据。
     *      <p>[0]:经度；<br>
     *         [1]:纬度；<br>
     *         [2]:省份名称；<br>
     *         [3]:省份代码；<br>
     *         [4]:市名称；<br>
     *         [5]:市代码；<br>
     *         [6]:县\区名称；<br>
     *         [7]:县\区代码;
     */
//    public static String[] getCurrentAreaInfo(Context context){
//        String[] areaInfo = null;
//        String provinceStr = "";
//        String provinceId = "";
//        String cityStr = "";
//        String cityId = "";
//        String districtStr = "";
//        String districtId = "";
//        
//        Address address = getAddress(context);
//        if(address == null)
//            return null;
//        
//        // 经纬度
//        String latitude = String.valueOf(address.getLatitude());
//        String longtitude = String.valueOf(address.getLongitude());
//        if(!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longtitude)){
//            // 省
//            provinceStr = address.getAdminArea();
//            if(TextUtils.isEmpty(provinceStr)){ // 只有经纬度
//                areaInfo = new String[2];
//                areaInfo[0] = latitude;
//                areaInfo[1] = longtitude;
//            }
//            else{ 
//                provinceStr = trimEnd(provinceStr, province);
//                provinceStr = trimEnd(provinceStr, autonomous_region);
//                provinceId = AreaTable.getProvinceIdByName(context, provinceStr);
//                // 市
//                cityStr = address.getLocality();
//                
//                if(TextUtils.isEmpty(cityStr)){ // 市取不到，只有省份数据
//                    areaInfo = new String[4];
//                    areaInfo[0] = latitude;
//                    areaInfo[1] = longtitude;
//                    areaInfo[2] = provinceStr;
//                    areaInfo[3] = provinceId;
//                }
//                else{
//                    cityStr = trimEnd(cityStr, city);
//                    cityStr = trimEnd(cityStr, autonomous_prefecture);
//                    cityId = AreaTable.getCityIdByName(context, provinceId, cityStr);
//                    // 县/区
//                    districtStr = address.getSubLocality();
//                    
//                    if(TextUtils.isEmpty(districtStr)){ //县\区取不到，只有省、市数据
//                        areaInfo = new String[6];
//                        areaInfo = new String[4];
//                        areaInfo[0] = latitude;
//                        areaInfo[1] = longtitude;
//                        areaInfo[2] = provinceStr;
//                        areaInfo[3] = provinceId;
//                        areaInfo[4] = cityStr;
//                        areaInfo[5] = cityId;
//                    }
//                    else{
//                        districtStr = trimEnd(districtStr, district);
//                        districtStr = trimEnd(districtStr, autonomous_county);
//                        districtStr = trimEnd(districtStr, city);   // 有的县是县级市
//                        districtId = AreaTable.getDistrictIdByName(context, provinceId, cityId, districtStr);
//                        
//                        areaInfo = new String[8];
//                        areaInfo[0] = latitude;
//                        areaInfo[1] = longtitude;
//                        areaInfo[2] = provinceStr;
//                        areaInfo[3] = provinceId;
//                        areaInfo[4] = cityStr;
//                        areaInfo[5] = cityId;
//                        areaInfo[6] = districtStr;
//                        areaInfo[7] = districtId;
//                    }
//                }
//            }
//        }
//        
//        return areaInfo;        
//    }
    
    /**
     * 获取当前所处的城市信息。
     * <br><b>该方法需要耗时的联网，可能会阻塞UI。
     * @param context
     * @return 返回数组的规则：
     *      <p>null，没有任何数据;<br>
     *         length == 2，有城市名和Id
     *      <p>[0]:城市名称；<br>
     *         [1]:城市代码；
     */
//    public static String[] getCurrentCityInfo(Context context){
//        String[] cityInfo = null;
//        String provinceStr = "";
//        String provinceId = "";
//        String cityStr = "";
//        String cityId = "";
//        
//        Address address = getAddress(context);
//        provinceStr = address.getAdminArea();
//        
//        if(!TextUtils.isEmpty(provinceStr)){  
//            provinceStr = trimEnd(provinceStr, province);
//            provinceStr = trimEnd(provinceStr, autonomous_region);
//            provinceId = AreaTable.getProvinceIdByName(context, provinceStr);
//            cityStr = address.getLocality();
//            
//            if(!TextUtils.isEmpty(cityStr)){
//                cityStr = trimEnd(cityStr, city);
//                cityStr = trimEnd(cityStr, autonomous_prefecture);
//                cityId = AreaTable.getCityIdByName(context, provinceId, cityStr);
//                
//                cityInfo = new String[2];
//                cityInfo[0] = cityStr;
//                cityInfo[1] = cityId;
//            }
//        }
//        
//        return cityInfo;
//    }
    
    /**
     * 获取当前位置Address对象。
     * <br><b>该方法需要耗时的联网，可能会阻塞UI。
     * @param context
     * @return 结果有可能不准确，是上一次获取的，如果获取失败则为null。
     */
//    public static Address getAddress(Context context){
//        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        String provider = getProvider(locationManager, Criteria.ACCURACY_COARSE);
//        
//        if(provider == null)
//            return null;
//                    
//        Location location = locationManager.getLastKnownLocation(provider); // 开始获取
//        if(location == null){
//            return null;
//        }
//        
//        return location2Address(context, location.getLatitude(), location.getLongitude());
//    }
    
    /**
     * 发起请求，请求当前位置的经纬度。
     * <br><b>该方法最好在非UI线程中调用，它要耗时的联网，否则可能会阻塞UI。
     * <br><b>注意，地址获取完成后要记得调用LocationManager.removeUpdates()方法解除监听注册。
     * @param context
     * @param interval  每次发起请求的间隔，单位为秒。
     * @param listener  得到位置的监听
     * @return 如果为null，那么此处请求失败，否则可能成功
     */
//    public static LocationManager requestLocation(Context context, int interval, LocationListener listener){
//        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        String provider = getProvider(locationManager, Criteria.ACCURACY_COARSE);
//        
//        if(provider == null)
//            return null;
//                    
//        locationManager.requestLocationUpdates(provider, 1000 * interval, 0, listener);
//        locationManager.getLastKnownLocation(provider); // 开始获取
//        
//        return locationManager;
//    }
    
    /**
     * 发起请求，请求当前位置的经纬度。
     * <br><b>注意，地址获取完成后要记得调用LocationManager.removeUpdates()方法解除监听注册。
     * @param context
     * @param listener  得到位置的监听
     * @return 如果为null，那么此处请求失败，否则可能成功
     */
//    public static LocationManager requestLocation(Context context, LocationListener listener){
//        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        String provider = getProvider(locationManager, Criteria.ACCURACY_COARSE);
//        
//        if(provider == null)
//            return null;
//                    
//        locationManager.requestSingleUpdate(provider, listener, null);
//        locationManager.getLastKnownLocation(provider); // 开始获取
//        return locationManager;
//    }
    
    /**
     * 将经纬度转为具体地址。
     * <br><b>该方法需要耗时的联网，可能会阻塞UI。。
     * @param context
     * @param location
     * @return Address对象, 可能为null
     */
//    public static Address location2Address(Context context, double latitude, double longtitude){
//        Address address = null;
//        Geocoder geocoder = new Geocoder(context);
//        
//        try {
//            List<Address> listAddress = geocoder.getFromLocation(latitude, longtitude, 1);
//            if(listAddress.size() != 0){
//                address = listAddress.get(0);
//            }
//        } 
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//         
//        return address;
//    }
    
    /**
     * 获取指定精度的Provider名称
     * @param lm
     * @param accuracy 参考Criteria的精度值
     * @return
     */
//    private static String getProvider(LocationManager lm, int accuracy){
//        Criteria criteria = new Criteria(); 
//        
//        criteria.setAccuracy(accuracy); 
//        criteria.setAltitudeRequired(false); 
//        criteria.setBearingRequired(false); 
//        criteria.setCostAllowed(false); 
//        criteria.setPowerRequirement(Criteria.POWER_LOW); 
//        
//        return lm.getBestProvider(criteria, true); 
//    }
    
    /**
     * 如果orgStr以end结尾，则去掉end，否则不变。
     * 用于去掉区域名称后的行政级别。
     * @param orgStr 原字符串
     * @param end 要去掉的尾部字符串
     * @return 如果orgStr以end结尾，则去掉end，否则不变
     */
//    private static String trimEnd(String orgStr, String end){
//        if(TextUtils.isEmpty(orgStr))
//            return "";
//        
//        if(orgStr.endsWith(end)){
//            int length = end.length();
//            return orgStr.substring(0, orgStr.length() - length);
//        }
//        
//        return orgStr;
//    }
    
    
}
