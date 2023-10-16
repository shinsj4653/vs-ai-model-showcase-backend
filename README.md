﻿# VISANG AI Model Showcase
## 비상교육 AI 수학 지식 추적 모델 시연 웹 사이트
> 학생의 수학 문제 풀이 데이터를 기반으로 학생이 어느 정도의 난이도의 문제를 해결할 수 있는지에 대한 지식 수준 산출 결과와 해당 학생에게 적합한 문항을 추천해주는 로직을 진단평가 -> 학습준비 -> 형성평가 순으로 보여주는 모델 시연 웹사이트

![image](https://github.com/shinsj4653/vs-ai-model-showcase-backend/assets/49470452/79399a88-24ae-416a-8a6f-67cb67a6c17a)

접속 URL : http://13.209.224.148/main/  

## 목차
- [사용 기술 스택](#사용-기술-스택)
- [ERD](#erd)
- [수학 지식 추적 모델](#수학-지식-추적-모델)
- [모델에 사용된 이론 및 트리톤을 활용한 모델 서빙](#모델에-사용된-이론-및-트리톤을-활용한-모델-서빙)
- [TOKT 모델의 지식 추천 기반 코스웨어](#TOKT-모델의-지식-추천-기반-코스웨어)
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
- `AI Model Serve` : Nvidia Triton Server

## ERD
![image](https://github.com/shinsj4653/vs-ai-model-showcase-backend/assets/49470452/319a9d68-9fb7-4b4b-bafb-ec695b3b3512)

## 수학 지식 추적 모델
### 지식추적
지식추적이란, `학습자의 숙달정도를 모델링하는 것이고 배운 것을 재현할 수 있는지 판단하는 것`이다.  
지식추적에는 2가지 기법 존재
- 지식간 독립 기법
  - 지식이 다른 지식에 영향을 미치지 않는 것
- `지식간 의존 기법`
  - 맞고 틀림의 여부로 식을 만들고, N개의 데이터로 N + 1 의 맞춤 여부를 예측
 
### 지식 수준 모델
GKT 모델의 구조를 변형하여 모델학습에 활용하였음  
결과에 대한 해설을 용이하게 하기 위해 `지식수준을 1~9 사이의 실수값으로 표현`  
만약 지식수준이 5.3이라면, 문항 난이도가 6 이상인 문항은 풀어서 맞추기 어렵다는 뜻
```py
# 추론기능 입출력 데이터 예시

batch_size = 2 # 입력 배치사이즈
-> 시연때는 1로 고정
-> 즉 data는 [ [1, 1, 0, ...] ] 형태로 입력

seq_len = 10 # 입력 시퀀스 길이 -> 문항 수
num_q = 317 # 토픽 수

# 입력 데이터
# INPUT__0: 문항번호
# INPUT__1: 정답여부
# INPUT__2: 난이도

{
    "inputs": [
        {
            "name": "INPUT__0",
            "datatype": "INT64",
            "shape": [batch_size, seq_len],
            "data": [[0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
                     [10, 11, 12, 13, 14, 15, 16, 17, 18, 19]]
        },
        {
            "name": "INPUT__1",
            "datatype": "INT64",
            "shape": [batch_size, seq_len],
            "data": [[0,0,1,0, 1,0,0,1,0,0],
                     [0,0,1,0, 1,0,0,1,0,0]]
        },
        {
            "name": "INPUT__2",
            "datatype": "INT64",
            "shape": [batch_size, seq_len],
            "data": [[2, 4, 3, 3, 2, 4, 5, 5, 5, 5],
                     [3, 2, 1, 3, 4, 8, 1, 9, 2, 7]]
        }
    ]
}
# 출력 데이터
# OUTPUT__0: 문항별 마지막 시퀀스의 정답확률
-> 각 토픽에 대한 지식수준(확률) 값
{
    "outputs": [
        {
            "name": "OUTPUT__0",
            "datatype": "FP32",
            "shape": [batch_size, num_q],
            "data": [[0.1, ... 0.5],
                     [0.1, ... 0.5]]
        },
    ]
}
```
 - 입력
   - 토픽 인덱스
   - 정오답 여부
   - 문항 난이도
 - 출력
   - 모든 토픽 인덱스에 대한 지식수준 실수값 반환 (1~9 사이)

### 문항 추천 모델
```py
##추천기능 입출력 예시
# url : http://10.214.2.33:8000/v2/models/gkt_reco/infer
# batch_size 는 한번에 추천할 입력시퀀스 개수, 시연시에는 1로 고정
# seq_len 은 입력 시퀀스 길이, 15개로
# recommend_num 은 추천할 문항 수 (출력결과는 추천할 문항 수 만큼 나옴)


batch_size = 2
seq_len = 10
recommend_num = 5
# 입력 데이터
# INPUT__0, INPUT__1, INPUT__2 은 위의 추론기능과 동일
# INPUT__3 은 추천옵션으로 옵션은 5개의 정수로 구성되어 있음
# 옵션은 [target, depth, search_len, difficulty, recommend_num] 으로 구성

# target 인덱스에 대해 가장 확률을 높이는 문항을 추천 0~316


# depth 는 추천할 문항을 선정할때 지식맵 깊이 (1이상) default=5, 몇번까지 연관되는지
# search_len 은 추천할 문항을 선정할때 문항을 연속으로 맞춘 횟수 (1이상) default=1, 1로 고정
# difficulty 는 추천할 문항의 난이도 (1~9) default=5, 5로 고정
# recommend_num 은 추천할 문항의 수 (1이상) default=5, 첫번쨰 값으로 배치마다 통일

{
    "inputs": [
        {
            "name": "INPUT__0",
            "datatype": "INT64",
            "shape": [batch_size, seq_len],
            "data": [[0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
                     [10, 11, 12, 13, 14, 15, 16, 17, 18, 19]]
        },
        {
            "name": "INPUT__1",
            "datatype": "INT64",
            "shape": [batch_size, seq_len],
            "data": [[0,0,1,0, 1,0,0,1,0,0],
                     [0,0,1,0, 1,0,0,1,0,0]]
        },
        {
            "name": "INPUT__2",
            "datatype": "INT64",
            "shape": [batch_size, seq_len],
            "data": [[2, 4, 3, 3, 2, 4, 5, 5, 5, 5],
                     [3, 2, 1, 3, 4, 8, 1, 9, 2, 7]]
        },
        {
            "name": "INPUT__3",
            "datatype": "INT64",
            "shape": [batch_size, 5], # seq_len은 5로 고정
            "data": [[1, 2, 3, 4, 5],
                     [1, 2, 3, 4, 5]]
        }
    ]
}
# 출력 데이터
{
    "outputs": [
        {
            "name": "OUTPUT__0", # output1을 풀었을떄 타겟의 지식수준이 바뀐 후의 확률(output0)
            "datatype": "FP32",
            "shape": [batch_size, recommend_num],
            "data": [[4.4, 4.5, 5.3, 5.4, 6.5],
                      [3.1, 5.2, 4.3, 5.4, 6.5]]
        },
        {
            "name": "OUTPUT__1",
            "datatype": "INT64",
            "shape": [batch_size, recommend_num],
            "data": [[0, 1, 2, 3, 4],
                     [0, 1, 2, 3, 4]] // 학습준비 토픽에 해당
        }
    ]
}
```
진단평가 단계 이후에 쌓인 `문제 풀이 이력들 및 타겟 토픽`을 입력값으로 줄 시, `타겟 토픽의 지식수준을 올려줄 추천 연관 토픽들`과 `해당 토픽들의 문제를 풀었을 때 바뀔 지식 수준 값을 반환`해줌
 
## 모델에 사용된 이론 및 트리톤을 활용한 모델 서빙
- `Deep KT(DKT)` : n번째 문제 직전의 지식 상태에서 n번째 문제 정오답을 입력하고 임베딩된 딥러닝 모델을 거치면 n번째 문제 직후의 지식 상태를 획득하여 활용하는 방식
- 수학 문제 추천 및 지식 수준 도출 -> DKT 변형 방식인 `GKT(Graph-Based Knowledge Tracing)` 기법을 사용
- 지식 간 연관된 정도를 나타내는 `지식맵(Knowledge Graph)`을 활용하여 만약 문제를 맞추면 연관된 지식들의 수준이 함께 향상됨
- 실제 맞춤여부를 확인하는 과정을 무수히 반복하면서 모델을 학습 -> 모든 학생의 데이터로 학습시키려면 많은 시간이 소요되므로, `타겟 학생과 문제 토픽(주제)를 지정하여 학습` 시키는 `목표 지향 지식 추적` 기법을 사용 
- 비상교육 만의 `TOKT(Target Oriented Knowledge Tracing) 모델`의 추론 결과를 웹에서 볼 수 있는 형태로 가공하기 위해 `NVIDIA Triton 서버`를 사용하여 HTTP 요청을 주고 받을 수 있도록 세팅

![image](https://github.com/shinsj4653/vs-ai-model-showcase-backend/assets/49470452/62e82424-670d-4986-9acd-67211dac521e)

*Triton의 아키텍쳐*  
트리톤으로 모델 서빙을 위해 다음 절차가 필요하다.  
1. 서버를 세팅
- 저희 부서 같은 경우 AWS EC2 인스턴스에 세팅함. GPU 타입의 인스턴스에서 머신러닝 작업을 하기 위해 필요한 `AMI` 를 설치함
2. Model Repository 생성 후, 서빙할 모델 파일들을 이곳에 저장
3. Nvidia Cloud에서 제공하는 Triton 서버 이미지를 서버에서 `docker run` 을 함
4. Triton 서버를 launch 한다.
5. Triton 서버에 추론 요청을 보

참고링크 - https://peaceatlast.tistory.com/25  

## TOKT 모델의 지식 추천 기반 코스웨어

1. 진단평가

- 학생의 이미 푼 문제 기록들을 기반으로 향후 얼마나 잘 풀지 예측
- 진단평가 이후, `지식 수준 모델`을 통해 타겟 토픽의 `지식 수준`(앞으로 배울 토픽의 예상 지식 수준) 산출

2. 학습준비

- `진단평가 문항 이력 및 타겟 토픽`을 가지고 `문항 추론 모델` 사용
- 학습준비에 필요한 토픽 획득 -> 각 토픽에 해당하는 문제를 가지고 학습준비 진행 

3. 형성평가

- 학생의 타겟 토픽에 해당하는 문제 5개를 가지고 형성평가 진행
- 각 문제를 풀었을 경우, 변화되는 `지식 수준` 값과 정오답 여부를 기준으로 새로운 문항 추천
- 형성평가 완료 시, 변화된 지식 수준 값을 기반으로 `실수로 틀린 문항과 점검이 필요한 문항` 보여줌

## 나의 주요 구현 기능

### 1. CI/CD 환경 구축
- Github 프로젝트 배포를 위해 `Github Actions, EC2, S3, 그리고 CodeDeploy 설정` 진행

- `AppSepc 파일 및 배포 스크립트`을 통한 배포 워크플로우 관리

![image](https://github.com/shinsj4653/vs-ai-model-showcase-backend/assets/49470452/c8c32930-57fc-4835-971f-cf4cf8e1c042)  
*CodeDeploy 설정 이후 발생한 UnknownError*

- 백엔드 배포 스크립트 작성 후, 배포 도중 `UnknownError 에러` 발생
-> 배포 서버의 CodeDeploy 로그를 확인해보면서 해결하기로 함

- CodeDeploy의 역할이 인스턴스에 제대로 부여되어 있지 않은 것을 확인
-> 역할 부여 후, CodeDeploy Agent를 재시작함으로써 에러 해결 완료

### 2. 진단평가
> 학생의 타겟 토픽의 지식 수준 도출 및 대시보드 생성

https://github.com/shinsj4653/vs-ai-model-showcase-backend/assets/49470452/cb371140-6039-443f-b88f-41b74f64252e  

*진단평가 진행 후, 지식 수준 결과 기반 대시보드 생성*
- 진단평가를 진행하면서 학생의 타켓 토픽에 해당하는 문제 15개 풀이

- 기존 DB에 저장된 85개의 문항 데이터 이력 + 진단평가에서 푼 15개 문항 
-> 총 100개의 문항에 대한 `토픽, 난이도, 정오답 여부를 지식 수준 도출 모델의 입력 값으로 전달`

- 모델의 출력 값으로 `모든 토픽 및 타겟 토픽에 해당하는 지식 수준값` 획득
-> 해당 값을 통해 진단평가 결과 대시보드 화면에 필요한 API 개발

### 3. 학습준비
> 학생의 문제 풀이 이력 및 타겟 토픽에 맞는 문항 추천

https://github.com/shinsj4653/vs-ai-model-showcase-backend/assets/49470452/f8c9150a-ac5b-4d3a-acfa-f69933f1f145  

*진단평가 결과 기반으로 문항 추천 모델의 결과를 받은 후, 학습준비 진행*  
- 진단평가 완료 시, 학생의 타겟 토픽에 해당하는 지식 수준이 4.0 을 못 넘길 경우엔, 학습준비 단계 진행

- `지식 수준 4.0 이하 -> 난이도 4 짜리 문항을 풀 지 못한다`는 의미

- `진단평가 문제 풀이 이력과 타겟 토픽, 추천할 문항의 난이도를 문항 추천 모델의 입력 값으로 전달`
-> 출력 값으로 `추천 토픽 인덱스 5개 획득`하여 각각의 토픽에 해당하는 문항 1개씩을 모아 `총 5문항을 학습준비 문항으로 사용`

### 4. 형성평가
> 문항의 난이도 및 정오답 결과에 따른 문항 추천

https://github.com/shinsj4653/vs-ai-model-showcase-backend/assets/49470452/ec6a93e1-5cfa-490f-b584-83de119d13ca  

*형성평가 후, 최종 지식 수준을 기반으로 실수로 틀린 문항 및 점검이 필요한 문항 결과 도출*
- 학생의 타겟 토픽에 해당하는 문항 5개를 가지고 형성평가 진행

- 형성평가의 문항을 풀 때마다, `해당 문항을 맞았을 때 현재 지식 수준을 기준으로 높은 난이도의 문항 추천`
-> 그 반대의 경우, 낮은 난이도의 문항 반환

- 형성평가 완료 시, 변화된 지식 수준값을 기반으로 `실수로 틀린 문항과 점검이 필요한 문항` 보여주기
-> 학습 후 변화된 지식 수준이 2.44 인데 난이도 3을 맞췄으면 찍어서 맞췄을 확률이 높음
-> 점검이 필요한 문항으로 지정
## 참고 사항
- 회사 프로젝트의 접근 권한은 private이기 때문에, 제 리포지토리에 보이도록 하기 위해 `main 브랜치만 가져온 상태`입니다.
- 제 레포에서는 Github Actions의 Deploy 실패 문구가 보이지만, `실제 현업에서는 정상작동` 하고 있습니다.

![image](https://github.com/shinsj4653/vs-ai-model-showcase-backend/assets/49470452/cab21a04-d007-4844-8c60-f87d1be0b811)

*현업에서 사용되었던, 혹은 사용중인 브랜치명 목록들*  

![image](https://github.com/shinsj4653/vs-ai-model-showcase-backend/assets/49470452/a7142502-80a0-4461-a356-8fd43851e66c)

*정상 작동한 Github Actions의 Workflows 이력*

