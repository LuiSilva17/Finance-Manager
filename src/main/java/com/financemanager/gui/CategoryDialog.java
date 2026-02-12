package com.financemanager.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.*;

import com.financemanager.service.CategoryManager;

public class CategoryDialog extends JDialog {
    
    public CategoryDialog(Window window) {
        super(window, "Manage Categories", ModalityType.APPLICATION_MODAL);
        this.setLayout(new GridLayout(1, 2, 10, 0));
        this.setSize(750, 500);
        this.setResizable(false);
        this.setLocationRelativeTo(window); // Para abrir centrada na janela "pai"
        CategoryManager catManager = CategoryManager.getInstance();
        catManager.syncOrder(); 

        DefaultListModel<String> categoryModel = new DefaultListModel<>();
        DefaultListModel<String> keywordModel = new DefaultListModel<>();

        for (String cat : catManager.getOrderedCategories()) {
            categoryModel.addElement(cat);
        }

        JList<String> categoryList = new JList<>(categoryModel);
        JList<String> keywordList = new JList<>(keywordModel);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Groups"));

        JPanel leftButtons = new JPanel(new FlowLayout());
        JButton btnAddCat = new JButton("Add");
        JButton btnEditCat = new JButton("Edit");
        JButton btnRemCat = new JButton("Remove");
        JButton btnUp = new JButton("⬆");
        JButton btnDown = new JButton("⬇");

        leftButtons.add(btnAddCat);
        leftButtons.add(btnEditCat);
        leftButtons.add(btnRemCat);
        leftButtons.add(btnUp);
        leftButtons.add(btnDown);

        leftPanel.add(new JScrollPane(categoryList), BorderLayout.CENTER);
        leftPanel.add(leftButtons, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Keywords"));

        JPanel rightButtons = new JPanel(new FlowLayout());
        JButton btnAddKey = new JButton("Add");
        JButton btnEditKey = new JButton("Edit");
        JButton btnRemKey = new JButton("Remove");
        JButton btnInfo = new JButton("ℹ");

        rightButtons.add(btnAddKey);
        rightButtons.add(btnEditKey);
        rightButtons.add(btnRemKey);
        rightButtons.add(btnInfo);

        rightPanel.add(new JScrollPane(keywordList), BorderLayout.CENTER);
        rightPanel.add(rightButtons, BorderLayout.SOUTH);

        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedCat = categoryList.getSelectedValue();
                keywordModel.clear();

                if (selectedCat != null) {
                    ArrayList<String> keys = catManager.getCategoriesMap().get(selectedCat);
                    if (keys != null) {
                        for (String k : keys) {
                            keywordModel.addElement(k);
                        }
                    }
                }
            }
        });

        btnAddCat.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "New Group Name:");
            if (name != null && !name.trim().isEmpty()) {
                String finalName = name.trim();
                if (!catManager.getCategoriesMap().containsKey(finalName)) {
                    catManager.addCategory(finalName);
                    categoryModel.addElement(finalName);
                    catManager.save();
                } else {
                    JOptionPane.showMessageDialog(this, "Group already exists!");
                }
            }
        });

        btnRemCat.addActionListener(e -> {
            String selected = categoryList.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete group '" + selected + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    catManager.getCategoriesMap().remove(selected);
                    categoryModel.removeElement(selected);
                    keywordModel.clear();
                    catManager.save();
                }
            }
        });

        btnUp.addActionListener(e -> {
            int index = categoryList.getSelectedIndex();
            String selected = categoryList.getSelectedValue();
            
            if (index > 0 && selected != null) {
                catManager.moveCategoryUp(selected);
                
                String temp = categoryModel.get(index - 1);
                categoryModel.set(index - 1, selected);
                categoryModel.set(index, temp);
                
                categoryList.setSelectedIndex(index - 1);
                catManager.save();
            }
        });

        btnDown.addActionListener(e -> {
            int index = categoryList.getSelectedIndex();
            String selected = categoryList.getSelectedValue();
            
            if (index >= 0 && index < categoryModel.getSize() - 1 && selected != null) {
                catManager.moveCategoryDown(selected);

                String temp = categoryModel.get(index + 1);
                categoryModel.set(index + 1, selected);
                categoryModel.set(index, temp);
                
                categoryList.setSelectedIndex(index + 1);
                catManager.save();
            }
        });

        btnAddKey.addActionListener(e -> {
            String selectedCat = categoryList.getSelectedValue();
            if (selectedCat == null) {
                JOptionPane.showMessageDialog(this, "Select a Group on the left first!");
                return;
            }

            String key = JOptionPane.showInputDialog(this, "Add keyword for " + selectedCat + ":");
            if (key != null && !key.trim().isEmpty()) {
                catManager.addKeyword(selectedCat, key.trim());
                keywordModel.clear();
                for (String k : catManager.getCategoriesMap().get(selectedCat)) {
                    keywordModel.addElement(k);
                }
                catManager.save();
            }
        });

        btnRemKey.addActionListener(e -> {
            String selectedCat = categoryList.getSelectedValue();
            String selectedKey = keywordList.getSelectedValue();

            if (selectedCat != null && selectedKey != null) {
                catManager.getCategoriesMap().get(selectedCat).remove(selectedKey);
                keywordModel.removeElement(selectedKey); 
                catManager.save();
            }
        });

        btnEditCat.addActionListener(e -> {
            String selected = categoryList.getSelectedValue();
            if (selected != null) {
                String newName = JOptionPane.showInputDialog(this, "Rename '" + selected + "' to:", selected);
                if (newName != null && !newName.trim().isEmpty()) {
                    String finalName = newName.trim();
                    ArrayList<String> keys = catManager.getCategoriesMap().get(selected);
                    catManager.getCategoriesMap().remove(selected);
                    catManager.getCategoriesMap().put(finalName, keys);
                    
                    int index = categoryList.getSelectedIndex();
                    categoryModel.set(index, finalName);
                    catManager.save();
                }
            }
        });

        btnEditKey.addActionListener(e -> {
            String selectedCat = categoryList.getSelectedValue();
            String selectedKey = keywordList.getSelectedValue();

            if (selectedCat != null && selectedKey != null) {
                String newKey = JOptionPane.showInputDialog(this, "Rename keyword:", selectedKey);
                if (newKey != null && !newKey.trim().isEmpty()) {
                    ArrayList<String> keys = catManager.getCategoriesMap().get(selectedCat);
                    int index = keys.indexOf(selectedKey);
                    if (index >= 0) {
                        keys.set(index, newKey.trim());
                        keywordModel.set(keywordList.getSelectedIndex(), newKey.trim());
                        catManager.save();
                    }
                }
            }
        });

        btnInfo.addActionListener(e -> {
             JOptionPane.showMessageDialog(this, "Select a category and add keywords found in bank descriptions.", "Help", JOptionPane.INFORMATION_MESSAGE);
        });

        this.add(leftPanel);
        this.add(rightPanel);
    }

}
