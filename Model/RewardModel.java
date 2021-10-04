package com.harsh.enitr.Model;

import com.google.firebase.Timestamp;

import java.util.Date;

public class RewardModel {
    private String type;
    private String lowerLimit;
    private String upperLimit;
    private String discORamt;
    private String coupenBody;
    private Date timestamp;
    private Boolean alreadyUsed;
    private String coupenId;

    public RewardModel(String coupenId,String type, String lowerLimit, String upperLimit, String discORamt, String coupenBody, Date timestamp,Boolean alreadyUsed) {
        this.coupenId=coupenId;
        this.type = type;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.discORamt = discORamt;
        this.coupenBody = coupenBody;
        this.timestamp = timestamp;
        this.alreadyUsed=alreadyUsed;
    }

    public String getCoupenId() {
        return coupenId;
    }

    public void setCoupenId(String coupenId) {
        this.coupenId = coupenId;
    }

    public Boolean getAlreadyUsed() {
        return alreadyUsed;
    }

    public void setAlreadyUsed(Boolean alreadyUsed) {
        this.alreadyUsed = alreadyUsed;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(String lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public String getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(String upperLimit) {
        this.upperLimit = upperLimit;
    }

    public String getDiscORamt() {
        return discORamt;
    }

    public void setDiscORamt(String discount) {
        this.discORamt = discount;
    }

    public String getCoupenBody() {
        return coupenBody;
    }

    public void setCoupenBody(String coupenBody) {
        this.coupenBody = coupenBody;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
