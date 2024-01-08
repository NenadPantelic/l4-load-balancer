package com.np.handler;

import com.np.data.ClientContext;
import com.np.data.Server;
import com.np.loadbalancer.LoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class ClientSocketHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ClientSocketHandler.class);
    private Socket serverConnection = null;
    private final Socket clientSocket;
    private final LoadBalancer loadBalancer;

    public ClientSocketHandler(Socket clientSocket, LoadBalancer loadBalancer) {
        this.clientSocket = clientSocket;
        this.loadBalancer = loadBalancer;
    }

    @Override
    public void run() {
        log.info("New connection from {}:{}", clientSocket.getInetAddress().getHostName(), clientSocket.getPort());
        Server server = null;

        try {
            // Client context
            ClientContext clientContext = new ClientContext(clientSocket.getRemoteSocketAddress().toString());
            // Choose the server
            server = loadBalancer.route(clientContext);
            serverConnection = new Socket(server.address(), server.port());
        } catch (IOException e) {
            log.error("Could not establish the connection between the load balancer and the target server {}.", server, e);
            return;
        }

        log.info(
                "Connection established between {}:{} <-> {}:{}",
                clientSocket.getInetAddress().getHostName(), clientSocket.getPort(),
                serverConnection.getInetAddress().getHostName(), serverConnection.getPort()
        );

        // client ---> server
        new Thread(new Proxy(clientSocket, serverConnection)).start();
        // client <--- server
        new Thread(new Proxy(serverConnection, clientSocket)).start();

        // thread to monitor whether the socket from the client is closed, to close the socket to the target server
        new Thread(() -> {
            while (true) {
                if (clientSocket.isClosed()) {
                    log.info("Client socket ({}:{}) closed", clientSocket.getInetAddress().getHostName(), clientSocket.getPort());
                    closeServerConnection();
                    break;
                }

                // to prevent overusing the CPU, making this job less wasteful
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }

    private void closeServerConnection() {
        if (serverConnection != null && !serverConnection.isClosed()) {
            try {
                log.info(
                        "Closing the connection to the server {}:{}",
                        serverConnection.getInetAddress().getHostName(), serverConnection.getPort()
                );
                serverConnection.close();
            } catch (IOException e) {
                log.error(
                        "Could not close the connection to {}:{}",
                        serverConnection.getInetAddress().getHostName(), serverConnection.getPort(), e
                );
            }
        }
    }
}