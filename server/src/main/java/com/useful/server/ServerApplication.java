package com.useful.server;

import com.useful.server.session.SessionManager;
import com.useful.server.typeServer.TCPServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:application.properties")
public class ServerApplication {

    static final int PORT = Integer.parseInt(System.getProperty("port", "9090"));

    public static void main(String[] args) {
        try {
            ConfigurableApplicationContext context = SpringApplication.run(ServerApplication.class, args);

            new Thread(new Runnable() {
                public void run() {
                    while(true) {
                        try {
                            Thread.sleep(1000);

                            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> SESSION COUNT: " + SessionManager.getCount());

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }).start();

            TCPServer tcpServer = context.getBean(TCPServer.class);
            tcpServer.start();
            System.out.println("已经开启服务器, 127.0.0.1:8228");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
