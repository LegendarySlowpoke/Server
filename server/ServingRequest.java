package server.server;

import server.ACTIONS;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static server.ACTIONS.*;

public class ServingRequest {
    //service fields
    private ACTIONS action;
    private byte[] fileData;
    private DataInputStream dataInputStream;
    private FileWorker fileWorker;
    private String filePath;
    //data fields
    private DBmap dbmap;
    private REQTYPES reqType;
    private Socket socket;


    protected ServingRequest(Socket socket, DBmap dbMap) {
        this.dbmap = dbMap;
        this.socket = socket;
        this.filePath = dbMap.getFilePath();
        fileWorker = new FileWorker(dbMap, filePath);
    }

    public void launch() {

        String fileName;

        System.out.println("SERVER SERVINGREQUEST runSocket() request received!");
        action = null;
        reqType = null;
        fileData = null;

        //Reading data
        fileName = readIncomeData();
        System.out.println("SERVER Received data from client: action = '" + action.name() +
                "', typeBy = '" + reqType + "', fileName = '" + fileName + "';");
        switch (action) {

            case GET:
                if (fileWorker.checkIfFileExists(reqType, fileName)) {
                    fileData = fileWorker.readFile(reqType, fileName);
                    if (fileData != null) {
                        sendResponse("200");
                    } else {
                        sendResponse("404");
                    }
                } else {
                    sendResponse("404");
                }
                break;

            case PUT:
                if (!fileWorker.checkIfFileExists(reqType, fileName)) {
                    long savedFileId = fileWorker.writeFile(fileName, fileData);
                    if (savedFileId > 0 ) {
                        sendResponse("200 " + savedFileId);
                    } else {
                        sendResponse("403");
                    }
                } else {
                    sendResponse("403");
                }
                break;

            case DELETE:
                if (fileWorker.checkIfFileExists(reqType, fileName)) {
                    if (fileWorker.deleteFile(reqType, fileName)) {
                        sendResponse("200");
                    } else {
                        sendResponse("404");
                    }
                } else {
                    sendResponse("404");
                }
                break;
            case CLOSESERVER:
                System.out.println("SERVER Exit command has been received.");
                break;
            default:
                break;
        }
    }

    protected ACTIONS getAction() {
        return this.action;
    }

    private String readIncomeData() {
        //this method returns fileName\ fileID
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            int length = dataInputStream.readInt();
            byte[] inputData = new byte[length];
            dataInputStream.readFully(inputData, 0, inputData.length);
            String message = new String(inputData, StandardCharsets.UTF_8);

            //Next string splitting splits only 3 first words separated by space, you should split whole text with
            //spaces, or substring this first three words form  'message' string, to get whole received file data

            String[] decodeData = message.split(" ", 3);
            //System.out.println("SERVER income message: " + message);
            System.out.print("SERVER income data:");
            for (int i = 0; i < decodeData.length - 1; i++) {
                System.out.print(" '" + decodeData[i] + "'");
            }
            if (decodeData[0].equals("PUT")) {
                System.out.println(", lengthINT = " + length + ", fileData.length = "
                        + decodeData[decodeData.length - 1].length()
                        + ", total income message length = " + message.length());
            } else {
                System.out.println(" '" + decodeData[decodeData.length - 1] + "'");
            }

            switch (decodeData[0]) {
                case "GET":
                    action = GET;
                    if (decodeData[1].equals("BY_ID")) {
                        reqType = REQTYPES.BY_ID;
                        fileData = null;
                    } else if (decodeData[1].equals("BY_NAME")) {
                        reqType = REQTYPES.BY_NAME;
                        fileData = null;
                    } else {
                        reqType = REQTYPES.ERROR;
                    }
                    return decodeData[2];

                case "PUT":
                    action = PUT;
                    fileData = message.replace(decodeData[0] + " " + decodeData[1]
                            + " ", "").getBytes();
                    //decodeData[2].getBytes();
                    reqType = REQTYPES.NO_TYPE;
                    return decodeData[1];

                case "DELETE":
                    action = DELETE;
                    if (decodeData[1].equals("BY_ID")) {
                        reqType = REQTYPES.BY_ID;
                        fileData = null;
                    } else if (decodeData[1].equals("BY_NAME")) {
                        reqType = REQTYPES.BY_NAME;
                        fileData = null;
                    } else {
                        reqType = REQTYPES.ERROR;
                    }
                    return decodeData[2];

                case "exit":
                    action = CLOSESERVER;
                    return null;
                default:
                    action = ERROR;
                    return null;
            }
        } catch (IOException e) {
            action = ERROR;
            return null;
        }
    }

    private void sendResponse(String response) {
        DataOutputStream output = null;
        byte[] finalResponse;
        if (response.equals("200") && action == GET) {
            // In case response contains file
            byte[] respHead = (response + " ").getBytes();
            byte[] respBody = fileData;
            finalResponse = new byte[respHead.length + respBody.length];
            for (int i = 0; i < respHead.length; i++) {
                finalResponse[i] = respHead[i];
            }
            //todo????
            for (int i = respHead.length; i < respBody.length; i++) {
                finalResponse[i] = respBody[i - respHead.length];
            }
            for (int i = respHead.length; i < finalResponse.length; i++) {
                finalResponse[i] = respBody[i - respHead.length];
            }
        } else if (response.equals("200") && action == PUT) {
            //todo Can't use this solution in multithread app, you should return exact value
            //todo DONE, but can be improved: String response already contains id number
            finalResponse = response.getBytes();
        } else {
            finalResponse = response.getBytes();
        }

        try {
            output = new DataOutputStream(socket.getOutputStream());
            output.writeInt(finalResponse.length);
            output.write(finalResponse);
            output.close();
            if (finalResponse.length < 500){
                System.out.println("        Here is response:\n        "
                        + new String(finalResponse, StandardCharsets.UTF_8));
            } else {
                System.out.println("        Response is too long to be printed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}