package com.bookstore.bookstore_backend.services.Impl;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionTimerService implements Serializable {

    private static final long serialVersionUID = 1L;  // 序列化版本号
    private long startTime;  // 起始时间戳（毫秒）

    /**
     * 初始化并开始计时。
     */
    public void startTimer() {
        this.startTime = System.currentTimeMillis();
        System.out.println("Session timer started at: " + startTime);
    }

    /**
     * 停止计时，返回会话持续时间（秒）。
     * @return 会话时间（秒）
     */
    public long stopTimer() {
        System.out.println("Entering stopTimer, startTime: " + startTime);
        if (startTime == 0) {
            System.out.println("Timer was not started.");
            throw new IllegalStateException("Timer not started");
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Session timer stopped at: " + endTime);
        long durationMillis = endTime - startTime;
        long durationSeconds = durationMillis / 1000;  // 转换为秒
        System.out.println("Session timer stopped. Duration: " + durationSeconds + " seconds");
        startTime = 0;  // 重置
        return durationSeconds;
    }
}