# Voting System - E2E Testing & Security Audit Guide

## 1. Web Automation (Selenium + Node.js)
**Path**: `C:\Users\VRV Reddy\Desktop\webpage voting system\automation`

### Setup
1. Install Node.js
2. `cd automation`
3. `npm install`

### Execution
- Run all tests: `npm test`
- Generate Excel Report: `npm run report`

## 2. Android Automation (Appium + Java)
**Path**: `C:\Users\VRV Reddy\AndroidStudioProjects\VotingSystem\automation`

### Setup
1. Install Appium Server (`npm install -g appium`)
2. Install UiAutomator2 driver (`appium driver install uiautomator2`)
3. Ensure Android Emulator is running.

### Execution
- `mvn test` from the automation directory.

## 3. Backend Audit (SAST/Performance)
**Path**: `C:\Users\VRV Reddy\Desktop\Voting system backend`

### Load Testing
1. Install k6 (`brew install k6` or `choco install k6`)
2. `k6 run k6-load-test.js`

### Security Scan
- View `security-review.md` for the latest manual audit findings.
- Run `semgrep` for automated SAST.

## 4. CI/CD Pipeline
The `.github/workflows` folder in the backend directory contains a production-ready audit pipeline.

---
**Status Summary**:
- **Android App**: Framework Ready, Base Tests Passed.
- **Webpage**: Framework Ready, Page Objects Implemented.
- **Backend**: Audit Completed, Performance Bottlenecks Identified.
