package com.example.propertysupervision;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.propertysupervision.persistence.mapper")
@SpringBootApplication
public class PropertySupervisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PropertySupervisionApplication.class, args);
    }

}
