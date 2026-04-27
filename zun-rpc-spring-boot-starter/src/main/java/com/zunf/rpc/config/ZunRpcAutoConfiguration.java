package com.zunf.rpc.config;

import com.zunf.rpc.fault.retry.RetryStrategy;
import com.zunf.rpc.fault.retry.impl.NoRetryStrategy;
import com.zunf.rpc.fault.tolerance.ToleranceStrategy;
import com.zunf.rpc.fault.tolerance.impl.FailFastToleranceStrategy;
import com.zunf.rpc.loadbalancer.LoadBalancer;
import com.zunf.rpc.loadbalancer.impl.RoundRobinLoadBalancer;
import com.zunf.rpc.registry.Registry;
import com.zunf.rpc.registry.impl.DirectRegistry;
import com.zunf.rpc.serializer.Serializer;
import com.zunf.rpc.serializer.impl.JdkSerializer;
import com.zunf.rpc.serializer.impl.JsonSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({RpcConfig.class, RegistryConfig.class})
public class ZunRpcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Serializer.class)
    public Serializer serializer() {
        return new JsonSerializer();
    }

    @Bean
    @ConditionalOnMissingBean(Registry.class)
    public Registry registry() {
        return new DirectRegistry();
    }

    @Bean
    @ConditionalOnMissingBean(LoadBalancer.class)
    public LoadBalancer loadBalancer() {
        return new RoundRobinLoadBalancer();
    }

    @Bean
    @ConditionalOnMissingBean(RetryStrategy.class)
    public RetryStrategy retryStrategy() {
        return new NoRetryStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(ToleranceStrategy.class)
    public ToleranceStrategy toleranceStrategy() {
        return new FailFastToleranceStrategy();
    }
}
