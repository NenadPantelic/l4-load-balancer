package com.np.util;

import com.np.data.Server;
import com.np.loadbalancer.LoadBalancingStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ApplicationPropertiesManager {

    private final int port;
    private final LoadBalancingStrategy loadBalancingStrategy;
    private final List<Server> servers;

    public ApplicationPropertiesManager(String filepath) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(filepath)) {

            Properties properties = new Properties();

            // load a properties file
            properties.load(input);

            String portProperty = properties.getProperty("load-balancer.port");
            throwExceptionIfBlank("load-balancer.port", portProperty);
            port = Integer.parseInt(portProperty);

            Object strategyProperty = properties.getOrDefault("load-balancer.strategy", "ROUND_ROBIN");
            loadBalancingStrategy = LoadBalancingStrategy.valueOf((String) strategyProperty);

            String serversProperty = properties.getProperty("load-balancer.servers");
            throwExceptionIfBlank("load-balancer.servers", serversProperty);

            servers = new ArrayList<>();
            for (String server : serversProperty.split(",")) {
                URI uri = URI.create(server);
                servers.add(new Server(uri.getHost(), uri.getPort()));
            }
        }
    }

    public int getPort() {
        return port;
    }

    public LoadBalancingStrategy getLoadBalancingStrategy() {
        return loadBalancingStrategy;
    }

    public List<Server> getServers() {
        return servers;
    }

    private void throwExceptionIfBlank(String property, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(String.format("Value of the property %s is missing.", property));
        }
    }
}
