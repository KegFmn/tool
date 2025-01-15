package com.likc.tool.util;

import com.ama.recharge.common.BizException;
import org.redisson.api.*;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 获取字符串值
     *
     * @param key Redis 键
     * @return Redis 中的值
     */
    public String getString(String key) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * 设置字符串值
     *
     * @param key   Redis 键
     * @param value 要设置的值
     */
    public void setString(String key, String value, Long time, TimeUnit timeUnit) {
        Duration duration;
        if (timeUnit.equals(TimeUnit.SECONDS)) {
            duration = Duration.ofSeconds(time);
        } else if (timeUnit.equals(TimeUnit.MINUTES)) {
            duration = Duration.ofMinutes(time);
        } else if (timeUnit.equals(TimeUnit.HOURS)) {
            duration = Duration.ofHours(time);
        } else {
            throw new BizException("不支持该时间单位");
        }

        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.set(value, duration);
    }

    /**
     * 限流
     *
     * @param key          限流key
     * @param rateType     限流类型
     * @param rate         速率
     * @param rateInterval 速率间隔
     * @return false 表示失败
     */
    public Boolean rateLimiter(String key, RateType rateType, Long rate, Long rateInterval, RateIntervalUnit rateIntervalUnit) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(rateType, rate, rateInterval, rateIntervalUnit);
        rateLimiter.expire(Instant.now().plus(1, ChronoUnit.HOURS));
        return rateLimiter.tryAcquire();
    }

    @PreDestroy
    public void shutdownRedisson() {
        if (!redissonClient.isShutdown()) {
            redissonClient.shutdown();
        }
    }
}
