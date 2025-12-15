# 티켓 예매 시스템

온라인 공연 티켓 예매 과정에서 발생하는 **동시성(Concurrency) 문제를 해결**하고, **안정적인 예매 환경**을 제공하는 백엔드 애플리케이션입니다.

---

## 프로젝트 개요 및 목표

* **주요 목표:** 티켓팅과 같이 단시간에 대량의 요청이 발생하는 상황에서, **데이터의 정합성(Consistency)을 유지**하며 안전하게 좌석을 점유하고 판매하는 로직 구현.
* **핵심 기능:** 좌석 동시성 제어, 예매 시작 시간 기반의 버튼 통제 로직 구현.

### 개발 기간
* 2025.12.15 - (진행 중 | 개인 프로젝트)

---

## 기술 스택

### Backend
  ![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
  ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
  ![Lombok](https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge&logo=lombok&logoColor=white)
  ![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white) 

---

## 데이터베이스 스키마 명세

### 1. `users` (사용자)
| 컬럼명 | 타입 | 제약 조건 | 설명 |
| :--- | :--- | :--- | :--- |
| **id** | `BIGINT` | PK, AI | 사용자 ID |
| **email** | `VARCHAR(100)` | UQ, Not Null | 로그인 이메일 |
| **password** | `VARCHAR(255)` | Not Null | 비밀번호 |
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
| :--- | :--- | :--- | :--- |
| **id** | `BIGINT` | PK, AI | 좌석 ID |
| **schedule_id** | `BIGINT` | FK | 해당 좌석 일정 ID |
| **seat_no** | `INT` | Not Null | 좌석 번호 |
| **price** | `INT` | Not Null | 좌석 가격 |
| **status** | `VARCHAR(20)` | Default | 좌석 상태 (AVAILABLE, TEMPORARY, SOLD) |
| **version** | `INT` | Default | - |
