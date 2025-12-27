// 페이지 로드 시 실행
window.onload = async function() {
    await checkAuthAndUpdateNav();  // 인증 확인 + 네비게이션 업데이트
    loadConcerts();                  // 콘서트 목록 로드
};

// 인증 확인 + 네비게이션 업데이트
async function checkAuthAndUpdateNav() {
    const nav = document.getElementById('nav');
    const accessToken = localStorage.getItem('accessToken');

    // 비로그인 상태
    if (!accessToken) {
        nav.innerHTML = `
            <a href="/login.html">로그인</a>
            <a href="/signup.html">회원가입</a>
        `;
        return;
    }

    // 로그인 상태 - 토큰 유효성 확인
    try {
        const response = await fetch('/api/users/me', {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + accessToken
            }
        });

        if (response.status === 401) {
            // 다른 기기에서 로그인됨 또는 토큰 만료
            alert('다른 기기에서 로그인되어 로그아웃됩니다.');
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            nav.innerHTML = `
                <a href="/login.html">로그인</a>
                <a href="/signup.html">회원가입</a>
            `;
            return;
        }

        if (response.ok) {
            // 인증 성공 - 로그아웃 버튼 표시
            nav.innerHTML = `<a href="#" id="logoutBtn">로그아웃</a>`;
            document.getElementById('logoutBtn').addEventListener('click', logout);
        }

    } catch (error) {
        console.error('인증 확인 실패:', error);
        // 에러 시에도 비로그인 상태로 처리
        nav.innerHTML = `
            <a href="/login.html">로그인</a>
            <a href="/signup.html">회원가입</a>
        `;
    }
}

// 로그아웃
async function logout(e) {
    e.preventDefault();
    const accessToken = localStorage.getItem('accessToken');

    try {
        await fetch('/api/auth/logout', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + accessToken
            }
        });
    } catch (error) {
        console.error('로그아웃 API 호출 실패:', error);
    }

    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    alert('로그아웃 되었습니다.');

    // 네비게이션 업데이트 (페이지 이동 없이)
    document.getElementById('nav').innerHTML = `
        <a href="/login.html">로그인</a>
        <a href="/signup.html">회원가입</a>
    `;
}

// 콘서트 목록 로드
async function loadConcerts() {
    const listContainer = document.getElementById('concertList');

    try {
        const response = await fetch('/api/concerts');

        if (!response.ok) {
            throw new Error('콘서트 목록을 불러오지 못했습니다.');
        }

        const concerts = await response.json();

        if (concerts.length === 0) {
            listContainer.innerHTML = '<p class="message">등록된 공연이 없습니다.</p>';
            return;
        }

        // 콘서트 카드 생성
        listContainer.innerHTML = concerts.map(concert => `
            <div class="concert-card" onclick="location.href='/concert-detail.html?id=${concert.id}'">
                <h3>${concert.title}</h3>
                <p class="artist">${concert.artist}</p>
            </div>
        `).join('');

    } catch (error) {
        console.error(error);
        listContainer.innerHTML = '<p class="message">공연 목록을 불러오는 중 오류가 발생했습니다.</p>';
    }
}