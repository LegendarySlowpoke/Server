package server.server;

import server.ACTIONS;

import java.net.Socket;

public class SocketUnit extends Thread {

    private Socket socket;
    private DBmap dbMap;
    private Server server;


    protected SocketUnit(Socket socket, DBmap dbMap, Server server) {
        this.socket = socket;
        this.dbMap = dbMap;
        this.server = server;
    }

    protected Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        ServingRequest newServingRequest = new ServingRequest(socket, dbMap);
        newServingRequest.launch();
        if (newServingRequest.getAction() == ACTIONS.CLOSESERVER) {
            server.setExitTrue();

        }
        System.out.println("Current session has been finished!\n");
    }
}