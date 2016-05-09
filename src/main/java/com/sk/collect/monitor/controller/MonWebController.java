package com.sk.collect.monitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MonWebController {
	@RequestMapping("/mon")
	public String monitor() {
		return "monitor";
	}
	
	@RequestMapping("/schd")
	public String schedule() {
		return "schedule";
	}
}