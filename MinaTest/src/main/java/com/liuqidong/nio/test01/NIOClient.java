package com.liuqidong.nio.test01;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOClient {
	private static int flag = 1;
	
	private static int port = 7080;
	
	private  static int blockSize = 4096;
	
	private static ByteBuffer sendBuffer = ByteBuffer.allocate(blockSize);
	private static ByteBuffer recvBuffer = ByteBuffer.allocate(blockSize);
	
	private final static InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", port);
	
	public static void main(String[] args) throws IOException {
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		//打开选择器
		Selector selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
		socketChannel.connect(serverAddress);
		
		Set<SelectionKey> selectionKeys;
		Iterator<SelectionKey> iter;
		SelectionKey selectionKey;
		SocketChannel client;
		String receiveText;
		String sendText;
		
		int count = 0;
		
		while(true) {
			selector.select();
			selectionKeys = selector.selectedKeys();
			iter= selectionKeys.iterator();
			while (iter.hasNext()) {
				selectionKey = iter.next();
				if(selectionKey.isConnectable()) {
					System.out.println("client connect");
					client = (SocketChannel) selectionKey.channel();
					if(client.isConnectionPending()) {
						client.finishConnect();
						System.out.println("客户端完成连接操作");
						sendBuffer.clear();
						sendBuffer.put("Hello,Server".getBytes());
						sendBuffer.flip();
						 client.write(sendBuffer);
						
					}
					client.register(selector, SelectionKey.OP_READ);
				}
				
				if(selectionKey.isReadable()) {
					client = (SocketChannel) selectionKey.channel();
					recvBuffer.clear();
					count = client.read(recvBuffer);
					if(count > 0) {
						receiveText = new String(recvBuffer.array(),0,count);
						System.out.println("客户端接收到服务端的数据："+receiveText);
						client.register(selector, SelectionKey.OP_WRITE);
					}
				}
				
				if(selectionKey.isWritable()) {
					sendBuffer.clear();
					client = (SocketChannel) selectionKey.channel();
					sendText = "Msg send to Server->"+flag++;
					sendBuffer.put(sendText.getBytes());
					sendBuffer.flip();
					client.write(sendBuffer);
					System.out.println("客户端发送数据给服务端："+sendText);
					client.register(selector, SelectionKey.OP_READ);
				}
			}
			
			selectionKeys.clear(); 
		}
		
	}
}
