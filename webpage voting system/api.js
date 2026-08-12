const API_BASE_URL = "https://voting-system-backend-qsld.onrender.com";

const api = {
    async login(email, password) {
        const formData = new URLSearchParams();
        formData.append('username', email);
        formData.append('password', password);

        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData
        });
        return await response.json();
    },

    async signup(name, email, password) {
        const response = await fetch(`${API_BASE_URL}/auth/signup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password })
        });
        return await response.json();
    },

    async sendOtp(email) {
        const response = await fetch(`${API_BASE_URL}/auth/send-otp`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });
        return await response.json();
    },

    async verifyOtp(email, otp) {
        const response = await fetch(`${API_BASE_URL}/auth/verify-otp`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, otp })
        });
        return await response.json();
    },

    async createElection(electionData, token) {
        const response = await fetch(`${API_BASE_URL}/elections/create`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token
            },
            body: JSON.stringify(electionData)
        });
        return await response.json();
    },

    async getMyElections(token) {
        const response = await fetch(`${API_BASE_URL}/elections/my-elections`, {
            method: 'GET',
            headers: { 'Authorization': token }
        });
        return await response.json();
    },

    async joinElection(joinData, token) {
        const response = await fetch(`${API_BASE_URL}/elections/join`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token
            },
            body: JSON.stringify(joinData)
        });
        return await response.json();
    },

    async getJoinedElections(token) {
        const response = await fetch(`${API_BASE_URL}/elections/joined`, {
            method: 'GET',
            headers: { 'Authorization': token }
        });
        return await response.json();
    },

    async updateElectionSchedule(electionId, startTime, endTime, token) {
        const response = await fetch(`${API_BASE_URL}/elections/${electionId}/schedule`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token
            },
            body: JSON.stringify({ start_time: startTime, end_time: endTime })
        });
        return await response.json();
    },

    async getResults(electionId) {
        const response = await fetch(`${API_BASE_URL}/votes/results/${electionId}`, {
            method: 'GET'
        });
        return await response.json();
    },

    async castVote(voteData, token) {
        const response = await fetch(`${API_BASE_URL}/votes/cast`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token
            },
            body: JSON.stringify(voteData)
        });
        return await response.json();
    },

    async initiatePasswordChange(old_password, new_password, token) {
        const response = await fetch(`${API_BASE_URL}/auth/initiate-password-change`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token
            },
            body: JSON.stringify({ old_password, new_password })
        });
        return await response.json();
    },

    async finalizePasswordChange(email, otp, new_password) {
        const response = await fetch(`${API_BASE_URL}/auth/finalize-password-change`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, otp, new_password })
        });
        return await response.json();
    }
};

function checkAuth() {
    const token = localStorage.getItem('authToken');
    if (!token && !window.location.pathname.includes('login.html') &&
        !window.location.pathname.includes('signup.html') &&
        !window.location.pathname.includes('index.html') &&
        !window.location.pathname.includes('otp-verify.html')) {
        window.location.href = 'index.html';
    }
    return token;
}

function logout() {
    localStorage.clear();
    window.location.href = 'index.html';
}
