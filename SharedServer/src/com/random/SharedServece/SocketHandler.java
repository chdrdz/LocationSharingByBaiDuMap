package com.random.SharedServece;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZH on 2016/1/7.
 */
public class SocketHandler extends IoHandlerAdapter {

	private List<IoSession> sessionsList = new ArrayList<IoSession>();

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
		System.out.println("链接已经建立");
		sessionsList.add(session);
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {

		String str = ((String) message).trim();
		System.out.println(str);
		for (IoSession ioSession : sessionsList) {
			if (ioSession != session) {
				ioSession.write(str); // 向客户端发送位置数据
			}
		}
		super.messageReceived(session, message);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		sessionsList.remove(session);
		System.out.println("链接已经关闭");
		super.sessionClosed(session);
	}
}
