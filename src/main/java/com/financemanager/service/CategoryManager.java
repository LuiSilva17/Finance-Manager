package com.financemanager.service;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CategoryManager implements Serializable {

    @Serial private static final long serialVersionUID = 1L;
    private static CategoryManager instance = null;

    private String filePath;
    private static final String FILE_NAME = "categories.dat";

    private HashMap<String, ArrayList<String>> categories;
    private List<String> categoryOrder;

    private CategoryManager() {
        this.categories = new HashMap<>();
        this.categoryOrder = new ArrayList<>();
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
                    System.out.println("Category already has that keyword");
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

    private static File resolveConfigDirectory() {
        File directory = new File(System.getProperty("user.home") + File.separator
                + "Finance Manager Data" + File.separator + "Configs" + File.separator);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    public void save() {
        File directory = resolveConfigDirectory();
        this.filePath = directory.getAbsolutePath();

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(this.filePath + File.separator + FILE_NAME))) {
            out.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static CategoryManager loadFromDisk() {
        File directory = resolveConfigDirectory();
        File file = new File(directory, FILE_NAME);

        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                CategoryManager loaded = (CategoryManager) in.readObject();
                loaded.filePath = directory.getAbsolutePath();
                if (loaded.categories == null) {
                    loaded.categories = new HashMap<>();
                }
                if (loaded.categoryOrder == null) {
                    loaded.categoryOrder = new ArrayList<>();
                }
                return loaded;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        CategoryManager fresh = new CategoryManager();
        fresh.filePath = directory.getAbsolutePath();
        return fresh;
    }

    public void importFromFile(File file) {
        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                CategoryManager loadedObject = (CategoryManager) in.readObject();
                this.categories = loadedObject.getCategoriesMap();
                this.categoryOrder = loadedObject.getOrderedCategories();
                this.syncOrder();
                this.save();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static CategoryManager getInstance() {
        if (instance == null) {
            instance = loadFromDisk();
        }
        return instance;
    }

    public HashMap<String, ArrayList<String>> getCategoriesMap() {
        return this.categories;
    }

    public List<String> getCategoriesList() {
        return new ArrayList<String>(this.categories.keySet());
    }

    public void syncOrder() {
        if (this.categoryOrder == null) {
            this.categoryOrder = new ArrayList<>();
        }

        for (String catName : this.categories.keySet()) {
            if (!categoryOrder.contains(catName)) {
                categoryOrder.add(catName);
            }
        }
    }

    public void moveCategoryUp(String categoryName) {
        int index = categoryOrder.indexOf(categoryName);
        if (index > 0) {
            Collections.swap(categoryOrder, index, index - 1);
        }
    }

    public void moveCategoryDown(String categoryName) {
        int index = categoryOrder.indexOf(categoryName);
        if (index >= 0 && index < categoryOrder.size() - 1) {
            Collections.swap(categoryOrder, index, index + 1);
        }
    }

    public List<String> getOrderedCategories() {
        return categoryOrder;
    }

    public void removeCategory(String newName) {
        this.getCategoriesMap().remove(newName);
    }

    public void renameCategory(String oldName, String newName) {
        ArrayList<String> keywords = this.getCategoriesMap().get(oldName);
        this.getCategoriesMap().remove(oldName);
        this.getCategoriesMap().put(newName, keywords);
    }
}