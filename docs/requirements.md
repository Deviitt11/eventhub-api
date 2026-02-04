# Requirements

This document captures functional requirements as user stories and acceptance criteria.

## US-001 Create an Event
**As a client**, I want to create an event, so it can be listed and accessed later.

### Acceptance Criteria (Gherkin)
Scenario: Create event with valid payload  
  Given a valid event payload  
  When I POST /api/v1/events  
  Then I receive 201 Created  
  And the response contains an id  

Scenario: Reject invalid time range  
  Given startsAt is after endsAt  
  When I POST /api/v1/events  
  Then I receive 400 Bad Request  
  And the error payload follows the standard error format  

## US-002 Get an Event by id
**As a client**, I want to fetch an event by its id, so I can display details.

### Acceptance Criteria
Scenario: Get existing event  
  Given an event exists  
  When I GET /api/v1/events/{id}  
  Then I receive 200 OK  
  And the response matches the event data  

Scenario: Get missing event  
  Given an event does not exist  
  When I GET /api/v1/events/{id}  
  Then I receive 404 Not Found  
  And the error payload follows the standard error format  

## US-003 List Events
**As a client**, I want to list events, so I can browse them.

### Acceptance Criteria
Scenario: List events  
  Given multiple events exist  
  When I GET /api/v1/events  
  Then I receive 200 OK  
  And the response contains a list of events  

## US-004 Update an Event
**As a client**, I want to update an event, so I can correct information.

### Acceptance Criteria
Scenario: Update existing event  
  Given an event exists  
  When I PUT /api/v1/events/{id}  
  Then I receive 200 OK  
  And the updated fields are returned  

## US-005 Delete an Event
**As a client**, I want to delete an event, so it is no longer available.

### Acceptance Criteria
Scenario: Delete existing event  
  Given an event exists  
  When I DELETE /api/v1/events/{id}  
  Then I receive 204 No Content  
  And subsequent GET returns 404  