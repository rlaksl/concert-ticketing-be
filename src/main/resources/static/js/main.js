// 페이지 로드 시 인증 확인
window.onload = async function() {
    const accessToken = localStorage.getItem('accessToken');

    // 토큰 없으면 로그인 페이지로
    if (!accessToken) {
        alert('로그인이 필요합니다.');
        window.location.href = '/login.html';
        return;
    }

    // 토큰으로 인증 확인
    try {
        const response = await fetch('/api/users/me', {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + accessToken
            }
        });

        if (response.status === 401) {
            // 다른 기기에서 로그인됨
            alert('다른 기기에서 로그인되어 로그아웃됩니다.');
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            window.location.href = '/login.html';
            return;
        }

        if (response.ok) {
            document.getElementById('userInfo').textContent = '인증된 사용자입니다!';
        }

    } catch (error) {
        console.error('인증 확인 실패:', error);
    }
};

// 로그아웃 버튼
document.getElementById('logoutBtn').addEventListener('click', async function() {
    const accessToken = localStorage.getItem('accessToken');

    try {
        // 서버에 로그아웃 요청 (레디스 토큰 삭제)
        await fetch('/api/auth/logout', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + accessToken
            }
        });
    } catch (error) {
        console.error('로그아웃 API 호출 실패: ', error);
    }
    // 로컬 토큰 삭제 후 이동
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    alert('로그아웃 되었습니다.');
    window.location.href = '/login.html';
});