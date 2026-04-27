package com.zunf.examplespringbootprovider;

import com.zunf.rpc.annotation.EnableZunRpcProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableZunRpcProvider
@SpringBootApplication
public class ExampleSpringBootProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleSpringBootProviderApplication.class, args);
    }

}
