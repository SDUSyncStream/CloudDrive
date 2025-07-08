package cn.sdu.fileupdownservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@SpringBootApplication
@MapperScan("cn.sdu.fileupdownservice.mappers")
@EnableTransactionManagement
@EnableScheduling

public class FileupdownServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileupdownServiceApplication.class, args);
    }

}
