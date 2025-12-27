let selectedSeat = null;  // 현재 선택된 좌석
let otherSelectedSeats = new Set();  // 다른 사용자가 선택 중인 좌석 ID

// 웹소켓 연결
let stompClient = null;

function connectWebSocket(scheduleId) {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    // 디버그 로그 끄기
    stompClient.debug = null;

    stompClient.connect({}, function(frame) {
        console.log('WebSocket 연결됨');

        // 해당 일정의 좌석 변경 구독
        stompClient.subscribe('/topic/seats/' + scheduleId, function(message) {
            const seatMessage = JSON.parse(message.body);
            handleSeatUpdate(seatMessage);
        });
    });
}

// 다른 사용자의 좌석 변경 처리
function handleSeatUpdate(seatMessage) {
    const seatElement = document.querySelector(`[data-id="${seatMessage.seatId}"]`);
    if (!seatElement) return;

    // 내가 선택한 좌석이면 무시
    if (seatElement.classList.contains('selected')) return;

    switch (seatMessage.action) {
        case 'SELECT':
            // 다른 사용자가 선택 중 - 외관은 그대로, 추적만
            otherSelectedSeats.add(seatMessage.seatId);
            break;
        case 'DESELECT':
            // 다른 사용자가 선택 해제
            otherSelectedSeats.delete(seatMessage.seatId);
            break;
        case 'RESERVE':
        case 'CONFIRM':
            // 예약/결제 완료 - sold 처리
            otherSelectedSeats.delete(seatMessage.seatId);
            seatElement.classList.remove('available', 'temporary', 'sold');
            seatElement.classList.add('sold');
            seatElement.textContent = '';
            break;
        case 'CANCEL':
            // 취소 - 다시 available
            otherSelectedSeats.delete(seatMessage.seatId);
            seatElement.classList.remove('available', 'temporary', 'sold');
            seatElement.classList.add('available');
            seatElement.textContent = seatElement.dataset.seatNo;
            break;
    }
}

// WebSocket으로 선택 상태 전송
function sendSeatSelection(seatId, action) {
    if (stompClient && stompClient.connected) {
        const scheduleId = getScheduleId();
        stompClient.send('/app/seats/select', {}, JSON.stringify({
            seatId: seatId,
            scheduleId: scheduleId,
            status: 'AVAILABLE',
            action: action
        }));
    }
}

// 페이지 떠날 때 연결 해제 + 선택 해제 알림
window.addEventListener('beforeunload', function() {
    if (selectedSeat && stompClient && stompClient.connected) {
        sendSeatSelection(selectedSeat.dataset.id, 'DESELECT');
    }
    if (stompClient !== null) {
        stompClient.disconnect();
    }
});

window.onload = function() {
    updateNav();
    loadSeats();
};

// URL에서 일정 ID 추출
function getScheduleId() {
    const params = new URLSearchParams(window.location.search);
    return params.get('scheduleId');
}

// 좌석 목록 로드
async function loadSeats() {
    const scheduleId = getScheduleId();

    if (!scheduleId) {
        alert('잘못된 접근입니다.');
        location.href = '/main.html';
        return;
    }

    try {
        const response = await fetch(`/api/seats/schedule/${scheduleId}`);
        if (!response.ok) throw new Error('좌석 정보를 불러오지 못했습니다.');

        const seats = await response.json();
        renderSeats(seats);

        connectWebSocket(scheduleId);

    } catch (error) {
        console.error(error);
        document.getElementById('seatContainer').innerHTML =
            '<p class="message">좌석 정보를 불러오는 중 오류가 발생했습니다.</p>';
    }
}

// 좌석 렌더링
function renderSeats(seats) {
    const container = document.getElementById('seatContainer');

    container.innerHTML = seats.map(seat => {
        const statusClass = seat.status.toLowerCase();
        // SOLD와 TEMPORARY 모두 빈칸으로 표시
        const displayText = (seat.status === 'SOLD' || seat.status === 'TEMPORARY') ? '' : seat.seatNo;

        return `
            <div class="seat ${statusClass}" 
                 data-id="${seat.id}"
                 data-seat-no="${seat.seatNo}"
                 data-price="${seat.price}"
                 data-status="${seat.status}"
                 onclick="handleSeatClick(this)">
                ${displayText}
            </div>
        `;
    }).join('');
}

// 좌석 클릭 핸들러
function handleSeatClick(element) {
    const status = element.dataset.status;
    const seatId = Number(element.dataset.id);

    // SOLD나 TEMPORARY는 클릭 불가
    if (status === 'SOLD' || status === 'TEMPORARY') {
        return;
    }

    // 다른 사용자가 선택 중인 좌석
    if (otherSelectedSeats.has(seatId)) {
        alert('이미 선택된 좌석입니다.');
        return;
    }

    // 정상 선택 진행
    selectSeat(element);
}

// 좌석 선택
function selectSeat(element) {
    // 로그인 체크
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) {
        alert('로그인이 필요합니다.');
        location.href = '/login.html';
        return;
    }

    // 이전 선택 해제
    if (selectedSeat) {
        selectedSeat.classList.remove('selected');
        selectedSeat.classList.add('available');
        // 이전 좌석 선택 해제 브로드캐스트
        sendSeatSelection(selectedSeat.dataset.id, 'DESELECT');
    }

    // 새 좌석 선택
    element.classList.remove('available');
    element.classList.add('selected');
    selectedSeat = element;

    // 새 좌석 선택 브로드캐스트
    sendSeatSelection(element.dataset.id, 'SELECT');

    // 선택 정보 표시
    const seatNo = element.dataset.seatNo;
    const price = Number(element.dataset.price).toLocaleString();

    document.getElementById('selectedSeatNo').textContent = seatNo + '번';
    document.getElementById('selectedPrice').textContent = price;
    document.getElementById('selectionInfo').style.display = 'block';

    // 예약 버튼 이벤트 설정
    document.getElementById('reserveBtn').onclick = () => reserveSeat(element.dataset.id);
}

// 좌석 예약
async function reserveSeat(seatId) {
    const accessToken = localStorage.getItem('accessToken');

    try {
        const response = await fetch(`/api/seats/${seatId}/reserve`, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + accessToken
            }
        });

        if (response.status === 401) {
            alert('로그인이 만료되었습니다. 다시 로그인해주세요.');
            location.href = '/login.html';
            return;
        }

        if (response.status === 409) {
            alert('다른 사용자가 먼저 예약한 좌석입니다.');
            loadSeats();
            return;
        }

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || '예약 실패');
        }

        alert('좌석 예약이 완료되었습니다!\n결제를 진행해주세요.');
        selectedSeat = null;  // 선택 초기화
        loadSeats();

    } catch (error) {
        console.error(error);
        alert(error.message || '예약 중 오류가 발생했습니다.');
    }
}