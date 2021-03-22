package server.serverHandler;

class ServerHandler {

    HandlerCommunicator handlerCommunicator;

    ServerHandler(HandlerCommunicator handlerCommunicator) {
        this.handlerCommunicator = handlerCommunicator;
    }

    void launch() {
        long startTime = System.currentTimeMillis();
        System.out.println("--------------------------------------------------------------------" +
                "SERVER ServerHandler is running");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            if (!handlerCommunicator.isAlive()) {
                System.out.println("--------------------------------------------------------------------" +
                        "SERVER ServerHandler: server is alive");

                while (true) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if ((System.currentTimeMillis() - startTime) % 10000 == 0) {
                        System.out.println("--------------------------------------------------------------------" +
                                "SERVER ServerHandler is doing nothing for 10 sec more, total runtime "
                                + (System.currentTimeMillis() - startTime) / 1000 +
                                " seconds, " + " isExit() = " + handlerCommunicator.isExit());
                        startTime--;
                    }
                    if (handlerCommunicator.isExit()) {
                        break;
                    }
                }
                System.out.println("--------------------------------------------------------------" +
                        "SERVER ServerHandler is stopping the server, total runtime " +
                        (System.currentTimeMillis() - startTime) / 1000 + "seconds,  isExit() = "
                        + handlerCommunicator.isExit());
                handlerCommunicator.stopServer();
            }
        } catch (NullPointerException e) {
            System.out.println("--------------------------------------------------------------------" +
                    "SERVER ServerHandler: ERROR! server response is null, closing serverHandler.");
        }
    }
}