package com.ji.gmall_publication.mapper;

import java.util.List;
import java.util.Map;

public interface DauMapper {
    public Integer selectDauTotal(String date);
    public List<Map> selectDauTotalHourMap(String date);
}
