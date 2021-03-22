package server.server;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

class DBmapReader {

    //filePath for launching on PC
    private String filePath = System.getProperty("user.dir") + File.separator + "File Server" + File.separator + "task" + File.separator + "src" + File.separator + "server" + File.separator + "data" + File.separator;
    private String filePathSaveFile = filePath + File.separator + "mpFl.smf";
    //filePath for launching HyperSkill tests
    //private static final String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "server" + File.separator + "data" + File.separator;
    ObjToSave fileObject;

    //Object saved to file
    private static class ObjToSave implements Serializable {
        private static final long serialVersionUID = 1L;
        Map<Long, String> map;
        long lastId;

        private ObjToSave(Map<Long, String> map, long id) {
            this.map = map;
            this.lastId = id;
        }

        private long getLastId(){
            return this.lastId;
        }
        private Map<Long, String> getMap() {
            return this.map;
        }

    }

    //READING FROM FILE
    protected Map<Long, String> readData() {
        try {
            FileInputStream fileIn = new FileInputStream(filePathSaveFile);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            fileObject = (ObjToSave) objectIn.readObject();
            objectIn.close();
            fileIn.close();
            System.out.println("SERVER DBmapReader readData(): data was loaded from file successfully!");
            return fileObject.getMap();
        } catch (Exception e ) {
            System.out.println("SERVER DBmapReader readData(): failed to read data from file. " +
                    "Error message: " + e.getMessage());
            //e.printStackTrace();
            Map<Long, String> newMap = new HashMap<>();
            return newMap;
        }
    }

    protected long getLastId() {
        try {
            return fileObject.getLastId();
        } catch (NullPointerException e) {
            System.out.println("SERVER DBmapREADER getLastID(): ERROR! " + e.getMessage() +
                    "; returning '0' value.");
            return 0;
        }
    }

    public String getFilePath() {
        return filePath;
    }

    //WRITING TO FILE
    protected String writeData(Map<Long, String> mapToSave, long lastId) {
        try {
            FileOutputStream fileOut = new FileOutputStream(filePathSaveFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            fileObject = new ObjToSave(mapToSave, lastId);
            objectOut.writeObject(fileObject);
            objectOut.close();
            fileOut.close();
            return "MAP HAS BEEN SAVED TO FILE mpFl.smf: map.size = " + mapToSave.size() + ", lastId = " + lastId;
        } catch (Exception e ) {
            e.printStackTrace();
            return "Error during saving map to File: " + e.getMessage();
        }
    }
}