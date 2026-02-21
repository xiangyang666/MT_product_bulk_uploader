# Design Document

## Overview

This design addresses the API URL configuration issue in the packaged Electron application. The solution modifies the axios instance configuration to use the full backend URL from environment variables instead of relying on relative paths that only work with Vite's development proxy.

## Architecture

The fix involves modifying the API client initialization to:
1. Import the API_BASE_URL from the centralized config module
2. Use the full URL as the axios baseURL instead of the relative `/api` path
3. Ensure environment variables are properly loaded in both development and production builds

### Component Interaction

```
Environment Variables (.env.production)
    ↓
Config Module (src/config/index.js)
    ↓
API Client (src/api/index.js)
    ↓
Backend Server (http://106.55.102.48:8080)
```

## Components and Interfaces

### 1. Environment Configuration Files

**Files:**
- `.env.development`: Contains `VITE_API_BASE_URL=http://localhost:8080`
- `.env.production`: Contains `VITE_API_BASE_URL=http://106.55.102.48:8080`

**Purpose:** Store environment-specific configuration values that Vite will embed during build.

### 2. Config Module (`src/config/index.js`)

**Current Implementation:** Already correctly exports `API_BASE_URL` from environment variables.

**No changes needed** - this module is working correctly.

### 3. API Client (`src/api/index.js`)

**Current Issue:**
```javascript
const request = axios.create({
  baseURL: '/api',  // ❌ Relative path fails in Electron
  timeout: 30000,
  // ...
})
```

**Required Change:**
```javascript
import { API_BASE_URL } from '@/config'

const request = axios.create({
  baseURL: `${API_BASE_URL}/api`,  // ✅ Full URL works everywhere
  timeout: 30000,
  // ...
})
```

## Data Models

No data model changes required.

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: API URL Construction
*For any* environment configuration, the constructed API base URL should be a valid absolute HTTP/HTTPS URL that includes the protocol, host, port, and `/api` path.
**Validates: Requirements 1.1, 1.2, 1.3**

### Property 2: Environment Variable Fallback
*For any* execution context where VITE_API_BASE_URL is undefined, the API Client should use the default localhost URL without throwing errors.
**Validates: Requirements 1.4**

### Property 3: Development Mode Compatibility
*For any* API request made in development mode, the request should successfully reach the backend server using either the proxy or direct URL.
**Validates: Requirements 1.5**

## Error Handling

The existing error handling in the axios interceptors is sufficient. The response interceptor already handles:
- Network connection failures
- HTTP status codes (401, 500, 502, etc.)
- Request timeouts

No additional error handling is needed for this fix.

## Testing Strategy

### Unit Tests

1. **Config Module Test**: Verify that `API_BASE_URL` is correctly loaded from environment variables
2. **URL Construction Test**: Verify that the axios baseURL is correctly constructed with the full URL

### Manual Testing

1. **Development Environment Test**:
   - Start Vite dev server
   - Verify login works
   - Check browser network tab shows requests to `http://localhost:8080/api/auth/login`

2. **Production Build Test**:
   - Build the application with `npm run build`
   - Package with Electron
   - Run the packaged app
   - Verify login works
   - Check that requests go to `http://106.55.102.48:8080/api/auth/login`

3. **Environment Variable Test**:
   - Test with different VITE_API_BASE_URL values
   - Test with missing environment variable (should use localhost)

### Property-Based Tests

Property-based testing is not applicable for this configuration fix, as it involves environment-specific setup rather than algorithmic logic that can be tested across random inputs.

## Implementation Notes

1. The fix requires only a single-line change in `src/api/index.js`
2. The import statement for `API_BASE_URL` must use the `@` alias which is already configured in Vite
3. No changes to environment files are needed - they already contain the correct values
4. The Vite build process will automatically embed the environment variables at build time
