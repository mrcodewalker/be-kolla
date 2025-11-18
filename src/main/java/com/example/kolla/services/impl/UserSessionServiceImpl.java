package com.example.kolla.services.impl;

import com.example.kolla.dto.search.UserSessionSearchDTO;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.UserSession;
import com.example.kolla.repositories.UserSessionRepository;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.responses.UserSessionResponse;
import com.example.kolla.services.UserSessionService;
import com.example.kolla.specifications.UserSessionSpecifications;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionServiceImpl implements UserSessionService {
    private final UserSessionRepository userSessionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public UserSession saveUserSession(UserSession studentSessions, HttpServletRequest request) {
        studentSessions.setDeviceInfo(request.getHeader("User-Agent"));
        studentSessions.setUserAgent(request.getHeader("User-Agent"));
        
        String clientIp = getClientIp(request);
        studentSessions.setIpAddress(clientIp);
        
        String location = this.getLocationFromIp(clientIp);
        studentSessions.setLocation(location);
        
        // Log để debug trong production
        log.info("User session created - IP: {}, Location: {}, User-Agent: {}", 
                clientIp, location, request.getHeader("User-Agent"));
        
        return this.userSessionRepository.save(studentSessions);
    }
    public String getClientIp(HttpServletRequest request) {
        // Kiểm tra các headers proxy phổ biến theo thứ tự ưu tiên
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "CF-Connecting-IP", // Cloudflare
            "X-Client-IP",
            "X-Forwarded",
            "Forwarded-For",
            "Forwarded"
        };
        
        for (String headerName : headerNames) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null && !headerValue.isEmpty() && !"unknown".equalsIgnoreCase(headerValue)) {
                // X-Forwarded-For có thể chứa nhiều IP, lấy IP đầu tiên
                String ip = headerValue.split(",")[0].trim();
                if (isValidIp(ip)) {
                    return ip;
                }
            }
        }
        
        // Fallback về remote address
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "127.0.0.1";
    }
    
    private boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }
        // Loại bỏ các IP local/private
        return !ip.startsWith("127.") && 
               !ip.startsWith("192.168.") && 
               !ip.startsWith("10.") && 
               !ip.startsWith("172.16.") &&
               !ip.equals("::1") &&
               !ip.equals("0:0:0:0:0:0:0:1");
    }
    public String getLocationFromIp(String ip) {
        // Kiểm tra nếu IP là local/private thì không cần gọi API
        if (!isValidIp(ip) || ip.startsWith("127.") || ip.startsWith("192.168.") || 
            ip.startsWith("10.") || ip.startsWith("172.16.")) {
            return "Local Network";
        }
        
        try {
            // Thử API ip-api.com trước
            String url = "http://ip-api.com/json/" + ip + "?fields=country,regionName,city";
            String response = restTemplate.getForObject(url, String.class);
            
            if (response != null && !response.isEmpty()) {
                JsonNode json = objectMapper.readTree(response);
                String country = json.path("country").asText("");
                String region = json.path("regionName").asText("");
                String city = json.path("city").asText("");
                
                String location = String.format("%s, %s, %s", city, region, country)
                    .replaceAll("(^[ ,]+|[ ,]+$)", "");
                
                if (!location.isEmpty() && !location.equals(", ,")) {
                    return location;
                }
            }
        } catch (Exception e) {
            // Log lỗi nhưng không throw exception
            log.warn("Error getting location from ip-api.com for IP {}: {}", ip, e.getMessage());
        }
        
        try {
            // Fallback: thử API ipinfo.io
            String fallbackUrl = "https://ipinfo.io/" + ip + "/json";
            String fallbackResponse = restTemplate.getForObject(fallbackUrl, String.class);
            
            if (fallbackResponse != null && !fallbackResponse.isEmpty()) {
                JsonNode json = objectMapper.readTree(fallbackResponse);
                String city = json.path("city").asText("");
                String region = json.path("region").asText("");
                String country = json.path("country").asText("");
                
                String location = String.format("%s, %s, %s", city, region, country)
                    .replaceAll("(^[ ,]+|[ ,]+$)", "");
                
                if (!location.isEmpty() && !location.equals(", ,")) {
                    return location;
                }
            }
        } catch (Exception e) {
            log.warn("Error getting location from ipinfo.io for IP {}: {}", ip, e.getMessage());
        }
        
        return "Unknown Location";
    }

    @Override
    @Transactional(readOnly = true)
    public UserSessionResponse getUserSessionById(Long id) {
        UserSession userSession = userSessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User session not found with id: " + id));
        return UserSessionResponse.mapToResponse(userSession);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSessionResponse> getUserSessionsByUserId(Long userId) {
        return userSessionRepository.findByUserId(userId).stream()
            .map(UserSessionResponse::mapToResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserSessionResponse> searchUserSessions(UserSessionSearchDTO searchDTO) {
        // Tạo specification từ search criteria
        Specification<UserSession> spec = UserSessionSpecifications.withSearchCriteria(searchDTO);
        
        // Tạo sort
        String sortBy = searchDTO.getSortBy() != null ? searchDTO.getSortBy() : "createdAt";
        String sortDirection = searchDTO.getSortDirection() != null ? searchDTO.getSortDirection() : "desc";
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        // Tạo pageable
        int page = searchDTO.getPage() != null ? searchDTO.getPage() : 0;
        int size = searchDTO.getSize() != null ? searchDTO.getSize() : 10;
        PageRequest pageable = PageRequest.of(page, size, sort);
        
        // Thực hiện query
        Page<UserSession> sessionPage = userSessionRepository.findAll(spec, pageable);
        
        // Chuyển đổi sang response
        List<UserSessionResponse> sessionResponses = sessionPage.getContent().stream()
                .map(UserSessionResponse::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
        
        // Tạo PageResponse
        PageResponse<UserSessionResponse> response = new PageResponse<>();
        response.setContent(sessionResponses);
        response.setPageNumber(sessionPage.getNumber());
        response.setPageSize(sessionPage.getSize());
        response.setTotalElements(sessionPage.getTotalElements());
        response.setTotalPages(sessionPage.getTotalPages());
        response.setLast(sessionPage.isLast());
        
        return response;
    }

    @Override
    @Transactional
    public void deleteUserSession(Long id) {
        if (!userSessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("User session not found with id: " + id);
        }
        userSessionRepository.deleteById(id);
    }
}