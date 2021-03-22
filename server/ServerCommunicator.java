package server.server;

import server.CommunicationNode;

public class ServerCommunicator implements Runnable {

    CommunicationNode communicationNode;
    Server server;

    public ServerCommunicator() {
        server = new Server(this);
    }

    public void setCommunicationNode(CommunicationNode communicationNode) {
        this.communicationNode = communicationNode;
    }

    @Override
    public void run() {
        server.launch();
    }

    public boolean isExit() {
        return server.isExit();
    }

    public void stopServer() {
        server.stopServer();
    }

    public boolean isAlive() {
        return server.isAlive();
    }
}
