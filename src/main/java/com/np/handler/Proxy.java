package com.np.handler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Proxy implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Proxy.class);
    private final Socket in;
    private final Socket out;

    public Proxy(Socket in, Socket out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        log.info(
                "Proxy {}:{} --> {}:{}",
                in.getInetAddress().getHostName(), in.getPort(), out.getInetAddress().getHostName(), out.getPort()
        );
        try {
            InputStream inputStream = getInputStream();
            OutputStream outputStream = getOutputStream();

            if (inputStream == null || outputStream == null) {
                return;
            }

            byte[] reply = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(reply)) != -1) {
                outputStream.write(reply, 0, bytesRead);
            }
        } catch (Exception e) {
            log.error("Could not transfer the date between the hosts {} --> {}.", in, out, e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.error("An unexpected error occurred.", e);
            }
        }
    }

    private InputStream getInputStream() {
        try {
            return in.getInputStream();
        } catch (IOException e) {
            log.error("Could not get the input stream from {}.", in, e);
            return null;
        }
    }

    private OutputStream getOutputStream() {
        try {
            return out.getOutputStream();
        } catch (IOException e) {
            log.error("Could not get the output stream from {}.", out, e);
            return null;
        }
    }
}