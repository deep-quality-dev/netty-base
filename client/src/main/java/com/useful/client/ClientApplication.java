package com.useful.client;

import com.useful.client.listener.ClientListenerImpl;
import com.useful.client.typeClient.TCPClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@PropertySource(value = "classpath:application.properties")
public class ClientApplication {

    public static void main(String[] args) {
        try {
            ConfigurableApplicationContext context = SpringApplication.run(ClientApplication.class, args);

            final int clientCount = 10;

            final CountDownLatch countDownLatch = new CountDownLatch(clientCount);

            for (int idx = 0; idx < clientCount; idx++) {
                final int threadIndex = idx;
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        String clientId = "CLIENT_" + String.valueOf(threadIndex);
                        TCPClient tcpServer = new TCPClient(clientId);
                        tcpServer.start(new ClientListenerImpl());
                        countDownLatch.countDown();
                    }
                });
                thread.start();
            }
            countDownLatch.await();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
