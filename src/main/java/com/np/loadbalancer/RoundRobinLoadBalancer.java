package com.np.loadbalancer;

import com.np.data.ClientContext;
import com.np.data.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {

    Logger log = LoggerFactory.getLogger(RoundRobinLoadBalancer.class);

    private final List<Server> servers;
    private final AtomicInteger counter = new AtomicInteger(0);

    public RoundRobinLoadBalancer(List<Server> servers) {
        this.servers = servers;
    }

    @Override
    public LoadBalancingStrategy getStrategy() {
        return LoadBalancingStrategy.ROUND_ROBIN;
    }

    @Override
    public Server route(ClientContext clientContext) {
        Server server = servers.get(
                // cyclic choosing
                counter.getAndUpdate(value -> (value + 1) % servers.size())
        );

        log.info("The request made by {} will be routed to {}.", clientContext.ip(), server);
        return server;
    }
}
