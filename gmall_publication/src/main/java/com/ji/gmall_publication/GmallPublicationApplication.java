package com.ji.gmall_publication;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.ji.gmall_publication.mapper")
public class GmallPublicationApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPublicationApplication.class, args);
    }

}
