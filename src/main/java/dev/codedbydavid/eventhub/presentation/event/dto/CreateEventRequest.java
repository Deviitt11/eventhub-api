package dev.codedbydavid.eventhub.presentation.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class CreateEventRequest {
	@NotBlank(message = "Title is required")
	@Size(max = 255, message = "Title must not exceed 255 characters")
	private String title;

	@NotNull(message = "startsAt is required")
	private Instant startsAt;

	@NotNull(message = "endsAt is required")
	private Instant endsAt;

	public CreateEventRequest() {
	}

	public CreateEventRequest(String title, Instant startsAt, Instant endsAt) {
		this.title = title;
		this.startsAt = startsAt;
		this.endsAt = endsAt;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Instant getStartsAt() {
		return startsAt;
	}

	public void setStartsAt(Instant startsAt) {
		this.startsAt = startsAt;
	}

	public Instant getEndsAt() {
		return endsAt;
	}

	public void setEndsAt(Instant endsAt) {
		this.endsAt = endsAt;
	}
}