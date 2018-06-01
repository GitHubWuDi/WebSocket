package com.vrv.vap.springnetty.server;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;


public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

	private static final Logger logger = Logger.getLogger(WebSocketServerHandler.class.getName());
	private WebSocketServerHandshaker handshaker;
	private int port;
	private String ip;
	private String name;
	
	public WebSocketServerHandler(int port,String ip,String name){
		this.port = port;
		this.ip = ip;
		this.name = name;
	}
	
	
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
		//传统的HTTP接入	
		if(msg instanceof FullHttpRequest){
    		handleHttpRequest(ctx,(FullHttpRequest)msg);
    	}
    	//WebSocket接入	
		if(msg instanceof WebSocketFrame){
			handleWebSocketFrame(ctx,(WebSocketFrame)msg);
		}
		
	}
	
	private void handleHttpRequest(ChannelHandlerContext ctx,FullHttpRequest req){
		if(!"websocket".equals(req.headers().get("Upgrade"))){
			sendHttpResponse(ctx,req,new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://"+ip+":"+port+"/"+name,null,false);
		handshaker = wsFactory.newHandshaker(req);
		if(handshaker==null){
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		}else{
			handshaker.handshake(ctx.channel(), req);
		}
	}
	
	private static void sendHttpResponse(ChannelHandlerContext ctx,FullHttpRequest req,FullHttpResponse res){
		if(res.status().code()!=200){
			ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpHeaderUtil.setContentLength(res, res.content().readableBytes());
		}
		ChannelFuture channelFuture = ctx.channel().writeAndFlush(res);
		if(HttpHeaderUtil.isKeepAlive(req)||res.status().code()!=200){
			channelFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	private void handleWebSocketFrame(ChannelHandlerContext ctx,WebSocketFrame frame){
		//判断是否是关闭链路指令
		if(frame instanceof CloseWebSocketFrame){
			handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
			return;
		}
		//判断是否是Ping消息
		if(frame instanceof PingWebSocketFrame){
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
		}
		
		//不支持文本文件以外的信息
		if(!(frame instanceof TextWebSocketFrame)){
			throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
		}
		String request = ((TextWebSocketFrame)frame).text();
		ctx.channel().writeAndFlush(new TextWebSocketFrame(request+",欢迎使用Netty WebSocket服务，现在时刻："+new Date().toString()));
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

}
