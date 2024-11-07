package com.educationplatform.gangchew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinalProjectApplication {

    public static void main(String[] args) {

        SpringApplication.run(FinalProjectApplication.class, args);
        // 메모리 사용량 출력
        long heapSize = Runtime.getRuntime().totalMemory();
        System.out.println("서비스 시작, HEAP Size(M) : "+ heapSize / (1024*1024) + " MB");
    }

}
