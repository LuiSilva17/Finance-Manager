package com.financemanager.service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale.Category;

public class CategoryManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String CONFIG_PATH =
        System.getProperty("user.home") +
        File.separator +
        "Finance Manager Data" +
        File.separator;
    private static final String FILE_NAME = "categories.config";

    private static CategoryManager instance = null;
    private HashMap<String, ArrayList<String>> categories;

    private CategoryManager() {
        this.categories = new HashMap<>();
    }

    public void addCategory(String name) {
        if (this.categories.containsKey(name)) {
            System.out.println("Category name already exists");
            return;
        }
        this.categories.put(name, new ArrayList<>());
    }

    public void addKeyword(String name, String keyword) {
        if (this.categories.containsKey(name)) {
            ArrayList<String> k = this.categories.get(name);
            if (k != null) {
                if (k.contains(keyword)) {
                    System.out.println("Category has already that keyword");
                    return;
                }
                k.add(keyword);
                return;
            }
            this.categories.put(name, k = new ArrayList<>());
            k.add(keyword);
            return;
        }
        System.out.println("Category does not exist");
        return;
    }

    public String getCategoryFor(String description) {
        for (String name : this.categories.keySet()) {
            for (String keyword : this.categories.get(name)) {
                if (description.toUpperCase().contains(keyword.toUpperCase())) {
                    return name;
                }
            }
        }
        return "Uncategorized";
    }

    public void save() {
        File directory = new File(CONFIG_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (
            ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(CONFIG_PATH + FILE_NAME)
            )
        ) {
            out.writeObject(getInstance());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CategoryManager load() {
        File file = new File(CONFIG_PATH + FILE_NAME);
        if (file.exists()) {
            try (
                ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(file)
                )
            ) {
                instance = (CategoryManager) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return instance;
        }
        return instance = new CategoryManager();
    }

    public static CategoryManager getInstance() {
        if (instance == null) {
            instance = new CategoryManager();
        }
        return instance;
    }

    public HashMap<String, ArrayList<String>> getCategoriesMap() {
        return this.categories;
    }
}
