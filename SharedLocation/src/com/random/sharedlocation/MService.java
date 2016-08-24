package com.random.sharedlocation;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MService extends Service {

	Socket socket = null;
	BufferedWriter writer = null;
	BufferedReader reader = null;

	public MService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new Binder();
	}

	public class Binder extends android.os.Binder {
		public void sendData(String localLocateData) {
			try {
				writer.write(localLocateData + "\n"); // 向服务端发送定位数据
				writer.flush();
				System.out.println("向服务器发送位置数据：" + localLocateData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public MService getService() {// 外界通过该方法来添加事件的绑定
			return MService.this;
		}
	}

	@Override
	public void onCreate() {

		super.onCreate();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// 生成套接字
					socket = new Socket("10.190.2.56", 12345);
					// 向输出流写数据
					writer = new BufferedWriter(new OutputStreamWriter(
							socket.getOutputStream()));
					// 获取输入流数据
					reader = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
					
					// 处理其他位置信息 
					String otherLocateData = "";
					while ((otherLocateData = reader.readLine()) != null) {
						System.out.println(otherLocateData);
						if (callBack != null) {
							callBack.onDateChange(otherLocateData); // 将otherLocateData传给MainActivity，
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private ICallBack callBack = null;// 初值设置为null

	public void setCallBack(ICallBack callBack) {
		this.callBack = callBack;
	}

	public interface ICallBack {
		void onDateChange(String data);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("定位服务关闭");
	}
}
