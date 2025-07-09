package cn.sdu.clouddrive.filemanageservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.sdu.clouddrive.filemanageservice.mapper")
public class FileManageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileManageServiceApplication.class, args);
    }

}
