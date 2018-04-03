package com.poetry.hearing;

import com.poetry.hearing.filter.LoginFilter;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.sql.SQLException;

@MapperScan("com.poetry.hearing.dao")
@SpringBootApplication
//@Configuration
//@ComponentScan
//@EnableAutoConfiguration
//public class HearingApplication extends SpringBootServletInitializer{
public class HearingApplication{

//	@Override
//	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//		return application.sources(HearingApplication.class);
//	}

	@Autowired
	private LoginFilter loginFilter;

	@Bean
	public FilterRegistrationBean loginFilterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(loginFilter);
		registration.addUrlPatterns("/myself");
		registration.addUrlPatterns("/collect");
		registration.setOrder(1);
		return registration;
	}

	public static void main(String[] args) {
		SpringApplication.run(HearingApplication.class, args);
	}
}