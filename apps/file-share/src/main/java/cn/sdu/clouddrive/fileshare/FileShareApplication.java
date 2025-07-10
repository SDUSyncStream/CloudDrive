package cn.sdu.clouddrive.fileshare;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.sdu.clouddrive.fileshare.mappers")
public class FileShareApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileShareApplication.class, args);
    }
}
