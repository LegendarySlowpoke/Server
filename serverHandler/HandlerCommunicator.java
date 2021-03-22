package server.serverHandler;

import server.CommunicationNode;

public class HandlerCommunicator implements Runnable {

    CommunicationNode communicationNode;
    ServerHandler serverHandler;

    public HandlerCommunicator() {
        serverHandler = new ServerHandler(this);
    }

    public void setCommunicationNode(CommunicationNode communicationNode) {
        this.communicationNode = communicationNode;
    }

    @Override
    public void run() {
        serverHandler.launch();
    }

    public boolean isExit() {
        return communicationNode.isExit();
    }

    public void stopServer() {
        communicationNode.stopServer();
    }

    public boolean isAlive() {
        return communicationNode.isAlive();
    }
}
