# Design Document

## Overview

The blank page issue in the packaged Electron application is caused by incorrect path resolution and routing configuration. When Electron loads files using the `file://` protocol, the Vue Router's `createWebHistory()` mode fails because it requires a server to handle routing. Additionally, static assets may not resolve correctly if the base path is not properly configured.

This design addresses these issues by:
1. Switching Vue Router to hash mode (`createWebHashHistory`) which works with the file protocol
2. Ensuring Vite's base path is set to relative (`./`)
3. Verifying Electron's main process correctly loads the built index.html
4. Adding proper error logging for debugging production issues

## Architecture

### Component Overview

```
┌─────────────────────────────────────────┐
│         Electron Main Process           │
│  - Loads index.html from dist/          │
│  - Uses file:// protocol                │
│  - Provides error logging               │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│         Vue Application (Renderer)       │
│  - Hash-based routing (#/)              │
│  - Relative asset paths                 │
│  - Client-side navigation               │
└─────────────────────────────────────────┘
```

### Key Changes

1. **Router Configuration**: Change from `createWebHistory()` to `createWebHashHistory()`
2. **Vite Base Path**: Already correctly set to `'./'` in vite.config.js
3. **Electron Main Process**: Path resolution is correct, but add better error handling
4. **Build Output**: Verify dist directory structure is correct

## Components and Interfaces

### 1. Vue Router Configuration

**File**: `src/router/index.js`

**Current Issue**: Uses `createWebHistory()` which requires a server to handle HTML5 history API

**Solution**: Use `createWebHashHistory()` which works with file:// protocol

```javascript
import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes
})
```

**Why Hash Mode**:
- Hash mode uses URL hash (#) for routing: `file:///path/to/app/index.html#/products`
- Works perfectly with file:// protocol
- No server-side configuration needed
- All navigation happens client-side

### 2. Electron Main Process

**File**: `electron/main.js`

**Current State**: Correctly loads from `../dist/index.html` in production

**Enhancement**: Add more detailed error logging and verification

```javascript
// Production environment
const indexPath = join(__dirname, '../dist/index.html')
console.log('Loading file:', indexPath)
console.log('File exists:', require('fs').existsSync(indexPath))

mainWindow.loadFile(indexPath).catch(err => {
  console.error('Failed to load file:', err)
  console.error('Attempted path:', indexPath)
  console.error('__dirname:', __dirname)
})
```

### 3. Vite Configuration

**File**: `vite.config.js`

**Current State**: Already correctly configured with `base: './'`

**Verification**: Ensure build output uses relative paths

```javascript
export default defineConfig({
  base: './',  // ✓ Correct - uses relative paths
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    emptyOutDir: true
  }
})
```

### 4. Package.json Build Configuration

**File**: `package.json`

**Current State**: Build configuration looks correct

**Verification Points**:
- `files` array includes `dist/**/*` ✓
- `main` points to `electron/main.js` ✓
- Build script runs `vite build` before `electron-builder` ✓

## Data Models

No data model changes required. This is purely a configuration and routing fix.

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property Reflection

After reviewing all acceptance criteria, most of them are specific examples or integration tests rather than universal properties. Since this is a configuration fix rather than algorithmic logic, we don't have traditional "for all" properties. Instead, we have specific verification examples that must pass.

The acceptance criteria are primarily:
- Configuration verification (checking specific settings)
- Integration tests (does the packaged app work?)
- Manual verification steps (inspecting build output)

Therefore, no universal property-based tests are applicable for this feature. All testing will be example-based verification and manual testing of the packaged application.

## Error Handling

### Build-Time Errors

1. **Missing Dependencies**: If Vite or Electron Builder dependencies are missing, the build will fail with clear error messages
2. **Invalid Configuration**: If vite.config.js has syntax errors, the build process will halt
3. **File System Errors**: If dist directory cannot be created, appropriate errors will be logged

### Runtime Errors

1. **File Not Found**: If index.html cannot be loaded, Electron will log the error with the attempted path
2. **Asset Loading Failures**: If CSS/JS files fail to load, browser console will show 404 errors
3. **Router Errors**: If routing fails, Vue Router will log navigation errors

### Error Logging Strategy

```javascript
// Main process error logging
mainWindow.loadFile(indexPath).catch(err => {
  console.error('Failed to load file:', err)
  console.error('Attempted path:', indexPath)
  console.error('__dirname:', __dirname)
  console.error('Process cwd:', process.cwd())
})

// Renderer process error logging (via DevTools)
mainWindow.webContents.on('console-message', (event, level, message) => {
  console.log(`Renderer console [${level}]:`, message)
})
```

## Testing Strategy

### Manual Testing Approach

Since this is a configuration fix for Electron packaging, testing will primarily be manual verification:

1. **Development Mode Test**
   - Run `npm run electron:dev`
   - Verify application loads correctly
   - Test all routes and navigation
   - Verify assets load properly

2. **Build Output Verification**
   - Run `npm run build`
   - Inspect `dist/index.html` for relative paths
   - Verify all assets are in `dist/assets/`
   - Check that no absolute paths exist in built files

3. **Packaged Application Test**
   - Run `npm run electron:build:win`
   - Install the generated .exe file
   - Launch the installed application
   - Verify no blank page appears
   - Test all navigation routes
   - Verify all features work correctly

4. **DevTools Inspection**
   - Open DevTools in packaged app (Ctrl+Shift+D)
   - Check Console for errors
   - Check Network tab for 404s
   - Verify all resources load successfully

### Verification Checklist

- [ ] Router uses `createWebHashHistory()`
- [ ] Vite config has `base: './'`
- [ ] Built index.html uses relative paths
- [ ] Electron main.js loads from correct path
- [ ] DevTools show no console errors
- [ ] DevTools show no network 404s
- [ ] All routes navigate correctly
- [ ] Application displays content (not blank)
- [ ] Packaged app installs successfully
- [ ] Installed app launches without errors

### No Property-Based Testing Required

This feature does not require property-based testing because:
- It's a configuration fix, not algorithmic logic
- There are no universal properties to test across random inputs
- Verification is binary: the app either loads or it doesn't
- Testing is primarily integration and manual verification

### No Unit Testing Required

This feature does not require unit tests because:
- Changes are configuration-only (router mode, build settings)
- No new functions or logic are being added
- Verification requires end-to-end testing of the packaged application
- The fix is validated by the application working correctly after packaging

## Implementation Notes

### Critical Changes

1. **Router Mode Change** (CRITICAL)
   - Change from `createWebHistory()` to `createWebHashHistory()`
   - This is the primary fix for the blank page issue

2. **Verify Vite Config** (VERIFICATION)
   - Confirm `base: './'` is set
   - Already correct in current configuration

3. **Enhanced Error Logging** (HELPFUL)
   - Add detailed logging in main.js
   - Helps diagnose future issues

### Build Process

```bash
# Development testing
npm run electron:dev

# Build for production
npm run build

# Package for Windows
npm run electron:build:win

# Package for all platforms
npm run electron:build:all
```

### Post-Implementation Verification

After implementing the fix:
1. Build the application
2. Package it for Windows
3. Install the .exe on a clean machine
4. Launch and verify no blank page
5. Test all navigation routes
6. Verify all features work

## Dependencies

- Vue Router 4.x (already installed)
- Vite 5.x (already installed)
- Electron 40.x (already installed)
- Electron Builder 24.x (already installed)

No new dependencies required.

## Performance Considerations

- Hash-based routing has no performance impact compared to history mode
- Build output size remains the same
- Application startup time unchanged
- No runtime performance differences

## Security Considerations

- Hash-based routing is as secure as history mode for Electron apps
- File protocol prevents external access to local files
- No new security concerns introduced

## Compatibility

- Works on Windows (primary target)
- Works on macOS (if needed)
- Works on Linux (if needed)
- Compatible with all modern Electron versions
