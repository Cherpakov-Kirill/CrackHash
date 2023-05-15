package ru.nsu.fit.crackhash.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class CrackHashManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrackHashManagerApplication.class, args);
    }

}
