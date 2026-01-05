package com.example.tasks;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.Timer;

public class MainUI {
    private JFrame frame;
    private DefaultListModel<Task> listModel;
    private JList<Task> taskJList;
    private TaskManager manager;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MainUI() {
        manager = new TaskManager();
        initUI();
        startReminderTimer();    
    }

    private void initUI() {
        frame = new JFrame("Smart Task Scheduler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 450);
        frame.setLocationRelativeTo(null);

        listModel = new DefaultListModel<>();
        taskJList = new JList<>(listModel);
        taskJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroll = new JScrollPane(taskJList);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(0, 2, 6, 6));

        JTextField titleField = new JTextField();
        JTextField priorityField = new JTextField("1");
        JTextField deadlineField = new JTextField("yyyy-MM-dd HH:mm");
        JTextArea notesArea = new JTextArea(3, 20);

        controlPanel.add(new JLabel("Title:"));
        controlPanel.add(titleField);

        controlPanel.add(new JLabel("Priority (1 highest):"));
        controlPanel.add(priorityField);

        controlPanel.add(new JLabel("Deadline (yyyy-MM-dd HH:mm) or blank:"));
        controlPanel.add(deadlineField);

        controlPanel.add(new JLabel("Notes:"));
        controlPanel.add(new JScrollPane(notesArea));

        JPanel buttons = new JPanel();
        JButton addBtn = new JButton("Add Task");
        JButton editBtn = new JButton("Edit Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton saveBtn = new JButton("Save to file");
        JButton loadBtn = new JButton("Load from file");
        JButton refreshBtn = new JButton("Refresh list");

        buttons.add(addBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        buttons.add(saveBtn);
        buttons.add(loadBtn);

        frame.setLayout(new BorderLayout(8,8));
        frame.add(listScroll, BorderLayout.CENTER);

        JPanel east = new JPanel(new BorderLayout(6,6));
        east.add(controlPanel, BorderLayout.NORTH);
        east.add(buttons, BorderLayout.SOUTH);
        frame.add(east, BorderLayout.EAST);

        // Button actions
        addBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) { JOptionPane.showMessageDialog(frame, "Title required"); return; }

            int priority;
            try { priority = Integer.parseInt(priorityField.getText().trim()); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(frame, "Invalid priority"); return; }

            LocalDateTime deadline = null;
            String dlText = deadlineField.getText().trim();
            if (!dlText.isEmpty() && !dlText.equals("yyyy-MM-dd HH:mm")) {
                try { deadline = LocalDateTime.parse(dlText, fmt); }
                catch (DateTimeParseException ex) { JOptionPane.showMessageDialog(frame, "Invalid deadline format"); return; }
            }

            String notes = notesArea.getText().trim();
            Task t = new Task(title, priority, deadline, notes);
            manager.add(t);
            refreshList();
            clearFields(titleField, priorityField, deadlineField, notesArea);
        });

        editBtn.addActionListener(e -> {
            Task sel = taskJList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(frame, "Select a task to edit"); return; }

            // simple edit dialog
            String title = JOptionPane.showInputDialog(frame, "Title:", sel.getTitle());
            if (title == null) return;
            String pStr = JOptionPane.showInputDialog(frame, "Priority (1 highest):", sel.getPriority());
            if (pStr == null) return;
            int p;
            try { p = Integer.parseInt(pStr.trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(frame, "Invalid priority"); return; }

            String dlStr = JOptionPane.showInputDialog(frame, "Deadline (yyyy-MM-dd HH:mm) or blank:", sel.getDeadline() == null ? "" : sel.getDeadline().format(fmt));
            if (dlStr == null) return;
            LocalDateTime dl = null;
            if (!dlStr.trim().isEmpty()) {
                try { dl = LocalDateTime.parse(dlStr.trim(), fmt); } catch (DateTimeParseException ex) { JOptionPane.showMessageDialog(frame, "Invalid deadline"); return; }
            }

            String notes = JOptionPane.showInputDialog(frame, "Notes:", sel.getNotes());
            if (notes == null) notes = "";

            // replace: remove then add updated task
            manager.remove(sel);
            Task updated = new Task(title, p, dl, notes);
            manager.add(updated);
            refreshList();
        });

        deleteBtn.addActionListener(e -> {
            Task sel = taskJList.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(frame, "Select a task to delete"); return; }
            int confirm = JOptionPane.showConfirmDialog(frame, "Delete selected task?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                manager.remove(sel);
                refreshList();
            }
        });

        refreshBtn.addActionListener(e -> refreshList());

        saveBtn.addActionListener(e -> {
            JFileChooser jc = new JFileChooser();
            int res = jc.showSaveDialog(frame);
            if (res == JFileChooser.APPROVE_OPTION) {
                try {
                    manager.saveToFile(jc.getSelectedFile());
                    JOptionPane.showMessageDialog(frame, "Saved successfully");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Save failed: " + ex.getMessage());
                }
            }
        });

        loadBtn.addActionListener(e -> {
            JFileChooser jc = new JFileChooser();
            int res = jc.showOpenDialog(frame);
            if (res == JFileChooser.APPROVE_OPTION) {
                try {
                    manager = TaskManager.loadFromFile(jc.getSelectedFile());
                    refreshList();
                    JOptionPane.showMessageDialog(frame, "Loaded successfully");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Load failed: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        frame.setVisible(true);
    }

    private void clearFields(JTextField titleField, JTextField priorityField, JTextField deadlineField, JTextArea notesArea) {
        titleField.setText("");
        priorityField.setText("1");
        deadlineField.setText("yyyy-MM-dd HH:mm");
        notesArea.setText("");
    }

    private void refreshList() {
        List<Task> tasks = manager.getAllSorted();
        listModel.clear();
        for (Task t : tasks) listModel.addElement(t);
    }

    private void startReminderTimer() {
        // check every 60 seconds
        Timer timer = new Timer(60 * 1000, e -> {
            manager.peekNext().ifPresent(t -> {
                if (t.getDeadline() != null && !t.getDeadline().isAfter(LocalDateTime.now())) {
                    // remind and remove from queue (or keep, depending on design). Here we show reminder but keep it.
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
                            "Reminder: " + t.getTitle() + "\nDue: " + t.getDeadline().format(fmt),
                            "Task Reminder", JOptionPane.INFORMATION_MESSAGE));
                }
            });
        });
        timer.setInitialDelay(5000);
        timer.start();
    }

    public static void main(String[] args) {
        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(MainUI::new);
    }
    
}
