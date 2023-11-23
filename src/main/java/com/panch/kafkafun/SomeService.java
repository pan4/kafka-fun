package com.panch.kafkafun;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SomeService {

    @Async("vtaExecutor")
    public void foo() throws InterruptedException {

        System.out.println(Thread.currentThread().getName());
        TimeUnit.SECONDS.sleep(3);
    }

    @Async("vtaExecutor")
    public void handle(String message) {
        try {
            System.out.println("handling ... " + message);
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
