import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 100, // 100 virtual users
  duration: '1m', // 1 minute duration
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
    http_req_failed: ['rate<0.01'], // error rate should be less than 1%
  },
};

const BASE_URL = __ENV.API_URL || 'http://localhost:8000';

export default function () {
  // 1. Health check or Home
  let res = http.get(`${BASE_URL}/`);
  check(res, { 'status is 200': (r) => r.status === 200 });

  // 2. Simulate Login (Example endpoint)
  // let loginRes = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
  //   email: 'test@example.com',
  //   password: 'password123'
  // }), { headers: { 'Content-Type': 'application/json' } });
  // check(loginRes, { 'login successful': (r) => r.status === 200 });

  sleep(1);
}
