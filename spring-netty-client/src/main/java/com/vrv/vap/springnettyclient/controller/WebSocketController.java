package com.vrv.vap.springnettyclient.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/websocket")
public class WebSocketController {

	
	@GetMapping("/test")
	public String test(){
		return "websocket";
	}
	
	@GetMapping("/test1")
	public  String test1(){
		return "ws";
	}
}
