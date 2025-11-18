package com.example.kolla.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    
    private static final ZoneId VIETNAM_TIMEZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Lấy thời gian hiện tại theo múi giờ Việt Nam
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(VIETNAM_TIMEZONE);
    }
    
    /**
     * Chuyển đổi LocalDateTime sang ZonedDateTime với timezone Việt Nam
     */
    public static ZonedDateTime toVietnamTime(LocalDateTime localDateTime) {
        return localDateTime.atZone(VIETNAM_TIMEZONE);
    }
    
    /**
     * Format LocalDateTime theo định dạng Việt Nam
     */
    public static String formatVietnamTime(LocalDateTime localDateTime) {
        return localDateTime.format(FORMATTER);
    }
    
    /**
     * Lấy thời gian hiện tại đã format theo múi giờ Việt Nam
     */
    public static String nowFormatted() {
        return formatVietnamTime(now());
    }
}

