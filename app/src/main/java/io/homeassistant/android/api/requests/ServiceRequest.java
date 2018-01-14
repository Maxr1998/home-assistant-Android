package io.homeassistant.android.api.requests;

import com.afollestad.ason.AsonName;

public class ServiceRequest extends HassRequest {
    protected final String domain;
    protected final String service;
    @AsonName(name = "service_data")
    protected final ServiceData data;

    public ServiceRequest(String domain, String service, String entityId) {
        super("call_service");
        this.domain = domain;
        this.service = service;
        data = new ServiceData(entityId);
    }
}