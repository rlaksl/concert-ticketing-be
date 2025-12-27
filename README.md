# 티켓 예매 시스템

온라인 공연 티켓 예매 과정에서 발생하는 **동시성(Concurrency) 문제를 해결**하고, **안정적인 예매 환경**을 제공하는 백엔드 애플리케이션입니다.

---

## 프로젝트 개요 및 목표

* **주요 목표:** 티켓팅과 같이 단시간에 대량의 요청이 발생하는 상황에서, **데이터의 정합성(Consistency)을 유지**하며 안전하게 좌석을 점유하고 판매하는 로직 구현.
* **핵심 기능:** 좌석 동시성 제어(Optimistic Locking), 중복 로그인 방지, 예매 시작 시간 기반의 버튼 통제 로직 구현.

### 개발 기간
* 2025.12.15 - (진행 중 | 개인 프로젝트)

---

## 기술 스택

### Backend
![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)

### Database & Cache
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![H2](https://img.shields.io/badge/H2-0000BB?style=for-the-badge&logo=h2&logoColor=white)

### Authentication
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)

### DevOps & Tools
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Lombok](https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge&logo=lombok&logoColor=white)

---

## 주요 기능

### 1. 좌석 동시성 제어 (Optimistic Locking)
- JPA `@Version`을 활용한 낙관적 락킹 구현
- 동시에 같은 좌석 예약 시도 시 1명만 성공, 나머지는 실패 처리
- **10,000명 동시 요청 테스트 통과**

### 2. JWT 기반 인증 시스템
- Access Token / Refresh Token 분리 발급
- Redis를 활용한 토큰 저장 및 관리
- BCrypt를 이용한 비밀번호 암호화

### 3. 중복 로그인 방지
- 새로운 기기에서 로그인 시 기존 세션 자동 만료
- Redis에 저장된 토큰과 요청 토큰 비교 검증

### 4. 좌석 상태 관리
| 상태 | 설명 |
|:---|:---|
| `AVAILABLE` | 예매 가능 |
| `TEMPORARY` | 임시 점유 (결제 대기) |
| `SOLD` | 판매 완료 |

### 5. 예매 시간 제어
- `bookingAvailableAt` 필드를 통한 예매 오픈 시간 관리
- 오픈 전에는 예매 버튼 비활성화

---

## API 명세

### 회원 (Users)
| Method | Endpoint | Description | Auth |
|:---|:---|:---|:---:|
| POST | `/api/users/signup` | 회원가입 | X |
| GET | `/api/users/check-email` | 이메일 중복 확인 | X |
| GET | `/api/users/check-phone` | 전화번호 중복 확인 | X |
| GET | `/api/users/me` | 내 정보 조회 | O |

### 인증 (Auth)
| Method | Endpoint | Description | Auth |
|:---|:---|:---|:---:|
| POST | `/api/auth/login` | 로그인 | X |
| POST | `/api/auth/logout` | 로그아웃 | O |

### 공연 (Concerts)
| Method | Endpoint | Description | Auth |
|:---|:---|:---|:---:|
| GET | `/api/concerts` | 공연 전체 조회 | X |
| GET | `/api/concerts/{id}` | 공연 단건 조회 | X |
| GET | `/api/concerts/{id}/schedules` | 공연 일정 목록 조회 | X |

### 좌석 (Seats)
| Method | Endpoint | Description | Auth |
|:---|:---|:---|:---:|
| GET | `/api/seats/schedule/{scheduleId}` | 일정별 좌석 목록 조회 | X |
| POST | `/api/seats/{seatId}/reserve` | 좌석 예약 (임시 점유) | O |
| POST | `/api/seats/{seatId}/confirm` | 결제 완료 | O |
| POST | `/api/seats/{seatId}/cancel` | 예약 취소 | O |

---

## 데이터베이스 설계 (ERD)

<img width="500" height="auto" alt="image" src="https://github.com/user-attachments/assets/54d26855-bea3-4465-a536-4e703cf1727c" />

---

## 데이터베이스 상세 명

### 1. `users` (사용자)
| 컬럼명 | 타입 | 제약 조건 | 설명 |
| :--- | :--- | :--- | :--- |
| **id** | `BIGINT` | PK, AI | 사용자 ID |
| **email** | `VARCHAR(100)` | UQ, Not Null | 로그인 이메일 |
| **password** | `VARCHAR(255)` | Not Null | 암호화된 비밀번호 |
| **name** | `VARCHAR(50)` | Not Null | 사용자 이름 |
| **phone** | `VARCHAR(20)` | UQ, Not Null | 전화번호 (중복 가입 방지) |
| **created_at** | `DATETIME` | Default | 가입 일시 |

### 2. `concerts` (공연 정보)
| 컬럼명 | 타입 | 제약 조건 | 설명 |
| :--- | :--- | :--- | :--- |
| **id** | `BIGINT` | PK, AI | 공연 ID |
| **title** | `VARCHAR(200)` | Not Null | 공연 제목 |
| **artist** | `VARCHAR(100)` | Not Null | 출연 아티스트명 |
| **created_at** | `DATETIME` | Default | 생성일 |

### 3. `concert_schedules` (공연 일정)
| 컬럼명 | 타입 | 제약 조건 | 설명 |
| :--- | :--- | :--- | :--- |
| **id** | `BIGINT` | PK, AI | 일정 ID |
| **concert_id** | `BIGINT` | FK | 어떤 공연의 일정인지 |
| **concert_at** | `DATETIME` | Not Null | 실제 공연 날짜 및 시간 |
| **booking_available_at** | `DATETIME` | Not Null | 예매 가능 시작 시간 |
| **total_seats** | `INT` | Not Null | 총 좌석 수 |

### 4. `seats` (좌석 정보)
| 컬럼명 | 타입 | 제약 조건 | 설명 |
|:---|:---|:---|:---|
| **id** | `BIGINT` | PK, AI | 좌석 ID |
| **schedule_id** | `BIGINT` | FK | 해당 좌석 일정 ID |
| **seat_no** | `INT` | Not Null | 좌석 번호 |
| **price** | `INT` | Not Null | 좌석 가격 |
| **status** | `VARCHAR(20)` | Default | 좌석 상태 (AVAILABLE, TEMPORARY, SOLD) |
| **user_id** | `BIGINT` | Nullable | 예약한 사용자 ID |
| **version** | `BIGINT` | Default | 낙관적 락킹용 버전 |

---

## 동시성 제어 방식

### Optimistic Locking (낙관적 락킹)
```java
@Entity
public class Seat {
    @Version
    private Long version;  // 버전 관리
}
```

1. 사용자 A, B가 동시에 같은 좌석 조회 (version = 0)
2. 사용자 A가 먼저 예약 요청 → version 0 → 1로 업데이트, 성공
3. 사용자 B가 예약 요청 → version 0으로 시도하지만 이미 1 → `OptimisticLockingFailureException` 발생
4. 예외 처리 → "다른 사용자가 먼저 예약한 좌석입니다" 응답

### 테스트 결과
```
========== 테스트 결과 ==========
성공: 1명
실패: 9999명
```
- 10,000명 동시 요청 테스트 통과
- 소요 시간: 약 1분 25초

---

## 프로젝트 구조
```
src/main/java/com/ticket/
├── domain/
│   ├── user/          # 회원 도메인
│   ├── auth/          # 인증 도메인
│   ├── concert/       # 공연/일정 도메인
│   └── seat/          # 좌석 도메인
├── global/
│   ├── config/        # 설정 (Security, Redis, Swagger)
│   ├── security/      # JWT, 인증 필터
│   └── exception/     # 예외 처리
└── ConcertTicketingBeApplication.java
```

---

## 실행 방법

### 1. 사전 요구사항
- Java 17
- MySQL 8.x
- Redis

### 2. 데이터베이스 설정
```sql
CREATE DATABASE ticket_db;
```

### 3. Redis 실행 (Docker)
```bash
docker run -d --name ticket-redis -p 6379:6379 redis
```

### 4. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 5. API 문서 확인
```
http://localhost:8080/swagger-ui/index.html
```

---

## 테스트 실행
```bash
./gradlew test
```

- **H2 인메모리 데이터베이스**: 테스트 시 MySQL 대신 H2 사용 (별도 DB 설치 불필요)
- **Mock Redis**: 테스트용 Mock 설정으로 Redis 없이 테스트 가능

---

## CI/CD

GitHub Actions를 통한 자동 빌드 및 테스트
- `main` 브랜치 push/PR 시 자동 실행
- JDK 17, Gradle 빌드

---

## 향후 계획

- [x] 10,000명 동시 요청 테스트
- [ ] 부하 테스트 (JMeter/Gatling)
- [ ] 결제 시스템 연동
- [ ] 좌석 선택 UI 구현
- [ ] 대기열 시스템 도입
