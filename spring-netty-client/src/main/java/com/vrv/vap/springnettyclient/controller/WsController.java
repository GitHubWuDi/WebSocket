package com.vrv.vap.springnettyclient.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.vrv.vap.springnettyclient.model.RequestMessage;
import com.vrv.vap.springnettyclient.model.ResponseMessage;

@Controller
public class WsController {
    
	@MessageMapping("/welcome")
    @SendTo("/topic/getResponse")
	public ResponseMessage say(RequestMessage message){
		return new ResponseMessage("welcome,"+message.getContent());
	}
}
