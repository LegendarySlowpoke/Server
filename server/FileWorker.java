package server.server;

import server.ACTIONS;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FileWorker {

    //Service fields
    private DBmap dbMap;
    private String filePath;

    protected FileWorker(DBmap dbMap, String filePath) {
        this.dbMap = dbMap;
        this.filePath = filePath;
    }

    //WORKING WITH FILES METHODS
    protected boolean checkIfFileExists(REQTYPES reqType, String fileName) {
        if (reqType == REQTYPES.BY_ID) {
            try {
                System.out.println("SERVER FILEWORKER trying to parseLong '" + fileName + "';");
                if (dbMap.getMap().containsKey(Long.parseLong(fileName))) {
                    fileName = dbMap.getMap().get(Long.parseLong(fileName));
                    if (Files.exists(Path.of(filePath + fileName))) {
                        System.out.println("SERVER FILEWORKER  checkIfFileExists() returns true 1;");
                        return true;
                    } else {
                        System.out.println("SERVER FILEWORKER  checkIfFileExists() returns false: " +
                                "filePath: '" + filePath + fileName + "' doesn't exist;");
                        return false;
                    }
                } else {
                    System.out.println("    SERVER FILEWORKER checkIfFileExists() returns false: no such ID in map;");
                    return false;
                }
            } catch (Exception e) {
                System.out.println("\n    !!!! EXCEPTION !!!!\n" +
                        "SERVER FILEWORKER   In checkIfFileExists(), message: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else if (reqType == REQTYPES.BY_NAME) {
            if (Files.exists(Path.of(filePath + fileName))) {
                System.out.println("    SERVER FILEWORKER checkIfFileExists() returns true 2;");
                return true;
            } else {
                System.out.println("    SERVER FILEWORKER checkIfFileExists() returns false: " +
                        "filePath: '" + filePath + fileName + "' doesn't exist;");
                return false;
            }
        } else if (reqType == REQTYPES.NO_TYPE) {
            if (fileName.equals("") || fileName.length() < 1) {
                System.out.println("    SERVER FILEWORKER checkIfFileExists() returns false: " +
                        "reqType == NO_TYPE, fileName wasn't stated (filename is '" + fileName + "').");
                return false;
            } else if (Files.exists(Path.of(filePath + fileName))) {
                System.out.println("    SERVER FILEWORKER checkIfFileExists() returns true 3;");
                return true;
            } else {
                System.out.println("    SERVER FILEWORKER checkIfFileExists() returns false (NO_TYPE request).\n" +
                        "filePath: '" + filePath + fileName + "' doesn't exist;");
                return false;
            }
        } else {
            System.out.println("    SERVER FILEWORKER checkIfFileExists() returns true (unknown reqType)." +
                    " ACHTUNG! Always returns true with this reqType");
            return true;
        }
    }

    protected byte[] readFile(REQTYPES reqType, String fileName) {
        try {
            //In case the request is BY_ID
            if (reqType == REQTYPES.BY_ID) fileName = dbMap.getMap().get(Long.parseLong(fileName));
            FileInputStream fis = new FileInputStream(filePath + File.separator + fileName);
            byte[] varFileData = fis.readAllBytes();
            fis.close();
            return varFileData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected long writeFile(String fileName, byte[] fileData) {
        try {
            //Adding new record to dbMap
            //todo Should emplement solution for saving changes in file
            //todo Maybe best solution is to create smth like saveNewRecord() if DBmap.
            //dbMap.put(idNumb, fileName);
            boolean savedNewRecord = false;
            long idNumb = generateIdNumber();
            int failCounter = 0;
            while(!savedNewRecord) {
                //Checking if request contains fileName for saving file on server
                if (fileName.equals("") || fileName.length() < 1 || fileName == null) {
                    System.out.println("        SERVER FILEWORKER writeFile(): fileName equals null, or \"\"." +
                            " Trying to generate new name.");
                    fileName = "newFileId" + idNumb + ".txt";
                    int counter = 0;
                    while (true) {
                        if (!checkIfFileExists(REQTYPES.NO_TYPE, fileName)) {
                            System.out.println("        SERVER FILEWORKER writeFile(): generated file name is:"
                                    + fileName);
                            break;
                        } else {
                            fileName = "newFileId" + idNumb + "_" + counter + ".txt";
                        }
                        if (counter > 1000) {
                            System.out.println("        SERVER FILEWORKER writeFile(): counter exceeded " +
                                    "1000 while trying to create a new file name, last try: '" + fileName +
                                    "'. Name generating has been stopped.");
                            break;
                        }
                        counter++;
                    }
                } else {
                    //Request contains filename
                    System.out.println("        SERVER FILEWORKER writeFile(): fileName is '" + fileName + "'");
                }
                //Saving to dbMap & trying to record file
                savedNewRecord = dbMap.saveCurrentRecord(ACTIONS.PUT, idNumb + failCounter, fileName);
                if (savedNewRecord) {
                    FileOutputStream fos = new FileOutputStream(filePath + fileName);
                    fos.write(fileData);
                    fos.close();
                    System.out.println("    SERVER FILEWORKER File was saved on server: generatedID = '" + idNumb +
                            "', fileName = '" + fileName + "'");
                    if (fileData.length < 500) {
                        System.out.println("        Data written to file:\n            "
                                + new String(fileData, StandardCharsets.UTF_8));
                    } else {
                        System.out.println("        ");
                    }
                }
                if (failCounter > 10) {
                    break;
                }
                failCounter++;
            }
            if (savedNewRecord) {
                System.out.println("        SERVER FILEWORKER writeFile(): Success, returning idNumb.");
                return idNumb;
            } else {
                System.out.println("        SERVER FILEWORKER writeFile(): Failed, returning -1, " +
                        "failed to create record in dbMap, failCounter == " + failCounter);
                return -1;
            }
        } catch (IOException e) {
            System.out.println("        SERVER FILEWORKER writeFile(): Failed, returning -1, " +
                    "IOException e '" + e.getMessage() + "'.");
            e.printStackTrace();
            return -1;
        }
    }

    protected boolean deleteFile(REQTYPES reqType, String fileName) {
        long idtoDel = -1;
        //In case the request is BY_ID
        if (reqType == REQTYPES.BY_ID) {
            idtoDel = Long.parseLong(fileName);
            fileName = dbMap.getMap().get(Long.parseLong(fileName));
        } else {
        //In case the request is BY_NAME
            for (Map.Entry<Long, String> entry : dbMap.getMap().entrySet()) {
                if (entry.getValue().equals(fileName)) {
                    idtoDel = entry.getKey();
                    break;
                }
            }
        }
        System.out.println("    SERVER FILEWORKER deleteFile(): id = " + idtoDel + ", fileName = " + fileName);
        if (idtoDel == -1) {
            System.out.println("    SERVER FILEWORKER cannot find the file in dbMap, canceling deleting");
            return false;
        } else {
            File file = new File(filePath + fileName);
            if (dbMap.saveCurrentRecord(ACTIONS.DELETE, idtoDel, null)) {
                if (file.delete()) {
                    System.out.println("    SERVER FILEWORKER deleteFile(): file was successfully deleted!");
                    return true;
                } else {
                    System.out.println("    SERVER FILEWORKER deleteFile(): file was deleted from dbMap," +
                            "but file wasn't found on HDD: nothing to delete.");
                    return false;
                }
            } else {
                System.out.println("    SERVER FILEWORKER deleteFile(): failed to delete the file.!");
                return false;
            }
        }
    }

    private long generateIdNumber() {
        long lastId = dbMap.getLastId();
        if (dbMap.getMap().containsKey(lastId + 1)) {
            System.out.println("        SERVER FILEWORKER generateIdNumber(): !dbMap.containsKey(lastId + 1); return: '"
                    + (dbMap.getLastId() + 1) + "'");
            lastId++;
        } else {
            System.out.println("        SERVER FILEWORKER generateIdNumber(): dbMap contains this key,"
                    + " generating new keys:");
            System.out.print("           ");
            while (dbMap.getMap().containsKey(lastId + 1)) {
                lastId++;
                System.out.print(" " + lastId);
            }
            lastId++;
            System.out.println("\n        SERVER FILEWORKER generateIdNumber(): finished generating, returns = "
                    + lastId);
        }
        return lastId;
    }
}