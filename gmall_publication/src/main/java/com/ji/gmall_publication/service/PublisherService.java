package com.ji.gmall_publication.service;

import java.util.Map;

public interface PublisherService {
    public Integer getRealtimeTotal(String date);

    public Map getDauTotalHourMap(String date);
}
