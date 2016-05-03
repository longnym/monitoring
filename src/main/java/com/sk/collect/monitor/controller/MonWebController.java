package com.sk.collect.monitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MonWebController {
	@RequestMapping("/")
	public String index(Model model) {
		model.addAttribute("message", "Welcome!");
		return "index";
	}
}