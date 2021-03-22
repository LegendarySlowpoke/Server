package server;

import server.server.ServerCommunicator;
import server.serverHandler.HandlerCommunicator;

public class Main {

    public static void main(String[] args) {
        ServerCommunicator serverCommunicator = new ServerCommunicator();
        HandlerCommunicator handlerCommunicator = new HandlerCommunicator();
        CommunicationNode communicationNode = new CommunicationNode(serverCommunicator, handlerCommunicator);
        handlerCommunicator.setCommunicationNode(communicationNode);
        serverCommunicator.setCommunicationNode(communicationNode);
        communicationNode.launch();
    }
}