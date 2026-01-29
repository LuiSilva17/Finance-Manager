package com.financemanager.service;

import java.io.*;
import java.util.HashSet;

public class SettingsManager implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static SettingsManager instance = null;

    private HashSet<String> installedParsers;
    private static final String[] SUPPORTED_BANKS = {"CDG", "CA", "BPI", "MILLENIUM", "NOVO BANCO", "REVOLUT", "SANTANDER"};
    
    private static final String CONFIG_PATH = System.getProperty("user.home") + File.separator + "Finance Manager Data" + File.separator;
    private static final String FILE_NAME = "settings.dat";

    private SettingsManager() {
        installedParsers = new HashSet<>();
    }

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    public void save() {
        File directory = new File(CONFIG_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(CONFIG_PATH + FILE_NAME))) {
            out.writeObject(getInstance());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        File file = new File(CONFIG_PATH + FILE_NAME);
        if (file.exists()) {
           try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                instance = (SettingsManager) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } 
        }
    }

    public boolean install(String bankName) {
        for (String string : SUPPORTED_BANKS) {
            if (bankName.equals(string)) {
                if (!this.installedParsers.contains(bankName)) {
                    this.installedParsers.add(bankName);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean uninstall(String bankName) {
        for (String string : SUPPORTED_BANKS) {
            if (bankName.equals(string)) {
                if (this.installedParsers.contains(bankName)) {
                    this.installedParsers.remove(bankName);
                return true;
                }
            }
        }
        return false;
    }

    public boolean isInstalled(String bankName) {
        return this.installedParsers.contains(bankName);
    }

    public HashSet<String> getInstalledBanks() {
        return this.installedParsers;
    }
}