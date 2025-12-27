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
                            onclick="goToSeatSelect(${schedule.id})"
                            ${!isAvailable ? 'disabled' : ''}>
                        ${isAvailable ? '예매하기' : '예매 준비중'}
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

// 좌석 선택 페이지로 이동
function goToSeatSelect(scheduleId) {
    location.href = `/seat-select.html?scheduleId=${scheduleId}`;
}