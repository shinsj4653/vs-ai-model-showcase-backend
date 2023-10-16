# VISANG AI Model Showcase
## 비상교육 AI 수학 지식 추적 모델 시연 웹 사이트
> 학생의 수학 문제 풀이 데이터를 기반으로 학생이 어느 정도의 난이도의 문제를 해결할 수 있는지에 대한 지식 수준 산출 결과와 해당 학생에게 적합한 문항을 추천해주는 로직을 진단평가 -> 학습준비 -> 형성평가 순으로 보여주는 모델 시연 웹사이트

![image](https://github.com/shinsj4653/vs-ai-model-showcase-backend/assets/49470452/79399a88-24ae-416a-8a6f-67cb67a6c17a)



접속 URL : http://13.209.224.148/main/  

## 목차
- [사용 기술 스택](#사용-기술-스택)
- [ERD](#erd)
- [나의 주요 구현 기능](#나의-주요-구현-기능)
  * [1. CI/CD 환경 구축](#1-CI/CD-환경-구축)
  * [2. 진단평가](#2-진단평가)
  * [3. 학습준비](#3-학습준비)
  * [4. 형성평가](#4-형성평가)

## 사용 기술 스택
- `Language` : Java 11, JUnit 4
- `Framework` : SpringBoot 2.7.15, MyBatis
- `Database` : PostgreSQL 42.5.0, AWS RDS
- `Deploy` : Github Actions, AWS S3 & CodeDeploy
- `JWT` : Json Web Token

## ERD
![image](https://github.com/shinsj4653/vs-ai-model-showcase-backend/assets/49470452/319a9d68-9fb7-4b4b-bafb-ec695b3b3512)

## 나의 주요 구현 기능

### 1. CI/CD 환경 구축


### 2. 진단평가


### 3. 학습준비


### 4. 형성평가



## 참고 사항
- 회사 프로젝트의 접근 권한은 private이기 때문에, 제 리포지토리에 보이도록 하기 위해 `main 브랜치만 가져온 상태`입니다.
- 제 레포에서는 Github Actions의 Deploy 실패 문구가 보이지만, `실제 현업에서는 정상작동` 하고 있습니다.

![image](https://github.com/shinsj4653/vs-data-service-backend/assets/49470452/787fcdc3-686f-4363-9066-adcf37970793)
*현업에서 사용되었던, 혹은 사용중인 브랜치명 목록들*  



![image](https://github.com/shinsj4653/vs-data-service-backend/assets/49470452/811ab1df-f5c6-4c97-9fd3-b5f73935c673)
*정상 작동한 Github Actions의 Workflows 이력*

