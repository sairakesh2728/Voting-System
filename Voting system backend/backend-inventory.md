# Backend Inventory Report

## Technology Stack
- **Language**: Python 3.14 (Detected from Render logs)
- **Framework**: FastAPI
- **Database**: MongoDB (Atlas)
- **ODM**: Beanie
- **Auth**: JWT (PyJWT) + OAuth2 Password Bearer
- **Mail**: Brevo (SMTP) via `fastapi-mail`

## Architecture
- **Pattern**: Layered/Controller-Service pattern (Beanie Documents as Models)
- **Deployment**: Render (Web Service)

## API Inventory
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/auth/signup` | POST | No | User registration |
| `/auth/login` | POST | No | Login and Token issuance |
| `/auth/send-otp` | POST | No | Trigger email OTP |
| `/auth/verify-otp` | POST | No | Validate email OTP |
| `/elections/create` | POST | Yes | Create new election |
| `/elections/my-elections` | GET | Yes | List creator's elections |
| `/elections/join` | POST | Yes | Join an election as participant |
| `/elections/joined` | GET | Yes | List elections user has joined |
| `/votes/cast` | POST | Yes | Submit a vote |
| `/votes/results/{id}` | GET | No | Public results view |
