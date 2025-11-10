# 🔄 synco - 통합 협업 지식관리 툴

<!-- <img width="1918" height="909" alt="image" src="https://github.com/user-attachments/assets/0497f4c8-dc34-43a3-9964-b6e58021eed8" /> -->
<img width="1695" height="884" alt="스크린샷 2025-11-07 오후 5 45 37" src="https://github.com/user-attachments/assets/4c303e06-73f4-45ac-b9e9-3b48b71f607b" />

<!-- <img width="1536" height="1024" alt="Image" src="https://github.com/user-attachments/assets/3b6f4d87-7c0e-4575-a2af-2910df5dc87b" /> -->

## 🙋🏻 팀원 소개

<table>
  <tr>
    <!-- 1행: 사진(클릭 가능) + 이름 -->
    <td align="center">
      <a href="https://github.com/astraglus03" target="_blank">
        <img src="https://avatars.githubusercontent.com/astraglus03" width="100px;" alt="김건동"/>
      </a><br />
      <b>김건동</b>
    </td>
    <td align="center">
      <a href="https://github.com/suyeonkwong" target="_blank">
        <img src="https://avatars.githubusercontent.com/suyeonkwong" width="100px;" alt="권수연"/>
      </a><br />
      <b>권수연</b>
    </td>
    <td align="center">
      <a href="https://github.com/Chanjin629" target="_blank">
        <img src="https://avatars.githubusercontent.com/Chanjin629" width="100px;" alt="김찬진"/>
      </a><br />
      <b>김찬진</b>
    </td>
    <td align="center">
      <a href="https://github.com/userkimjihyeon" target="_blank">
        <img src="https://avatars.githubusercontent.com/userkimjihyeon" width="100px;" alt="김지현"/>
      </a><br />
      <b>김지현</b>
    </td>
    <td align="center">
      <a href="https://github.com/SuOhYoon" target="_blank">
        <img src="https://avatars.githubusercontent.com/SuOhYoon" width="100px;" alt="윤수오"/>
      </a><br />
      <b>윤수오</b>
    </td>
  </tr>
  <tr>
    <!-- 2행: GitHub 배지 -->
    <td align="center">
      <a href="https://github.com/astraglus03" target="_blank">
        <img src="https://img.shields.io/badge/GitHub_Profile-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/suyeonkwong" target="_blank">
        <img src="https://img.shields.io/badge/GitHub_Profile-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/Chanjin629" target="_blank">
        <img src="https://img.shields.io/badge/GitHub_Profile-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/userkimjihyeon" target="_blank">
        <img src="https://img.shields.io/badge/GitHub_Profile-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/SuOhYoon" target="_blank">
        <img src="https://img.shields.io/badge/GitHub_Profile-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
  </tr>
</table>

---
## 🎬 프로젝트 개요

### 프로젝트 소개
Synco는 분산된 협업 도구들을 하나로 통합한 **워크스페이스 기반 협업 플랫폼**이다.  
노션(Notion)의 문서·보드·캘린더 관리 기능과 디스코드(Discord)의 실시간 음성·채팅·화상회의 기능을 결합하여, 팀과 개인이 동시에 효율적으로 협업할 수 있는 환경을 제공한다.  
Synco의 핵심은 **팀 워크스페이스 + 개인 공간 동시 지원**과 **업무/커뮤니케이션 단일 경험**이다.

---

### 프로젝트 배경 및 필요성
- 기존 툴은 문서 기반 협업(노션)과 실시간 커뮤니케이션(디스코드)이 분리되어 있다.
- 협업 기록이 파편화되고, 히스토리/맥락 추적이 어렵다.
- 개인 프로젝트·스터디·소규모 팀도 바로 쓸 수 있는 일체형 협업툴이 필요하다.
- 회의/업무/드라이브가 연결되는 흐름형 협업 수요가 증가하고 있다.

---

## 📌 주요 기능

#### 1) 📅 통합 일정 관리
- 워크스페이스/프로젝트/개인 일정 통합 캘린더 (월/주/일 뷰)
- 일정 상태/중요도 색상 구분, 날짜 클릭 시 상세 조회 및 수정
- 워크스페이스별 일정 공유 및 협업

#### 2) 💬 실시간 채팅
- 워크스페이스/프로젝트 전용 채팅방 자동 개설
- STOMP WebSocket 기반 실시간 메시지 전송
- 멘션, 파일 공유, 메시지 히스토리 관리
- 채팅 내 날짜/일정 정보 추출 및 연동

#### 3) 📝 실시간 공동 문서 편집
- TipTap 에디터 기반 문서 동시 편집
- Redis Line Lock으로 라인 단위 편집 충돌 방지
- Redis Pub/Sub으로 멀티 인스턴스 간 실시간 동기화
- 변경사항 실시간 반영 및 편집 히스토리 관리

#### 4) 🎥 화상회의 & AI 회의록
- LiveKit 기반 WebRTC 화상회의
- 회의 녹화 및 S3 저장
- Whisper(STT)를 통한 음성 → 텍스트 변환
- Naver CLOVA Studio Summary API로 회의록 자동 요약
- 회의 종료 시 자동으로 회의록 생성 및 공유

#### 5) 📁 통합 드라이브
- 워크스페이스별 파일 관리 및 공유
- AWS S3 기반 파일 저장 및 CloudFront CDN 연동
- 파일 업로드/다운로드, 폴더 구조 관리

#### 6) ⏰ 체계적인 알림 시스템
- 프로젝트 초대, 프로젝트 업무 할당, 업무 댓글, 답글등 여러 상황에서의 알림 시스템 구축
- SSE(Server Sent Events) 기반 실시간 알림 전송
- MSA 모듈간 Redis Pub/Sub으로 알림 전송
- 워크스페이스/프로젝트/문서/채팅 등 다양한 이벤트 알림
- 알림 설정 및 필터링 기능

#### 7) 🔍 통합 검색
- Elasticsearch 기반 통합 검색 엔진
- 문서, 채팅, 일정, 파일 등 모든 콘텐츠 검색
- 워크스페이스별 검색 범위 설정
- 검색 결과 하이라이트 및 필터링

#### 8) 🏢 워크스페이스 & 개인 공간
- 팀 워크스페이스와 개인 공간 동시 지원
- 워크스페이스별 멤버 관리 및 권한 설정
- 개인 프로젝트 및 팀 프로젝트 분리 관리
- 워크스페이스별 독립적인 문서/채팅/일정 관리
- 개인 공유문서 팀 드라이브로 공유 가능

<br>

## 🧾 주요 화면

공유 문서 편집<br>
<img src="https://github.com/user-attachments/assets/2a124458-012d-49be-b805-ee9ddf70c3ba" alt="GIF 설명" />

화상회의 녹화 및 화면공유 회의<br>
<img src="https://github.com/user-attachments/assets/9939ad79-46c6-4f35-b6c9-31394f9146c3" alt="GIF 설명" />

실시간 사용량 모니터링(Prometheus + Grafana)
<img width="2560" height="1347" alt="스크린샷 2025-11-08 오후 3 09 34" src="https://github.com/user-attachments/assets/c7ca28b1-fcce-4b5b-9f1d-66c6947cb09d" />

---


## 📌 주요 기술

| 기술명 | 내용 |
|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **화상회의 (LiveKit)** | WebRTC 기반 오픈소스 SFU인 LiveKit을 사용하여 실시간 화상회의 구현. LiveKit Server SDK로 방 생성/관리 및 JWT 토큰 발급. Egress를 통한 회의 녹화 및 S3 자동 저장. Webhook을 통한 참가자 입장/퇴장, 녹화 완료 등 이벤트 처리. STOMP WebSocket으로 회의 내 채팅 메시지 전송. |
| **문서 편집 (실시간 협업)** | TipTap 리치 텍스트 에디터 사용. STOMP WebSocket으로 실시간 양방향 통신. Redis Pub/Sub으로 멀티 인스턴스 간 변경사항 동기화. CRDT 대신 라인 단위 관리로 충돌 최소화. 라인 락 메커니즘으로 동시 편집 충돌 방지 (30초 TTL 자동 해제). |
| **모니터링 (Prometheus + Grafana)** | Spring Boot Actuator로 `/actuator/prometheus` 엔드포인트 제공. Prometheus Operator의 ServiceMonitor로 자동 타겟 발견 및 메트릭 수집. Grafana 대시보드로 시각화 및 알람 관리. AWS EKS 환경에서 AWS Load Balancer Controller로 ALB Ingress 관리. EBS CSI Driver로 PVC 동적 프로비저닝. |
| **최종 배포 (AWS EKS)** | AWS EKS Kubernetes 클러스터 사용. AWS Load Balancer Controller로 ALB 기반 Ingress 관리. Kafka KRaft 모드로 Zookeeper 없이 단일 노드 Kafka 실행. GitHub Actions로 CI/CD 파이프라인 구축. Docker + ECR로 컨테이너 이미지 관리. |
| **카프카 (Apache Kafka)** | 각 모듈(Task, Drive, Chat, Meeting)에서 발생한 생성·수정·삭제 이벤트를 Kafka 토픽으로 송출. Search 서비스가 동일 토픽을 구독해 Elasticsearch 인덱스를 실시간 갱신. API 트랜잭션과 검색 인덱싱을 분리함으로써 응답 지연 최소화. 동일 이벤트 스트림을 알림·로그·통계 등 후속 서비스가 재사용하도록 확장성 확보. |
| **검색 (Elasticsearch + Nori)** | 통합 검색 품질 최적화를 위해 Elasticsearch 도입. Nori 기반 분석기 커스터마이징으로 복합어 분해(Mixed 모드), 품사 필터, Reading Form 필터 적용. “회의록공유” 등 붙임표 없는 표현도 정밀 색인/검색. Task·Chat·Drive·Meeting 인덱스에 동일 설정을 적용해 일관된 형태소 처리와 빠른 응답 제공. |
| **소셜 로그인 (OAuth 2.0)** | Google·Kakao·Naver OAuth 플로우를 서비스별로 구현. 공통 후처리에서 회원 등록, 탈퇴 상태 검증, JWT 발급 수행. 신규 소셜 사용자는 즉시 Member 엔티티로 저장되며 Synco 전용 아이디 추가 등록을 유도. 다중 인증 경로를 유지하면서 회원 데이터 정합성 보장. |
| **이메일 서비스 (SMTP)** | 비밀번호 찾기 요청 시 EmailService가 임시 비밀번호 생성. JavaMailSender로 SMTP 서버(AWS SES·Gmail 등) 연동 후 인증 메일 자동 발송. 생성된 임시 비밀번호는 즉시 암호화되어 저장. Thymeleaf 템플릿(tempPassword.html)로 HTML 메일 구성해 계정 보안과 사용자 경험 강화. 
| **OpenFeign을 이용한 모듈간 통신** | MSA 설계 구조로 인해 WorkSpace 모듈과 다른 모듈간의 통신을 OpenFeign을 통해 동기적 통신방식으로 구현 |
| **알림 (SSE / Redis Pub&Sub)** | SSE를 이용한 서버->클라이언트로의 실시간 알림 생성. MSA 설계방식으로 인해 알림이 발생하는 모듈을 알림 모듈이 구독(Subscribe)하고 알림이 발생하는 모듈은 메시지를 발행(Publish)해서 알림을 받아옴. |
---

## ⚙️🛠️ Technical Stack
  
### 🎯 Frontend

<a href="https://vuejs.org/" target="_blank"><img src="https://img.shields.io/badge/Vue.js-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white"/></a>
<a href="https://vitejs.dev/" target="_blank"><img src="https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white"/></a>
<a href="https://vuetifyjs.com/" target="_blank"><img src="https://img.shields.io/badge/Vuetify-1867C0?style=for-the-badge&logo=vuetify&logoColor=white"/></a>
<a href="https://pinia.vuejs.org/" target="_blank"><img src="https://img.shields.io/badge/Pinia-FFD859?style=for-the-badge&logo=vue.js&logoColor=black"/></a>
<a href="https://router.vuejs.org/" target="_blank"><img src="https://img.shields.io/badge/Vue_Router-35495E?style=for-the-badge&logo=vue.js&logoColor=white"/></a>
<a href="https://axios-http.com/" target="_blank"><img src="https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white"/></a>
<a><img src="https://img.shields.io/badge/JavaScript-ES6+-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black"/></a>

### 🚀 Backend

<a href="https://spring.io/projects/spring-boot" target="_blank"><img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/></a>
<a href="https://spring.io/projects/spring-data-jpa" target="_blank"><img src="https://img.shields.io/badge/Spring_Data_JPA-007396?style=for-the-badge&logo=hibernate&logoColor=white"/></a>
<a><img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white"/></a>
<a><img src="https://img.shields.io/badge/STOMP/WebSocket-FF6B6B?style=for-the-badge&logo=socket.io&logoColor=white"/></a>
<a href="https://kafka.apache.org/" target="_blank"><img src="https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white"/></a>
<a href="https://www.elastic.co/elasticsearch/" target="_blank"><img src="https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white"/></a>
<a href="https://livekit.io/" target="_blank"><img src="https://img.shields.io/badge/LiveKit-1FD5F9?style=for-the-badge&logo=livekit&logoColor=white"/></a>
<a href="https://fastapi.tiangolo.com/" target="_blank"><img src="https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white"/></a>
<a><img src="https://img.shields.io/badge/OAuth2-3A3A3A?style=for-the-badge&logo=oauth&logoColor=white"/></a><a href="https://github.com/openai/whisper" target="_blank"><img src="https://img.shields.io/badge/Whisper-STT-74AA9C?style=for-the-badge&logo=openai&logoColor=white"/></a>
<a href="https://www.ncloud.com/product/aiService/clovaStudio" target="_blank"><img src="https://img.shields.io/badge/Naver%20CLOVA%20Studio-Summary%20API-03C75A?style=for-the-badge&logo=naver&logoColor=white"/></a>



### 🚀 Deployment & DevOps

<div> <a href="https://aws.amazon.com/ec2/" target="_blank"> <img src="https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"/> </a> <a href="https://aws.amazon.com/rds/" target="_blank"> <img src="https://img.shields.io/badge/AWS%20RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white"/> </a> <a href="https://aws.amazon.com/s3/" target="_blank"> <img src="https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white"/> </a> <a href="https://aws.amazon.com/cloudfront/" target="_blank"> <img src="https://img.shields.io/badge/AWS%20CloudFront-8C4FFF?style=for-the-badge&logo=amazonaws&logoColor=white"/> </a> <a href="https://nginx.org/" target="_blank"> <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"/> </a> <a href="https://github.com/features/actions" target="_blank"> <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"/> </a> <a href="https://www.docker.com/" target="_blank"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/> </a>
<a href="https://kubernetes.io/" target="_blank"><img src="https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white"/></a> <a href="https://prometheus.io/" target="_blank"> <img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white"/> </a> <a href="https://grafana.com/" target="_blank"> <img src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white"/> </a> </div>

<h2>DB</h2>

<a href="https://mariadb.org" target="_blank"> <img src="https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white"/> </a> 
<a href="https://redis.io/" target="_blank"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white"/> </a>

<h2>Tool</h2>

<div> <!-- 협업 및 관리 툴 --> 
  <a href="https://github.com" target="_blank"> <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white"/> </a> 
  <a href="https://discord.com" target="_blank"> <img src="https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white"/> </a> 
  <a href="https://www.notion.so" target="_blank"> <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white"/> </a> 
  <a href="https://www.figma.com" target="_blank"> <img src="https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white"/> </a> 
  <a href="https://www.erdcloud.com" target="_blank"> <img src="https://img.shields.io/badge/ERD%20Cloud-4285F4?style=for-the-badge&logo=googlecloud&logoColor=white"/> </a> 
  <a href="https://www.postman.com/" target="_blank"> <img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white"/> </a> 
</div>
</details>


<br>

## 📂 산출물
<details>
  <summary><b> <a href='https://docs.google.com/spreadsheets/d/1T9HX4fSAoEJpjI7ewdls1ylfE5ZluGYYnR-oLL4OooU/edit?gid=1270453977#gid=1270453977' style="text-decoration: none; color: inherit;"> WBS</a></b></summary>
  <img width="2048" height="2106" alt="sheet_merged_vertical" src="https://github.com/user-attachments/assets/3a5c2142-e174-4372-9e9a-88a86ac8a7b4" />
</details>

<details>
  <summary><b><a href='https://docs.google.com/spreadsheets/d/1T9HX4fSAoEJpjI7ewdls1ylfE5ZluGYYnR-oLL4OooU/edit?gid=1270453977#gid=1270453977' style="text-decoration: none; color: inherit;"> 요구사항 명세서</a></b></summary>
  <img width="1949" height="3142" alt="sheet_merged_vertical_set2" src="https://github.com/user-attachments/assets/1827f5e4-1d71-433a-b021-0330a511dda8" />
</details>

<details>
  <summary><b> <a href='https://www.erdcloud.com/d/4XEmWM7BEZB2zLQPo' style="text-decoration: none; color: inherit;"> ERD</a></b></summary>
  <img width="2048" height="1740" alt="erd_merged_vertical_2" src="https://github.com/user-attachments/assets/b22cb670-45f9-4057-88ef-1fd6dc3c223d" />

  <a href="https://www.erdcloud.com/...">
</details>

<details>
  <summary><b> <a href='https://tranquil-fuchsia-64e.notion.site/Synco-2717cd1f5ed981d685b1c0a1776dc85b?source=copy_link' style="text-decoration: none; color: inherit;"> 프로젝트 기획서</a></b></summary>
</details>


<details>
  <summary><b> <a href='https://www.figma.com/design/JQZDxh0wwj80OqMN1poe7o/synco?node-id=0-1&p=f&t=zr4p7P0tSrSaxWi3-0'  style="text-decoration: none; color: inherit;"> 피그마</a></b></summary>
  <img width="1758" height="1085" alt="스크린샷 2025-11-08 오전 1 02 08" src="https://github.com/user-attachments/assets/a4468337-0a60-470c-9b7b-692d909592d2" />
</details>

<details>
  <summary><b>시스템 아키텍처</b></summary>
  <img width="1287" height="1143" alt="Image" src="https://github.com/user-attachments/assets/534d3d19-097f-4a4e-a0f1-7a1029138d48" />
</details>
---

## 🧾 화면 테스트 결과

<details><summary><b>회원가입 / 로그인</b></summary>

<details><summary>회원가입</summary>

![회원가입](https://github.com/user-attachments/assets/c0ea5412-97ba-48d7-aae2-647d51f35dd1)

</details>
<details><summary>소셜회원가입</summary>
  
![Image](https://github.com/user-attachments/assets/4c5159f9-87ea-49f7-850b-0a86856e54a0)
</details>

<details><summary>로그인 및 ID 기억하기</summary>
  
![로그인 및 아이디 저장](https://github.com/user-attachments/assets/e5f20cf1-873b-49b3-a417-147d86f2dd09)
</details>
<details><summary>ID 찾기</summary>
  
![아이디 찾기](https://github.com/user-attachments/assets/6e7cbd81-79c9-4df9-ace6-41535c5deb55)
</details>

<details><summary>비밀번호 찾기</summary>
  
![비밀번호 찾기](https://github.com/user-attachments/assets/1457a95c-8be9-4398-9486-5c49c71ba352)
</details>
<details><summary>비밀번호 설정</summary>
  
![Image](https://github.com/user-attachments/assets/63f8f5c7-bdb1-47a5-9c2b-f258c3fab306)
</details>
<details><summary>회원탈퇴</summary>
  
![Image](https://github.com/user-attachments/assets/83ef9527-b81d-4699-a3fb-f09b13b204d0)
</details>

</details>


<details><summary><b>마이페이지 / 알림설정 / 친구</b></summary>

<details><summary>마이페이지 정보 조회 및 수정</summary>
  
![프로필 수정](https://github.com/user-attachments/assets/ed7bbd2b-5225-4283-9966-bcc8c5698efe)

</details>
<details><summary>친구 추가(알림 ON), 이후 취소</summary>
  
![친구 요청 알림OFF](https://github.com/user-attachments/assets/ef6521bd-bba6-4344-b2f5-b80a6ccdb307)

</details>
<details><summary>친구 추가(알림 ON), 이후 수락</summary>
  
![친구 요청 알림 ON 수락](https://github.com/user-attachments/assets/1c57a4a5-44c3-4840-951b-85ec8c02fa49)
</details>
<details><summary>친구 상태 조회</summary>
  
![친구 상태 변경 오프라인온라인](https://github.com/user-attachments/assets/6769b4ba-76cd-49e4-8efc-eca066ad23d5)
![친구 상태 변경(사용자 지정)](https://github.com/user-attachments/assets/38574999-89a0-4283-a0bc-bdf164853a14)
![친구 상태 변경 온라인오프라인](https://github.com/user-attachments/assets/3bceb251-cb82-4e18-8954-7bb437a84d8d)
</details>
<details><summary>친구 삭제</summary>
  
![친구삭제](https://github.com/user-attachments/assets/ab4032ca-4208-4f59-b5b0-8ed7a2180726)
</details>

</details>


<details><summary><b>개인 대시보드</b></summary>

<details><summary>통계</summary>
  
![개인-통계](https://github.com/user-attachments/assets/1fc4e82a-0b3d-4a3a-91ae-7c758a56f254)
</details>

<details><summary>빠른 작업</summary>

![개인-빠른 작업](https://github.com/user-attachments/assets/f58cbb9e-b28f-4ee3-b458-c753a813b8b2)
</details>
<details><summary>내 업무 및 개인일정</summary>
  
![내 업무 및 개인일정](https://github.com/user-attachments/assets/d98b16c8-14b7-4bd5-b01d-a681ff8893ba)
</details>
</details>
<details><summary><b>프로젝트 대시보드</b></summary>
<details><summary>통계(진행률, 진행중인 업무, 완료된 업무, 팀 멤버)</summary>

![팀-대시보드 통계](https://github.com/user-attachments/assets/8aea211b-a769-42a3-82b2-fa61d99c5611)
</details>
<details><summary>프로젝트 진행 흐름(계획/실제), 진행 현황</summary>

![팀-프로젝트 진행 흐름](https://github.com/user-attachments/assets/7d76c7ef-f6d1-4d12-9283-376342d258f8)
</details>
<details><summary>마감일 임박 업무 / 담당자별 업무 현황</summary>

![팀-담당자별 업무 현황](https://github.com/user-attachments/assets/2489eb0f-4fd3-4e8e-9e11-6294c642e548)
</details>
</details>


<details><summary><b>개인 일정</b></summary>

<details><summary>일정 생성</summary>

![개인 일정 생성](https://github.com/user-attachments/assets/61f54a23-dea6-4316-8509-bf166a0306c0)
</details>
<details><summary>일정 목록 조회</summary>

![개인 일정 목록 조회 및 상태 변경](https://github.com/user-attachments/assets/cc5afda2-0166-4ac8-9473-7c25ffc87760)
</details>
<details><summary>일정 상세 조회</summary>

![개인 일정 상세 조회](https://github.com/user-attachments/assets/a95a8450-73b8-457d-bc21-25601a7f29cc)
</details>
<details><summary>일정 수정</summary>

![개인 일정 수정](https://github.com/user-attachments/assets/ebbb6a60-e547-49a2-8215-9fed2b15156b)
</details>

<details><summary>일정 삭제</summary>

![개인 일정 삭제](https://github.com/user-attachments/assets/0bcd3b29-8900-48a7-a6ab-2133b96fbb11)
</details>
</details>

<details><summary><b>프로젝트</b></summary>

<details><summary>프로젝트 생성</summary>


![프로젝트 생성](https://github.com/user-attachments/assets/18646e57-685a-4248-9482-2328e66d3db5)

</details>
<details><summary>프로젝트 생성 후 초대</summary>


![프로젝트 생성 후 초대](https://github.com/user-attachments/assets/aa340a3d-aed0-4244-8e23-417f450acc7b)

</details>
<details><summary>프로젝트 멤버 목록 조회 및 실시간 상태 변경</summary>


![프로젝트 멤버 상태 실시간(사용자 지정 상태-오프라인)](https://github.com/user-attachments/assets/dc2e3f11-51d8-45ff-a8b7-612d759edcb9)
![프로젝트 멤버 상태 실시간(온라인)](https://github.com/user-attachments/assets/aa6d104a-a50b-4ddd-af7d-cbdbd6eae575)
</details>
<details><summary>프로젝트 수정</summary>


![프로젝트 수정](https://github.com/user-attachments/assets/c67df3de-b3d2-4782-bad1-883464acc18d)
</details>
<details><summary>프로젝트 권한 위임</summary>



![프로젝트 SUPER 권한 위임](https://github.com/user-attachments/assets/5cd5d096-d244-4454-97b5-176651ffad23)
</details>

<details><summary>프로젝트 탈퇴</summary>


![프로젝트 탈퇴](https://github.com/user-attachments/assets/27258972-57e8-47b9-a322-bae04d923fa0)
</details>
<details><summary>프로젝트 강제 탈퇴</summary>


![프로젝트 강제탈퇴](https://github.com/user-attachments/assets/ba55692a-a251-45ee-bb11-fa30bcc2fe85)
</details>
<details><summary>프로젝트 삭제</summary>

![프로젝트 삭제](https://github.com/user-attachments/assets/e1d82208-a706-413f-b3de-55b0f0ca3f3b)
</details>
</details>

<details><summary><b>프로젝트 업무</b></summary>
<details><summary>업무 생성</summary>
  
![업무 생성](https://github.com/user-attachments/assets/12172f44-962c-4675-bc10-82d29e507a3d)
</details>
<details><summary>내 보드 등록</summary>

![보드 생성](https://github.com/user-attachments/assets/6297096c-d295-4a74-abfe-801ae4cd39be)
</details>

<details><summary>업무 상태 변경</summary>

![업무 항목 상태 변경](https://github.com/user-attachments/assets/5ed5b171-6b8f-4bf9-81ed-94fb5a7e7a77)
</details>
<details><summary>업무 수정</summary>
  
![일정상세수정](https://github.com/user-attachments/assets/f62d1995-0a4a-45c6-b911-e99fb987f1c5)
</details>
<details><summary>업무 할당 보드 변경</summary>

![보드 항목 이동](https://github.com/user-attachments/assets/449760e8-ddf3-409e-869f-53ab1267aa15)
</details>
<details><summary>보드 수정</summary>

![보드 수정](https://github.com/user-attachments/assets/117a710e-524e-409f-b354-6ff6c4ad58c2)
</details>
<details><summary>업무 상세 조회</summary>

![보드 업무 상세 조회](https://github.com/user-attachments/assets/0f744a48-bf4b-4c98-8265-86a150a9105a)
</details>

<details><summary>댓글 생성</summary>

![업무 댓글](https://github.com/user-attachments/assets/2de600c5-2a3f-420d-8d24-a9055587a514)
</details>
<details><summary>댓글 수정</summary>

![업무 댓글 수정](https://github.com/user-attachments/assets/b81b0b78-224e-481d-acfa-a00ed5b0072c)
</details>
<details><summary>답글</summary>

![업무 댓글 삭제](https://github.com/user-attachments/assets/0f550598-c4b9-49aa-b7e0-b75184af6416)
</details>
<details><summary>댓글 삭제</summary>

![업무 댓글 삭제](https://github.com/user-attachments/assets/0f550598-c4b9-49aa-b7e0-b75184af6416)
</details>

<details><summary>참여자 목록 조회 및 권한변경</summary>

![참여자 목록 조회 및 권한 변경](https://github.com/user-attachments/assets/92d99b85-a4eb-4bbd-bc30-efd90251afd7)
</details>
</details>


<details><summary><b>개인 드라이브</b></summary>

<details><summary>개인 드라이브 아이템 목록 조회</summary>

![아이템 조회 방식 변경](https://github.com/user-attachments/assets/5ad37abd-ec88-46cf-9b5d-a6e42e812a05)
</details>
<details><summary>폴더 생성</summary>

![폴더 생성](https://github.com/user-attachments/assets/dac4e5e2-5368-4237-8050-fd5ff3a185ac)
</details>
<details><summary>공유문서 생성 / 폴더 트리구조 조회</summary>

![폴더 트리 구조 조회 및 공유문서 생성](https://github.com/user-attachments/assets/0cf765e6-1be7-44c0-b709-d59971e04c89)
</details>
<details><summary>파일 업로드</summary>

![파일 업로드](https://github.com/user-attachments/assets/c2e8419b-6add-4115-a2b0-ea01fd642d4e)
</details>
<details><summary>폴더 및 아이템 이동</summary>

![폴더 및 아이템 이동](https://github.com/user-attachments/assets/0bca4ab9-4cd5-436e-bf79-fec3039c283e)
</details>
<details><summary>폴더 순서 변경</summary>

![폴더 순서 변경](https://github.com/user-attachments/assets/2ade1e20-cfb0-4cf4-a5b1-fdaeccf43dad)
</details>
<details><summary>공유문서 편집</summary>

![공유문서 편집(실시간 동시 편집X)](https://github.com/user-attachments/assets/3ce47c1e-971a-415c-87e9-5d7580a4d57e)
</details>
<details><summary>개인 공유문서 → 프로젝트 이동</summary>

![공유문서 프로젝트로 이동](https://github.com/user-attachments/assets/eb0f0971-e3cc-4776-9b71-d8a0dd9d489b)
</details>
</details>


<details><summary><b>프로젝트 드라이브</b></summary>

<details><summary>폴더 생성</summary>

![폴더 생성](https://github.com/user-attachments/assets/39619de1-6cb4-4f60-896e-a14062eca9cb)
</details>
<details><summary>공유문서 생성</summary>

![공유문서 만들기](https://github.com/user-attachments/assets/96da4909-73cb-46a8-bd64-4d681d82ab48)
</details>
<details><summary>파일 업로드</summary>

![파일 업로드](https://github.com/user-attachments/assets/35be7be3-dd98-41cc-a0e9-7ffc4cf40e82)
</details>

<details><summary>파일 다운로드</summary>

![파일 다운로드](https://github.com/user-attachments/assets/886d2bf9-c9f1-40cf-912e-d0fe64b007af)
</details>
<details><summary>폴더 이름 변경</summary>

![폴더 이름 변경](https://github.com/user-attachments/assets/c44b5149-d7fe-49c3-b173-818e0581eae1)
</details>

<details><summary>공유문서 실시간 편집</summary>

![공유문서 실시간 동시 편집](https://github.com/user-attachments/assets/de4e4679-1348-4b24-9cbb-8f8de7a20f69)
</details>
<details><summary>공유문서 참여자 목록 조회</summary>

![참여자 목록 조회](https://github.com/user-attachments/assets/b2e211ec-8c4b-4547-80e5-b5d3969e9745)
</details>
<details><summary>공유문서 잠금/해제</summary>

![공유문서 잠금](https://github.com/user-attachments/assets/9d8f23cd-d429-4338-8099-0fe5fcb79f60)
</details>
<details><summary>공유문서 다운로드</summary>

![공유문서 다운로드](https://github.com/user-attachments/assets/95943b7e-d1fb-489f-b9e0-56ac878d6eb1)
</details>
<details><summary>문서 이름 변경</summary>

![문서 이름 변경](https://github.com/user-attachments/assets/84ea2274-0992-40d0-8f26-a8d50415c1fd)
</details>

</details>


<details><summary><b>1:1 채팅</b></summary>

<details><summary>메시지 전송 및 타이핑 이벤트</summary>

![Image](https://github.com/user-attachments/assets/81c8bb6c-ca6a-4d6b-b2ea-e96ac5b70d97)

</details>
<details><summary>첨부파일 개수 제한</summary>

![Image](https://github.com/user-attachments/assets/27583118-b267-437b-9d33-bb336bb90bf6)

</details>
<details><summary>첨부파일 업로드</summary>

![Image](https://github.com/user-attachments/assets/3c5b12a7-8214-442a-8dac-827f69823b67)

</details>
<details><summary>1:1 사용자 정보 조회</summary>

![Image](https://github.com/user-attachments/assets/a93cd371-7c58-498f-b9b7-ca61b7f39689)

</details>
<details><summary>메시지 삭제</summary>

![Image](https://github.com/user-attachments/assets/d7aebe46-f39d-4d50-9f12-d6459872b108)

</details>
<details><summary>이전 메시지 조회</summary>

![Image](https://github.com/user-attachments/assets/7b1a65e1-2fe7-46ce-a0dc-e5b3bef7f6a3)

</details>
<details><summary>마지막 읽은 메시지 업데이트 및 조회</summary>

![Image](https://github.com/user-attachments/assets/90598f69-cb8b-47a9-94ea-f3183b8f27e5)

</details>
<details><summary>채팅 나가기</summary>

![Image](https://github.com/user-attachments/assets/b00e8add-c16f-485b-98e7-641a46fbc579)

</details>
</details>


<details><summary><b>프로젝트 채팅</b></summary>
<details><summary>메시지 전송 및 타이핑 이벤트</summary>

![Image](https://github.com/user-attachments/assets/5b3bed84-c609-42d1-9869-1b6351d59832)
  
</details>
<details><summary>첨부파일 업로드</summary>

![Image](https://github.com/user-attachments/assets/97ae1c25-c281-413d-b92b-ef8db6f9a1db)

</details>
<details><summary>메시지 삭제</summary>

![Image](https://github.com/user-attachments/assets/f449c01d-dee4-4c4a-a847-897d82355013)

</details>
<details><summary>멘션</summary>
  
  ![Image](https://github.com/user-attachments/assets/7e61243d-acf2-4b2a-abd5-89256f4f360e)
  
</details>
<details><summary>이전 메시지 조회</summary>

![Image](https://github.com/user-attachments/assets/c5e6cf76-f2ba-4b12-a731-df12b8b44ad9)

</details>
<details><summary>마지막 읽은 메시지 업데이트 및 조회</summary>

![Image](https://github.com/user-attachments/assets/41df6411-989e-41bc-b732-17e2f1747bdc)

</details>
<details><summary>권한 변경</summary>
  
  ![Image](https://github.com/user-attachments/assets/6b8318ad-dbd0-430f-977e-d8fdcce4f3ec)
  
</details>
<details><summary>채널 추가</summary>

![Image](https://github.com/user-attachments/assets/16ee7e09-ade4-4685-99f3-7f6c82c9f569)

</details>
</details>


<details><summary><b>화상회의</b></summary>

<details><summary>채널 권한 설정</summary>
  <img src="https://github.com/user-attachments/assets/ab6830af-0606-448c-8ebd-05310a7f2ab9" alt="GIF 설명" />

</details>
<details><summary>화상회의 방 생성</summary>
  <img src="https://github.com/user-attachments/assets/75816971-c3cc-4860-84e6-a32271dada0d" alt="GIF 설명" />

</details>
<details><summary>화상회의 방 참여</summary>
  <img src="https://github.com/user-attachments/assets/a1c40779-3b3a-4934-9d12-4d2676581847" alt="GIF 설명" />

</details>

<details><summary>채팅 전송</summary>
  <img src="https://github.com/user-attachments/assets/dd92de8c-ac1f-49fb-a9b5-b3ede501b3b4" alt="GIF 설명" />

</details>
<details><summary>채팅 목록 조회</summary>
  <img src="https://github.com/user-attachments/assets/92b3992d-21fc-4ad4-8dab-a9041657010f" alt="GIF 설명" />

</details>
<details><summary>녹화 시작</summary>
  <img src="https://github.com/user-attachments/assets/9939ad79-46c6-4f35-b6c9-31394f9146c3" alt="GIF 설명" />

</details>
<details><summary>진행중인 회의 목록</summary>
<img src="https://github.com/user-attachments/assets/2f514eaf-204b-4bf9-8a39-353a9a20476c" alt="GIF 설명" />

</details>
<details><summary>종료된 회의 목록</summary>
  <img src="https://github.com/user-attachments/assets/9b446b5c-8f20-4f9d-b6e9-d06ec3c9812f" alt="GIF 설명" />

</details>
<details><summary>특정 회의 요약 조회</summary>
  <img src="https://github.com/user-attachments/assets/e19f3cea-47c4-4012-918c-3b44389c6e6a" alt="GIF 설명" />

</details>
<details><summary>녹화 영상 다운로드</summary>
  <img src="https://github.com/user-attachments/assets/74e61275-668e-4332-8b9f-e39943246a3e" alt="GIF 설명" />

</details>

</details>



<details><summary><b>검색</b></summary>
  
<details><summary>일정 검색</summary>

![01b0a2e0e0d978d8](https://github.com/user-attachments/assets/5d5396fe-cae3-4eaf-96e4-11d985090ef5)
</details>
<details><summary>드라이브 검색</summary>

![f7751259e917165e](https://github.com/user-attachments/assets/290aa867-b4eb-43ba-8e3d-8df244f98615)
</details>
</details>



<details><summary><b>알림</b></summary>

<details><summary>알림 목록 조회</summary>

![알림 목록 조회](https://github.com/user-attachments/assets/7e5e7166-f06a-47d8-8932-741e52f3caaa)

</details>

<details><summary>특정 알림 클릭시 이동</summary>

![알림 라우팅](https://github.com/user-attachments/assets/ff8c2264-190d-4e78-a749-5319175b19ce)
</details>
<details><summary>알림 타입별 조회</summary>

![알림 타입별 조회](https://github.com/user-attachments/assets/5c98d007-1bf2-4ae6-9f74-c3a9b6cd0633)
</details>
<details><summary>단건 읽음/삭제 처리</summary>

![단건 읽음_삭제](https://github.com/user-attachments/assets/49916203-2c01-4a36-b647-d0313178c295)
</details>
<details><summary>모두 읽음/삭제 처리</summary>

![모두읽음_삭제](https://github.com/user-attachments/assets/233381a6-3f7a-4a89-9ccf-623487ac03e1)
</details>

</details>

---
## 🛠️ 트러블 슈팅

<details> 
  <summary><b> 김건동</b></summary>

  <details>
    <summary><b>화상회의 - ICE Participant Connection Error</b></summary>

  **증상**

  - 화상회의 방에 참가하려고 할 때 브라우저 콘솔에 "ICE participant connection failed" 에러 발생
  - 참가자가 LiveKit 서버에 연결되지 않음
  - WebRTC 연결이 실패하여 비디오/오디오 스트림이 전송되지 않음
  - LiveKit 웹훅 이벤트가 서버로 전달되지 않음

    **원인 분석**

    1. LiveKit 서버가 웹훅 URL을 외부에서 접근 가능한 주소로 설정해야 하는데, 개발 환경에서는 localhost로만 실행 중
    2. LiveKit 서버가 웹훅 이벤트를 전송하려 하지만, 외부에서 접근 불가능한 주소로 인해 연결 실패
    3. ICE (Interactive Connectivity Establishment) 프로세스가 완료되지 않아 WebRTC 연결 실패

    **해결 과정**

    1. **로컬 웹훅 서버 실행**: Spring Boot 애플리케이션을 localhost에서 실행하여 웹훅 엔드포인트 준비

    2. **ngrok 설치 및 실행**:

       ```bash
       ngrok http 8080
       ```

       - ngrok이 제공하는 공개 URL 획득 (예: `https://xxxx-xxx-xxx.ngrok.io`)

    3. **LiveKit 서버 설정**: LiveKit 서버의 `config.yaml`에 웹훅 URL 설정

       ```yaml
       webhook:
         urls:
           - https://xxxx-xxx-xxx.ngrok.io/task-service/livekit/webhook
       ```

    4. **웹훅 검증**: LiveKit 서버가 ngrok URL을 통해 웹훅 이벤트를 성공적으로 전송하는지 확인

    5. **재연결 테스트**: 화상회의 방 참가 시 정상적으로 연결되는지 확인

    **결과**

    - ngrok을 통한 터널링으로 외부에서 웹훅 이벤트 수신 가능
    - ICE 연결 성공 및 WebRTC 스트림 정상 전송
    - 참가자 입장/퇴장 이벤트가 정상적으로 서버에 전달됨

  </details>

  <details>
    <summary><b>화상회의 - 중복 참가 방지</b></summary>

  **증상**

  - 한 사용자가 여러 화상회의 방에 동시에 참가할 수 있음
  - 같은 사용자가 여러 방에 중복으로 참가자로 등록됨
  - 데이터베이스에 중복된 `RoomParticipant` 레코드 생성

    **원인 분석**

    - 방 참가 시 이미 다른 방에 참가 중인지 확인하는 로직이 없음
    - `joinRoom()` 메서드에서 기존 참가 여부를 체크하지 않음

    **해결 과정**

    1. **기존 참가자 조회 로직 추가**:

       ```java
       List<RoomParticipant> activeParticipants = 
           participantRepository.findByVirtualMeetingChannelMember_MemberSeqAndLeftAtIsNull(memberSeq);
       ```

    2. **중복 참가 체크**:

       ```java
       if(!activeParticipants.isEmpty()){
           throw new IllegalStateException("이미 참여중인 화상회의 방이 있습니다.");
       }
       ```

    3. **방 생성 시에도 동일 로직 적용**: `createImmediateRoom()` 메서드에도 동일한 체크 로직 추가

    **결과**

    - 한 사용자가 동시에 하나의 방에만 참가 가능
    - 중복 참가 시도 시 명확한 에러 메시지 반환
    - 데이터 무결성 보장

  </details>

  <details>
    <summary><b>문서 편집 - 동시 편집 충돌</b></summary>

  **증상**

  - 여러 사용자가 같은 라인을 동시에 편집할 때 마지막에 저장한 내용만 남음
  - 한 사용자의 편집 내용이 다른 사용자의 편집으로 덮어씌워짐
  - 데이터 손실 발생

    **원인 분석**

    - 라인 단위로 편집 제어 메커니즘이 없음
    - 동시 편집 시 충돌을 방지하는 락(Lock) 시스템이 없음
    - CRDT를 사용하지 않고 단순 DB 업데이트 방식 사용

    **해결 과정**

    1. **라인 락 메커니즘 설계**:

       - 사용자가 라인 편집 시작 시 Redis에 락 정보 저장
       - 락 키 형식: `document:locks:{documentId}:{lineId}`
       - 락 정보: `userId`, `userName`, `timestamp`

    2. **락 설정 로직 구현**:

       ```java
       public void publishLineLockToRedis(EditorMessageDto lockDto) {
           String lockKey = LINE_LOCKS_KEY + lockDto.getDocumentId() + ":" + lockDto.getLineId();
           Map<String, String> lockInfo = new HashMap<>();
           lockInfo.put("userId", lockDto.getUserId().toString());
           lockInfo.put("userName", lockDto.getUserName());
           lockInfo.put("timestamp", String.valueOf(System.currentTimeMillis()));
           
           documentOnlineUsersTemplate.opsForValue().set(
               lockKey, 
               lockValue, 
               LINE_LOCK_TTL_SECONDS, 
               TimeUnit.SECONDS
           );
       }
       ```

    3. **TTL 기반 자동 해제**:

       - 락에 30초 TTL 설정 (`LINE_LOCK_TTL_SECONDS = 30`)
       - 비정상 종료 시(브라우저 종료, 네트워크 끊김 등) 자동으로 락 해제
       - Redis Key 만료 이벤트 리스너 구현:

       ```java
       @EventListener
       public void handleKeyExpiration(RedisKeyExpiredEvent<String> event) {
           String expiredKey = event.getKey();
           if (expiredKey.startsWith("document:locks:")) {
               // 자동 UNLOCK 브로드캐스트
           }
       }
       ```

    4. **STOMP를 통한 실시간 락 상태 전달**:

       - 락 설정/해제 시 Redis Pub/Sub으로 다른 서버 인스턴스에 브로드캐스트
       - STOMP를 통해 같은 문서를 보고 있는 모든 클라이언트에 락 상태 전달
       - 프론트엔드에서 락된 라인을 시각적으로 표시 (회색 처리 등)

    5. **락 해제 로직**:

       - 편집 종료 시 명시적으로 락 해제
       - 락을 건 사용자만 해제 가능하도록 권한 체크

    **결과**

    - 동시 편집 시 충돌 없이 각 라인을 안전하게 편집 가능
    - 비정상 종료 시에도 30초 후 자동으로 락 해제되어 다른 사용자가 편집 가능
    - 실시간으로 락 상태가 모든 사용자에게 표시됨

  </details>

  <details>
    <summary><b>문서 편집 - 멀티 인스턴스 동기화</b></summary>

  **증상**

  - 서버가 여러 인스턴스로 실행될 때, 한 인스턴스에서 편집한 내용이 다른 인스턴스에 반영되지 않음
  - 사용자 A가 인스턴스 1에서 편집 → 사용자 B가 인스턴스 2에서 같은 문서를 보고 있어도 변경사항이 보이지 않음
  - STOMP 메시지가 같은 인스턴스 내의 클라이언트에게만 전달됨

    **원인 분석**

    - STOMP는 기본적으로 단일 서버 인스턴스 내에서만 메시지 브로드캐스트
    - 여러 서버 인스턴스 간 메시지 동기화 메커니즘 없음
    - 각 인스턴스가 독립적으로 동작하여 변경사항이 공유되지 않음

    **해결 과정**

    1. **Redis Pub/Sub 도입**:

       - Redis를 메시지 브로커로 사용하여 인스턴스 간 통신
       - 각 인스턴스가 Redis 채널을 구독하여 다른 인스턴스의 메시지 수신

    2. **메시지 발행 로직**:

       ```java
       public void publishDocumentUpdateToRedis(EditorMessageDto messageDto) {
           String channel = TOPIC_PREFIX + messageDto.getDocumentId() + SUFFIX_DOCUMENT_UPDATE;
           String message = objectMapper.writeValueAsString(messageDto);
           documentPubSubTemplate.convertAndSend(channel, message);
       }
       ```

    3. **메시지 수신 및 재브로드캐스트**:

       ```java
       @Override
       public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {
           String channel = new String(message.getChannel());
           String body = new String(message.getBody());
           
           // Redis에서 받은 메시지를 STOMP 클라이언트들에게 전달
           EditorMessageDto dto = objectMapper.readValue(body, EditorMessageDto.class);
           messagingTemplate.convertAndSend(
               TOPIC_PREFIX + dto.getDocumentId() + SUFFIX_DOCUMENT_UPDATE, 
               dto
           );
       }
       ```

    4. **채널 구독 설정**:

       ```java
       container.addMessageListener(messageListenerAdapter, new PatternTopic("/topic/document/*"));
       ```

    **결과**

    - 여러 서버 인스턴스에서도 실시간으로 편집 내용이 동기화됨
    - 모든 사용자가 동일한 문서 상태를 실시간으로 확인 가능
    - 수평 확장(Scale Out) 가능한 아키텍처 구성

  </details>

  <details>
    <summary><b>문서 편집 - 라인 순서 관리</b></summary>

  **증상**

  - 라인을 삽입하거나 삭제할 때 순서가 꼬임
  - 라인 간 연결 관계가 깨져서 문서 구조가 망가짐
  - 라인 삭제 시 다음 라인들이 사라지거나 순서가 뒤바뀜

    **원인 분석**

    - 라인을 배열 인덱스로 관리하려고 시도
    - 라인 삽입/삭제 시 모든 라인의 인덱스를 재정렬해야 하는 문제
    - 동시 편집 시 인덱스 충돌 발생

    **해결 과정**

    1. **연결 리스트 방식 채택**:

       - 각 라인에 `prevId` 필드를 두어 이전 라인과의 연결 관계 표현
       - 배열 인덱스 대신 `prevId`로 순서 관리

    2. **라인 삽입 로직**:

       ```java
       public void createDocumentLine(EditorMessageDto message) {
           // 중간에 끼어들어갈 경우 순서 바꿔주기
           Optional<DocumentLine> existingLine = 
               documentLineRepository.findByPrevId(message.getPrevLineId());
           existingLine.ifPresent(line -> line.updatePrevId(message.getLineId()));
           
           // 새 라인 생성
           DocumentLine newDocumentLine = DocumentLine.builder()
               .prevId(message.getPrevLineId())
               .document(document)
               .lineId(message.getLineId())
               .documentContent(message.getContent())
               .build();
       }
       ```

    3. **라인 삭제 로직**:

       ```java
       public void deleteDocumentLines(EditorMessageDto message) {
           for (EditorMessageDto.LineChange change : message.getChanges()) {
               // 뒷 라인이 있다면 앞단과 연결 시켜주기
               Optional<DocumentLine> documentLine = 
                   documentLineRepository.findByPrevId(change.getLineId());
               documentLine.ifPresent(line -> 
                   line.updatePrevId(change.getPrevLineId())
               );
               
               // 현재 라인 삭제
               documentLineRepository.delete(...);
           }
       }
       ```

    4. **문서 조회 시 순서 정렬**:

       - `prevId`를 따라가며 연결 리스트 순회
       - 프론트엔드에서 순서대로 렌더링

    **결과**

    - 라인 삽입/삭제 시에도 순서가 정확하게 유지됨
    - 동시 편집 시에도 라인 순서 충돌 없음
    - 문서 구조가 안정적으로 관리됨

  </details>

  <details>
    <summary><b>모니터링 - PVC 바인딩 실패</b></summary>

  **증상**

  - Grafana와 Prometheus Pod가 `Pending` 상태로 유지됨
  - `kubectl describe pod` 명령 시 "unbound immediate PersistentVolumeClaims" 에러
  - PVC가 생성되었지만 실제 볼륨에 바인딩되지 않음

    **원인 분석**

    1. `values-monitoring.yml`에서 `storageClassName`을 명시하지 않음
    2. EKS 클러스터에 `gp2` StorageClass가 있지만, PVC가 이를 참조하지 않음
    3. 기본 StorageClass가 없거나 설정되지 않음

    **해결 과정**

    1. **StorageClass 확인**:

       ```bash
       kubectl get storageclass
       ```

       - `gp2` StorageClass가 존재하는지 확인

    2. **values-monitoring.yml 수정**:

       ```yaml
       grafana:
         persistence:
           enabled: true
           size: 10Gi
           storageClassName: gp2  # 명시적으로 추가
       
       prometheus:
         prometheusSpec:
           storageSpec:
             volumeClaimTemplate:
               spec:
                 accessModes: ["ReadWriteOnce"]
                 storageClassName: gp2  # 명시적으로 추가
                 resources:
                   requests:
                     storage: 20Gi
       ```

    3. **기존 PVC 삭제 및 재생성**:

       ```bash
       # 기존 PVC 삭제
       kubectl delete pvc -n monitoring monitoring-grafana
       kubectl delete pvc -n monitoring monitoring-prometheus-prometheus-0
       
       # Helm 업그레이드
       helm upgrade --install monitoring prometheus-community/kube-prometheus-stack \
         -n monitoring \
         -f k8s/values-monitoring.yml
       ```

    4. **PVC 바인딩 확인**:

       ```bash
       kubectl get pvc -n monitoring
       kubectl get pods -n monitoring
       ```

    **결과**

    - PVC가 `gp2` StorageClass를 사용하여 EBS 볼륨에 정상 바인딩됨
    - Grafana와 Prometheus Pod가 `Running` 상태로 전환
    - 데이터 영속성 보장

  </details>

  <details>
    <summary><b>모니터링 - OIDC Provider 누락</b></summary>

  **증상**

  - AWS Load Balancer Controller Pod가 정상 동작하지 않음
  - Ingress 리소스를 생성해도 ALB가 생성되지 않음
  - Controller 로그에 "InvalidIdentityToken: No OpenIDConnect provider found" 에러

    **원인 분석**

    1. EKS 클러스터의 OIDC Provider가 IAM에 등록되지 않음
    2. AWS Load Balancer Controller가 IAM Role을 사용하려 하지만, OIDC Provider가 없어서 인증 실패
    3. IRSA (IAM Roles for Service Accounts) 설정이 완료되지 않음

    **해결 과정**

    1. **EKS 클러스터 OIDC Issuer URL 확인**:

       ```bash
       aws eks describe-cluster --name <CLUSTER_NAME> --query "cluster.identity.oidc.issuer" --output text
       ```

       - 예: `https://oidc.eks.ap-northeast-2.amazonaws.com/id/BA2425E8EFC00C321C0D312B330F7F78`

    2. **OIDC Provider 등록**:

       ```bash
       aws iam create-open-id-connect-provider \
         --url https://oidc.eks.ap-northeast-2.amazonaws.com/id/BA2425E8EFC00C321C0D312B330F7F78 \
         --client-id-list sts.amazonaws.com \
         --thumbprint-list 9e99a48a9960b14926bb7f3b02e22da2b0ab7280
       ```

    3. **IAM Policy 생성 및 Role 연결**:

       ```bash
       # IAM Policy 다운로드
       curl -o iam-policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/main/docs/install/iam_policy.json
       
       # Policy 생성
       aws iam create-policy \
         --policy-name AWSLoadBalancerControllerIAMPolicy \
         --policy-document file://iam-policy.json
       
       # ServiceAccount에 Role 연결 (eksctl 사용 또는 수동 설정)
       ```

    4. **Controller 재시작**:

       ```bash
       kubectl rollout restart deployment -n kube-system aws-load-balancer-controller
       ```

    **결과**

    - OIDC Provider가 정상 등록되어 IAM 인증 가능
    - AWS Load Balancer Controller가 정상 동작
    - Ingress 리소스 생성 시 ALB가 자동으로 생성됨

  </details>

  <details>
    <summary><b>모니터링 - ServiceMonitor 라벨 오류</b></summary>

  **증상**

  - ServiceMonitor를 적용했지만 Prometheus가 타겟을 발견하지 못함
  - `kubectl apply` 시 "Invalid value: \"/actuator/prometheus\"" 에러 발생
  - Kubernetes 라벨에 `/` 문자를 포함할 수 없다는 오류

    **원인 분석**

    1. Service 리소스에 `prometheus.io/path: "/actuator/prometheus"` 라벨을 직접 추가하려고 시도
    2. Kubernetes 라벨은 RFC 1123 규칙을 따라야 하며, `/` 문자를 포함할 수 없음
    3. 라벨 값에 허용되지 않는 문자가 포함됨

    **해결 과정**

    1. **Service 리소스에서 잘못된 라벨 제거**:

       - `prometheus.io/path` 라벨을 Service에서 제거

    2. **ServiceMonitor에서 직접 경로 지정**:

       ```yaml
       apiVersion: monitoring.coreos.com/v1
       kind: ServiceMonitor
       metadata:
         name: springboot-services
         namespace: monitoring
       spec:
         selector:
           matchLabels:
             app.kubernetes.io/name: springboot
         endpoints:
           - port: http
             path: /actuator/prometheus  # ServiceMonitor에서 직접 경로 지정
             interval: 30s
       ```

    3. **Service에 올바른 라벨만 추가**:

       ```bash
       kubectl label svc -n synco-namespace task-service app.kubernetes.io/name=springboot
       kubectl label svc -n synco-namespace chat-service app.kubernetes.io/name=springboot
       # ... 다른 서비스들도 동일하게
       ```

    4. **Prometheus 타겟 확인**:

       ```bash
       # Prometheus UI에서 Status > Targets 확인
       # 또는
       kubectl port-forward -n monitoring svc/monitoring-kube-prometheus-prometheus 9090:9090
       ```

    **결과**

    - ServiceMonitor가 정상적으로 적용됨
    - Prometheus가 Spring Boot 서비스의 `/actuator/prometheus` 엔드포인트를 정상적으로 스크랩
    - 모든 서비스의 메트릭이 수집됨

  </details>

  <details>
    <summary><b>최종 배포 - ALB Ingress 생성 실패</b></summary>

  **증상**

  - Ingress 리소스를 생성했지만 ALB가 생성되지 않음
  - `kubectl get ingress` 시 ADDRESS가 비어있음
  - AWS Load Balancer Controller 로그에 에러 메시지

    **원인 분석**

    1. AWS Load Balancer Controller가 설치되지 않음
    2. OIDC Provider가 IAM에 등록되지 않음
    3. IAM Role/Policy가 올바르게 설정되지 않음
    4. ServiceAccount에 IAM Role이 연결되지 않음

    **해결 과정**

    1. **AWS Load Balancer Controller 설치**:

       ```bash
       # Helm repo 추가
       helm repo add eks https://aws.github.io/eks-charts
       helm repo update
       
       # Controller 설치
       helm upgrade --install aws-load-balancer-controller eks/aws-load-balancer-controller \
         -n kube-system \
         --set clusterName=<CLUSTER_NAME> \
         --set region=ap-northeast-2 \
         --set vpcId=<VPC_ID> \
         --set serviceAccount.create=false \
         --set serviceAccount.name=aws-load-balancer-controller
       ```

    2. **OIDC Provider 등록** (위의 "OIDC Provider 누락" 섹션 참조)

    3. **IAM Policy 및 Role 설정**:

       ```bash
       # IAM Policy 생성
       aws iam create-policy \
         --policy-name AWSLoadBalancerControllerIAMPolicy \
         --policy-document file://iam-policy.json
       
       # Trust Policy 생성 (OIDC 기반)
       # ServiceAccount에 Role 연결
       ```

    4. **Ingress 리소스 확인**:

       ```yaml
       apiVersion: networking.k8s.io/v1
       kind: Ingress
       metadata:
         name: monitoring-grafana
         namespace: monitoring
         annotations:
           alb.ingress.kubernetes.io/scheme: internet-facing
           alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS":443}]'
           alb.ingress.kubernetes.io/target-type: ip
           alb.ingress.kubernetes.io/certificate-arn: <ACM_CERT_ARN>
       spec:
         ingressClassName: alb
         rules:
           - host: grafana.synco1.shop
             http:
               paths:
                 - path: /
                   pathType: Prefix
                   backend:
                     service:
                       name: monitoring-grafana
                       port:
                         number: 80
       ```

    5. **Controller 로그 확인**:

       ```bash
       kubectl logs -n kube-system deploy/aws-load-balancer-controller
       ```

    **결과**

    - Ingress 리소스 생성 시 ALB가 자동으로 생성됨
    - ALB DNS 이름이 Ingress의 ADDRESS에 표시됨
    - 외부에서 서비스 접근 가능

  </details>

  <details>
    <summary><b>최종 배포 - Kafka Zookeeper 의존성</b></summary>

  **증상**

  - Kafka를 실행하려고 하면 Zookeeper가 필요하다는 에러 발생
  - 기존 Kafka 설정이 Zookeeper에 의존적
  - Zookeeper를 별도로 설치하고 관리해야 하는 부담

    **원인 분석**

    - Kafka 2.8 이전 버전은 Zookeeper가 필수
    - Zookeeper는 별도의 클러스터 관리가 필요하여 복잡도 증가
    - 단일 노드 환경에서는 Zookeeper가 불필요한 오버헤드

    **해결 과정**

    1. **Kafka KRaft 모드 채택**:

       - Kafka 3.0+ 버전에서 지원하는 Zookeeper 없는 모드
       - KRaft (Kafka Raft)는 Kafka 자체의 메타데이터 관리 시스템

    2. **Kafka Deployment 설정**:

       ```yaml
       apiVersion: apps/v1
       kind: Deployment
       metadata:
         name: kafka
       spec:
         template:
           spec:
             containers:
             - name: kafka
               image: apache/kafka:3.7.0
               env:
               # KRaft 모드 설정
               - name: KAFKA_NODE_ID
                 value: "1"
               - name: KAFKA_PROCESS_ROLES
                 value: "broker,controller"  # broker와 controller 역할 모두 수행
               - name: KAFKA_LISTENERS
                 value: "PLAINTEXT://:9092,CONTROLLER://:9093"
               - name: KAFKA_ADVERTISED_LISTENERS
                 value: "PLAINTEXT://kafka-service:9092"
               - name: KAFKA_CONTROLLER_LISTENER_NAMES
                 value: "CONTROLLER"
               - name: KAFKA_CONTROLLER_QUORUM_VOTERS
                 value: "1@kafka-service:9093"  # 단일 노드이므로 자기 자신
               - name: KAFKA_LOG_DIRS
                 value: "/tmp/kraft-combined-logs"
               - name: KAFKA_AUTO_CREATE_TOPICS_ENABLE
                 value: "true"
       ```

    3. **초기 포맷팅** (최초 실행 시):

       ```bash
       kubectl exec -it kafka-pod -- kafka-storage.sh format \
         -t <CLUSTER_ID> \
         -c /opt/kafka/config/kraft/server.properties
       ```

    4. **애플리케이션 설정 확인**:

       - Spring Boot의 `application-prod.yml`에서 `bootstrap-servers: kafka-service:9092` 설정 확인
       - Consumer/Producer 설정이 정상 동작하는지 확인

    **결과**

    - Zookeeper 없이 Kafka 단일 노드 실행 성공
    - 메타데이터 관리가 Kafka 내부에서 처리되어 간소화
    - 프로덕션 환경에서는 여러 노드로 확장 가능 (주의: 여러 노드 구성 시 `KAFKA_CONTROLLER_QUORUM_VOTERS` 수정 필요)

  </details>

  <details>
    <summary><b>최종 배포 - ACM 인증서 검증 실패</b></summary>

  **증상**

  - Ingress에 ACM 인증서 ARN을 설정했지만 에러 발생
  - "Certificate ARN is not valid" 에러
  - ALB가 생성되지 않거나 HTTPS 연결 실패

    **원인 분석**

    1. ACM 인증서가 아직 발급되지 않았거나 검증이 완료되지 않음
    2. 인증서 ARN에 잘못된 값이 입력됨 (예: placeholder `<CERT_ID>` 그대로 사용)
    3. 인증서가 다른 리전에 있거나 다른 계정에 속함
    4. DNS 검증 레코드가 Route53에 추가되지 않음

    **해결 과정**

    1. **ACM 인증서 확인**:

       ```bash
       aws acm list-certificates --region ap-northeast-2
       ```

       - 발급된 인증서 목록 확인
       - 인증서 상태가 "Issued"인지 확인

    2. **인증서 상세 정보 확인**:

       ```bash
       aws acm describe-certificate \
         --certificate-arn <CERT_ARN> \
         --region ap-northeast-2
       ```

       - 인증서 상태, 도메인, 검증 상태 확인

    3. **DNS 검증 레코드 추가** (인증서가 "Pending validation" 상태인 경우):

       - ACM에서 제공하는 CNAME 레코드를 Route53에 추가

       ```bash
       aws route53 change-resource-record-sets \
         --hosted-zone-id <ZONE_ID> \
         --change-batch file://dns-validation.json
       ```

    4. **새 인증서 발급** (필요한 경우):

       ```bash
       aws acm request-certificate \
         --domain-name "*.synco1.shop" \
         --validation-method DNS \
         --region ap-northeast-2
       ```

    5. **values-monitoring.yml에 올바른 ARN 설정**:

       ```yaml
       grafana:
         ingress:
           annotations:
             alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:ap-northeast-2:864981729633:certificate/594ebe5e-b2af-4dac-8108-05ef826a282a
       ```

    6. **Ingress 재적용**:

       ```bash
       helm upgrade --install monitoring prometheus-community/kube-prometheus-stack \
         -n monitoring \
         -f k8s/values-monitoring.yml
       ```

    **결과**

    - ACM 인증서가 정상적으로 인식됨
    - ALB에 HTTPS 리스너가 정상적으로 생성됨
    - 브라우저에서 HTTPS로 접근 시 정상적으로 인증서 표시

  </details>

  <details>
    <summary><b>최종 배포 - 서비스 간 통신 실패</b></summary>

  **증상**

  - Kubernetes 내부에서 서비스 간 HTTP 호출이 실패함
  - `Connection refused` 또는 `Name resolution failed` 에러
  - 한 서비스가 다른 서비스의 엔드포인트를 찾지 못함

    **원인 분석**

    1. Service 리소스의 `selector`와 Pod의 `labels`가 일치하지 않음
    2. 잘못된 네임스페이스에서 서비스를 찾으려고 시도
    3. DNS 이름 형식이 잘못됨
    4. Service가 생성되지 않았거나 Pod가 Ready 상태가 아님

    **해결 과정**

    1. **Service와 Pod 라벨 확인**:

       ```bash
       # Service의 selector 확인
       kubectl get svc task-service -n synco-namespace -o yaml | grep selector
       
       # Pod의 labels 확인
       kubectl get pods -n synco-namespace -l app=task-service --show-labels
       ```

       - `selector.app`과 Pod의 `app` 라벨이 일치해야 함

    2. **네임스페이스 확인**:

       - 모든 서비스가 `synco-namespace`에 있는지 확인
       - 애플리케이션 설정에서 서비스 이름이 올바른지 확인:

       ```yaml
       spring:
         kafka:
           bootstrap-servers: kafka-service:9092  # 네임스페이스가 같으면 서비스 이름만으로 가능
         redis:
           host: redis-service  # 네임스페이스가 같으면 서비스 이름만으로 가능
       ```

    3. **DNS 이름 형식 확인**:

       - 같은 네임스페이스: `service-name:port`
       - 다른 네임스페이스: `service-name.namespace.svc.cluster.local:port`
       - 예: `kafka-service.synco-namespace.svc.cluster.local:9092`

    4. **Service 엔드포인트 확인**:

       ```bash
       kubectl get endpoints -n synco-namespace
       ```

       - Service에 Pod IP가 등록되어 있는지 확인
       - 엔드포인트가 비어있으면 selector와 labels 불일치

    5. **Pod 상태 확인**:

       ```bash
       kubectl get pods -n synco-namespace
       ```

       - Pod가 `Running` 상태이고 `Ready`가 `1/1`인지 확인

    6. **네트워크 정책 확인** (NetworkPolicy가 있는 경우):

       - NetworkPolicy가 서비스 간 통신을 차단하지 않는지 확인

    **결과**

    - 서비스 간 통신이 정상적으로 동작
    - DNS 이름으로 서비스를 찾을 수 있음
    - 마이크로서비스 간 API 호출이 성공

  </details>

</details>
<details> 
  <summary><b> 권수연</b></summary>
</details>
<details> 
  <summary><b> 김찬진</b></summary>
  <details>
  <summary><b>한글 검색 누락 (Elasticsearch Nori 커스터마이저 도입)</b></summary>

  **증상**
  
  - “회의록공유”, “프로젝트현황”처럼 붙여 쓴 키워드를 검색하면 결과가 비어 있음
  - 사용자들이 실제로 입력하는 한글 키워드가 제대로 분해되지 않아 관련 문서가 검색되지 않음
  - 검색 실패 CS가 반복되고, 팀 내부 테스트에서도 동일 현상 재현

  **원인 분석**
  
  - Elasticsearch 기본 분석기는 한글 복합어를 분해하지 못해 하나의 토큰으로만 색인
  - 형태소 분석기를 적용하지 않아 “회의록”으로 검색해도 “회의록공유” 문서를 찾지 못하는 구조
  - Task·Chat·Drive·Meeting 인덱스 모두 동일한 analyzer를 쓰고 있어 전 영역에서 문제가 발생

  **해결 과정**
  
  1. Elasticsearch 인덱스 설정에 Nori tokenizer 도입
  2. `decompound_mode=mixed`로 설정해 원형+분해 형태를 동시에 색인
  3. 품사 필터(nori_part_of_speech), Reading Form 필터 등을 추가해 조사/표기 흔들림 보정
  4. 커스텀 analyzer (nori_synco_custom)를 생성하고 각 인덱스 매핑에 적용
  5. 기존 데이터 재색인 및 Kibana/통합 검색 API로 재현 테스트

  **결과**
  
  - “회의록공유”, “회의록 공유” 등 다양한 입력 형태에서 안정적으로 검색 가능
  - 검색 실패 건수가 크게 감소하고 사용자 만족도 향상
  - Task·Chat·Drive·Meeting 전반에서 일관된 형태소 분석 결과 확보

</details>
</details>
<details> 
  <summary><b> 김지현</b></summary>
    <details>
  <summary><b>[채팅 안정화] STOMP 중복 연결</b></summary>

**증상**
  - 새로고침 없이 라우터 이동 후 다시 돌아오면 `connect session ID` 로그가 계속 증가  
  - 브라우저 콘솔에도 WebSocket 연결 로그가 중복 출력

**원인 분석**
  - `Chat.vue`의 `onUnmounted` 훅에서 이벤트 버스·스크롤 리스너는 해제했지만, STOMP 구독을 해제하는 `disconnectWebsocket()` 호출이 누락되어 기존 연결이 살아 있음

**해결 방법**
  - 언마운트 시점에 `disconnectWebsocket()`을 명시적으로 호출해 현재 구독과 STOMP 세션을 정리하고, 구독 객체와 클라이언트를 `null`로 초기화하도록 변경
  - 이렇게 하면 컴포넌트가 다시 마운트될 때 새 연결만 생성되고, 이전 세션이 중복으로 남지 않음

  ```diff
  onUnmounted(() => {
    emitter.off("select-chat-channel", handleSubChannelSelect);
    window.removeEventListener("click", closeContextMenu);
+   disconnectWebsocket();          // STOMP 구독 및 세션 정리
  });
  ```

**결과**
  - 라우터 이동 후에도 불필요한 STOMP 세션이 남지 않아 로그가 안정화  
  - 서버와 브라우저 모두에서 연결 개수 정상 유지
</details>


<details>
<summary><b>[파일 전송 안정화] STOMP 프레임 한계</b></summary>

 **증상**  
- 이미지·PDF 등을 채팅으로 보내면 상대방 화면에는 빈 메시지 또는 [파일]만 표시되고 실제 파일은 내려오지 않음

**원인 분석**  
- 초기에 첨부 파일을 Base64로 변환해 STOMP 메시지 본문에 실어 보냈음
- STOMP/WebSocket이 텍스트 프레임 기반이다 보니 브라우저 메모리, 네트워크, Redis 모두 부담이 커짐

**해결 방법** 
- `Chat.vue`의 전송 흐름을 수정해, 메시지를 보내기 전에 `uploadFilesToS3()`로 REST API(S3 업로드)를 선행 실행
- 업로드가 성공하면 반환된 URL만 STOMP 메시지의 `chatMessageFileUrls` 필드에 넣어 전송
- `ChatService.saveMessage()` 역시 URL 문자열만 저장하고 Redis 브로드캐스트에 올려, **실시간 메시지와 첨부 파일을 분리**

**결과**  
- STOMP 메시지 크기가 수십 KB 이하로 유지되어 브라우저/서버/Redis 모두 안정화
- 대용량 파일도 S3 업로드만 성공하면 즉시 채팅에 반영되고, 상대방도 같은 URL로 파일을 다운로드 가능
- 실시간 메시지와 첨부 파일이 역할분담을 하면서 UX는 그대로 유지되고 장애는 크게 줄어듦
</details>
</details>
<details> 
  <summary><b> 윤수오</b></summary>
  <details>
    <summary><b>Redis 장애시 DB FallBack</b></summary>
    
  **증상**

  - Redis 먹통 또는 Redis 조회 이상시 서버가 바로 멈춰버리는 현상

  **원인 분석**
  
  - WorkSpace정보, 워크스페이스에 소속되어 있는 멤버 정보등을 Redis에서 조회해옴
  - WorkSpace 정보는 DB에서 바로 조회가 가능하나, 소속되어 있는 멤버 정보들은 WorkSpace DB에 생성자 정보만 저장하기 때문에 실패.

  **해결 과정**
  
  1. AOP를 활용한 RedisFallBack 서비스를 WorkSpace 모듈에 만들기
  2. ```@RedisFallBack``` 어노테이션을 FallBack이 필요한 메서드에 붙임.
  3. 컨트롤러가 어노테이션이 붙은 메서드 호출할 경우 바로 RedisFallBack 서비스를 호출함
  4. RedisFallBack 서비스에서 Redis 조회 결과 이상이 발생시 메서드 명에 따라 분기, 해당하는 정보를 TaskModule에 Feign 통신 요청.
  5. 요청 받은 TaskModule은 DB에서 조회 후 해당 결과를 다시 WorkSpaceModule에 반환하여 정보를 가져옴.

  **결과**
  
  - Redis 장애 발생시 DB로 신속하게 FallBack
  - Redis 조회가 이상하더라도 DB에서 똑같은 정보를 안전하게 가져옴
  </details>
  <details>
    <summary><b>SSE 알림 전송시 DB Connection Full 문제 해결</b></summary>

  **증상**
  
  - SSE 알림 전송시 여러 클라이언트가 SSE 연결 요청을 해 DB Connection이 고갈되어 DB ConnectionPool 문제 발생
  - 기본값인 10개 ConnectionPool이 모두 꽉 차게되어 아무런 API 호출이 되지 않는 문제 발생
  
  **원인 분석**

  - 알림 서비스 트랜잭션이랑 SSE 연결이랑 분리되지 않아 SSE로 알림 전송시 DB Connection을 사용
  - DB Connection을 사용하고 반환해줘야하는데 반환하지 않아 계속 기존 Pool을 사용하면서 새로운 Pool을 가져가서 고갈되는 문제 발생
  - 트랜잭션이 끝났을때 DB Connection을 반납하지 않음
  - HTTP Connection이 열려있는 동안 DB Connection도 같이 열려있어 Connection 고갈 문제 발생
  
  **해결 과정**
  
  1. ```Open-In-View```를 false로 설정해 트랜잭션이 끝나면 DB Connection을 반납하도록 설정
  ```
  jpa:
    open-in-view: false
  ```
  2. 알림 트랜잭션 서비스와 SSE 서비스를 분리.
  
  **결과**
  
  - DB ConnectionPool 문제 해결 및 원할하게 알림 전송됨
  </details>
  <details>
    <summary><b>SSE 알림 전송시 연결 끊어지는 문제 해결</b></summary>
    
  **증상**
  
  - SSE 연결이 지속적으로 끊어짐
  - 연결 끊어짐으로 인해 알림 실시간 전송이 안되는 문제 발생
  
  **원인 분석**
  
  - SSE Emitter와 Client의 EventSource-Pool의 Connection 시간이 너무 짧음
  - 서버에서 주기적으로 연결 신호를 보내줘야하는데 그렇지 않고 있음.
  
  **해결 과정**
  
  1. SSE Emitter와 클라이언트의 SSE Connection 유효기간을 10일로 길게 설정.
  2. 스케줄러를 이용해 15초 간격으로 클라이언트에 ping 신호를 주기적으로 보내서 연결이 끊기지 않게끔 설정

  **결과**
  
  - SSE 연결이 안정적으로 연결되어 실시간 알림이 원할하게 작동됨
  </details>
</details>

---

## 📝 프로젝트 회고

| 팀원 | 회고 내용 |
|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 김건동 | 이번 부트캠프에서 처음으로 SaaS를 제대로 다뤄 보았다. 기업 도메인을 바탕으로 주제를 여러 번 전환·재기획하는 과정을 거치며 쉽지 않았지만, 그만큼 새로운 도전의 재미를 느꼈다.<br><br>아키텍처는 모놀리식이 아닌 MSA로 설계했다. ERD 초안부터 시작해 “어떤 기능을 어떤 모듈로 분리할지”, “반정규화는 어디까지 허용할지”, “Redis는 어떤 범위까지 캐시·세션·큐에 쓸지”를 팀원들과 끝까지 토론하며 결정했다. 서로의 의견을 부딪히고 조율하는 과정에서 데이터 모델링과 모듈 경계 설정에 대한 감각이 확실히 좋아졌다.<br><br>협업 문서 편집은 원래 CRDT를 검토했지만, 일정 내 안정성을 우선해 락 기반 접근으로 대체했다. 화상회의는 OpenVidu를 참고하며 LiveKit을 채택했고, FastAPI와 NAVER Summary API를 연동해 LiveKit 웹훅으로 회의 녹화 이벤트를 수신→전사/요약까지 자동화 파이프라인을 구성했다. 이 과정에서 비동기 이벤트 처리, 스트림 처리, 권한·보안 키 관리 등 백엔드 심화 주제를 몸으로 익혔다.<br><br>배포는 쿠버네티스로 프론트·백엔드를 올리고, Prometheus + Grafana로 리소스/요청 지표를 모니터링해 운영 관점의 가시성을 확보했다. 실서비스에 가까운 CI/CD와 관측성까지 끌어올리면서 프로젝트의 완성도를 한 단계 높였다. 이후에는 Electron 기반 PC 앱과 Flutter 앱으로 확장해 멀티 플랫폼 경험을 제공하는 로드맵을 그리고 있다.<br><br>무엇보다 큰 규모의 팀 프로젝트를 팀장으로 이끌며 의사소통의 중요성을 다시 확인했다. 요구사항 변경, 버그·로직 이슈, 우선순위 충돌 등 다양한 난관이 있었지만, 투명한 공유와 빠른 피드백 사이클 덕분에 끝까지 밀어붙일 수 있었다. 갈등이 없어서가 아니라, 갈등을 관리하는 방식을 함께 배웠다는 점이 가장 큰 성과다.<br><br>이번 프로젝트로 얻은 핵심 배움은 다음과 같다.<br><br>• 기능보다 경계(모듈·데이터 소유권) 를 먼저 설계할 것<br>• 이상적인 기술보다 일정 안에서 가장 신뢰할 수 있는 선택을 할 것<br>• 운영·모니터링을 포함해 개발→배포→관측까지 끝단을 책임질 것<br>• 팀장은 답을 내리는 사람이 아니라, 결정을 가능하게 만드는 사람일 것<br><br>다음 스프린트에서는 문서 편집의 CRDT 재도입, 검색·지식화 고도화, 클라이언트 앱 배포 자동화, 그리고 운영지표 기반의 성능 튜닝을 단계적으로 추진할 계획이다. 이번 경험은 단순 결과물이 아니라, 제품을 끝까지 책임지는 방식을 체득한 시간이었다. |
| 권수연 |  |
| 김찬진 |  |
| 김지현 | 이번 프로젝트를 통해 WebSocket과 STOMP 기반의 실시간 통신 구조를 직접 설계하고 구현하며, 발송 → 구독 → 세션 관리 단계의 전체 흐름을 체계적으로 익힐 수 있었다. 또한 Redis Pub/Sub을 적용해 여러 서버 간 메시지를 동기화하는 과정을 직접 다뤄보며, 분산 환경에서의 데이터 일관성을 경험했다.<br>다만, MSA 아키텍처를 적용하면서 Gateway 라우팅, 인증 헤더 전달 등 인프라 설정에서 예기치 않은 오류를 자주 겪었다. 예를 들어 Gateway가 /chat-service/connect/info 요청을 프록시하지 않아 WebSocket 연결이 끊기거나, 헤더에 Bearer가 중복되어 인증이 실패하는 문제 등이 있었다.<br>앞으로는 이런 분산 환경에서 라우팅 흐름과 설정 의존성을 명확히 분리해, 인프라 변경이 서비스 로직에 영향을 주지 않도록 구조를 개선해 나갈 계획이다.<br>협업툴을 개발하면서 실제로 synco와 같은 자원관리와 소통이 결합된 협업툴의 필요성을 실감했다. 그런 서비스를 능력있는 팀원들과 함께 만들 수 있어서 감사한 시간이었다. |
| 윤수오 |  이번 부트캠프에서 직접 OpenFeign을 사용해 MSA 설계방식하의 통신 방법을 배우고 구현하였으며, 이를 통해 MSA 구조를 이해할 수 있었다.<br><br> 또한 Redis 장애시 FallBack 기능, SSE 알림 전송시 DB ConnectionPool 문제 등을 직면하고 이를 직접 해결하여 문제 해결 역량 및 성능 개선 역량도 키울 수 있었다.<br><br> 이번 프로젝트를 통해 의사소통의 중요성을 깨달았으며, 무엇보다 Git 활용의 중요성을 뼈저리게 느꼈다. 다만 개발 일정의 촉박함으로 인해 알림 시스템 개발시 kafka를 도입하지 못해 Redis Pub/Sub으로 이벤트를 발행하는것은 아쉬웠다. 이번 대규모 프로젝트를 진행하면서 실무적으로 성장했다고 느껴졌던 시간이었다.<br><br> 팀장을 비롯한 팀원들에게 모두 고생많았다고 이야기 하고 싶다. |


