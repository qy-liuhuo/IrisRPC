package io.github.qylh.iris.example.serverspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class ExampleServerSpringApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(ExampleServerSpringApplication.class, args);
    }

}
