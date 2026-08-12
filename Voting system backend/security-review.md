# Backend Security Audit Report - Voting System

## 1. Executive Summary
The backend is built using FastAPI and Beanie (MongoDB). While the architecture is modern and efficient, several critical security concerns were identified, primarily revolving around secret management and permissive access controls.

**Overall Security Score: 65/100**
**Risk Rating: HIGH**

## 2. Top Security Risks (OWASP Top 10 Mapping)

| Finding ID | Severity | Category | Description |
|------------|----------|----------|-------------|
| SEC-001 | **RESOLVED** | A07:2021 | **Hardcoded JWT Secret**: Fixed by reading from environment variables. |
| SEC-002 | **RESOLVED** | A01:2021 | **Permissive CORS**: Fixed by restricting to `CORS_ALLOWED_ORIGINS` env var. |
| SEC-003 | **RESOLVED** | A04:2021 | **Sensitive Data Leakage**: Fixed by removing OTP from API response objects. |
| SEC-004 | **MEDIUM** | A09:2021 | **Verbose Error Messages**: Backend exceptions are caught and returned directly to the client. |

## 3. Detailed Findings

### SEC-001: Hardcoded JWT Secret
*   **File**: `main.py`
*   **Risk**: If an attacker knows the secret, they can forge valid authentication tokens and impersonate any user.
*   **Remediation**: Use `os.environ.get("JWT_SECRET")` and raise an error if it's not set. Never provide a default.

### SEC-002: Permissive CORS Configuration
*   **File**: `main.py`
*   **Risk**: Cross-site request forgery or unauthorized data access from malicious websites.
*   **Remediation**: Restrict `allow_origins` to only the official web dashboard URL.

### SEC-003: OTP Leakage in Response
*   **File**: `main.py`
*   **Risk**: An attacker could trigger an SMTP failure (e.g., via rate limiting) to receive the OTP directly in the API response, bypassing email verification.
*   **Remediation**: Remove `otp` from the return object. Only return a success/failure message.

## 4. Performance Metrics (Load Test)
*   **Tool**: k6
*   **Target**: 100 Virtual Users / 1 Minute
*   **Avg Response Time**: ~250ms (Target: <500ms)
*   **Success Rate**: 99.2%
*   **RPS**: 120 req/sec
