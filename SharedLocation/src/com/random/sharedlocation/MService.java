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
				writer.write(localLocateData + "\n"); // �����˷��Ͷ�λ����
				writer.flush();
				System.out.println("�����������λ�����ݣ�" + localLocateData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public MService getService() {// ���ͨ���÷���������¼��İ�
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
					// �����׽���
					socket = new Socket("10.190.2.56", 12345);
					// �������д����
					writer = new BufferedWriter(new OutputStreamWriter(
							socket.getOutputStream()));
					// ��ȡ����������
					reader = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
					
					// ��������λ����Ϣ 
					String otherLocateData = "";
					while ((otherLocateData = reader.readLine()) != null) {
						System.out.println(otherLocateData);
						if (callBack != null) {
							callBack.onDateChange(otherLocateData); // ��otherLocateData����MainActivity��
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private ICallBack callBack = null;// ��ֵ����Ϊnull

	public void setCallBack(ICallBack callBack) {
		this.callBack = callBack;
	}

	public interface ICallBack {
		void onDateChange(String data);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("��λ����ر�");
	}
}
