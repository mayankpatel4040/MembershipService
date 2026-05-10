package com.firstclub.membership.service;

import com.firstclub.membership.dto.CreateUserDto;
import com.firstclub.membership.entity.User;
import com.firstclub.membership.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TierRuleEvaluatorService tierRuleEvaluatorService;

    @Transactional
    public String createUser(CreateUserDto dto) {
        userRepository.save(
                User.builder()
                        .name(dto.getName())
                        .phoneNumber(dto.getPhoneNumber())
                        .cohortType(dto.getCohortType())
                        .build()
        );
        tierRuleEvaluatorService.evaluateRules(dto.getPhoneNumber());
        return "User created successfully: " + dto.getName() + " (" + dto.getPhoneNumber() + ")";
    }
}
