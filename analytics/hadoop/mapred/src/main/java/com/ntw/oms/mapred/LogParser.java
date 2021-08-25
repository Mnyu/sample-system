package com.ntw.oms.mapred;

import java.util.*;

/**
 * Created by anurag on 16/06/17.
 */

public class LogParser {

    public static final String HOSTNAME = "hostname";
    public static final String LOGLEVEL = "level";

    public Map<String, String> parseLog(String line) {

        Map<String, String> dataMap = new HashMap<>();
        String[] tokens = line.split("\\s\\|\\s");

        for (String token : tokens) {
            String[] keyValuePair = token.split("=", 2);
            String tokenKey = (keyValuePair.length > 0) ? keyValuePair[0] : null;
            String tokenValue = (keyValuePair.length > 1) ? keyValuePair[1] : "";
            if (tokenKey != null) {
                tokenKey = tokenKey.trim().toLowerCase();
                dataMap.put(tokenKey, tokenValue);
            }
        }
        return dataMap;
    }

    public String getKey(Long filePos, Map<String, String> dataMap) {
        String time = dataMap.get("time");
        return time+"|"+filePos.toString();
    }
}


