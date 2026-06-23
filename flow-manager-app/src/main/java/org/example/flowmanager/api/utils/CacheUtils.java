package org.example.flowmanager.api.utils;

import java.time.Duration;

public class CacheUtils {

    public final static String PREFIX_KEY_REDIS_SUBSCRIPTION = "user:subs:";

    public final static Duration EXPIRATION_24_HOURS = Duration.ofHours(24);
}