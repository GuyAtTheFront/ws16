package com.example.workshop16.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.workshop16.repositories.RedisRepository;

import jakarta.json.Json;

@RestController
@RequestMapping(
    path="/api", 
    produces = MediaType.APPLICATION_JSON_VALUE
    // consumes = MediaType.APPLICATION_JSON_VALUE)
    )
public class RestControllers {
    
    @Autowired
    Environment env;

    @Autowired
    RedisRepository redis;
    
    @PostMapping(path="/boardgame", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> test(@RequestBody String payload) {

        // save to redis
        String response = redis.insertRecord(payload);

        return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
    }

    @GetMapping(path="/boardgame/{id}")
    public ResponseEntity<String> getBoardgameByID(@PathVariable String id) {

        // get response from repo
        Optional<String> response = redis.getRecordById(id);
        System.out.println("test \n\n\n\n\n\n");
        
        if (response.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Json.createObjectBuilder()
                            .add("error", "id %s not found".formatted(id))
                            .build()
                            .toString());
        }

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.get());
                
    }

    @PutMapping(path="boardgame/{id}", consumes=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateGameboardById(
                                    @PathVariable String id, 
                                    @RequestParam(name="upsert", required=false) String upsert, 
                                    @RequestBody String payload) {
        
        boolean isUpsert = false;

        try {
            isUpsert = Boolean.parseBoolean(upsert);
        } catch (Exception e) {
            isUpsert = false;
        }

        // upsert true and exist --> update + 200
        // upsert false and exist --> update + 200
        // upsert true and dont exist --> insert + 201
        // upsert false and dont exist --> 400
        String response;

        if(redis.keyExists(id)) {
        // { “update_count”: <count>, “id”: <Redis key>  } 
            response = redis.updateRecordById(id, payload);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }

        if(isUpsert) {
            // create to redis
            response = redis.insertRecord(id, payload);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }

        return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Json.createObjectBuilder()
                .add("error", "id %s does not exist".formatted(id))
                .build()
                .toString());
    }

}
