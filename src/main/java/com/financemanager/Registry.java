package com.financemanager;

import java.io.*;
import java.util.HashMap;

public class Registry {
    
    private HashMap<String, String> map;
    private File file;

    public Registry(String fileName) {
        this.file = new File(System.getProperty("user.home") + File.separator + fileName);
        this.map = new HashMap<>();
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
