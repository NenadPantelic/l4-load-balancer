package com.np;

import com.np.data.Server;
import com.np.handler.ClientSocketHandler;
import com.np.loadbalancer.HashingLoadBalancer;
import com.np.loadbalancer.LoadBalancer;
import com.np.loadbalancer.LoadBalancingStrategy;
import com.np.loadbalancer.RoundRobinLoadBalancer;
import com.np.util.ApplicationPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        ApplicationPropertiesManager applicationPropertiesManager = new ApplicationPropertiesManager("/application.properties");

        log.info("Load balancer server listening at port {} with strategy {} routing traffic to {}",
                applicationPropertiesManager.getPort(),
                applicationPropertiesManager.getLoadBalancingStrategy(),
                applicationPropertiesManager.getServers()
        );
        LoadBalancer loadBalancer = createLoadBalancer(
                applicationPropertiesManager.getLoadBalancingStrategy(),
                applicationPropertiesManager.getServers()
        );

        try (ServerSocket serverSocket = new ServerSocket(applicationPropertiesManager.getPort())) {
            while (true) {
                Socket socket = serverSocket.accept();
                log.info("Connection established with {}", socket);
                handleSocket(socket, loadBalancer);
            }
        }
    }

    private static void handleSocket(Socket socket, LoadBalancer loadBalancer) {
        ClientSocketHandler clientSocketHandler = new ClientSocketHandler(socket, loadBalancer);
        Thread handlerThread = new Thread(clientSocketHandler);
        handlerThread.start();
    }

    private static LoadBalancer createLoadBalancer(LoadBalancingStrategy loadBalancingStrategy, List<Server> servers) {
        return switch (loadBalancingStrategy) {
            case ROUND_ROBIN -> new RoundRobinLoadBalancer(servers);
            case WEIGHTED_ROUND_ROBIN -> null;
            case LEAST_CONNECTIONS -> null;
            case IP_HASHING -> new HashingLoadBalancer(servers);
            case CONSISTENT_HASHING -> null;
        };
    }
}