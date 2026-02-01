package com.roomreservationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class RoomReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomReservationServiceApplication.class, args);
	}

}
