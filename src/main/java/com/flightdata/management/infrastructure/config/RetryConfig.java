package com.flightdata.management.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Configuration for Spring Retry functionality.
 *
 * <p>Enables declarative retry logic using @Retryable annotations.
 * Used by CrazySupplierClient for automatic retry on transient failures.
 *
 * @author Vítor Matosinho Martins
 * @version 1.0
 * @since 2025-10-24
 */
@Configuration
@EnableRetry
public class RetryConfig {
  // Spring Retry is now enabled for @Retryable annotations
}