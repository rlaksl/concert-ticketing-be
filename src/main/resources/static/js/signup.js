// 이메일 중복확인
document.getElementById('checkEmailBtn').addEventListener('click', async function() {
    const email = document.getElementById('email').value.trim();
    const errorSpan = document.getElementById('emailError');

    // 입력값 확인
    if (!email) {
        errorSpan.textContent = '이메일을 입력해주세요.';
        errorSpan.className = 'error-message error';
        return;
    }

    // API 호출
    const response = await fetch(`/api/users/check-email?email=${email}`);
    const isDuplicate = await response.json();

    if (isDuplicate) {
        errorSpan.textContent = '이미 사용 중인 이메일입니다.';
        errorSpan.className = 'error-message error';
    } else {
        errorSpan.textContent = '사용 가능한 이메일입니다.';
        errorSpan.className = 'error-message success';
    }
});

// 전화번호 중복확인
document.getElementById('checkPhoneBtn').addEventListener('click', async function() {
    const phone = document.getElementById('phone').value.trim();
    const errorSpan = document.getElementById('phoneError');

    // 입력값 확인
    if (!phone) {
        errorSpan.textContent = '전화번호를 입력해주세요.';
        errorSpan.className = 'error-message error';
        return;
    }

    // API 호출
    const response = await fetch(`/api/users/check-phone?phone=${phone}`);
    const isDuplicate = await response.json();

    if (isDuplicate) {
        errorSpan.textContent = '이미 사용 중인 전화번호입니다.';
        errorSpan.className = 'error-message error';
    } else {
        errorSpan.textContent = '사용 가능한 전화번호입니다.';
        errorSpan.className = 'error-message success';
    }
});

// 회원가입 폼 제출
document.getElementById('signupForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const data = {
        email: document.getElementById('email').value,
        password: document.getElementById('password').value,
        name: document.getElementById('name').value,
        phone: document.getElementById('phone').value
    };

    try {
        const response = await fetch('/api/users/signup', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        // 실패 시 에러 처리
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || '회원가입 실패');
        }

        // 성공 시
        alert('회원가입이 완료되었습니다!');
        window.location.href = '/login.html'; // 여기가 핵심!

    } catch (error) {
        console.error(error);
        alert(error.message || '서버 통신 오류가 발생했습니다.');
    }
});