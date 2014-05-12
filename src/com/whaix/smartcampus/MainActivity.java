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
	
	//��ͼ���
	BMapManager mBMapMan = null;  	//��ͼ���������
	MapView mMapView = null; 
	MapController mMapController=null;		//��ͼ������
	private String mapKey="lFwjGfrAmLujIMtsqx6S8yTh";		//key
	
	//��λ���	
	LocationData locData = null;	//��Ŷ�λ�������
	public BDLocation location = null;
	public LocationClient mLocationClient = null;	//��λ����ͷ���
	public BDLocationListener myListener = new MyLocationListener();
	
	//����ͼ��
	MyLocationOverlay myLocationOverlay=null;
	
	//UI���
	boolean isRequest = false;		//�Ƿ��ֶ���������λ
	boolean isFirstLoc = true;		//�Ƿ��״ζ�λ
	Button local;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//��ʼ��BMapManager,������setContentView֮ǰ����
		mBMapMan=new BMapManager(getApplication());  
		mBMapMan.init(mapKey,new MyGeneralListener());   
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);	//ȥ��������
		setContentView(R.layout.activity_main);
		local=(Button)findViewById(R.id.local_bnt);
		local.setOnClickListener(new OnClickListener(){
			public void onClick(View view){
				isRequest=true;
				mLocationClient.requestLocation();
				Toast.makeText(MainActivity.this, "���ڶ�λ", Toast.LENGTH_SHORT).show();
			}
		});
		
		//���ص�ͼ
		mMapView=(MapView)findViewById(R.id.bmapsView);  		
		mMapView.setBuiltInZoomControls(true);  //�����������õ����ſؼ�  		
		mMapController=mMapView.getController();  // �õ�mMapView�Ŀ���Ȩ,�����������ƺ�����ƽ�ƺ����� 
		mMapController.setZoom(12);		//���õ�ͼzoom���� 
		mMapController.enableClick(true);
		
		GeoPoint point =new GeoPoint((int)(39.915* 1E6),(int)(116.404* 1E6));  //�ø����ľ�γ�ȹ���һ��GeoPoint����λ��΢�� (�� * 1E6)  		
		mMapController.setCenter(point);	//���õ�ͼ���ĵ�  
		 
		
		
		mLocationClient = new LocationClient(getApplicationContext());     //����LocationClient��
		locData = new LocationData();
	    mLocationClient.registerLocationListener(myListener);    //ע���������
	    
	  //���ö�λ����
  		LocationClientOption option = new LocationClientOption();
  		
  		option.setOpenGps(true);
  		option.setCoorType("bd0911");
  		option.setScanSpan(3000);	//���ö�ʱ��λ��ʱ��������λms
  		option.setIsNeedAddress(true);//���صĶ�λ���������ַ��Ϣ
  		option.setNeedDeviceDirect(true);//���صĶ�λ��������ֻ���ͷ�ķ���
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
		@Override//���ն�λ���
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			
			if(location == null)
				return;		
			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();			
			locData.accuracy = location.getRadius();	//��ʾ��λ����Ȧ
			locData.direction = location.getDerect();			
			myLocationOverlay.setData(locData);	//���¶�λ����			
			mMapView.refresh();		//����ͼ������ִ��ˢ��
			
			//�ֶ�������������״ζ�λʱ���ƶ�����λ��
			if(isFirstLoc || isRequest)
			{
				//Log.d("LoctionOverLay","receive location,animate to it");
				mMapController.animateTo(new GeoPoint((int)(locData.latitude*1e6),(int)(locData.longitude*1e6)));
				isRequest = false;			
			}
			MainActivity.this.location=location;
			String locDataInfo = String.format(" ��������%d \n ���ȣ�%f m\n �ٶȣ�%f m/s \nγ�ȣ�%f \n���ȣ�%f\n��ַ��%s ", location.getSatelliteNumber(),location.getRadius(),location.getSpeed(),location.getLatitude(),location.getLongitude(),location.getAddrStr());
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
	
	// �����¼���������������ͨ�������������Ȩ��֤�����  
		  class MyGeneralListener implements MKGeneralListener {  
	          @Override  
	          public void onGetNetworkState(int iError) {  
	              Log.d("MyGeneralListener", "onGetNetworkState error is "+ iError);  
	              Toast.makeText(MainActivity.this, "���������������",  
	                      Toast.LENGTH_LONG).show();  
	          }  

	          @Override  
	          public void onGetPermissionState(int iError) {  
	              Log.d("MyGeneralListener", "onGetPermissionState error is "+ iError);  
	              if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {  
	                  // ��ȨKey����  
	                  Toast.makeText(MainActivity.this,   
	                          "��������ȷ����ȨKey��",  
	                          Toast.LENGTH_LONG).show();  
	              }  
	          }  
	      }  
	// ��д���·���������API					
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
