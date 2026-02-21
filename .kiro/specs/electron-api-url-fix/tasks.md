# Implementation Plan

- [x] 1. Update API client to use full URL from environment variables



  - Modify `src/api/index.js` to import `API_BASE_URL` from config module
  - Change axios `baseURL` from `/api` to `${API_BASE_URL}/api`
  - Verify the import uses the correct path alias `@/config`
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [ ] 2. Test in development environment
  - Start Vite dev server with `npm run dev`
  - Test login functionality
  - Verify API requests reach the backend successfully
  - Check browser network tab shows correct URL format


  - _Requirements: 1.5_

- [ ] 3. Test production build
  - Build the application with production environment variables
  - Package the Electron application
  - Run the packaged app and test login
  - Verify API requests use the full production URL
  - _Requirements: 1.1, 1.2, 2.2_

- [ ] 4. Verify environment variable handling
  - Test with VITE_API_BASE_URL set to production server
  - Test with VITE_API_BASE_URL set to localhost
  - Verify fallback behavior when variable is not set
  - _Requirements: 1.3, 1.4, 2.1_
