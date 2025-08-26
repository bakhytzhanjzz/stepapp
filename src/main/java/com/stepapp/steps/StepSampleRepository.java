package com.stepapp.steps;

import com.stepapp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface StepSampleRepository extends JpaRepository<StepSample, Long> {
    Optional<StepSample> findByUserAndProviderAndExternalId(User user, StepSample.Provider provider, String externalId);
    List<StepSample> findAllByUserAndStartedAtBetween(User user, OffsetDateTime from, OffsetDateTime to);
}
