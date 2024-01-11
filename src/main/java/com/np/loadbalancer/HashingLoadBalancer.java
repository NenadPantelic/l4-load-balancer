package com.np.loadbalancer;

import com.np.data.ClientContext;
import com.np.data.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HashingLoadBalancer implements LoadBalancer {

    private static final Logger log = LoggerFactory.getLogger(HashingLoadBalancer.class);

    private final List<Server> servers;

    public HashingLoadBalancer(List<Server> servers) {
        this.servers = servers;
    }

    @Override
    public LoadBalancingStrategy getStrategy() {
        return LoadBalancingStrategy.IP_HASHING;
    }

    @Override
    public Server route(ClientContext clientContext) {
        Server server = servers.get(clientContext.hashCode() % servers.size());
        log.info("The request made by {} will be routed to {}", clientContext.ip(), server);
        return server;
    }
}
