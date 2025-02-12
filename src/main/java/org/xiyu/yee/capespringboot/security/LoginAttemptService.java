package org.xiyu.yee.capespringboot.security;

import org.springframework.stereotype.Service;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    private final LoadingCache<String, Integer> attemptsCache;
    private final LoadingCache<String, LocalDateTime> blockCache;
    private final int MAX_ATTEMPT = 5;

    public LoginAttemptService() {
        attemptsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<>() {
                @Override
                public Integer load(String key) {
                    return 0;
                }
            });

        blockCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<>() {
                @Override
                public LocalDateTime load(String key) {
                    return LocalDateTime.now();
                }
            });
    }

    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
    }

    public void loginFailed(String key) {
        int attempts = attemptsCache.getUnchecked(key);
        attemptsCache.put(key, ++attempts);
    }

    public boolean isBlocked(String key) {
        return attemptsCache.getUnchecked(key) >= MAX_ATTEMPT;
    }

    // 获取剩余封禁时间（分钟）
    public long getRemainingBlockTime(String username) {
        try {
            LocalDateTime blockedUntil = blockCache.get(username);
            if (blockedUntil != null && LocalDateTime.now().isBefore(blockedUntil)) {
                return java.time.Duration.between(LocalDateTime.now(), blockedUntil).toMinutes();
            }
        } catch (ExecutionException e) {
            // 忽略异常
        }
        return 0;
    }

    // 获取剩余尝试次数
    public int getRemainingAttempts(String username) {
        try {
            int attempts = attemptsCache.get(username);
            if ("adminOPxiyu".equals(username)) {
                return 1 - attempts; // adminOPxiyu只有1次机会
            } else {
                return 3 - attempts; // 其他用户有3次机会
            }
        } catch (ExecutionException e) {
            return "adminOPxiyu".equals(username) ? 1 : 3;
        }
    }

    // 添加错误消息提示方法
    public String getBlockMessage(String username) {
        long remainingTime = getRemainingBlockTime(username);
        if (remainingTime > 0) {
            return String.format("账号已被锁定，请等待 %d 分钟后重试", remainingTime);
        }
        return null;
    }
    
    // 添加尝试次数提示方法
    public String getAttemptsMessage(String username) {
        int remaining = getRemainingAttempts(username);
        if ("adminOPxiyu".equals(username)) {
            return "管理员账号安全限制：密码错误将直接锁定5分钟";
        }
        return String.format("还剩 %d 次尝试机会", remaining);
    }
} 