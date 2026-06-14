package com.tripreport.model;

/**
 * Represents a single trip entry (row) in the Trip Report.
 * Corresponds to one row on the PDF template.
 */
public class TripEntry {
    private String mileage;
    private String cityZipCode;
    private String shipper;
    private String consignee;
    private String type;  // EM, TL, LTL
    private String trailerNumber;
    private String dutyCode;
    private boolean detailRow;  // Mark this as a detail row for H/J duty codes (merged shipper/consignee)
    private String detailText;

    public TripEntry() {
        this("", "", "", "", "", "", "", false);
    }

    public TripEntry(String mileage, String cityZipCode, String shipper, String consignee,
                     String type, String trailerNumber, String dutyCode) {
        this(mileage, cityZipCode, shipper, consignee, type, trailerNumber, dutyCode, false);
    }

    public TripEntry(String mileage, String cityZipCode, String shipper, String consignee,
                     String type, String trailerNumber, String dutyCode, boolean detailRow) {
        this.mileage = mileage;
        this.cityZipCode = cityZipCode;
        this.shipper = shipper;
        this.consignee = consignee;
        this.type = type;
        this.trailerNumber = trailerNumber;
        this.dutyCode = dutyCode;
        this.detailRow = detailRow;
    }

    // Getters and Setters
    public String getMileage() {
        return mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getCityZipCode() {
        return cityZipCode;
    }

    public void setCityZipCode(String cityZipCode) {
        this.cityZipCode = cityZipCode;
    }

    public String getShipper() {
        return shipper;
    }

    public void setShipper(String shipper) {
        this.shipper = shipper;
    }

    public void setDetailText(String detailText) {
        this.detailText = detailText;
    }

    public String getDetailText() {
        return detailText;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTrailerNumber() {
        return trailerNumber;
    }

    public void setTrailerNumber(String trailerNumber) {
        this.trailerNumber = trailerNumber;
    }

    public String getDutyCode() {
        return dutyCode;
    }

    public void setDutyCode(String dutyCode) {
        this.dutyCode = dutyCode;
    }

    public boolean isDetailRow() {
        return detailRow;
    }

    public void setDetailRow(boolean detailRow) {
        this.detailRow = detailRow;
    }

    /**
     * Check if this row is populated (has at least one field filled).
     * Used to determine if the row should be written to the PDF.
     */
    public boolean isPopulated() {
        if (detailRow) {
            return (shipper != null && !shipper.trim().isEmpty())
                || (consignee != null && !consignee.trim().isEmpty())
                || (detailText != null && !detailText.trim().isEmpty());
        }
        
        return mileage != null && !mileage.trim().isEmpty() ||
               cityZipCode != null && !cityZipCode.trim().isEmpty() ||
               shipper != null && !shipper.trim().isEmpty() ||
               consignee != null && !consignee.trim().isEmpty() ||
               type != null && !type.trim().isEmpty() ||
               trailerNumber != null && !trailerNumber.trim().isEmpty() ||
               dutyCode != null && !dutyCode.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "TripEntry{" +
                "mileage='" + mileage + '\'' +
                ", cityZipCode='" + cityZipCode + '\'' +
                ", shipper='" + shipper + '\'' +
                ", consignee='" + consignee + '\'' +
                ", type='" + type + '\'' +
                ", trailerNumber='" + trailerNumber + '\'' +
                ", dutyCode='" + dutyCode + '\'' +
                ", detailText='" + detailText + '\'' +
                '}';
    }
}
