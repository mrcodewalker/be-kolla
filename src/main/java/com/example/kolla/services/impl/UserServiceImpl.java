package com.example.kolla.services.impl;

import com.example.kolla.dto.UserDTO;
import com.example.kolla.dto.UserCreateDTO;
import com.example.kolla.dto.search.UserSearchDTO;
import com.example.kolla.enums.Degree;
import com.example.kolla.responses.UserResponse;
import com.example.kolla.specifications.UserSpecifications;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.enums.ActionLog;
import com.example.kolla.enums.Role;
import com.example.kolla.exceptions.BadRequestException;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.User;
import com.example.kolla.repositories.DepartmentRepository;
import com.example.kolla.repositories.RoleRepository;
import com.example.kolla.repositories.UserRepository;
import com.example.kolla.services.UserService;
import com.example.kolla.services.UserSessionService;
import com.example.kolla.models.UserSession;
import com.example.kolla.utils.AuthorizationTokenService;
import com.example.kolla.exceptions.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.example.kolla.utils.DateTimeUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserSessionService userSessionService;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final AuthorizationTokenService authorizationTokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public User login(String email, String password, HttpServletRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BadRequestException("User account is not active");
        }
        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setAction(ActionLog.LOGIN.toString());
        userSession.setCreatedAt(DateTimeUtils.now());
        userSession.setUpdatedAt(DateTimeUtils.now());
        userSession.setActive(true);
        userSessionService.saveUserSession(userSession, request);
        return user;
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateDTO createDTO, HttpServletRequest request) {
        if (userRepository.existsByEmail(createDTO.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setEmail(createDTO.getEmail());
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        user.setName(createDTO.getFullName());
        user.setActive(true);
        user.setDepartment(
                departmentRepository.findById(
                        createDTO.getDepartmentId()
                ).orElseThrow(() -> new ResourceNotFoundException("Can not find department"))
        );
        if (createDTO.getRoleId() != null) {
            user.setRole(
                    this.roleRepository.findById(createDTO.getRoleId())
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + createDTO.getRoleId()))
            );
        } else {
            user.setRole(
                    this.roleRepository.findByName(Role.USER.getDisplayName())
            );
        }
        user.setDegree(Degree.OTHER);

        User savedUser = userRepository.save(user);
        
        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setAction(ActionLog.CREATE_USER.toString());
        userSession.setCreatedAt(DateTimeUtils.now());
        userSession.setUpdatedAt(DateTimeUtils.now());
        userSession.setActive(true);
        userSessionService.saveUserSession(userSession, request);
        
        return UserResponse.mapToResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserDTO updateDTO, HttpServletRequest request) {
        User user = this.checkValidDTO(updateDTO);

        User updatedUser = userRepository.save(user);
        
        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setAction(ActionLog.UPDATE_INFO.toString());
        userSession.setCreatedAt(DateTimeUtils.now());
        userSession.setUpdatedAt(DateTimeUtils.now());
        userSession.setActive(true);
        userSessionService.saveUserSession(userSession, request);
        
        return UserResponse.mapToResponse(updatedUser);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserResponse.mapToResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserResponse.mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchUsers(UserSearchDTO searchDTO) {
        // Tạo specification từ search criteria
        org.springframework.data.jpa.domain.Specification<User> spec = UserSpecifications.withSearchCriteria(searchDTO);
        
        // Tạo sort
        String sortBy = searchDTO.getSortBy() != null ? searchDTO.getSortBy() : "createdAt";
        String sortDirection = searchDTO.getSortDirection() != null ? searchDTO.getSortDirection() : "desc";
        org.springframework.data.domain.Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                org.springframework.data.domain.Sort.by(sortBy).descending() : 
                org.springframework.data.domain.Sort.by(sortBy).ascending();
        
        // Tạo pageable
        int page = searchDTO.getPage() != null ? searchDTO.getPage() : 0;
        int size = searchDTO.getSize() != null ? searchDTO.getSize() : 10;
        org.springframework.data.domain.Pageable pageable = PageRequest.of(page, size, sort);
        
        // Thực hiện query
        Page<User> userPage = userRepository.findAll(spec, pageable);
        
        // Chuyển đổi sang response
        PageResponse<UserResponse> response = new PageResponse<>();
        response.setContent(userPage.getContent().stream()
            .map(UserResponse::mapToResponse)
            .toList());
        response.setPageNumber(userPage.getNumber());
        response.setPageSize(userPage.getSize());
        response.setTotalElements(userPage.getTotalElements());
        response.setTotalPages(userPage.getTotalPages());
        response.setLast(userPage.isLast());
        
        return response;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public UserResponse updateAvatar(Long id, String avatarUrl, HttpServletRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setImgUrl(avatarUrl);
        User updatedUser = userRepository.save(user);
        
        // Create user session for avatar update
        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setAction(ActionLog.UPDATE_AVATAR.toString());
        userSession.setCreatedAt(LocalDateTime.now());
        userSession.setUpdatedAt(LocalDateTime.now());
        userSession.setActive(true);
        userSessionService.saveUserSession(userSession, request);
        
        return UserResponse.mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updatePassword(Long id, String oldPassword, String newPassword, HttpServletRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);
        
        // Create user session for password update
        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setAction(ActionLog.CHANGE_PASSWORD.toString());
        userSession.setCreatedAt(LocalDateTime.now());
        userSession.setUpdatedAt(LocalDateTime.now());
        userSession.setActive(true);
        userSessionService.saveUserSession(userSession, request);
        
        return UserResponse.mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateMyProfile(UserDTO updateDTO, HttpServletRequest request) {
        // Extract user từ token - đảm bảo A chỉ đổi được thông tin của A
        User currentUser = authorizationTokenService.extractUser(request);
        if (currentUser == null) {
            throw new UnauthorizedException("Unable to extract user from token");
        }

        // Update thông tin user hiện tại
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(currentUser.getEmail()) &&
                userRepository.existsByEmail(updateDTO.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (updateDTO.getEmail() != null) {
            currentUser.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getName() != null) {
            currentUser.setName(updateDTO.getName());
        }
        if (updateDTO.getPhoneNumber() != null) {
            currentUser.setPhoneNumber(updateDTO.getPhoneNumber());
        }
        if (updateDTO.getImgUrl() != null) {
            currentUser.setImgUrl(updateDTO.getImgUrl());
        }
        if (updateDTO.getDob() != null) {
            currentUser.setDob(updateDTO.getDob());
        }
        if (updateDTO.getAddress() != null) {
            currentUser.setAddress(updateDTO.getAddress());
        }
        if (updateDTO.getBankName() != null) {
            currentUser.setBankName(updateDTO.getBankName());
        }
        if (updateDTO.getBankNumber() != null) {
            currentUser.setBankNumber(updateDTO.getBankNumber());
        }
        if (updateDTO.getDegree() != null) {
            currentUser.setDegree(Degree.valueOf(updateDTO.getDegree()));
        }
        // Không cho phép user tự đổi role, department thông qua endpoint này
        // Chỉ admin mới có quyền đổi role/department

        User updatedUser = userRepository.save(currentUser);
        
        // Create user session log
        UserSession userSession = new UserSession();
        userSession.setUser(updatedUser);
        userSession.setAction(ActionLog.UPDATE_INFO.toString());
        userSession.setCreatedAt(DateTimeUtils.now());
        userSession.setUpdatedAt(DateTimeUtils.now());
        userSession.setActive(true);
        userSessionService.saveUserSession(userSession, request);
        
        return UserResponse.mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateMyPassword(String oldPassword, String newPassword, HttpServletRequest request) {
        // Extract user từ token - đảm bảo A chỉ đổi được password của A
        User currentUser = authorizationTokenService.extractUser(request);
        if (currentUser == null) {
            throw new UnauthorizedException("Unable to extract user from token");
        }

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new BadRequestException("Invalid old password");
        }

        // Update password
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(currentUser);
        
        // Create user session log
        UserSession userSession = new UserSession();
        userSession.setUser(updatedUser);
        userSession.setAction(ActionLog.CHANGE_PASSWORD.toString());
        userSession.setCreatedAt(DateTimeUtils.now());
        userSession.setUpdatedAt(DateTimeUtils.now());
        userSession.setActive(true);
        userSessionService.saveUserSession(userSession, request);
        
        return UserResponse.mapToResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<UserResponse> searchByNameOrEmail(String keyword) {
        String q = keyword == null ? "" : keyword.trim();
        return userRepository
                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q)
                .stream()
                .map(UserResponse::mapToResponse)
                .toList();
    }

    private User checkValidDTO(UserDTO userDTO){
        User user = this.userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Can not find user"));
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getName() != null) {
            user.setName(userDTO.getName());
        }
        if (userDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }
        if (userDTO.getImgUrl() != null) {
            user.setImgUrl(userDTO.getImgUrl());
        }
        if (userDTO.getRoleId() != null) {
            user.setRole(
                    roleRepository.findById(userDTO.getRoleId())
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + userDTO.getRoleId()))
            );
        }
        if (userDTO.getDegree() != null) {
            user.setDegree(Degree.valueOf(userDTO.getDegree()));
        }
        user.setActive(true);
        if (userDTO.getDob() != null) {
            user.setDob(userDTO.getDob());
        }
        if (userDTO.getAddress() != null) {
            user.setAddress(userDTO.getAddress());
        }
        if (userDTO.getBankName() != null) {
            user.setBankName(userDTO.getBankName());
        }
        if (userDTO.getBankNumber() != null) {
            user.setBankNumber(userDTO.getBankNumber());
        }
        return user;
    }
    protected UserDTO mapToResponse(User user){
        return UserDTO.builder()
                .imgUrl(user.getImgUrl())
                .name(user.getName())
                .email(user.getEmail())
                .id(user.getId())
                .userCode(user.getUserCode())
                .isActive(user.isActive())
                .dob(user.getDob())
                .address(user.getAddress())
                .bankName(user.getBankName())
                .phoneNumber(user.getPhoneNumber())
                .bankNumber(user.getBankNumber())
                .build();
    }
}