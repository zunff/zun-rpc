package com.zunf.examplespringbootconsumer;

import com.zunf.rpc.annotation.EnableZunRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableZunRpc
@SpringBootApplication
public class ExampleSpringBootConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleSpringBootConsumerApplication.class, args);
    }

}
