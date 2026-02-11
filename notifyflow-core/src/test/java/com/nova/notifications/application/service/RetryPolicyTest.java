package com.nova.notifications.application.service;

import com.nova.notifications.application.retry.RetryPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RetryPolicy - Configuration & Delay Calculation")
class RetryPolicyTest {

    @Test
    @DisplayName("Should reject maxAttempts less than 1")
    void rejectZeroAttempts() {
        assertThatThrownBy(() -> new RetryPolicy(0, Duration.ofSeconds(1), 2.0, Duration.ofSeconds(30)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject backoffMultiplier less than 1.0")
    void rejectLowMultiplier() {
        assertThatThrownBy(() -> new RetryPolicy(3, Duration.ofSeconds(1), 0.5, Duration.ofSeconds(30)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should calculate exponential delay correctly")
    void exponentialDelay() {
        var policy = new RetryPolicy(5, Duration.ofSeconds(1), 2.0, Duration.ofSeconds(30));

        assertThat(policy.delayForAttempt(0)).isEqualTo(Duration.ZERO);
        assertThat(policy.delayForAttempt(1)).isEqualTo(Duration.ofSeconds(1));   // 1s * 2^0
        assertThat(policy.delayForAttempt(2)).isEqualTo(Duration.ofSeconds(2));   // 1s * 2^1
        assertThat(policy.delayForAttempt(3)).isEqualTo(Duration.ofSeconds(4));   // 1s * 2^2
    }

    @Test
    @DisplayName("Should cap delay at maxDelay")
    void delayCapping() {
        var policy = new RetryPolicy(10, Duration.ofSeconds(1), 2.0, Duration.ofSeconds(5));

        assertThat(policy.delayForAttempt(5)).isEqualTo(Duration.ofSeconds(5));  // Would be 16s, capped at 5s
    }

    @Test
    @DisplayName("Default policy should have sensible values")
    void defaultPolicy() {
        var policy = RetryPolicy.defaultPolicy();

        assertThat(policy.maxAttempts()).isEqualTo(3);
        assertThat(policy.initialDelay()).isEqualTo(Duration.ofSeconds(1));
        assertThat(policy.backoffMultiplier()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("noRetry should have 1 attempt")
    void noRetryPolicy() {
        var policy = RetryPolicy.noRetry();

        assertThat(policy.maxAttempts()).isEqualTo(1);
    }
}
