package com.example.carins.task;

import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.model.InsurancePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
public class PolicyExpiryLogger {

    private static final Logger log = LoggerFactory.getLogger(PolicyExpiryLogger.class);

    private final InsurancePolicyRepository policyRepository;
    private final Set<Long> loggedPolicies = new HashSet<>();

    public PolicyExpiryLogger(InsurancePolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) // Every 5 minutes
    public void logExpiredPolicies() {
        LocalDate today = LocalDate.now();
        var policies = policyRepository.findAll();

        for (InsurancePolicy policy : policies) {
            if (policy.getEndDate() != null &&
                    policy.getEndDate().isBefore(today) &&
                    !loggedPolicies.contains(policy.getId())) {

                log.info("Policy {} for car {} expired on {}",
                        policy.getId(),
                        policy.getCar().getId(),
                        policy.getEndDate());

                loggedPolicies.add(policy.getId());
            }
        }
    }
}
