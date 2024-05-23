package com.zunf.examplespringbootprovider;

import com.zunf.rpc.annotation.EnableZunRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableZunRpc
@SpringBootApplication
public class ExampleSpringBootProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleSpringBootProviderApplication.class, args);
    }

}
