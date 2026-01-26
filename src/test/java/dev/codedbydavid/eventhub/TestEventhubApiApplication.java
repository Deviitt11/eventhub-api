package dev.codedbydavid.eventhub;

import org.springframework.boot.SpringApplication;

public class TestEventhubApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(EventhubApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
