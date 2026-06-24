package com.github.huangguasky.awesometools.audit;

import org.springframework.context.ApplicationEvent;

public class AuditLogEvent extends ApplicationEvent {

    public AuditLogEvent(AuditLogRecord source) {
        super(source);
    }

    public AuditLogRecord getRecord() {
        return (AuditLogRecord) getSource();
    }
}
