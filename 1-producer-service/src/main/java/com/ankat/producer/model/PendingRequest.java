package com.ankat.producer.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
public class PendingRequest {
    private final Map<String,CompletableFuture<String>> map = new ConcurrentHashMap<>();
    private CompletableFuture<String> completableFuture;

    public void add(String traceId,CompletableFuture<String> completableFuture){
        map.put(traceId,completableFuture);
    }
    public void remove(String traceId){
        map.remove(traceId);
    }
    public CompletableFuture get(String traceId){
        return map.get(traceId);
    }
}