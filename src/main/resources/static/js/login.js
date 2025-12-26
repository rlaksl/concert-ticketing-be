document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const data = {
        email: document.getElementById('email').value,
        password: document.getElementById('password').value
    };

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || '로그인 실패');
        }

        // 토큰 받기
        const tokens = await response.json();

        // localStorage에 토큰 저장
        localStorage.setItem('accessToken', tokens.accessToken);
        localStorage.setItem('refreshToken', tokens.refreshToken);

        alert('로그인 성공!');
        window.location.href = '/main.html';

    } catch (error) {
        alert(error.message || '로그인 중 오류가 발생했습니다.');
    }
});