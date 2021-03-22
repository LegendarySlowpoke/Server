package server;

import server.server.ServerCommunicator;
import server.serverHandler.HandlerCommunicator;

public class CommunicationNode {

    ServerCommunicator serverCommunicator;
    HandlerCommunicator handlerCommunicator;

    CommunicationNode(ServerCommunicator serverCommunicator, HandlerCommunicator handlerCommunicator) {
        this.serverCommunicator = serverCommunicator;
        this.handlerCommunicator = handlerCommunicator;
    }

    void launch() {
        Thread serverThread = new Thread(serverCommunicator);
        Thread handlerThread = new Thread(handlerCommunicator);
        serverThread.start();
        handlerThread.start();
    }

    public void stopServer() {
        serverCommunicator.stopServer();
    }

    public boolean isExit() {
        return serverCommunicator.isExit();
    }

    public boolean isAlive() {
        return serverCommunicator.isAlive();
    }
}