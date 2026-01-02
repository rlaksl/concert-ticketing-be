let stompClient = null;
let scheduleId = null;

window.onload = function() {
    updateNav();
    checkLoginAndEnterQueue();
};

// URL에서 scheduleId 추출
function getScheduleId() {
    const params = new URLSearchParams(window.location.search);
    return params.get('scheduleId');
}

// 로그인 확인 후 대기열 등록
async function checkLoginAndEnterQueue() {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        alert('로그인이 필요합니다.');
        location.href = '/login.html';
        return;
    }

    scheduleId = getScheduleId();
    if (!scheduleId) {
        alert('잘못된 접근입니다.');
        location.href = '/main.html';
        return;
    }

    // 대기열 등록
    await enterQueue();
}

// 대기열 등록 API 호출
async function enterQueue() {
    const accessToken = localStorage.getItem('accessToken');

    try {
        const response = await fetch('/api/queue/enter', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + accessToken
            },
            body: JSON.stringify({ scheduleId: parseInt(scheduleId) })
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
        updateQueueDisplay(data);

        // 이미 READY 상태면 바로 이동
        if (data.status === 'READY') {
            goToSeatSelect(data.entryToken);
            return;
        }

        // WebSocket 연결
        connectWebSocket();

    } catch (error) {
        console.error(error);
        alert(error.message || '대기열 등록 중 오류가 발생했습니다.');
        location.href = '/main.html';
    }
}

// WebSocket 연결
function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // 디버그 로그 끄기

    stompClient.connect({}, function(frame) {
        console.log('WebSocket 연결됨');

        // 대기열 업데이트 구독
        stompClient.subscribe('/topic/queue/' + scheduleId, function(message) {
            const signal = message.body;
            if (signal === 'UPDATE') {
                fetchQueueStatus();
            }
        });
    }, function(error) {
        console.error('WebSocket 연결 실패:', error);
        // 연결 실패 시 폴링으로 대체
        setInterval(fetchQueueStatus, 3000);
    });
}

// 순번 조회 API 호출
async function fetchQueueStatus() {
    const accessToken = localStorage.getItem('accessToken');

    try {
        const response = await fetch(`/api/queue/status?scheduleId=${scheduleId}`, {
            headers: {
                'Authorization': 'Bearer ' + accessToken
            }
        });

        if (!response.ok) {
            throw new Error('순번 조회 실패');
        }

        const data = await response.json();
        updateQueueDisplay(data);

        // READY 상태면 좌석 선택 페이지로 이동
        if (data.status === 'READY') {
            goToSeatSelect(data.entryToken);
        }

    } catch (error) {
        console.error('순번 조회 오류:', error);
    }
}

// 화면 업데이트
function updateQueueDisplay(data) {
    document.getElementById('myPosition').textContent = data.position || '-';
    document.getElementById('totalWaiting').textContent = data.totalWaiting || '-';

    // 예상 대기 시간 표시
    if (data.estimatedWaitSeconds) {
        const minutes = Math.ceil(data.estimatedWaitSeconds / 60);
        document.getElementById('estimatedTime').textContent = `약 ${minutes}분`;
    } else {
        document.getElementById('estimatedTime').textContent = '곧 입장 가능';
    }

    // READY 상태 표시
    const container = document.querySelector('.queue-container');
    if (data.status === 'READY') {
        container.classList.add('ready');
        document.getElementById('queueMessage').textContent = '입장 가능! 잠시 후 이동합니다...';
        document.getElementById('spinner').style.display = 'none';
    }
}

// 좌석 선택 페이지로 이동
function goToSeatSelect(entryToken) {
    // 토큰 저장
    localStorage.setItem('entryToken_' + scheduleId, entryToken);

    // 페이지 이동
    setTimeout(() => {
        location.href = `/seat-select.html?scheduleId=${scheduleId}`;
    }, 1500);
}

// 대기열 나가기
document.getElementById('exitBtn').addEventListener('click', async function() {
    if (!confirm('대기열에서 나가시겠습니까?\n다시 줄을 서야 합니다.')) {
        return;
    }

    const accessToken = localStorage.getItem('accessToken');

    try {
        await fetch('/api/queue/exit', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + accessToken
            },
            body: JSON.stringify({ scheduleId: parseInt(scheduleId) })
        });
    } catch (error) {
        console.error('대기열 이탈 오류:', error);
    }

    // WebSocket 연결 해제
    if (stompClient !== null) {
        stompClient.disconnect();
    }

    location.href = '/main.html';
});

// 페이지 떠날 때 정리
window.addEventListener('beforeunload', function() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
});