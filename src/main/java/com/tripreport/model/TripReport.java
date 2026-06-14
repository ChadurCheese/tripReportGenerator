package com.tripreport.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete Trip Report data.
 * Contains header information and a list of trip entries.
 */
public class TripReport {
    private String mileageStart;
    private String mileageFinish;
    private LocalDate tripStartDate;
    private LocalDate tripEndDate;
    private List<TripEntry> entries;

    public TripReport() {
        this.entries = new ArrayList<>();
        this.mileageStart = "";
        this.mileageFinish = "";
        this.tripStartDate = null;
        this.tripEndDate = null;
    }

    // Getters and Setters
    public String getMileageStart() {
        return mileageStart;
    }

    public void setMileageStart(String mileageStart) {
        this.mileageStart = mileageStart;
    }

    public String getMileageFinish() {
        return mileageFinish;
    }

    public void setMileageFinish(String mileageFinish) {
        this.mileageFinish = mileageFinish;
    }

    public LocalDate getTripStartDate() {
        return tripStartDate;
    }

    public void setTripStartDate(LocalDate tripStartDate) {
        this.tripStartDate = tripStartDate;
    }

    public LocalDate getTripEndDate() {
        return tripEndDate;
    }

    public void setTripEndDate(LocalDate tripEndDate) {
        this.tripEndDate = tripEndDate;
    }

    public List<TripEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<TripEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(TripEntry entry) {
        entries.add(entry);
    }

    public void removeEntry(int index) {
        if (index >= 0 && index < entries.size()) {
            entries.remove(index);
        }
    }

    /**
     * Calculate total mileage based on start and finish values.
     * @return total mileage, or -1 if values are invalid
     */
    public long getTotalMileage() {
        try {
            if (mileageStart == null || mileageStart.trim().isEmpty() ||
                mileageFinish == null || mileageFinish.trim().isEmpty()) {
                return -1;
            }
            long start = Long.parseLong(mileageStart.trim());
            long finish = Long.parseLong(mileageFinish.trim());
            return finish - start;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "TripReport{" +
                "mileageStart='" + mileageStart + '\'' +
                ", mileageFinish='" + mileageFinish + '\'' +
                ", tripStartDate=" + tripStartDate +
                ", tripEndDate=" + tripEndDate +
                ", entries=" + entries.size() +
                '}';
    }
}
