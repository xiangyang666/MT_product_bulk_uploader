# Design Document

## Overview

This feature implements a permission-based visual overlay system for the application version management section in system settings. The design focuses on providing a transparent yet secure user experience where non-admin users can see that version management functionality exists but cannot interact with it. The implementation uses Vue 3 composition API with reactive permission checking and CSS-based visual effects.

## Architecture

### Component Structure

```
Settings.vue (Modified)
├── System Settings Tab
│   ├── Merchant Config Form
│   ├── Config Info Card
│   └── Version Management Card (Enhanced)
│       ├── Permission Overlay (New)
│       │   ├── Blur Background
│       │   └── Permission Notice
│       ├── Card Header (Modified)
│       │   ├── Title
│       │   └── Upload Button (Conditional)
│       └── Version Tabs
│           ├── Windows Tab
│           │   └── VersionList Component
│           └── macOS Tab
│               └── VersionList Component
└── Profile Tab
```

### Permission Flow

```
User Login
    ↓
User Store Updates (role information)
    ↓
Settings Component Mounted
    ↓
Compute isAdmin (role === 'SUPER_ADMIN' || role === 'ADMIN')
    ↓
Render Version Management Section
    ↓
    ├─→ isAdmin = true  → Normal rendering
    │                     - Show upload button
    │                     - No overlay
    │                     - Full interaction
    │
    └─→ isAdmin = false → Restricted rendering
                          - Hide upload button
                          - Apply blur overlay
                          - Show permission notice
                          - Block interactions
```

## Components and Interfaces

### 1. Enhanced Settings.vue

**New Computed Properties:**
- `isAdmin`: Boolean computed property that checks if user role is SUPER_ADMIN or ADMIN
- Returns: `userStore.userInfo?.role === 'SUPER_ADMIN' || userStore.userInfo?.role === 'ADMIN'`

**New Template Structure:**
```vue
<div class="version-management-card" :class="{ 'restricted': !isAdmin }">
  <!-- Permission Overlay (conditional) -->
  <div v-if="!isAdmin" class="permission-overlay">
    <div class="permission-notice">
      <el-icon class="notice-icon"><Lock /></el-icon>
      <div class="notice-title">需要管理员权限</div>
      <div class="notice-description">应用版本管理功能仅对管理员开放</div>
    </div>
  </div>
  
  <!-- Existing content with conditional blur -->
  <div class="version-content" :class="{ 'blurred': !isAdmin }">
    <!-- Card header with conditional upload button -->
    <!-- Version tabs -->
  </div>
</div>
```

### 2. Permission Overlay Component (Inline)

**Structure:**
- Positioned absolutely over the version management card
- Semi-transparent background (rgba(255, 255, 255, 0.3))
- Centered permission notice with icon, title, and description
- Prevents all pointer events on underlying content

**Visual Properties:**
- Background: Semi-transparent white overlay
- Blur effect: Applied to content layer via CSS backdrop-filter
- Z-index: Higher than content but below modals
- Pointer events: None on overlay, allowing it to be non-interactive

## Data Models

### User Role Enumeration

```typescript
enum UserRole {
  SUPER_ADMIN = 'SUPER_ADMIN',  // Full access
  ADMIN = 'ADMIN',               // Full access
  USER = 'USER'                  // Restricted access
}
```

### Permission State

```typescript
interface PermissionState {
  isAdmin: boolean;              // Computed from user role
  canUploadVersion: boolean;     // Same as isAdmin
  canEditVersion: boolean;       // Same as isAdmin
  canDeleteVersion: boolean;     // Same as isAdmin
  canSetLatest: boolean;         // Same as isAdmin
}
```

### User Info (Existing)

```typescript
interface UserInfo {
  id: number;
  username: string;
  role: UserRole;
  realName?: string;
  email?: string;
  phone?: string;
  status: number;
  createdAt: string;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Admin Access Invariant

*For any* user with role SUPER_ADMIN or ADMIN, the version management section should render without overlay and with full interactive capabilities
**Validates: Requirements 1.4**

### Property 2: Non-Admin Restriction Invariant

*For any* user with role USER, the version management section should render with blur overlay and permission notice, and all interactive elements should be non-functional
**Validates: Requirements 1.1, 1.2, 1.3, 1.5**

### Property 3: Upload Button Visibility

*For any* user role, the upload button visibility should equal the isAdmin computed value (visible for admins, hidden for non-admins)
**Validates: Requirements 2.1, 2.2**

### Property 4: Overlay Coverage Completeness

*For any* viewport size, when the overlay is active, it should completely cover the version management card area without gaps
**Validates: Requirements 3.5, 6.3**

### Property 5: Interaction Prevention

*For any* non-admin user, clicking or keyboard navigation within the blurred content area should not trigger any underlying event handlers
**Validates: Requirements 5.1, 5.2, 5.3, 5.4**

### Property 6: Role Change Reactivity

*For any* user, when the user role changes in the user store, the overlay visibility should update reactively within one render cycle
**Validates: Requirements 4.4**

### Property 7: Default Security Posture

*For any* state where user role information is undefined or null, the system should default to showing the overlay (treating as non-admin)
**Validates: Requirements 4.5**

## Error Handling

### Permission Check Failures

**Scenario:** User store is not initialized or userInfo is null
**Handling:**
- Default to non-admin state (show overlay)
- Log warning to console
- Continue rendering with restricted access

**Scenario:** User role is an unexpected value
**Handling:**
- Treat as non-admin (show overlay)
- Log error with role value
- Display overlay as security measure

### Rendering Errors

**Scenario:** Overlay component fails to render
**Handling:**
- Fallback to hiding entire version management section
- Display error message to user
- Log error details for debugging

**Scenario:** CSS blur effect not supported by browser
**Handling:**
- Graceful degradation to solid overlay without blur
- Maintain permission notice visibility
- Ensure interaction prevention still works

## Testing Strategy

### Unit Tests

**Test Suite: Permission Computed Property**
- Test isAdmin returns true for SUPER_ADMIN role
- Test isAdmin returns true for ADMIN role
- Test isAdmin returns false for USER role
- Test isAdmin returns false for null/undefined role
- Test isAdmin returns false for invalid role string

**Test Suite: Conditional Rendering**
- Test overlay is rendered when isAdmin is false
- Test overlay is not rendered when isAdmin is true
- Test upload button is visible when isAdmin is true
- Test upload button is hidden when isAdmin is false
- Test blur class is applied when isAdmin is false

**Test Suite: Interaction Prevention**
- Test click events are blocked on blurred content
- Test keyboard navigation skips restricted elements
- Test pointer-events CSS is applied correctly

### Property-Based Tests

The testing framework will use **@fast-check/vitest** for Vue 3 applications, configured to run a minimum of 100 iterations per property test.

**Property Test 1: Admin Access Invariant**
```javascript
// Feature: version-management-permission-overlay, Property 1: Admin Access Invariant
// Validates: Requirements 1.4
fc.assert(
  fc.property(
    fc.constantFrom('SUPER_ADMIN', 'ADMIN'),
    (role) => {
      const wrapper = mount(Settings, {
        global: {
          plugins: [createTestingPinia({
            initialState: {
              user: { userInfo: { role } }
            }
          })]
        }
      });
      
      const overlay = wrapper.find('.permission-overlay');
      const uploadButton = wrapper.find('.card-header el-button');
      const blurredContent = wrapper.find('.blurred');
      
      return !overlay.exists() && 
             uploadButton.exists() && 
             !blurredContent.exists();
    }
  ),
  { numRuns: 100 }
);
```

**Property Test 2: Non-Admin Restriction Invariant**
```javascript
// Feature: version-management-permission-overlay, Property 2: Non-Admin Restriction Invariant
// Validates: Requirements 1.1, 1.2, 1.3, 1.5
fc.assert(
  fc.property(
    fc.constantFrom('USER', null, undefined, 'INVALID_ROLE'),
    (role) => {
      const wrapper = mount(Settings, {
        global: {
          plugins: [createTestingPinia({
            initialState: {
              user: { userInfo: { role } }
            }
          })]
        }
      });
      
      const overlay = wrapper.find('.permission-overlay');
      const blurredContent = wrapper.find('.blurred');
      const noticeIcon = wrapper.find('.notice-icon');
      const noticeTitle = wrapper.find('.notice-title');
      
      return overlay.exists() && 
             blurredContent.exists() &&
             noticeIcon.exists() &&
             noticeTitle.exists() &&
             noticeTitle.text().includes('管理员权限');
    }
  ),
  { numRuns: 100 }
);
```

**Property Test 3: Upload Button Visibility**
```javascript
// Feature: version-management-permission-overlay, Property 3: Upload Button Visibility
// Validates: Requirements 2.1, 2.2
fc.assert(
  fc.property(
    fc.constantFrom('SUPER_ADMIN', 'ADMIN', 'USER', null),
    (role) => {
      const wrapper = mount(Settings, {
        global: {
          plugins: [createTestingPinia({
            initialState: {
              user: { userInfo: { role } }
            }
          })]
        }
      });
      
      const isAdmin = role === 'SUPER_ADMIN' || role === 'ADMIN';
      const uploadButton = wrapper.find('[data-testid="upload-version-button"]');
      
      return uploadButton.exists() === isAdmin;
    }
  ),
  { numRuns: 100 }
);
```

**Property Test 4: Interaction Prevention**
```javascript
// Feature: version-management-permission-overlay, Property 5: Interaction Prevention
// Validates: Requirements 5.1, 5.2, 5.3, 5.4
fc.assert(
  fc.property(
    fc.constantFrom('USER', null, undefined),
    async (role) => {
      const clickHandler = vi.fn();
      const wrapper = mount(Settings, {
        global: {
          plugins: [createTestingPinia({
            initialState: {
              user: { userInfo: { role } }
            }
          })]
        }
      });
      
      // Try to click on blurred content
      const blurredContent = wrapper.find('.blurred');
      await blurredContent.trigger('click');
      
      // Click handler should not be called due to pointer-events: none
      const computedStyle = window.getComputedStyle(blurredContent.element);
      return computedStyle.pointerEvents === 'none';
    }
  ),
  { numRuns: 100 }
);
```

### Integration Tests

**Test: Full Permission Flow**
1. Mount Settings component with USER role
2. Verify overlay is visible
3. Verify upload button is hidden
4. Attempt to interact with version list
5. Verify no API calls are made
6. Update user role to ADMIN
7. Verify overlay disappears
8. Verify upload button appears
9. Verify interactions work normally

**Test: Responsive Behavior**
1. Mount Settings component with USER role
2. Test overlay at mobile viewport (375px)
3. Test overlay at tablet viewport (768px)
4. Test overlay at desktop viewport (1920px)
5. Verify overlay covers content at all sizes
6. Verify permission notice remains centered

## Implementation Details

### CSS Classes

```css
/* Version management card with restriction state */
.version-management-card {
  position: relative;
  background-color: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-top: 20px;
}

.version-management-card.restricted {
  overflow: hidden;
}

/* Blurred content layer */
.version-content.blurred {
  filter: blur(4px);
  pointer-events: none;
  user-select: none;
}

/* Permission overlay */
.permission-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border-radius: 12px;
}

/* Permission notice */
.permission-notice {
  text-align: center;
  padding: 32px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  max-width: 400px;
}

.notice-icon {
  font-size: 48px;
  color: #909399;
  margin-bottom: 16px;
}

.notice-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.notice-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .permission-notice {
    padding: 24px;
    max-width: 90%;
  }
  
  .notice-icon {
    font-size: 36px;
  }
  
  .notice-title {
    font-size: 16px;
  }
  
  .notice-description {
    font-size: 13px;
  }
}
```

### Vue Template Structure

```vue
<template>
  <div class="version-management-card" :class="{ 'restricted': !isAdmin }">
    <!-- Permission Overlay -->
    <div v-if="!isAdmin" class="permission-overlay">
      <div class="permission-notice">
        <el-icon class="notice-icon"><Lock /></el-icon>
        <div class="notice-title">需要管理员权限</div>
        <div class="notice-description">应用版本管理功能仅对管理员开放</div>
      </div>
    </div>
    
    <!-- Content with conditional blur -->
    <div class="version-content" :class="{ 'blurred': !isAdmin }">
      <div class="card-header">
        <h3>应用版本管理</h3>
        <el-button 
          v-if="isAdmin"
          type="primary" 
          @click="showUploadDialog"
          data-testid="upload-version-button"
          style="background-color: #FFD100; border-color: #FFD100; color: #333;"
        >
          <el-icon><Upload /></el-icon>
          上传新版本
        </el-button>
      </div>
      
      <el-tabs v-model="versionTab" @tab-change="handleVersionTabChange">
        <el-tab-pane label="Windows" name="Windows">
          <version-list 
            ref="windowsListRef" 
            platform="Windows" 
            @upload="showUploadDialog" 
            @refresh="handleVersionRefresh" 
          />
        </el-tab-pane>
        <el-tab-pane label="macOS" name="macOS">
          <version-list 
            ref="macosListRef" 
            platform="macOS" 
            @upload="showUploadDialog" 
            @refresh="handleVersionRefresh" 
          />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
```

### Script Setup Additions

```javascript
import { Lock } from '@element-plus/icons-vue'

// Computed property for admin check
const isAdmin = computed(() => {
  const role = userStore.userInfo?.role
  return role === 'SUPER_ADMIN' || role === 'ADMIN'
})
```

## Security Considerations

### Client-Side Restrictions

- The overlay is a UX enhancement, not a security measure
- All actual permission enforcement happens on the backend
- API endpoints must validate user roles independently
- Frontend restrictions prevent accidental actions, not malicious ones

### Backend Validation

- Every version management API endpoint must check user role
- Upload, edit, delete, and set-latest operations require ADMIN or SUPER_ADMIN
- Download and view operations may be allowed for all authenticated users
- Role checks should happen before any business logic execution

### Defense in Depth

1. Frontend: Visual overlay prevents UI interaction
2. API Layer: Role-based access control on all endpoints
3. Service Layer: Additional permission checks before operations
4. Database Layer: Audit logging of all version management actions

## Performance Considerations

### Rendering Performance

- Computed property `isAdmin` is cached and only recalculates when user store changes
- Conditional rendering (`v-if`) completely removes overlay from DOM when not needed
- CSS blur effect is GPU-accelerated on modern browsers
- No additional API calls required for permission checking

### Memory Impact

- Overlay component is lightweight (< 1KB additional HTML)
- No additional state management required
- CSS classes are reused across instances
- No event listeners on overlay (relies on pointer-events: none)

## Accessibility

### Screen Reader Support

- Permission notice includes semantic HTML structure
- Icon has appropriate aria-label
- Notice title and description are properly nested
- Blurred content is marked with aria-hidden="true" when restricted

### Keyboard Navigation

- Tab navigation skips over blurred content when restricted
- Focus management ensures users don't get trapped
- Upload button receives focus only when visible to admins

### Visual Accessibility

- High contrast between overlay and content
- Permission notice has sufficient color contrast (WCAG AA compliant)
- Text sizes are readable at all viewport sizes
- Blur effect provides clear visual distinction without relying solely on color

## Browser Compatibility

### Modern Browsers (Full Support)

- Chrome 76+: Full backdrop-filter support
- Firefox 103+: Full backdrop-filter support
- Safari 9+: Full backdrop-filter support with -webkit prefix
- Edge 79+: Full backdrop-filter support

### Fallback Strategy

For browsers without backdrop-filter support:
- Use solid semi-transparent background
- Increase opacity of permission notice background
- Maintain blur on content using filter property
- Ensure permission notice remains readable

### CSS Feature Detection

```css
@supports not (backdrop-filter: blur(2px)) {
  .permission-overlay {
    background-color: rgba(255, 255, 255, 0.85);
  }
}
```
