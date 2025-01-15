package com.likc.tool.util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class LockUtils {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 获取锁对象
     * @param lockKey 锁的key
     * @return 获得锁对象
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    /**
     * 获取锁，如果锁可用立即返回true，如果锁不可用立即返回false。
     * @param lockKey 锁的key
     * @return 是否成功获得锁
     */
    public boolean tryLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.tryLock();
    }

    /**
     * 尝试获取锁
     * @param lockKey 锁的名称
     * @param waitTime 等待时间
     * @param leaseTime 锁的持有时间
     * @return 是否成功获得锁
     */
    public boolean tryLock(String lockKey, Long waitTime, Long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试获取锁
            return lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 尝试批量获取锁
     * @param rLockList 锁的名称
     * @param waitTime 等待时间
     * @param leaseTime 锁的持有时间
     * @return 是否成功获得锁
     */
    public boolean tryLocks(List<RLock> rLockList, Long waitTime, Long leaseTime) {
        RLock lock = redissonClient.getMultiLock(rLockList.toArray(new RLock[0]));
        try {
            // 尝试获取锁
            return lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 批量释放锁
     * @param rLockList 锁的名称
     * @return 是否成功获得锁
     */
    public void unLocks(List<RLock> rLockList) {
        RLock lock = redissonClient.getMultiLock(rLockList.toArray(new RLock[0]));
        lock.unlock();
    }

    /**
     * 释放锁
     * @param lockKey 锁的名称
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    @PreDestroy
    public void shutdownRedisson() {
        if (!redissonClient.isShutdown()) {
            redissonClient.shutdown();
        }
    }
}
