package com.firstclub.membership.repository;

import com.firstclub.membership.entity.TierBenefit;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TierBenefitRepository extends JpaRepository<TierBenefit, Long> {

    @Cacheable("tierBenefits")
    List<TierBenefit> findByMembershipTierId(Long membershipTierId);
}
