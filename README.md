# Work Management App

A cross-platform work management application that allows users to discover and connect with other users, assign tasks, and monitor task progress.

## Features

- User registration and login
- User discovery
- Send and accept connection requests
- Assign tasks to connected users
- Create personal tasks
- Track task status and progress
- Receive task notifications

## Technology Stack

### Mobile
- Kotlin Multiplatform
- Compose Multiplatform
- Android and iOS

### Backend
- Kotlin
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL

## Project Structure

```text
work-management-app/
├── mobile/       # Kotlin Multiplatform application
├── backend/      # Spring Boot REST API
├── README.md
└── .gitignore
```

## Project Status

Android supports signing in with an existing account, searching registered users by name prefix, and creating direct connections. Accepted connections appear on both users’ home pages.

## Local Android development

Run the backend on port 8081 with the existing PostgreSQL schema. The Android emulator debug build uses `http://10.0.2.2:8081` by default. Set `-PworkManagementApiUrl=<url>` when building to point to a different backend.

On Android 17 or later, allow the debug build’s local network permission when signing in to the development server.

Sign in with the email and password of an account created through `POST /api/users`. The app uses the backend’s HTTP Basic authentication and holds credentials in memory until sign-out or the app process ends. Basic authentication sends reusable credentials with every request. Plain HTTP is available in the Android debug build for trusted local development. Use HTTPS for release builds and for any connection over an untrusted network.

## Planned Workflow

1. Register or log in.
2. Discover other users.
3. Confirm a direct connection from a search result.
4. Find the connected user under My Connections.
5. Assign and manage tasks.
6. Monitor task progress and completion.
