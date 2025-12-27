// 네비게이션 업데이트 (공통)
async function updateNav() {
    const nav = document.getElementById('nav');
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        nav.innerHTML = `
            <a href="/login.html">로그인</a>
            <a href="/signup.html">회원가입</a>
        `;
        return;
    }

    try {
        const response = await fetch('/api/users/me', {
            headers: { 'Authorization': 'Bearer ' + accessToken }
        });

        if (response.ok) {
            nav.innerHTML = `<a href="#" id="logoutBtn">로그아웃</a>`;
            document.getElementById('logoutBtn').addEventListener('click', logout);
        } else {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            nav.innerHTML = `
                <a href="/login.html">로그인</a>
                <a href="/signup.html">회원가입</a>
            `;
        }
    } catch (error) {
        console.error('인증 확인 실패:', error);
    }
}

// 로그아웃 (공통)
async function logout(e) {
    e.preventDefault();
    const accessToken = localStorage.getItem('accessToken');

    try {
        await fetch('/api/auth/logout', {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + accessToken }
        });
    } catch (error) {
        console.error('로그아웃 실패:', error);
    }

    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    alert('로그아웃 되었습니다.');
    location.reload();
}