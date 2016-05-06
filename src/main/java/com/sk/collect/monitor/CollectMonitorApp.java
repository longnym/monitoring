package com.sk.collect.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("elasticsearch.properties")
@PropertySource("scheduler.properties")
public class CollectMonitorApp {
	public static void main(String[] args) {
		SpringApplication.run(CollectMonitorApp.class, args);
	}
}