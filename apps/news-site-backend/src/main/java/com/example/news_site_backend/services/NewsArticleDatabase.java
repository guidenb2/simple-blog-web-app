package com.example.news_site_backend.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Component
public class NewsArticleDatabase {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ArrayNode db = mapper.createArrayNode();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Logger log = LoggerFactory.getLogger(NewsArticleDatabase.class);
    
    public NewsArticleDatabase() {
        log.info("Initializing database...");
        
        log.debug("Aquiring write lock...");
        lock.writeLock().lock();
        log.debug("Aquired write lock...");
        try {
            try (InputStream in = getClass().getResourceAsStream("/news_2024.json")) {
                if (in != null) {
                    JsonNode initialData = mapper.readTree(in);
                        if (initialData.isArray()) {
                            db.addAll((ArrayNode) initialData);
                            log.info("Database successfully initialized");
                        } else {
                            log.error("news_2024.json is not an Array!");
                        }
                } else {
                    log.error("news_2024.json not found!");
                }
            }
        } catch (IOException e) {
            log.error("Failed to initialize database", e);
        } finally {
            log.debug("Releasing write lock");
            lock.writeLock().unlock();
        }
        
    }

    public JsonNode get(int page, int size) {
        lock.readLock().lock();
        try {
            int start = page * size;
            int end = start + size;
            return getSubArray(db, start, end);
        } finally {
            lock.readLock().unlock();
        }
    }

    private static ArrayNode getSubArray(ArrayNode arrayNode, int start, int end) {
        ArrayNode subArray = arrayNode.objectNode().arrayNode();
        for (int i = start; i < end; i++) {
            JsonNode element = arrayNode.get(i);
            subArray.add(element);
        }
        return subArray;
    }
}
