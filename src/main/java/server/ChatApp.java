package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.service.ServerImpl;


public class ChatApp {

    public static final Logger LOGGER = LogManager.getLogger(ChatApp.class);

    public static void main(String[] args) {
        LOGGER.info("Application started");
        new ServerImpl();
    }
}
