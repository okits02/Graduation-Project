package com.okits02.delivery_serivce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DeliverySerivceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeliverySerivceApplication.class, args);
	}

}
