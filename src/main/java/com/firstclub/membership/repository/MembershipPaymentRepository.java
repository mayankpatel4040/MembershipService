package com.firstclub.membership.repository;

import com.firstclub.membership.entity.MembershipPaymentTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipPaymentRepository extends JpaRepository<MembershipPaymentTransactions, Long> {
}
