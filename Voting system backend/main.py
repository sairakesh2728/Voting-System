import os
import random
import string
import bcrypt 
import pymongo
from contextlib import asynccontextmanager
from datetime import datetime, timedelta
from typing import List, Optional

import jwt
from dotenv import load_dotenv
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from passlib.context import CryptContext
from beanie import PydanticObjectId
from pydantic import BaseModel, EmailStr
from fastapi_mail import FastMail, MessageSchema, ConnectionConfig, MessageType

# Database configuration and schemas
from database import init_db
from models import (
    User, Election, Participant, UserSignUp, UserResponse, Token,
    ElectionCreate, ElectionResponse, JoinElectionRequest, ParticipantResponse,
    OTPRecord, OtpVerifyRequest, ElectionTimeUpdate
)

# Compatibility fix
# bcrypt.__about__ = type('about', (object,), {'__version__': bcrypt.__version__})
load_dotenv()

# Config
JWT_SECRET = os.getenv("JWT_SECRET")
if not JWT_SECRET:
    # In production, this should raise an error. For now, we'll log a warning if it's missing.
    print("[WARNING] JWT_SECRET is not set in environment variables!")
    JWT_SECRET = "temp_development_key_change_me_immediately"

JWT_ALGORITHM = "HS256"
JWT_EXPIRATION_HOURS = 24

mail_conf = ConnectionConfig(
    MAIL_USERNAME = os.getenv("MAIL_USERNAME") or "",
    MAIL_PASSWORD = os.getenv("MAIL_PASSWORD") or "",
    MAIL_FROM = os.getenv("MAIL_FROM") or os.getenv("MAIL_USERNAME") or "no-reply@example.com",
    MAIL_PORT = int(os.getenv("MAIL_PORT", 2525)),
    MAIL_SERVER = os.getenv("MAIL_SERVER", "smtp-relay.brevo.com"),
    MAIL_STARTTLS = True,
    MAIL_SSL_TLS = False,
    USE_CREDENTIALS = True if os.getenv("MAIL_USERNAME") else False,
    VALIDATE_CERTS = True
)

# Models
class OtpRequest(BaseModel):
    email: str

class OtpResponse(BaseModel):
    success: bool = True
    message: str

class VoteCreate(BaseModel):
    election_id: str
    candidate_name: str
    voter_email: str
    timestamp: int
    signature: str

class ResultResponse(BaseModel):
    candidate: str
    votes: int
    symbol_url: Optional[str] = None

class ParticipantStatusUpdate(BaseModel):
    status: str # approved or rejected

class ChangePasswordRequest(BaseModel):
    old_password: str
    new_password: str

class FinalizePasswordChangeRequest(BaseModel):
    email: str
    otp: str
    new_password: str

# Security
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
def hash_password(password: str): return pwd_context.hash(password)
def verify_password(p, h): return pwd_context.verify(p, h)

def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(hours=JWT_EXPIRATION_HOURS)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, JWT_SECRET, algorithm=JWT_ALGORITHM)

@asynccontextmanager
async def lifespan(app: FastAPI):
    await init_db()
    yield

app = FastAPI(title="Voting System", lifespan=lifespan)

# Restrict CORS to specific origins from environment
allowed_origins = os.getenv("CORS_ALLOWED_ORIGINS", "*").split(",")
app.add_middleware(
    CORSMiddleware,
    allow_origins=allowed_origins,
    allow_methods=["*"],
    allow_headers=["*"]
)

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/login")

async def get_current_user(token: str = Depends(oauth2_scheme)) -> User:
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        email = payload.get("email")
        user = await User.find_one(User.email == email)
        if not user: raise Exception()
        return user
    except:
        raise HTTPException(status_code=401, detail="Invalid token")

@app.get("/health/mail")
async def health_mail():
    return {"status": "ok", "server": mail_conf.MAIL_SERVER, "from": mail_conf.MAIL_FROM}

# Endpoints
@app.post("/auth/send-otp")
async def send_otp(request: OtpRequest):
    otp = "".join(random.choices(string.digits, k=6))

    # Store OTP in database
    await OTPRecord(email=request.email, otp=otp).insert()

    mail_username = os.getenv("MAIL_USERNAME")
    mail_password = os.getenv("MAIL_PASSWORD")

    if not mail_username or not mail_password:
        print(f"[BYPASS] No email credentials. OTP for {request.email} is: {otp}")
        return {"success": True, "message": "OTP generated (Check server logs for bypass code)"}
        
    fm = FastMail(mail_conf)
    try:
        message = MessageSchema(
            subject="Voting System OTP",
            recipients=[request.email],
            body=f"Your verification code is: {otp}. It will expire in 10 minutes.",
            subtype="plain"
        )
        await fm.send_message(message)
        print(f"[SUCCESS] OTP sent to {request.email}")
        return {"success": True, "message": "Sent"}
    except Exception as e:
        print(f"[ERROR] SMTP Failed for {request.email}: {str(e)}. OTP was: {otp}")
        # In case of SMTP failure, we only return the error message, NOT the OTP
        return {"success": False, "message": f"SMTP Error: Check server logs for manual verification"}

@app.post("/auth/verify-otp")
async def verify_otp(request: OtpVerifyRequest):
    # Find the latest OTP for this email
    record = await OTPRecord.find(OTPRecord.email == request.email).sort("-created_at").first_or_none()

    if not record:
        raise HTTPException(status_code=400, detail="No OTP found or expired")

    if record.otp == request.otp:
        await record.delete()
        return {"success": True, "message": "Verified"}
    else:
        raise HTTPException(status_code=400, detail="Invalid OTP")

@app.post("/auth/initiate-password-change")
async def initiate_password_change(request: ChangePasswordRequest, user: User = Depends(get_current_user)):
    # 1. Verify old password
    if not verify_password(request.old_password, user.password_hash):
        raise HTTPException(status_code=401, detail="Incorrect old password")

    # 2. Send OTP
    otp = "".join(random.choices(string.digits, k=6))
    await OTPRecord(email=user.email, otp=otp).insert()

    mail_username = os.getenv("MAIL_USERNAME")
    mail_password = os.getenv("MAIL_PASSWORD")

    if not mail_username or not mail_password:
        print(f"[BYPASS] Password change OTP for {user.email}: {otp}")
        return {"success": True, "message": "OTP generated (Check logs)"}

    fm = FastMail(mail_conf)
    try:
        await fm.send_message(MessageSchema(
            subject="Security: Password Change OTP",
            recipients=[user.email],
            body=f"Your password change verification code is: {otp}",
            subtype="plain"
        ))
        return {"success": True, "message": "Sent"}
    except Exception as e:
        print(f"[ERROR] SMTP Failed: {e}")
        return {"success": False, "message": "Failed to send OTP"}

@app.post("/auth/finalize-password-change")
async def finalize_password_change(request: FinalizePasswordChangeRequest):
    # 1. Verify OTP
    record = await OTPRecord.find(OTPRecord.email == request.email).sort("-created_at").first_or_none()
    if not record or record.otp != request.otp:
        raise HTTPException(status_code=400, detail="Invalid or expired OTP")

    # 2. Update Password
    user = await User.find_one(User.email == request.email)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    user.password_hash = hash_password(request.new_password)
    await user.save()
    await record.delete()

    return {"success": True, "message": "Password updated successfully"}

@app.post("/auth/signup", response_model=UserResponse, status_code=201)
async def signup(user_data: UserSignUp):
    try:
        user = User(name=user_data.name, email=user_data.email, password_hash=hash_password(user_data.password))
        await user.insert()
        return user
    except pymongo.errors.DuplicateKeyError:
        raise HTTPException(status_code=400, detail="Email already registered")

@app.post("/auth/login", response_model=Token)
async def login(form_data: OAuth2PasswordRequestForm = Depends()):
    user = await User.find_one(User.email == form_data.username)
    if not user or not verify_password(form_data.password, user.password_hash):
        raise HTTPException(status_code=401, detail="Invalid credentials")
    token = create_access_token({"email": user.email})
    return Token(access_token=token, user=UserResponse(id=str(user.id), name=user.name, email=user.email, created_at=user.created_at))

@app.post("/elections/create", response_model=ElectionResponse, status_code=201)
async def create_election(data: ElectionCreate, user: User = Depends(get_current_user)):
    code = "".join(random.choices(string.ascii_uppercase + string.digits, k=6))
    election = Election(
        name=data.name,
        creator_email=user.email,
        date=data.date,
        election_code=code,
        candidates=data.candidates
    )
    await election.insert()
    return election

@app.patch("/elections/{election_id}/schedule", response_model=ElectionResponse)
async def update_election_schedule(election_id: str, data: ElectionTimeUpdate, user: User = Depends(get_current_user)):
    election = await Election.find_one(Election.id == PydanticObjectId(election_id))
    if not election:
        raise HTTPException(status_code=404, detail="Election not found")
    if election.creator_email != user.email:
        raise HTTPException(status_code=401, detail="Unauthorized")

    election.start_time = data.start_time
    election.end_time = data.end_time
    await election.save()
    return election

@app.get("/elections/my-elections", response_model=List[ElectionResponse])
async def my_elections(user: User = Depends(get_current_user)):
    return await Election.find(Election.creator_email == user.email).to_list()

@app.post("/elections/join", response_model=ParticipantResponse, status_code=201)
async def join_election(data: JoinElectionRequest, user: User = Depends(get_current_user)):
    election = await Election.find_one(Election.election_code == data.election_code)
    if not election:
        raise HTTPException(status_code=400, detail="Election not found")
    
    # Check if user already applied
    existing = await Participant.find_one(
        Participant.user_id == user.id,
        Participant.election_id == election.id
    )
    if existing:
        raise HTTPException(status_code=400, detail=f"Application already {existing.status}")
        
    participant = Participant(
        user_id=user.id,
        election_id=election.id,
        full_name=data.full_name,
        id_number=data.id_number,
        election_code=election.election_code,
        status="pending"
    )
    await participant.insert()
    return participant

@app.patch("/elections/participants/{participant_id}/status", response_model=ParticipantResponse)
async def update_participant_status(participant_id: str, data: ParticipantStatusUpdate, user: User = Depends(get_current_user)):
    participant = await Participant.find_one(Participant.id == PydanticObjectId(participant_id))
    if not participant:
        raise HTTPException(status_code=404, detail="Application not found")

    election = await Election.find_one(Election.id == participant.election_id)
    if election.creator_email != user.email:
        raise HTTPException(status_code=401, detail="Only election creator can approve/reject participants")

    participant.status = data.status
    await participant.save()
    return participant

@app.get("/elections/{election_id}/participants", response_model=List[ParticipantResponse])
async def get_election_participants(election_id: str, user: User = Depends(get_current_user)):
    election = await Election.find_one(Election.id == PydanticObjectId(election_id))
    if not election:
        raise HTTPException(status_code=404, detail="Election not found")
    if election.creator_email != user.email:
        raise HTTPException(status_code=401, detail="Unauthorized: Only the creator can view participants")
    return await Participant.find(Participant.election_id == PydanticObjectId(election_id)).to_list()

@app.get("/elections/joined", response_model=List[ElectionResponse])
async def joined_elections(user: User = Depends(get_current_user)):
    # Only return elections where status is 'approved'
    join_records = await Participant.find(
        Participant.user_id == user.id,
        Participant.status == "approved"
    ).to_list()
    return await Election.find({"_id": {"$in": [r.election_id for r in join_records]}}).to_list()

# --- NEW: Cloud Voting & Results ---
@app.post("/votes/cast")
async def cast_vote(vote: VoteCreate, user: User = Depends(get_current_user)):
    election = await Election.find_one(Election.id == PydanticObjectId(vote.election_id))
    if not election:
        raise HTTPException(status_code=404, detail="Election not found")

    if not election.start_time or not election.end_time:
        raise HTTPException(status_code=400, detail="Election timing not set by admin")

    # Time Validation (IST)
    now = datetime.utcnow() + timedelta(hours=5, minutes=30)
    current_date = now.strftime("%Y-%m-%d")
    current_time = now.strftime("%H:%M")

    if current_date != election.date:
        raise HTTPException(status_code=400, detail=f"Voting is scheduled for {election.date}")

    if current_time < election.start_time:
        raise HTTPException(status_code=400, detail=f"Voting starts at {election.start_time}")

    if current_time > election.end_time:
        raise HTTPException(status_code=400, detail=f"Voting ended at {election.end_time}")

    # Check if approved
    participant = await Participant.find_one(
        Participant.user_id == user.id,
        Participant.election_id == election.id
    )
    if not participant or participant.status != "approved":
        raise HTTPException(status_code=403, detail="Your application is not approved yet")

    # Simple check: one vote per email per election
    from database import db
    existing = await db.votes.find_one({"election_id": vote.election_id, "voter_email": vote.voter_email})
    if existing: return {"message": "Already voted"}
    await db.votes.insert_one(vote.dict())
    return {"message": "Successfully "}

@app.get("/votes/results/{election_id}", response_model=List[ResultResponse])
async def get_results(election_id: str):
    election = await Election.find_one(Election.id == PydanticObjectId(election_id))
    if not election:
        raise HTTPException(status_code=404, detail="Election not found")

    from database import db
    pipeline = [{"$match": {"election_id": election_id}}, {"$group": {"_id": "$candidate_name", "count": {"$sum": 1}}}]
    cursor = db.votes.aggregate(pipeline)

    results = {r["_id"]: r["count"] async for r in cursor}

    # Map symbols from election object
    response = []
    for c in election.candidates:
        response.append({
            "candidate": c.name,
            "votes": results.get(c.name, 0),
            "symbol_url": c.symbol_url
        })
    return response

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=int(os.environ.get("PORT", 5000)))
