# 🎨 UI/UX Design Document - Event Hive

## 1. Design Philosophy
The UI follows **Material Design 3 (M3)** principles, focusing on:
*   **Clarity**: Using cards and containers to separate hierarchical data (Categories vs. Clubs).
*   **Personalization**: Persistent dark/light theme support.
*   **Accessibility**: Semantic properties for screen readers and high-contrast color palettes.

## 2. Visual System
*   **Primary Color**: Professional Deep Blue (Brand)
*   **Typography**: Material 3 Headline, Body, and Label styles.
*   **Components**: 
    *   `SegmentedControl`: For toggling between Participation modes.
    *   `SectionCard`: Standardized containers for grouping related information.
    *   `BadgedBox`: Used in navigation for notifications and chat counts.

## 3. Core Screens
*   **Discovery**: Grid-based layout for Categories and Clubs with high-quality banners.
*   **Management Dashboard**: Statistics-heavy view for Admins to monitor registration numbers.
*   **Chat**: Bubble-based interface with distinct styles for DMs, Group, and Anonymous messages.
*   **Ticket View**: High-contrast QR code display for scanning in bright environments.

## 4. Interaction Patterns
*   **Swipe-to-Refresh**: Standardized data reloading across Lists.
*   **Dialogs**: Bottom-sheets or centered alerts for destructive actions (Delete, Leave Club).
*   **Deep Linking**: Instant redirection to specific nodes from external URLs.
