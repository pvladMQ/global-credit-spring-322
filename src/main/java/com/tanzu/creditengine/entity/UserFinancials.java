package com.tanzu.creditengine.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * JPA Entity representing user financial data stored in PostgreSQL.
 * This entity is used for the "Complex Join" simulation during credit scoring.
 */
@Entity
@Table(name = "user_financials")
public class UserFinancials {

    @Id
    @Column(name = "ssn", length = 11)
    private String ssn;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "credit_history_score")
    private Integer creditHistoryScore;

    @Column(name = "criminal_record")
    private Boolean criminalRecord;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    // Default constructor for JPA
    public UserFinancials() {
    }

    public UserFinancials(String ssn, String fullName, Integer creditHistoryScore,
            Boolean criminalRecord, String riskLevel) {
        this.ssn = ssn;
        this.fullName = fullName;
        this.creditHistoryScore = creditHistoryScore;
        this.criminalRecord = criminalRecord;
        this.riskLevel = riskLevel;
    }

    // Getters and Setters
    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getCreditHistoryScore() {
        return creditHistoryScore;
    }

    public void setCreditHistoryScore(Integer creditHistoryScore) {
        this.creditHistoryScore = creditHistoryScore;
    }

    public Boolean getCriminalRecord() {
        return criminalRecord;
    }

    public void setCriminalRecord(Boolean criminalRecord) {
        this.criminalRecord = criminalRecord;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    @Override
    public String toString() {
        return "UserFinancials{" +
                "ssn='" + ssn + '\'' +
                ", fullName='" + fullName + '\'' +
                ", creditHistoryScore=" + creditHistoryScore +
                ", criminalRecord=" + criminalRecord +
                ", riskLevel='" + riskLevel + '\'' +
                '}';
    }
}
