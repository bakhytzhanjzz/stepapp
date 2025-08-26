package com.stepapp.steps;

import com.stepapp.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "step_samples", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "provider", "external_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StepSample {

    public enum Provider { APPLE, GOOGLE, MOCK, DEVICE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "external_id", length = 200)
    private String externalId;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private OffsetDateTime endedAt;

    @Column(nullable = false)
    private Integer steps;

    @Column(name = "source", length = 200)
    private String source;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    @PrePersist
    public void prePersist() {
        if (receivedAt == null) receivedAt = OffsetDateTime.now();
    }
}
