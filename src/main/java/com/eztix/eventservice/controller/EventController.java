package com.eztix.eventservice.controller;

import com.eztix.eventservice.model.Event;

import com.eztix.eventservice.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/api/v1/event")
    public ResponseEntity<Event> addEvent(@RequestBody Event event) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.addNewEvent(event));

    }

    @GetMapping("/api/v1/event/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.getEventById(id));
    }

    @PutMapping("/api/v1/event/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event event) {
        event.setId(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.updateEvent(event));
    }

    @GetMapping("/api/v1/event")
    public ResponseEntity<Iterable<Event>>  getAllEvent() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.getAllEvents());
    }

}