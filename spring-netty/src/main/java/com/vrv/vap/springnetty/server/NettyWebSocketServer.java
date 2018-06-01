package com.vrv.vap.springnetty.server;



import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

@Component
public class NettyWebSocketServer {
       
	private static Logger logger = Logger.getLogger(NettyWebSocketServer.class);
	
	
	public void bind(int port,String ip,String name) throws InterruptedException{
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup(); // 一组NIO线程 
		
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap(); // 启动NIO服务端的辅助启动类
			serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100).handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
                                   ChannelPipeline pipeline = ch.pipeline();
                                   pipeline.addLast("http-codec",new HttpServerCodec()); //请求和应答消息编码或者解码为HTTP消息
                                   pipeline.addLast("aggregator", new HttpObjectAggregator(65536)); //将HTTP消息的多个部分组合成一条完整的HTTP消息
                                   ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler()); //向客户端发送HTML5文件
                                   pipeline.addLast(new WebSocketServerHandler(port,ip,name));
						}
					});
					
			// 绑定端口，同步等待成功
			ChannelFuture f = serverBootstrap.bind(new InetSocketAddress(port)).sync();
			logger.info("Web socket server started at port "+port+".");
			logger.info("Open your browser and navigate to http://localhost:"+port+"/");
			// 等待服务端监听端口关闭
			f.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
}
