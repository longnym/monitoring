package com.sk.collect.monitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MonWebController {
	@RequestMapping("/mon")
	public String monitor() {
		return "monitor";
	}
	
	@RequestMapping("/mon2")
	public String monitor2() {
		return "monitor2";
	}
	
	@RequestMapping("/mon3")
	public String monitor3() {
		return "monitor3";
	}
	
	@RequestMapping("/schd")
	public String schedule() {
		return "schedule";
	}
}