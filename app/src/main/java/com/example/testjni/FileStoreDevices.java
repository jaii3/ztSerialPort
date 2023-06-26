package com.example.testjni;

/**
 * @author HP
 */
public class FileStoreDevices {
    String path;

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    String name;

    FileStoreDevices(String name, String path){
        this.path = path;
        this.name= name;
    }
}
