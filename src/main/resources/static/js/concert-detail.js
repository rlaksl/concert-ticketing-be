window.onload = function() {
    updateNav();
    loadConcertDetail();
};

// URL에서 콘서트 ID 추출
function getConcertId() {
    const params = new URLSearchParams(window.location.search);
    return params.get('id');
}

// 공연 상세 + 일정 로드
async function loadConcertDetail() {
    const concertId = getConcertId();

    if (!concertId) {
        alert('잘못된 접근입니다.');
        location.href = '/main.html';
        return;
    }

    try {
        // 공연 정보 로드
        const concertRes = await fetch(`/api/concerts/${concertId}`);
        if (!concertRes.ok) throw new Error('공연 정보를 불러오지 못했습니다.');
        const concert = await concertRes.json();

        document.getElementById('concertInfo').innerHTML = `
            <h1>${concert.title}</h1>
            <p class="artist">${concert.artist}</p>
        `;

        // 일정 목록 로드
        const scheduleRes = await fetch(`/api/concerts/${concertId}/schedules`);
        if (!scheduleRes.ok) throw new Error('일정을 불러오지 못했습니다.');
        const schedules = await scheduleRes.json();

        const listContainer = document.getElementById('scheduleList');

        if (schedules.length === 0) {
            listContainer.innerHTML = '<p class="message">등록된 일정이 없습니다.</p>';
            return;
        }

        listContainer.innerHTML = schedules.map(schedule => {
            const concertDate = new Date(schedule.concertAt);
            const bookingDate = new Date(schedule.bookingAvailableAt);
            const now = new Date();
            const isAvailable = now >= bookingDate;

            return `
                <div class="schedule-card">
                    <div>
                        <p class="date-info">${formatDate(concertDate)}</p>
                        <p class="seat-info">총 ${schedule.totalSeats.toLocaleString()}석</p>
                    </div>
                    <button class="btn-reserve" 
                            onclick="handleReserveClick(${schedule.id})"
                            ${!isAvailable ? 'disabled' : ''}>
                        ${isAvailable ? '예매하기' : formatOpenDate(bookingDate) + ' 오픈 예정'}
                    </button>
                </div>
            `;
        }).join('');

    } catch (error) {
        console.error(error);
        document.getElementById('concertInfo').innerHTML =
            '<p class="message">공연 정보를 불러오는 중 오류가 발생했습니다.</p>';
    }
}

// 날짜 포맷
function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}.${month}.${day} ${hours}:${minutes}`;
}

// 오픈 날짜 포맷
function formatOpenDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    let hours = date.getHours();
    const period = hours < 12 ? '오전' : '오후';
    if (hours > 12) hours -= 12;
    if (hours === 0) hours = 12;

    return `${year}.${month}.${day} ${period} ${hours}시`;
}

async function handleReserveClick(scheduleId) {
    // 로그인 확인
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) {
        alert('로그인이 필요합니다.');
        location.href = '/login.html';
        return;
    }

    try {
        // 대기열 등록 API 호출
        const response = await fetch('/api/queue/enter', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + accessToken
            },
            body: JSON.stringify({ scheduleId: scheduleId })
        });

        if (response.status === 401) {
            alert('로그인이 만료되었습니다.');
            location.href = '/login.html';
            return;
        }

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || '대기열 등록 실패');
        }

        const data = await response.json();

        // READY 상태 → 바로 좌석 선택 페이지로
        if (data.status === 'READY') {
            localStorage.setItem('entryToken_' + scheduleId, data.entryToken);
            location.href = `/seat-select.html?scheduleId=${scheduleId}`;
            return;
        }

        // WAITING 상태 → 대기열 페이지로
        location.href = `/queue.html?scheduleId=${scheduleId}`;

    } catch (error) {
        console.error(error);
        alert(error.message || '예매 처리 중 오류가 발생했습니다.');
    }
}