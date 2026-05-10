package com.firstclub.membership.repository;

import com.firstclub.membership.entity.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipTierRepository extends JpaRepository<MembershipTier, Long> {

    //find by name
    MembershipTier findByName(String name);
}

