package server.server;

import server.ACTIONS;

import java.util.Map;

public class DBmap {

    private volatile Map<Long, String> dbMap;
    private volatile long lastId;
    private volatile DBmapReader dBmapReader;

    protected DBmap() {
        dBmapReader = new DBmapReader();
        dbMap = dBmapReader.readData();
        lastId = dBmapReader.getLastId();
        System.out.println("SERVER DBmap DBmap(): finished initialization.");
        printAllValues();
    }

    public long getLastId() {
        return lastId;
    }

    protected Map<Long, String> getMap() {
        return this.dbMap;
    }

    protected String getFilePath() {
        return dBmapReader.getFilePath();
    }

    protected boolean checkState() {
        if (dbMap != null) {
            return true;
        } else {
            return false;
        }
    }

    protected String saveMapChangesToFile() {
        return dBmapReader.writeData(this.dbMap, this.lastId);
    }

    protected boolean saveCurrentRecord(ACTIONS action, long newID, String newFileName) {
        switch (action) {
            case PUT:
                if (!dbMap.containsKey(newID)) {
                    dbMap.put(newID, newFileName);
                    lastId = newID;
                    saveMapChangesToFile();
                    System.out.println("    SERVER DBmap saveCurrentRecord(): action " + action + ", " +
                            "changes saved to dbMAP successfully!");
                    return true;
                } else {
                    System.out.println("    SERVER DBmap saveCurrentRecord(): action " + action + ", " +
                            "changes wasn't saved to dbMAP: dbMap.containsKey() == "
                            + dbMap.containsKey(newID) +  ".");
                    return false;
                }
            case DELETE:
                if (dbMap.containsKey(newID)) {
                    dbMap.remove(newID);
                    saveMapChangesToFile();
                    System.out.println("    SERVER DBmap saveCurrentRecord(): action " + action + ", " +
                            "changes saved to dbMAP successfully!");
                    return true;
                } else {
                    System.out.println("    SERVER DBmap saveCurrentRecord(): action " + action + ", " +
                            "changes wasn't saved to dbMAP: dbMap.containsKey() == false.");
                    return false;
                }
            default:
                System.out.println("    SERVER DBmap saveCurrentRecord(): wrong action request!");
                return false;
        }
    }

    protected void printAllValues() {
        System.out.println("\n            LastId is '" + lastId + "'");
        System.out.println("            Printing all data in dbMap:");
        if (dbMap != null && dbMap.size() > 0) {
            for (Map.Entry<Long, String> entry : dbMap.entrySet()) {
                System.out.println("id# " + entry.getKey() + ", filename '" + entry.getValue() + "';");
            }
            System.out.println("            All data was printed\n");
        } else {
            System.out.println("            ! There is no data in dbMap !\n");
        }
    }
}