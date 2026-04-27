package com.zunf.rpc.config;

import com.zunf.rpc.fault.retry.RetryStrategy;
import com.zunf.rpc.fault.retry.impl.FixedIntervalRetryStrategy;
import com.zunf.rpc.fault.retry.impl.NoRetryStrategy;
import com.zunf.rpc.fault.tolerance.ToleranceStrategy;
import com.zunf.rpc.fault.tolerance.impl.FailBackToleranceStrategy;
import com.zunf.rpc.fault.tolerance.impl.FailFastToleranceStrategy;
import com.zunf.rpc.fault.tolerance.impl.FailOverToleranceStrategy;
import com.zunf.rpc.fault.tolerance.impl.FailSafeToleranceStrategy;
import com.zunf.rpc.loadbalancer.LoadBalancer;
import com.zunf.rpc.loadbalancer.impl.ConsistentHashLoadBalancer;
import com.zunf.rpc.loadbalancer.impl.RandomLoadBalancer;
import com.zunf.rpc.loadbalancer.impl.RoundRobinLoadBalancer;
import com.zunf.rpc.registry.Registry;
import com.zunf.rpc.registry.impl.DirectRegistry;
import com.zunf.rpc.registry.impl.EtcdRegistry;
import com.zunf.rpc.serializer.Serializer;
import com.zunf.rpc.serializer.SerializerRegistry;
import com.zunf.rpc.serializer.impl.HessianSerializer;
import com.zunf.rpc.serializer.impl.JdkSerializer;
import com.zunf.rpc.serializer.impl.JsonSerializer;
import com.zunf.rpc.serializer.impl.KryoSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZunRpcAutoConfiguration {

    // ==================== Serializer（全部注册到 SerializerRegistry）====================

    @Bean
    public JdkSerializer jdkSerializer() {
        SerializerRegistry.register("jdk", new JdkSerializer());
        return new JdkSerializer();
    }

    @Bean
    public JsonSerializer jsonSerializer() {
        JsonSerializer serializer = new JsonSerializer();
        SerializerRegistry.register("json", serializer);
        return serializer;
    }

    @Bean
    public HessianSerializer hessianSerializer() {
        HessianSerializer serializer = new HessianSerializer();
        SerializerRegistry.register("hessian", serializer);
        return serializer;
    }

    @Bean
    public KryoSerializer kryoSerializer() {
        KryoSerializer serializer = new KryoSerializer();
        SerializerRegistry.register("kryo", serializer);
        return serializer;
    }

    // ==================== Registry ====================

    @Bean
    @ConditionalOnMissingBean(Registry.class)
    @ConditionalOnProperty(name = "zun.rpc.registry.type", havingValue = "etcd")
    public Registry etcdRegistry() {
        return new EtcdRegistry();
    }

    @Bean
    @ConditionalOnMissingBean(Registry.class)
    @ConditionalOnProperty(name = "zun.rpc.registry.type", havingValue = "direct")
    public Registry directRegistry() {
        return new DirectRegistry();
    }

    // ==================== LoadBalancer ====================

    @Bean
    @ConditionalOnMissingBean(LoadBalancer.class)
    @ConditionalOnProperty(name = "zun.rpc.load-balancer", havingValue = "roundRobin", matchIfMissing = true)
    public LoadBalancer roundRobinLoadBalancer() {
        return new RoundRobinLoadBalancer();
    }

    @Bean
    @ConditionalOnMissingBean(LoadBalancer.class)
    @ConditionalOnProperty(name = "zun.rpc.load-balancer", havingValue = "random")
    public LoadBalancer randomLoadBalancer() {
        return new RandomLoadBalancer();
    }

    @Bean
    @ConditionalOnMissingBean(LoadBalancer.class)
    @ConditionalOnProperty(name = "zun.rpc.load-balancer", havingValue = "consistentHash")
    public LoadBalancer consistentHashLoadBalancer() {
        return new ConsistentHashLoadBalancer();
    }

    // ==================== RetryStrategy ====================

    @Bean
    @ConditionalOnMissingBean(RetryStrategy.class)
    @ConditionalOnProperty(name = "zun.rpc.retry-strategy", havingValue = "no", matchIfMissing = true)
    public RetryStrategy noRetryStrategy() {
        return new NoRetryStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(RetryStrategy.class)
    @ConditionalOnProperty(name = "zun.rpc.retry-strategy", havingValue = "fixedInterval")
    public RetryStrategy fixedIntervalRetryStrategy() {
        return new FixedIntervalRetryStrategy();
    }

    // ==================== ToleranceStrategy ====================

    @Bean
    @ConditionalOnMissingBean(ToleranceStrategy.class)
    @ConditionalOnProperty(name = "zun.rpc.tolerance-strategy", havingValue = "failFast", matchIfMissing = true)
    public ToleranceStrategy failFastToleranceStrategy() {
        return new FailFastToleranceStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(ToleranceStrategy.class)
    @ConditionalOnProperty(name = "zun.rpc.tolerance-strategy", havingValue = "failSafe")
    public ToleranceStrategy failSafeToleranceStrategy() {
        return new FailSafeToleranceStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(ToleranceStrategy.class)
    @ConditionalOnProperty(name = "zun.rpc.tolerance-strategy", havingValue = "failBack")
    public ToleranceStrategy failBackToleranceStrategy() {
        return new FailBackToleranceStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(ToleranceStrategy.class)
    @ConditionalOnProperty(name = "zun.rpc.tolerance-strategy", havingValue = "failOver")
    public ToleranceStrategy failOverToleranceStrategy() {
        return new FailOverToleranceStrategy();
    }
}
