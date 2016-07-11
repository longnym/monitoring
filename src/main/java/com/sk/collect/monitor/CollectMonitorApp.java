package com.sk.collect.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import com.sk.collect.monitor.tcp.TcpServer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
@PropertySource("elasticsearch.properties")
@PropertySource("scheduler.properties")
@PropertySource("tcpserver.properties")
public class CollectMonitorApp {
	public static void main(String[] args) {
		ApplicationContext appContext = SpringApplication.run(CollectMonitorApp.class, args);

		TcpServer server = appContext.getBean(TcpServer.class);
		try {
			server.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurerAdapter() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/mon/*").allowedOrigins("http://localhost:8080");
			}
		};
	}
}