package com.whaix.smartcampus;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;


public class MainActivity extends Activity {
	
	//地图相关
	BMapManager mBMapMan = null;  	//地图引擎管理类
	MapView mMapView = null; 
	MapController mMapController=null;		//地图控制器
	private String mapKey="lFwjGfrAmLujIMtsqx6S8yTh";		//key
	
	//定位相关	
	LocationData locData = null;	//存放定位相关数据
	public BDLocation location = null;
	public LocationClient mLocationClient = null;	//定位服务客服端
	public BDLocationListener myListener = new MyLocationListener();
	
	//定义图层
	MyLocationOverlay myLocationOverlay=null;
	
	//UI相关
	boolean isRequest = false;		//是否手动触发请求定位
	boolean isFirstLoc = true;		//是否首次定位
	Button local;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//初始化BMapManager,必须在setContentView之前调用
		mBMapMan=new BMapManager(getApplication());  
		mBMapMan.init(mapKey,new MyGeneralListener());   
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);	//去掉标题栏
		setContentView(R.layout.activity_main);
		local=(Button)findViewById(R.id.local_bnt);
		local.setOnClickListener(new OnClickListener(){
			public void onClick(View view){
				isRequest=true;
				mLocationClient.requestLocation();
				Toast.makeText(MainActivity.this, "正在定位", Toast.LENGTH_SHORT).show();
			}
		});
		
		//加载地图
		mMapView=(MapView)findViewById(R.id.bmapsView);  		
		mMapView.setBuiltInZoomControls(true);  //设置启用内置的缩放控件  		
		mMapController=mMapView.getController();  // 得到mMapView的控制权,可以用它控制和驱动平移和缩放 
		mMapController.setZoom(12);		//设置地图zoom级别 
		mMapController.enableClick(true);
		
		GeoPoint point =new GeoPoint((int)(39.915* 1E6),(int)(116.404* 1E6));  //用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)  		
		mMapController.setCenter(point);	//设置地图中心点  
		 
		
		
		mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
		locData = new LocationData();
	    mLocationClient.registerLocationListener(myListener);    //注册监听函数
	    
	  //设置定位参数
  		LocationClientOption option = new LocationClientOption();
  		
  		option.setOpenGps(true);
  		option.setCoorType("bd0911");
  		option.setScanSpan(3000);	//设置定时定位的时间间隔。单位ms
  		option.setIsNeedAddress(true);//返回的定位结果包含地址信息
  		option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
  		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
  		mLocationClient.setLocOption(option);
  		mLocationClient.start();	
  	
  		myLocationOverlay = new MyLocationOverlay(mMapView);
  		myLocationOverlay.setData(locData);
  		mMapView.getOverlays().add(myLocationOverlay);
  		myLocationOverlay.enableCompass();
  		mMapView.refresh();
		
	}
	
	public class MyLocationListener implements BDLocationListener {
		@Override//接收定位结果
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			
			if(location == null)
				return;		
			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();			
			locData.accuracy = location.getRadius();	//显示定位精度圈
			locData.direction = location.getDerect();			
			myLocationOverlay.setData(locData);	//更新定位数据			
			mMapView.refresh();		//更新图层数据执行刷新
			
			//手动出发请求或者首次定位时，移动到定位点
			if(isFirstLoc || isRequest)
			{
				//Log.d("LoctionOverLay","receive location,animate to it");
				mMapController.animateTo(new GeoPoint((int)(locData.latitude*1e6),(int)(locData.longitude*1e6)));
				isRequest = false;			
			}
			MainActivity.this.location=location;
			String locDataInfo = String.format(" 卫星数：%d \n 精度：%f m\n 速度：%f m/s \n纬度：%f \n经度：%f\n地址：%s ", location.getSatelliteNumber(),location.getRadius(),location.getSpeed(),location.getLatitude(),location.getLongitude(),location.getAddrStr());
			TextView textview = (TextView)findViewById(R.id.showpos);
			textview.setText(locDataInfo);
			isFirstLoc = false;
		}

		@Override
		public void onReceivePoi(BDLocation poiLocation) {
			// TODO Auto-generated method stub
			if(poiLocation==null)
				return;
		}
	}
	
	// 常用事件监听，用来处理通常的网络错误，授权验证错误等  
		  class MyGeneralListener implements MKGeneralListener {  
	          @Override  
	          public void onGetNetworkState(int iError) {  
	              Log.d("MyGeneralListener", "onGetNetworkState error is "+ iError);  
	              Toast.makeText(MainActivity.this, "您的网络出错啦！",  
	                      Toast.LENGTH_LONG).show();  
	          }  

	          @Override  
	          public void onGetPermissionState(int iError) {  
	              Log.d("MyGeneralListener", "onGetPermissionState error is "+ iError);  
	              if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {  
	                  // 授权Key错误：  
	                  Toast.makeText(MainActivity.this,   
	                          "请输入正确的授权Key！",  
	                          Toast.LENGTH_LONG).show();  
	              }  
	          }  
	      }  
	// 重写以下方法，管理API					
	@Override  
	protected void onDestroy(){  
	        mMapView.destroy();  
	        if(mBMapMan!=null){  
	                mBMapMan.destroy();  
	                mBMapMan=null;  
	        }  
	        super.onDestroy();  
	}  
	@Override  
	protected void onPause(){  
	        mMapView.onPause();  
	        if(mBMapMan!=null){  
	               mBMapMan.stop();  
	        }  
	        super.onPause();  
	}  
	@Override  
	protected void onResume(){  
	        mMapView.onResume();  
	        if(mBMapMan!=null){  
	                mBMapMan.start();  
	        }  
	       super.onResume();  
	}  
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
