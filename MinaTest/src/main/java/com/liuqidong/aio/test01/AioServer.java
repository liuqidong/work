package com.liuqidong.aio.test01;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;

public class AioServer {
	
	
	public AioServer(int port) throws IOException {
		AsynchronousServerSocketChannel listener =  AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("127.0.0.1",port));
		listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

			@Override
			public void completed(AsynchronousSocketChannel result, Void attachment) {
				listener.accept(null, this);//接收下一个连接
				handler(result);
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				System.out.println("异步IO失败");
			}
		});
	}
	
	protected void handler(AsynchronousSocketChannel result) {
		ByteBuffer buffer = ByteBuffer.allocate(32);
		try {
			result.read(buffer).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		buffer.flip();
		System.out.println("服务端接收："+buffer.get());
	}

	public static void main(String[] args) throws Exception {
		int port = 7080;
		AioServer aioServer = new AioServer(port);
		System.out.println("服务器监听端口："+port);
		Thread.sleep(1000000);
	}
}
