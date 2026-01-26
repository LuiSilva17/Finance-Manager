package com.financemanager;

import java.io.*;
import java.util.HashMap;

public class Registry {
    
    private static final String FILE_NAME = ".finance_manager_config.manager";
    private HashMap<String, String> map; // key = name, value = file path
    private File file;

    public Registry() {
        this.file = new File(System.getProperty("user.home") + File.separator + FILE_NAME);
        this.map = new HashMap<>();
        load(); // So it can load from memory
    }

    public void load() {
        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                this.map = (HashMap<String, String>) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(this.map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerManager(String name, String path) {
        this.map.put(name, path);
        save();
    }

    public HashMap<String, String> getHashMap() {
        return this.map;
    }
}
