package com.CarRentalSystem.CarRentals.Services;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class CarCacheService {

    public static final String AVAILABLE_CARS_CACHE = "availableCars";

    @CacheEvict(value = AVAILABLE_CARS_CACHE, allEntries = true)
    public void evictAvailableCarsCache() {
        // Cache eviction handled by Spring AOP proxy
    }
}
