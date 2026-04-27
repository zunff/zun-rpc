package com.zunf.examplespringbootconsumer;

import com.zunf.rpc.annotation.EnableZunRpcConsumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableZunRpcConsumer
@SpringBootApplication
public class ExampleSpringBootConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleSpringBootConsumerApplication.class, args);
    }

}
