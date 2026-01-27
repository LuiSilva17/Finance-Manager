package com.financemanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class CategoryManager implements Serializable {
    
    private HashMap<String, ArrayList<String>> categories;

    public CategoryManager() {
        categories = new HashMap<>();
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
            this.categories.put(name ,k = new ArrayList<>());
            k.add(keyword);
            return;
        }
        System.out.println("Category does not exist");
        return;
    }

    //public String getCategoryFor(String description) {

    //}

}