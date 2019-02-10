package com.liuqidong.nio.test01;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {
	
	private int flag = 1;
	
	private int port = 7080;
	
	private int blockSize = 4096;
	
	private ByteBuffer sendBuffer = ByteBuffer.allocate(blockSize);
	private ByteBuffer recvBuffer = ByteBuffer.allocate(blockSize);
	
	private Selector selector;
	
	
	public NIOServer() throws Exception {
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		//设置是否阻塞
		serverSocketChannel.configureBlocking(false);
		ServerSocket serverSocket = serverSocketChannel.socket();
		//绑定IP和端口
		serverSocket.bind(new InetSocketAddress(port));
		//打开选择器
		selector = selector.open();
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("server start ->"+port);
	}
	
	//监听
	public void listen() throws IOException {
		while (true) {
			selector.select();
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> iter = keys.iterator();
			while (iter.hasNext()) {
				SelectionKey selectionKey = iter.next();
				iter.remove();
				//业务逻辑
				handleKey(selectionKey);
			}
		}
	}
	
	public void handleKey(SelectionKey selectionKey) throws IOException {
		ServerSocketChannel server = null;
		SocketChannel client = null;
		String recvieText;
		String sendText;
		int count = 0;
		if(selectionKey.isAcceptable()) {
			server = (ServerSocketChannel) selectionKey.channel();
			client = server.accept();
			client.configureBlocking(false);
			client.register(selector, SelectionKey.OP_READ);
		}else if(selectionKey.isReadable()) {
			client = (SocketChannel) selectionKey.channel();
			count = client.read(recvBuffer);
			if(count > 0 ) {
				recvieText = new String(recvBuffer.array(),0,count);
				System.out.println("服务端接收到客户端数据："+recvieText);
				client.register(selector, SelectionKey.OP_WRITE);
			}
		}else if(selectionKey.isWritable()) {
			sendBuffer.clear();
			client = (SocketChannel) selectionKey.channel();
			//发送数据
			sendText = "msg send to client:"+(flag++);
			sendBuffer.put(sendText.getBytes());
			sendBuffer.flip();
			client.write(sendBuffer);
			System.out.println("服务端发送数据给客户端："+sendText);
		}
	}



	public static void main(String[] args) throws Exception {
		NIOServer server = new NIOServer();
		server.listen();
	}
}
