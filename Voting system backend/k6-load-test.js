import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 100, // 100 virtual users
    duration: '1m', // 1 minute
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
        http_req_failed: ['rate<0.01'],   // Error rate should be less than 1%
    },
};

const BASE_URL = 'https://voting-system-backend-qsld.onrender.com';

export default function () {
    // 1. Health Check
    let res = http.get(`${BASE_URL}/health/mail`);
    check(res, { 'status is 200': (r) => r.status === 200 });

    // 2. Login Attempt (Simulated)
    const payload = JSON.stringify({
        username: 'testuser@example.com',
        password: 'password123',
    });

    const params = {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
    };

    // Using x-www-form-urlencoded as per FastAPI OAuth2 requirement
    let loginRes = http.post(`${BASE_URL}/auth/login`, {
        username: 'testuser@example.com',
        password: 'password123',
    }, params);

    check(loginRes, {
        'login status is 200 or 401': (r) => r.status === 200 || r.status === 401,
    });

    sleep(1);
}
