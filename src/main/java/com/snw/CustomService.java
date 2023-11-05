package com.snw;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
public class CustomService {

    private static final String CACHE_NAME = "app-cache";

    @Cacheable(value = CACHE_NAME)
    public void repeatableMethod() {
        System.out.println("waves of inner");
    }

    @Caching(evict = {@CacheEvict(value = CACHE_NAME, allEntries = true)})
    public void evict() {
        System.out.println("cache evicted");
    }
}
