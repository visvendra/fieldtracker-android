package com.allsmart.fieldtracker.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by allsmartlt218 on 02-12-2016.
 */

public class Leave implements Parcelable {
    private String days;
    private String fromDate;
    private String toDate;
    private String status;
    private String reason;
    private String enumType;
    private String reasonType;
    private String partyRelationShipId;
    private String agentName;
    private String enumTypeId;
    private String reasonTypeId;

    public String getEnumTypeId() {
        return enumTypeId;
    }

    public void setEnumTypeId(String enumTypeId) {
        this.enumTypeId = enumTypeId;
    }

    public String getReasonTypeId() {
        return reasonTypeId;
    }

    public void setReasonTypeId(String reasonTypeId) {
        this.reasonTypeId = reasonTypeId;
    }

    public Leave(){

    }

    protected Leave(Parcel in) {
        days = in.readString();
        fromDate = in.readString();
        toDate = in.readString();
        status = in.readString();
        reason = in.readString();
        enumType = in.readString();
        reasonType = in.readString();
        partyRelationShipId = in.readString();
        agentName = in.readString();
        enumTypeId = in.readString();
        reasonTypeId =in.readString();
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public static Creator<Leave> getCREATOR() {
        return CREATOR;
    }

    public static final Creator<Leave> CREATOR = new Creator<Leave>() {
        @Override
        public Leave createFromParcel(Parcel in) {
            return new Leave(in);
        }

        @Override
        public Leave[] newArray(int size) {
            return new Leave[size];
        }
    };

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        if(Integer.parseInt(days) == 1) {
            this.days = days + " Day";
        } else {
            this.days = days + " Days";
        }

    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getEnumType() {
        return enumType;
    }

    public void setEnumType(String enumType) {
        this.enumType = enumType;
    }

    public String getReasonType() {
        return reasonType;
    }

    public void setReasonType(String reasonType) {
        this.reasonType = reasonType;
    }

    public String getPartyRelationShipId() {
        return partyRelationShipId;
    }

    public void setPartyRelationShipId(String partyRelationShipId) {
        this.partyRelationShipId = partyRelationShipId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(days);
        dest.writeString(fromDate);
        dest.writeString(toDate);
        dest.writeString(status);
        dest.writeString(reason);
        dest.writeString(enumType);
        dest.writeString(reasonType);
        dest.writeString(partyRelationShipId);
        dest.writeString(agentName);
        dest.writeString(enumTypeId);
        dest.writeString(reasonTypeId);
    }
}
