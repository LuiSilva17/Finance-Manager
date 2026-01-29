package com.financemanager.data;

import java.io.*;
import java.util.HashMap;

public class Registry {
    
    private static final String DATA_PATH = System.getProperty("user.home") + File.separator + "Finance Manager Data" + File.separator;
    private static final String FILE_NAME = "finance_manager_config.dat";
    private HashMap<String, String> map; // key = name, value = file path
    private File file;

    public Registry() {
        this.file = new File(DATA_PATH + FILE_NAME);
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

    public void renameManager(String oldName, String newName) {
        if (this.map.containsKey(oldName)) {
            String path = this.map.get(oldName);
            this.map.remove(oldName);
            this.map.put(newName, path);
            save();
        }
    }
}