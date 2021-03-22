package server.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

//todo delete following imports: AFTER you will figure out why this doesn't work
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;

public class Server {
    //Server fields
    final private String address = "127.0.0.1";
    final private int port = 23456;
    private ServerCommunicator serverCommunicator;
    private ServerSocket serverSocket;
    private DBmap dbMap;
    private boolean exit;
    ArrayList<SocketUnit> socketUnitList;

    Server(ServerCommunicator serverCommunicator) {
        this.serverCommunicator = serverCommunicator;
    }

    //exit getter
    boolean isExit() {
        return exit;
    }

    protected void setExitTrue() {
        exit = true;
    }


    //METHODS FOR ESTABLISHING CONNECTIONS & TRANSFERRING DATA
    private boolean runServerSocket() {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(address));
            System.out.println("SERVER ServerSocket is running!");
            return true;
        } catch (IOException e) {
            System.out.println("SERVER Error occurred while trying to run server socket: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean stopServerSocket() {
        try {
            serverSocket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private Socket runSocket() {
        try {
                Socket socket = serverSocket.accept();
            if (!exit) {
                System.out.println("SERVER SERVER runSocket(): New socket is running!");
                return socket;
            } else {
                socket.close();
                System.out.println("SERVER SERVER runSocket(): exit command been received, refused to run new socket.");
                return null;
            }
        } catch (IOException e) {
            System.out.println("SERVER SERVER runSocket(): Error occurred while trying to run socket: "
                    + e.getMessage());
            return null;
        }
    }

    private boolean stopSocket(Socket socket) {
        try {
            socket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void launch() {
        System.out.println("==============================================================\nSERVER started!");

        //Loading/creating dbMap from file
        dbMap = new DBmap();
        String filePath = dbMap.getFilePath();

        if (dbMap.checkState()) {
            //Creating variables
            exit = false;
            socketUnitList = new ArrayList<>();
            //Running main algorithm
            //todo Old code with executor service, probably gonna be replaced with with Thread solution...
            // ..from stackOverflow(((
            if (runServerSocket()) {
                while (!exit) {

                    Socket socket = runSocket();
                    if (socket != null && !exit) {
                        // new thread for a client
                        SocketUnit socketUnit = new SocketUnit(socket, dbMap, this);
                        socketUnitList.add(socketUnit);
                        socketUnit.start();
                        System.out.println("New socketUnit started!\n");
                    }
                }
            }
            //todo Old code, probably gonna be deleted, but ypu should find out how to make it work
            /*
            ExecutorService service = Executors.newFixedThreadPool(10);
            if (runServerSocket()) {
                service.submit(() -> {
                    while (!exit) {
                        System.out.println("\n===============================\nSERVER Waiting for new request...");
                        SocketUnit socketUnit = new SocketUnit(serverSocket, dbMap);
                        socketUnitList.add(socketUnit);
                            if (socketUnit.runSocket() != null) {
                                socketUnit.start();
                                if (socketUnit.checkExitRequest()) {
                                    exit = true;
                                    stopServer();
                                }
                            }
                    }
                });
            }
            service.shutdown();

             */
            //printSocketList();
            //stopServer();

        } else {
            System.out.println("SERVER Loading dbMap has failed: server will be closed!");
        }

        System.out.println("==============================================================\nSERVER " +
                "launch() finished!\n");
    }

    protected void printSocketList() {
        String ans = "SERVER SERVER printSocketList():" +
                "\n   Socket list:";
        int counter = 0;
        for (SocketUnit socketUnit : socketUnitList) {
            ans += "\n" + counter + "# " + socketUnit.getSocket().toString();
            counter++;
        }
        ans += "\n   Socket list finished.\n\n";
        System.out.println(ans);
    }

    public void stopServer() {
        //Finishing server work
        //Stopping all sockets
        String ans = "\nSERVER SERVER stopServer():";
        for (SocketUnit socketUnit : socketUnitList) {
            if (stopSocket(socketUnit.getSocket())) {
                System.out.println("SERVER SERVER stopServer(): socket closed successfully: "
                        + socketUnit.getSocket().toString());
            }
        }

        if (stopServerSocket()) {
            System.out.println("SERVER SERVER stopServer(): serverSocket closed successfully!");
        } else {
            System.out.println("SERVER SERVER stopServer(): serverSocket failed closing.");
        }
        //Saving fileMap to the file
        System.out.println("SERVER SERVER stopServer(): dbMap closed with message: '"
                + dbMap.saveMapChangesToFile() + "'");
    }

    public boolean isAlive() {
        return serverSocket.isClosed();
    }
}