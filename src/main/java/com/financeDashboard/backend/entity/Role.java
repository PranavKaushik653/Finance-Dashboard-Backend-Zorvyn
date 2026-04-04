package com.financeDashboard.backend.entity;

public enum Role {
    VIEWER,         //can only view dashboard data
    ANALYST,        //can view records and access insights/summaries
    ADMIN           // full management access
}
