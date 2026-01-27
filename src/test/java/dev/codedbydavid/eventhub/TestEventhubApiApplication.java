package dev.codedbydavid.eventhub;

import org.springframework.boot.SpringApplication;

public class TestEventhubApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(EventHubApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
