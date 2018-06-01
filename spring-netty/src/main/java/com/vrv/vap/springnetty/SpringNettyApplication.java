package com.vrv.vap.springnetty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.vrv.vap.springnetty.server.NettyWebSocketServer;

@SpringBootApplication
public class SpringNettyApplication implements CommandLineRunner {
	@Value("${port}")
	private int port;
	@Value("${ip}")
	private String ip;
	@Value("${name}")
	private String name;
	@Autowired
	private NettyWebSocketServer nettyWebSocketServer;
	
	@Bean
    public NettyWebSocketServer nettyWebSocketServer() {
    	return new NettyWebSocketServer();
    }
	
	public static void main(String[] args) {
		SpringApplication.run(SpringNettyApplication.class, args);
	}
	

	@Override
	public void run(String... args) throws Exception {
		nettyWebSocketServer.bind(port,ip,name); 
	}
}
