package com.random.sharedlocation;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements ServiceConnection {

	MapView mMapView = null;
	private BaiduMap mBaiduMap;
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	private LocationClientOption option;
	/**
	 * ��һ�ζ�λ���
	 */
	private boolean isFirstLocation = true;
	private MService.Binder binder;
	private String otherLocateData;
	private BitmapDescriptor bitmap;
	private double socketIndex;
	private double latitude;
	private double longitude;
	private float direction;
	private String localLocateData = null;
	private double otherLatitude = 0, otherLongitude = 0, otherDirection = 0;
	private double otherSocketIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 1.��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ
		SDKInitializer.initialize(getApplicationContext());

		// 2.���ذٶȵ�ͼ
		setContentView(R.layout.activity_main);

		// 3.�ؼ���ʼ��
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();

		// ����Ϊ��ͨ��ͼ
		// mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);

		// 4.��λ��������ü�����
		mLocationClient = new LocationClient(getApplicationContext()); // ����LocationClient��
		mLocationClient.registerLocationListener(myListener); // ע���������

		// 5.��ʼ����λ��Ϣ,����������
		initLocation();
		mLocationClient.start();

		// 6.����socket�׽���
		Intent intent = new Intent(MainActivity.this, MService.class);
		bindService(intent, MainActivity.this, Context.BIND_AUTO_CREATE);
		socketIndex = Math.random() * 3;// ���������ʼ�����ؿͻ��˵ı�־
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// 1.ʵ�ֵ�ͼ�������ڹ���
		mMapView.onDestroy();

		// 2. ������Ҫ��λͼ��ʱ�رն�λͼ��
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.unRegisterLocationListener(myListener); // ע����������
		mLocationClient.stop(); // ֹͣ��λ����

		unbindService(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// ��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// ��activityִ��onPauseʱִ��mMapView. onPause ()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onPause();
	}

	/**
	 * ��λ��Ϣ��ʼ��
	 */
	private void initLocation() {
		option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// ��ѡ��Ĭ�ϸ߾��ȣ����ö�λģʽ���߾��ȣ��͹��ģ����豸
		option.setCoorType("bd09ll");// ��ѡ��Ĭ��gcj02�����÷��صĶ�λ�������ϵ
		option.setScanSpan(1000);// ��ѡ��Ĭ��0��������λһ�Σ����÷���λ����ļ����Ҫ���ڵ���1000ms������Ч��
		option.setIsNeedAddress(true);// ��ѡ�������Ƿ���Ҫ��ַ��Ϣ��Ĭ�ϲ���Ҫ
		option.setOpenGps(true);// ��ѡ��Ĭ��false,�����Ƿ�ʹ��gps
		option.setLocationNotify(true);// ��ѡ��Ĭ��false�������Ƿ�gps��Чʱ����1S1��Ƶ�����GPS���
		option.setIsNeedLocationDescribe(true);// ��ѡ��Ĭ��false�������Ƿ���Ҫλ�����廯�����������BDLocation.getLocationDescribe��õ�����������ڡ��ڱ����찲�Ÿ�����
		option.setIsNeedLocationPoiList(true);// ��ѡ��Ĭ��false�������Ƿ���ҪPOI�����������BDLocation.getPoiList��õ�
		option.setIgnoreKillProcess(false);// ��ѡ��Ĭ��true����λSDK�ڲ���һ��SERVICE�����ŵ��˶������̣������Ƿ���stop��ʱ��ɱ��������̣�Ĭ�ϲ�ɱ��
		option.SetIgnoreCacheException(false);// ��ѡ��Ĭ��false�������Ƿ��ռ�CRASH��Ϣ��Ĭ���ռ�
		option.setEnableSimulateGps(false);// ��ѡ��Ĭ��false�������Ƿ���Ҫ����gps��������Ĭ����Ҫ
		option.setNeedDeviceDirect(true);
		mLocationClient.setLocOption(option);
	}

	/**
	 * �ٶȵ�ͼ��λ����
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {

			// 1.��ȡλ����ϸ��Ϣ
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS��λ���
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());// ��λ������ÿСʱ
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
				sb.append("\nheight : ");
				sb.append(location.getAltitude());// ��λ����
				sb.append("\ndirection : ");
				sb.append(location.getDirection());// ��λ��
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append("\ndescribe : ");
				sb.append("gps��λ�ɹ�");

			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// ���綨λ���
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				// ��Ӫ����Ϣ
				sb.append("\noperationers : ");
				sb.append(location.getOperators());
				sb.append("\ndescribe : ");
				sb.append("���綨λ�ɹ�");

			} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// ���߶�λ���
				sb.append("\ndescribe : ");
				sb.append("���߶�λ�ɹ������߶�λ���Ҳ����Ч��");

			} else if (location.getLocType() == BDLocation.TypeServerError) {// ��λʧ��
				sb.append("\ndescribe : ");
				sb.append("��������綨λʧ�ܣ����Է���IMEI�źʹ��嶨λʱ�䵽loc-bugs@baidu.com��������׷��ԭ��");

			} else if (location.getLocType() == BDLocation.TypeNetWorkException) { // �������
				sb.append("\ndescribe : ");
				sb.append("���粻ͬ���¶�λʧ�ܣ����������Ƿ�ͨ��");
			} else if (location.getLocType() == BDLocation.TypeCriteriaException) {
				sb.append("\ndescribe : ");
				sb.append("�޷���ȡ��Ч��λ���ݵ��¶�λʧ�ܣ�һ���������ֻ���ԭ�򣬴��ڷ���ģʽ��һ���������ֽ�����������������ֻ�");
			}
			sb.append("\nlocationdescribe : ");
			sb.append(location.getLocationDescribe());// λ�����廯��Ϣ

			// ���poi��Ȥ����Ϣ
			List<Poi> list = location.getPoiList();// POI����
			if (list != null) {
				sb.append("\npoilist size = : ");
				sb.append(list.size());
				for (Poi p : list) {
					sb.append("\npoi= : ");
					sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
				}
			}
			Log.i("BaiduLocationApiDem", sb.toString());

			// ������λͼ��
			mBaiduMap.setMyLocationEnabled(true);

			// 2. ��ȡ��λ���ݣ���������Ϣ
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			direction = location.getDirection();// ��ȡ��λ��Ϣ
			// ��ȡ��λ����
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
					.direction(direction).latitude(latitude)
					.longitude(longitude).build();
			// ���ö�λ����
			mBaiduMap.setMyLocationData(locData);

			// ���ö�λͼ������ã���λģʽ���Ƿ���������Ϣ���û��Զ��嶨λͼ�꣩
			// BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
			// .fromResource(R.drawable.icon_openmap_focuse_mark);
			// MyLocationConfiguration config = new
			// MyLocationConfiguration(null, true, mCurrentMarker);
			// mBaiduMap.setMyLocationConfigeration(config);

			// ������Ҫ��λͼ��ʱ�رն�λͼ��
			// mBaiduMap.setMyLocationEnabled(false);

			// 3. ��һ�ζ�λʱ������ͼλ���ƶ�����ǰλ��
			LatLng ll = null;
			if (isFirstLocation) {
				isFirstLocation = false;
				ll = new LatLng(latitude, longitude);
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);

			}

			// 4. ���ÿͻ����ж�λ���ݣ�����λ����Ϣ��������
			if (location.getLocationDescribe() != null && binder != null) {

				binder.sendData("[" + socketIndex + ":" + latitude + ":"
						+ longitude + ":" + direction + ":]");// ����λ�ַ����б仯ʱͬ���ı�Myservice��data

				localLocateData = "[" + socketIndex + ":" + latitude + ":"
						+ longitude + ":" + direction + ":]";// ���¶�λ�ַ������趨λ���ݣ�localLocateData��
			}
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		binder = (MService.Binder) service;

		binder.getService().setCallBack(new MService.ICallBack() {
			@Override
			public void onDateChange(String data) {// ֱ��ʹ��һ���´������߳���ִ��UI�̵߳���Դ�Ļ����ǲ��еģ���android��
				// ��ȫ���ƣ�UI�߳��ǲ��������������߳����޸�����Դ�ģ��˴���ҪHandler��
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("data", data);
				message.setData(bundle);
				handler.sendMessage(message);
			}
		});

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			otherLocateData = msg.getData().getString("data");
			System.out.println("�յ��������ͻ���" + otherLocateData);
			// ���������ͻ��˵Ķ�λ���ݣ�
			Pattern overallPattern = Pattern
					.compile("\\[\\d+\\.\\d+:\\d+\\.\\d+:\\d+\\.\\d+:[-]?\\d+\\.\\d+:\\]");
			// String otherLocateData =
			// "[1:52.123465453:332.245345321:-1.0:]";//������
			Matcher matcher = overallPattern.matcher(otherLocateData);
			Pattern pattern = Pattern.compile("([-]?\\d+[.]?\\d{0,20})(:)");

			if (matcher.find()) {
				Matcher littleMatcher = pattern.matcher(matcher.group());
				int tempIndex = 0;
				while (littleMatcher.find()) {
					switch (tempIndex) {
					case 0:
						otherSocketIndex = Double.parseDouble(littleMatcher
								.group(1));
						break;
					case 1:
						otherLatitude = Double.parseDouble(littleMatcher
								.group(1));
						break;
					case 2:
						otherLongitude = Double.parseDouble(littleMatcher
								.group(1));
						break;
					case 3:
						otherDirection = Double.parseDouble(littleMatcher
								.group(1));
						break;
					}
					tempIndex++;
				}

				// ׼�� marker ��ͼƬ
				int socketTag = 0;
				int socketIcon = 0;
				switch ((int) otherSocketIndex + 1) {
				case 1:
					socketTag = R.drawable.location1;
					socketIcon = R.drawable.icon1;
					break;
				case 2:
					socketTag = R.drawable.location2;
					socketIcon = R.drawable.icon2;
					break;
				case 3:
					socketTag = R.drawable.location3;
					socketIcon = R.drawable.icon3;
					break;
				}
				bitmap = BitmapDescriptorFactory.fromResource(socketTag);

				// ׼�� marker option ��� marker ʹ��
				MarkerOptions markerOptions;
				if (otherDirection != -1.0) {
					markerOptions = new MarkerOptions()
							.icon(bitmap)
							.position(new LatLng(otherLatitude, otherLongitude))
							.flat(true).rotate((float) (360 - otherDirection));
				} else {
					markerOptions = new MarkerOptions()
							.icon(bitmap)
							.position(new LatLng(otherLatitude, otherLongitude))
							.flat(true);
				}

				// ��ȡ��ӵ� marker �������ں����Ĳ���
				mBaiduMap.clear(); // �����ʷ����
				Marker marker = (Marker) mBaiduMap.addOverlay(markerOptions);
				marker.setPerspective(true);// ����ԶС
				System.out.println("�յ��������ͻ���" + otherSocketIndex + "��"
						+ latitude + " " + longitude + " " + direction);
			}
		}
	};

	@Override
	public void onServiceDisconnected(ComponentName name) {
	}

}