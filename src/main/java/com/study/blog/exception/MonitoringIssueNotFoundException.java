package com.study.blog.exception;

public class MonitoringIssueNotFoundException extends RuntimeException {
    public MonitoringIssueNotFoundException() {
        super("not found error issue");
    }
}
