package com.eventproof;

import java.util.Objects;

public class Venue {
    private final int id;
    private final String name;
    private final int capacity;
    private final boolean backupPower;
    private final boolean backupInternet;

    public Venue(int id, String name, int capacity, boolean backupPower, boolean backupInternet) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.id = id;
        Objects.requireNonNull(name, "name");
        if (name.isBlank() || name.trim().length() > 150) {
            throw new IllegalArgumentException("Venue name must contain 1 to 150 characters");
        }
        this.name = name.trim();
        this.capacity = capacity;
        this.backupPower = backupPower;
        this.backupInternet = backupInternet;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public boolean hasBackupPower() { return backupPower; }
    public boolean hasBackupInternet() { return backupInternet; }

    @Override
    public String toString() { return name + " (capacity " + capacity + ")"; }
}
