# 📝 Product Requirement Document (PRD) - Event Hive

## 1. Vision & Purpose
**Event Hive** is a centralized ecosystem designed to streamline club management and event coordination within college campuses. It aims to bridge the communication gap between student organizers and attendees while providing robust oversight for college administrations.

## 2. Target Audience
*   **Students**: Event discovery, registration, and community interaction.
*   **Club Organizers**: Event management, member coordination, and attendee tracking.
*   **College Admin (Student Guides)**: Oversight of all clubs, role assignment, and reporting.
*   **Super Admins**: Platform-wide management and multi-college onboarding.

## 3. Functional Requirements
### A. User Management
*   Secure login/signup with college email verification.
*   Automated college assignment based on email domain.
*   Multi-role profile support (Student, Club Admin, College Admin).

### B. Club & Event Management
*   Hierarchical organization: Categories -> Clubs -> Events.
*   Event registration with support for Solo and Group (Team) modes.
*   Dynamic ticketing system with unique QR codes for entry verification.

### C. Communication (The Chat Hub)
*   **Direct Messages**: Peer-to-peer communication.
*   **Group Chats**: Automatic room creation for every Club and Event.
*   **Anonymous Chat**: A safe space for college-wide discussions using daily-changing aliases.

### D. Administration & Security
*   Granular Role-Based Access Control (RBAC).
*   Permission-protected navigation (NavGuards).
*   Automated chat locking for expired events.

## 4. Non-Functional Requirements
*   **Real-time**: Messaging and event updates must reflect instantly.
*   **Security**: Prevention of privilege escalation and sender spoofing.
*   **Scalability**: Architecture must support thousands of users per college.
*   **Accessibility**: Support for screen readers and inclusive UI.
