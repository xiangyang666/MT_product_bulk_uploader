# Requirements Document

## Introduction

This document specifies the requirements for fixing the API URL configuration issue in the packaged Electron application. Currently, when the application is packaged and run as a standalone Electron app, API requests fail because they use relative paths (`/api`) which resolve to `file:///` protocol URLs instead of HTTP URLs pointing to the backend server.

## Glossary

- **Electron Application**: The packaged desktop application that runs the frontend
- **API Client**: The axios instance used for making HTTP requests to the backend
- **Base URL**: The root URL used for all API requests
- **Development Environment**: Local development setup with Vite dev server and proxy
- **Production Environment**: Packaged Electron application running on user's machine
- **Environment Variable**: Configuration value loaded from `.env` files

## Requirements

### Requirement 1

**User Story:** As a user running the packaged Electron application, I want API requests to connect to the correct backend server, so that I can successfully log in and use all application features.

#### Acceptance Criteria

1. WHEN the Electron application is packaged and run THEN the API Client SHALL use the full HTTP URL from environment variables as the base URL
2. WHEN making API requests in production THEN the system SHALL construct URLs using the configured backend server address
3. WHEN the environment variable VITE_API_BASE_URL is set THEN the API Client SHALL use that value as the base URL
4. WHEN the environment variable is not set THEN the API Client SHALL fall back to a default localhost URL
5. WHEN running in development mode with Vite dev server THEN the API Client SHALL continue to work with the existing proxy configuration

### Requirement 2

**User Story:** As a developer, I want the API configuration to work seamlessly in both development and production environments, so that I don't need to change code when building the application.

#### Acceptance Criteria

1. WHEN the application is built for production THEN the environment variables SHALL be embedded in the build output
2. WHEN switching between development and production environments THEN the API Client SHALL automatically use the appropriate base URL
3. WHEN the configuration is loaded THEN the system SHALL log the current environment and API URL for debugging purposes
