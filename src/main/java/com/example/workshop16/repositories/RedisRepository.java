package com.example.workshop16.repositories;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.json.Json;

@Repository
public class RedisRepository {
    
    @Autowired
    RedisTemplate<String, Object> template;

    public String insertRecord(String record) {
        // Generate ID
        String id = generateID();
        
        while(template.keys("*").contains(id)) {
            id = generateID();
        }
        
        return insertRecord(id, record);
    }

    public String insertRecord(String id, String record) {

         // Insert Record
         template.opsForValue().set(id, record);        

         // Get Count
         template.opsForValue().setIfAbsent("insertCount", "0");
         long count = template.opsForValue().increment("insertCount");
 
         // Generate Json-String
         // { “insert_count”: 1, “id”: <Redis key>  }
         String payload = Json.createObjectBuilder()
                                         .add("insert_count", count)
                                         .add("id", id)
                                         .build()
                                         .toString();
        return payload;
    }
    
    public Optional<String> getRecordById(String id) {
        Optional<String> result = Optional.ofNullable((String) template.opsForValue().get(id));
        return result;
    }

    public boolean keyExists(String id) {
        return template.keys("*").contains(id);
    }

    public String updateRecordById(String id, String payload) {
        template.opsForValue().set(id, payload);
        template.opsForValue().setIfAbsent("updateCount", "0");
        long count = template.opsForValue().increment("updateCount");
        
        return Json.createObjectBuilder()
                    .add("update_count", count)
                    .add("id", id)
                    .build()
                    .toString();
    }

    private String generateID() {

        long max = Long.parseLong("ffffffff", 16);
        long randLong = ThreadLocalRandom.current().nextLong(0, max + 1);
        String fname = ("00000000" + Long.toHexString(randLong)).substring(8);

        return fname;
    }

}
