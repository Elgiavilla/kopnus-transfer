package com.kopnus.banking.shared.transform;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ConvertTransform {

    public String instantTimeToString(Instant dateTime){
        if (dateTime != null) {
            try {
                ZonedDateTime wibTime = dateTime.atZone(ZoneId.of("Asia/Jakarta"));

                String formatted = wibTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return formatted;
            }catch (Exception e){
                return dateTime.toString();
            }
        }
        return null;
    }

}
