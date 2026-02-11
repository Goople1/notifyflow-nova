package com.nova.notifications.domain.model;

/**
 * Defines the available notification channels.
 * <p>
 * Unlike a sealed interface approach, using an enum for channel types
 * combined with an open Notification interface allows external consumers
 * to add new channels WITHOUT modifying this library's source code,
 * respecting the Open/Closed principle.
 * </p>
 */
public enum ChannelType {
    EMAIL,
    SMS,
    PUSH,
    SLACK
}
