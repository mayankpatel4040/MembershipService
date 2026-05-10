package com.firstclub.membership.repository;

import com.firstclub.membership.entity.UserMembership;
import com.firstclub.membership.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {

    UserMembership findByUserIdAndStatus(Long userId, SubscriptionStatus status);

    UserMembership findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, SubscriptionStatus status);
}
