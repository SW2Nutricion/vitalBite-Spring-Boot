package com.vitalbite.documental;

import com.vitalbite.documental.infrastructure.config.DotenvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VitalbiteDocumentalApplication {

    public static void main(String[] args) {
        // Cargar .env antes que todo
        new DotenvConfig();
        SpringApplication.run(
                VitalbiteDocumentalApplication.class, args);
    }

}
