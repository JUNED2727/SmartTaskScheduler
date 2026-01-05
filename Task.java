
package com.example.tasks;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private int priority; // 1 = highest, larger number = lower priority
    private LocalDateTime deadline;
    private String notes;

    public Task(String title, int priority, LocalDateTime deadline, String notes) {
        this.title = title;
        this.priority = priority;
        this.deadline = deadline;
        this.notes = notes;
    }

    public String getTitle() { return title; }
    public int getPriority() { return priority; }
    public LocalDateTime getDeadline() { return deadline; }
    public String getNotes() { return notes; }

    public void setTitle(String title) { this.title = title; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format("[%d] %s (due: %s)%s",
                priority,
                title,
                deadline == null ? "no deadline" : deadline.format(fmt),
                (notes == null || notes.isEmpty()) ? "" : " - " + notes);
    }
}
