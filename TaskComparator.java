
package com.example.tasks;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;

public class TaskComparator implements Comparator<Task>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Task a, Task b) {
        // First by priority (smaller number = higher priority)
        int p = Integer.compare(a.getPriority(), b.getPriority());
        if (p != 0) return p;

        // Then by earliest deadline (null deadlines go last)
        LocalDateTime da = a.getDeadline();
        LocalDateTime db = b.getDeadline();
        if (da == null && db == null) return 0;
        if (da == null) return 1;
        if (db == null) return -1;
        return da.compareTo(db);
    }
}
