# 🌱 싹틔움

개발기간 : 2024.10.21 ~ 2024.11.22

# 🌳 서비스/프로젝트 소개
## 🌱싹틔움 ***-* 키우고, 나누고, 함께하다!**

바쁜 일상 속에서 식물과 함께 소소한 행복과 정서적 안정을 느껴보세요.
"싹틔움"은 식물을 키우며 얻는 즐거움을 나누고,
식물에 대한 정보를 공유할 수 있는 플랫폼입니다.

초보 가드너부터 숙련된 식물 애호가까지 모두가 함께할 수 있는 커뮤니티로,
당신의 식물 이야기를 들려주고 다른 사람들과 교감하며 성장해 보세요.

🌱 **주요 기능**

- **식물 정보 제공**: 각종 식물의 관리법, 식물 정보 제공.
- **소통과 공유**: 키우고 있는 식물 사진과 성장 이야기를 다른 사용자들과 공유.
- **커뮤니티 참여**: 식물 관리 꿀팁부터 고민 상담까지 활발한 소통의 장.

**"싹틔움"과 함께 식물을 키우는 즐거움과 따뜻한 연결을 느껴보세요!**



## 🌻 적용 기술
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![SpringBoot](https://img.shields.io/badge/springboot-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![Jpa](https://img.shields.io/badge/jpa-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-000?style=for-the-badge&logo=apachekafka)
![Elasticsearch](https://img.shields.io/badge/elasticsearch-%230377CC.svg?style=for-the-badge&logo=elasticsearch&logoColor=white)
![SpringSecurity](https://img.shields.io/badge/spring_security-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)
![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![Git](https://img.shields.io/badge/git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=Prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/grafana-%23F46800.svg?style=for-the-badge&logo=grafana&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white)

## 🍀 Key-Summary
<details>
  <summary>대용량 알림 발생 시 동기/비동기 처리 방식에 대한 의사결정</summary>
  
  ### 배경

- 알림 서버에서 대용량 알림 데이터(100만 건)이 발생을 가정.
    
    이를 클라이언트에 전달 시, 카프카 레코드를 DB에 저장 후 클라이언트에 SSE를 통해 알림을 전달하는데, 저장 방식의 처리를 동기와 비동기 중에 어떤 방식이 더 적합한지 판단이 필요하였음.
    

### **시나리오**

- **환경:
알림서버 :** CPU(Apple M1 Pro 10Core), RAM(32GB)
Docker(Kafka, Kafka-Ui, Zookeeper) : CPU(10 Core), RAM(7.48GB)
- **조건:** 100만 명 팔로워를 가진 유저가 게시판 등록 API호출 발생하는 알림 100만개 수신을 가정하여 테스트 진행.

### **테스트 결과**

- **동기/비동기 알림 저장 소요시간**
    
    ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2Fac38c314-8fa8-4b9c-b174-638f77950388%2Fimage.png?table=block&id=b76bf6a4-4c9e-41ee-b4b2-fc4d65c70442&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=2000&userId=&cache=v2)
    
    | 회차 | 동기 처리 | 비동기 처리 |
    | --- | --- | --- |
    | 1 | 414s | 191s |
    | 2 | 411s | 193s |
    | 3 | 415s | 190s |
    | 4 | 420s | 200s |
    | 5 | 418s | 194s |
    
    **평균 소요 시간**:
    
    - 동기 처리: **415.6초**
    - 비동기 처리: **193.6초**
    
    결과 정리
    
    - 비동기 처리 방식이 동기 처리 방식에 비해 2.14배 빠른 속도로 처리가 가능하였으나, 
    **여전히 193.6초라는 긴 시간이 소요됨.**
    - **Side Impact : 비동기 처리 방식을 선택하였을 때, 카프카에서 수신한 알림을 먼저 유저에게 보여주고 이후에 DB에 저장하는 순서로 로직이 구성되어 모든 사용자의 알림이 DB에 저장되기 전에
    알림메시지의 상태변경 API를 요청하게 되면 API 요청 대기시간이 발생함.**

### **결론**

- 비동기 처리 방식은 동기처리 대비 2.14배 빠른 처리속도를 보였으나, 여전히 큰 소요시간이라고 판단.
- 클라이언트의 메시지 수신시점과 DB 저장시점의 시간차이로 인해, 클라이언트의 알림 상태 변경확인 요청API의 대기시간이 발생하여, 유저경험에 악영향을 끼칠 것으로 판단.
- 위 2가지 판단을 근거로 동기처리 방식을 선택함.
  
</details>
<details>
  <summary>동의어 처리로 정확한 검색 결과 제공</summary>
  
## 배경

Spring에서 지원하는 @Query 기능의 한계로 인해 사용자의 오타 등으로 검색을 시도했을 경우 동의어,유사어에 대한 결과를 제공할 수 없는 한계를 극복하기 위해,

Elasticsearch를 도입하여 Elasticsearch에서 지원하는 기능 중 하나인 Synonym 필터를 사용하여,
 사용자의 실수(오타)나 잘 모르는 식물 이름에 대한 부정확한 정보를 입력했을 경우,

 설정한 analyzer 를 통해 동의어, 유사어에 해당하는 결과를 제공하는 것을 목표로 함.

## **시나리오**

**조건** : 같은 오타값을 입력했다는 가정에서 JPQL 과 Elasticsearch의 반환되는 결과값의 비교
          -. “장미”를 검색하고 싶었던 사용자는 오타로 인해 “당미”로 검색을 했음.

## **테스트 결과**

  **@Query 기능 적용 후 검색 테스트 결과**  
    ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F9a926e33-eec4-4182-bfe7-7fcffb1a125e%2Fimage.png?table=block&id=82b351bd-98a9-415b-9dde-38a4e4e4e8ae&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1300&userId=&cache=v2)
    ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F1ca67b50-edf6-46cb-8028-c9f9fc4a8095%2Fimage.png?table=block&id=2830becc-89fd-4535-aedb-6b80aff120be&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1060&userId=&cache=v2)
    
    
  **결과** : Spring에서 JPQL를 사용해 구현한 검색 기능에는 동의어 검색할 수 있는 기능이 없어 원하는 결과를 얻지 못함.


    
  **Elasticsearch를** Synonym 필터 적용 후 **검색 테스트 결과**
    
   ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F70e9b427-c843-43b4-b366-298b1251ea53%2Fimage.png?table=block&id=36a0ce23-82d2-4126-8baa-5f75bbf3b4b1&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1300&userId=&cache=v2)
  ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F40b41edc-70d4-4f44-96d8-5d9c3d899e3b%2Fimage.png?table=block&id=607848d4-0f34-4e7a-a022-f2e0bc075a40&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1300&userId=&cache=v2)
  ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F19a219d8-7c17-4312-a62a-00b6764c4f26%2Fimage.png?table=block&id=815adcc1-a031-413c-9a2c-f3588ef1b565&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=950&userId=&cache=v2)
    
   **결과** : Elasticsearch를 도입하여 설정한 analyzer를 통해 오타를 입력하더라도 목표한 결과값인 “장미”에 대한 검색 결과가 도출됨.
    

## 결론

- Elasticsearch의 **Synonym 필터**를 활용한 검색 기능 개선을 통해, **오타**나 **유사어**에 대한 처리가 가능해졌다.
- 서비스 확장성 면에서 많은 게시글이 생겼을 때, 검색 성능 최적화 부분에서도 Elasticsearch 의 역색인으로 기존 @Query 대비 86% 성능 개선을 이뤘다.
- 동의어검색, 성능 최적화로 검색 정확도 향상, 사용자 경험 개선 면에서 Elasticsearch를 도입을 결정

</details>
<details>
  <summary>쿠폰 발급 동시성 문제 해결을 위한 의사결정</summary>
  
  ## **💡 배경**

쿠폰 300 개를 유저 1000 명이 5초동안 발급을 받기 위해 테스트를 진행했는데, 중복된 쿠폰이 발급된다든지, 수량 300 개를 넘어서서 발급이 되는 문제가 발생했다.

이를 해결하기 위해서 락을 걸어 동시성 문제를 해결할 필요가 있었다.

 <details>
   <summary>python script 를 활용해 총 발급된 쿠폰 수와 중복된 쿠폰 코드를 확인</summary>
   
  ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F063572b5-81ad-472c-9291-bdde127868e1%2Fimage.png?table=block&id=a895cffa-ac61-4cec-956b-65024a31b1ae&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=960&userId=&cache=v2)

  
   ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F24088e01-be27-45d4-b0f2-33e5674b509b%2Fimage.png?table=block&id=c868c397-5e08-4860-842b-d840efa001a4&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=400&userId=&cache=v2)
  </details>
  <details>
    <summary>📝 Test : 락을 적용하지 않은 상태의 동시성 문제 확인`→ 채택 불가`*</summary>
    -🌻 Setting
        
  - Number of Threads (users) : 1000
  - Ramp-up period (seconds) : 5
  - Loop Count : 1
  - 발급한 유효 쿠폰 수 : 300
  - 실제 쿠폰을 발급받은 유저 수 : 595
    
  ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F3b557cae-0dca-4e48-9a5e-c75054784795%2Fimage.png?table=block&id=4802e525-c107-47d2-9424-47332506dd28&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=860&userId=&cache=v2)

  위 이미지와 같이 남은 쿠폰 수도 잘 줄지 않고 중복으로 발급되는 쿠폰이 많이 발생.
    
  실제로 같은 코드의 쿠폰을 발급받은 유저도 많다.
    
  ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F8aca2a6f-7653-41d8-a329-ce3ac48e1bb9%2Fimage.png?table=block&id=fd11cd9b-fd82-46e7-a9f9-92878fe1275d&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1250&userId=&cache=v2)
  </details>
  <details>
    <summary>📝 Test : DB 낙관적 락`→ 채택 불가`</summary>
    매 발급 마다 쿠폰의 상태를 변경해야하는 지금 상황에서는 충돌 발생 시 매 번 재시도를 해야하는 낙관적 락은 적합하지 않다고 판단
  </details>
  <details>
    <summary>📝 Test : DB 비관적 락</summary>
강력한 락을 걸기 위해서 성능상 손해를 보더라도 비관적 락을 통해 테스트 진행

    
- 🌻 Setting
  - Number of Threads (users) : 1000
  - Ramp-up period (seconds) : 5
  - Loop Count : 1
  - 발급한 유효 쿠폰 수 : 300
  - 실제 쿠폰을 발급받은 유저 수 : 300
 
    ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F4d33051b-167f-4a46-8025-ae73f614f4b8%2F57f7a9cd-ca37-434b-bff1-e49ea7c14d32.png?table=block&id=18beb104-c825-4548-a6cd-722ed26bf901&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=860&userId=&cache=v2)
        
    300 개의 쿠폰이 발급된 것으로 보아 동시성 문제가 해결된 것을 볼 수 있다.
    
    ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2Fbda3c12b-5914-4cc6-8482-1ffd492173d0%2Fimage.png?table=block&id=d9c03988-2f5e-4fd6-9b39-c3172212336c&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1300&userId=&cache=v2)
    
    ### **📚 결론**
    
    기존에 락을 걸기 전에 **평균 응답 속도가 181 ms** 였던 것에 비해 **평균 응답속도가 1609 ms** 성능적으로 약 **794.4%** 감소됐지만, 동시성 문제는 확실하게 해결된 것을 알 수 있었다.
  </details>
  <details>
    <summary>📝 Test : 분산 락 - Redisson `→ 추가 테스트 필요`</summary>
  
    
  - 기존 CouponService 트랜잭션을 CouponLockService 로 Rapping 해서 Redisson 을 활용해 락을 만들고 해제했다.   
  - 동시성을 보장하기 위해서 락 획득 시도 시간과 락 유지 시간을 지정해줬다.
  - 시간을 지정하다보니 성능적으로 어느 정도 손해를 볼 수 밖에 없었다.
  - 내 컴퓨터로 테스트를 했을 때 동시성 문제를 해결하면서 처리할 수 있는 최소 시간 설정이었다.

  ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F009c8670-495a-4621-8514-325194fbefec%2Fimage.png?table=block&id=7eab6f96-ba81-4acc-835b-ec57ad85dd08&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=810&userId=&cache=v2)
   
   - 🌻 Setting
      - Number of Threads (users) : 1000
      - Ramp-up period (seconds) : 5
      - Loop Count : 1
      - 발급한 유효 쿠폰 수 : 300
      - 실제 쿠폰을 발급받은 유저 수 : 300
            
     ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F12449796-32ae-4ab7-9d3b-4de4a10dbace%2Fimage.png?table=block&id=142c111c-d29b-4b33-83ca-bc8b27a73c5d&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1190&userId=&cache=v2)
        
        ### **📚 결론**
        
        Redisson 을 활용해 분산 락을 테스트 해보았는데, 동시성 문제를 해결하기 위해 락 획득 시도 시간과 락 유지 시간을 설정하는 과정에서 컴퓨터 성능 이슈로 과하게 설정이 된 것으로 보인다.
        
        그 때문에 평균 응답속도가 2870 ms 라는 다소 아쉬운 결과가 나왔다.
        
        Redis 환경에서 데이터가 주고 받아지기 때문에 더 빠른 결과가 나왔어야한다고 생각한다.
        
        이후 추가로 이 과정을 더 테스트 해볼 예정이다.
  </details>
  <details>
    <summary>📝 Test : Redis 적용 (락X) `→ 채택 불가`</summary>


  - 락을 걸면 성능이 많이 안 좋아져서 락을 걸지 않고 Redis 를 사용하는 방법으로 테스트 해보았다.
  - Redis 는 싱글 스레드 모델로 동작하기 때문에 여러 요청이 동시에 오더라도 각 요청이 순차적으로 처리될 것이라 예상하고 Redis 로만 테스트 진행했다.
  - 🌻 Setting
    - Number of Threads (users) : 1000
    - Ramp-up period (seconds) : 5 / 10 / 15 / 20
    - Loop Count : 1
    - 발급한 유효 쿠폰 수 : 300
    - 실제 쿠폰을 발급받은 유저 수 : 440 / 400 / 320 / 305
  - 기존과 동일하게 1000 명의 유저가 5초 동안 요청을 보내도록 하니까 동시성 문제가 심하게 발생했다.
  - `원인 분석 : 원자성 보장 부족`
    - Redis는 기본적으로 명령어가 **원자적**으로 처리되지만, **복잡한 트랜잭션**이나 **다수의 명령어가 결합된 작업**에 대해서는 **원자성**이 보장되지 않는다.
      - 쿠폰의 잉여 수량을 확인.
      - 수량이 남아있으면 쿠폰을 발급.
      - 발급된 쿠폰 수량을 업데이트
        
        ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2Faea7738f-6fc5-442f-aa51-2a42eaed1844%2Fimage.png?table=block&id=67034fe0-fe09-4abc-b444-502b2bb08bb9&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=860&userId=&cache=v2)
        
        위 그래프를 보다시피 1000명의 유저가 20 초 동안 300개의 쿠폰발급을 시도한다고 가정했을 때 부터 동시성 문제가 거의 해결되었다.
        
        ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F0ddc2859-dd37-419e-a73c-5549cdc3cc5a%2Fimage.png?table=block&id=b1dbc638-0d6c-4e3f-ac7e-d9585e3494ad&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=860&userId=&cache=v2)
        
    
    ### **📚 결론**
    
    redis 를 활용해 **평균 응답속도가 8ms** 로 대폭 상승했다.
    
    성능적인 부분에서 기존 락을 걸었을 때 보다 Redis 만 사용했을 때 월등히 증가했는데, 동시성 문제 부분에서 높은 처리량을 보여주지는 못했다.
    
    `→ 채택 불가`
  </details>
  <details>
    <summary>📝 Test : Lua Script 적용</summary>


  - Redis 의 Lua Script 를 사용하면 여러 명령을 하나의 원자적 작업으로 묶어 실행할 수 있기 때문에 쿠폰 발급 메서드에서 실행되는 코드들을 Lua Script 를 작성하여 트랜잭션처럼 처리할 수 있다.
  - Lua Script 를 사용해 Redis 의 성능적인 이점과 동시성 문제를 둘 다 해결할 수 있을 것이라 예상되어 테스트를 진행했다.
  - 🌻 Setting
    - Number of Threads (users) : 1000
      - Ramp-up period (seconds) : 5
      - Loop Count : 1
      - 발급한 유효 쿠폰 수 : 300
      - 실제 쿠폰을 발급받은 유저 수 : 300
        
        ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2Fd93240cf-c067-4c2d-9222-25a7fe15326d%2Fimage.png?table=block&id=41382b66-1b6e-4c9c-88bd-a8826e2cd880&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1150&userId=&cache=v2)
        
    
    ### **📚 결론**
    
    Redis의 Lua 스크립트를 활용한 결과, 평균 응답 속도가 **16ms**에 도달하여, Redis의 기본 사용에서 확인된 성능과 거의 동일한 높은 퍼포먼스를 유지했다.
    
    이는 단순히 성능 향상에 그치지 않고, **동시성 문제까지 완벽하게 해결**했다.
    
    Redis Lua 스크립트를 활용해 효율적인 처리 속도를 제공할 뿐 아니라, 여러 요청이 몰릴 때도 안정적인 성능을 유지할 수 있도록 동시성 관리를 강화했다.
    
    이를 통해 Redis는 대규모 트래픽 환경에서도 **높은 처리량과 안정성**을 보장하며, 특히 미션 크리티컬한 애플리케이션에서도 확실한 신뢰성을 제공한다.
  </details>

## **💡 최종 의사 결정**

![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F57704f26-a0b2-4d11-8904-34eb29c1a424%2Fimage.png?table=block&id=2ea8b58e-49b0-42d4-a036-cb05e5d50cf9&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1150&userId=&cache=v2)

`보라색 - 평균 처리 응답 시간` `( 낮을 수록 빠른 처리 응답 시간 )`

`분홍색 - 추가 발급된 쿠폰 수` `( 0이면 동시성 문제 해결 )`


  - 그래프로 확인할 수 있듯이 동시성 문제를 완벽하게 해결한 세 가지 방법은 **DB 비관적 락**, **Redisson을 활용한 분산 락**, 그리고 **Lua Script 활용**이다.
  - 이 중에서 `**Lua Script`를 활용한 방식**이 동시성 문제를 해결하면서도 가장 빠른 응답 속도를 보였기 때문에 가장 이상적이라고 할 수 있다.
  - 실제로, **비관적 락 방식**에서 **1609ms**의 응답 시간이 소요되었던 것에 비해, **`Lua Script`**를 활용한 후에는 **16ms**로 성능이 대폭 향상되었다.
</details>

## 🌿 기술적 의사 결정
<details>
  <summary>elasticsearch 도입 이유</summary>

### 1. Spring @Query 검색(JPQL) 구현 후 발생한 문제

@Query 를 이용한 검색 기능은 동의어 처리나 부분 일치 검색에서 한계가 있었고, "장미"와 "로즈"처럼 동일한 의미를 가진 검색어가 일치하지 않아 검색 정확도가 낮았습니다.

거기다 유명한 커뮤니티를 조사해본 결과 대부분의 커뮤니티에서 게시글 총량이 몇 십만건이 되어 기본 검색 메서드로는 성능에 한계가 있다고 생각했습니다.

### 2. 해결법

**동의어 검색의 필요성**

동의어 검색 기능을 구현하여, 서로 다른 표현을 동일하게 인식하도록 해야 했고, 이를 통해 사용자 의도에 맞는 검색 결과를 제공할 필요가 있었습니다.

**성능 최적화**

수십만건의 게시글을 검색하기 위해 성능 최적화가 필요했습니다. elasticsearch를 도입하여 검색속도 향상이 필요했습니다.

### 3. Elasticsearch 도입 및 선택 이유

Elasticsearch는 강력한 동의어 처리 기능과 빠른 검색 성능을 제공하며, 대규모 데이터 처리에도 적합하여 검색 결과의 정확도와 성능을 모두 향상 시킬 수 있었습니다.
</details>
<details>
  <summary>Kafka를 선택한 이유 (feat. 좋아요 기능)</summary>

### **배경**

1. **좋아요 기능에 대한 서비스 확장성과 안정성 필요**
- 좋아요 기능의 성능 최적화와 확장성 확보를 위해 Redis를 도입했지만, Redis를 단순 **읽기 캐시**로 활용하는 방식으로는 한계가 존재했습니다.
- 실시간 데이터 반영과 안정적인 데이터 저장을 병행할 수 있는
  기술이 필요했습니다.
  ![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F83a00fe4-70b6-4796-9da5-f897d917d954%2Fimage.png?table=block&id=d9e68d76-757f-4bcc-9e94-d8abe7e94dc4&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1120&userId=&cache=v2)
- Kafka
  Kafka는 분산 아키텍처와 배치 처리를 기반으로 높은 처리량을 제공하며,
  대량의 메시지를 실시간으로 처리할 수 있습니다.
- RebbitMQ
  RebbitMQ는 전통적인 메세지 브로커로,
  요청/응답 패턴이나 단기적인 메시지 전달에 강점이 있지만,
  대량의 데이터를 실시간으로 처리할 때는 비교적 부적합합니다.
2. **메시지 소비 모델**
- Kafka
  Kafka는 구독 기반 모델을 사용하여
  여러 소비자가 같은 메세지를 병렬적으로 차리할 수 있습니다.
  좋아요 수를 여러 시스템에서 동시에 처리하거나
  통계 데이터를 분석할 때 유리할 것이라고 생각했습니다.
- RabbitMQ
  RabbitMQ는 메시지가 큐에 소비되면 삭제되기 때문에,
  동일한 메시지를 여러 시스템에서 반복 처리하려면
  추가적인 설정이 필요합니다.

### **결과**

- **좋아요 데이터 처리 성능 개선**
    - Kafka를 통해 대량의 좋아요 메시지 처리 및 저장 할 수 있습니다.
    - Redis(consumer group1)와 DB(consumer group2)의 동기화를 통해 실시간 데이터 반영과 안정성을 동시에 수행합니다.
- **확장성 확보**
    - 서비스 확장 시 Redis의 부하를 Kafka로 분산, 추가적인 기능(쿠폰 관리, 알림 시스템 등) 도입 용이합니다.
</details>
<details>
  <summary>Redis Cluster 도입 이유</summary>

### 배경
- 서비스가 확장됨에 따라 Redis를 활용한 기능이 증가:
    - 좋아요 실시간 반영
    - 알람 서비스
    - 쿠폰 관리 등
- **향후 Redis 의존도가 높아질 것으로 예상**되는 상황에서,
  Redis 장애가 서비스 전체에 영향을 미칠 가능성을 고려해야 했습니다.

### **대안 기술 비교**
![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F99e25c07-9cd0-473a-b8de-19fa7a8a4733%2Fimage.png?table=block&id=912cde84-f89c-454f-be3f-21a0fd7e93cc&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1120&userId=&cache=v2)
### Redis Cluster 선택 이유

1. **확장성 확보**
   대량의 데이터를 처리하게 되더라도 분산 처리하기 때문에,
   트래픽 증가에도 안정적인 서비스 운영이 가능해졌습니다.
   (트래픽이 늘어나면 노드 추가를 통해 성능 저하없이 확장)
2. **성능 개선**
   요청이 자동으로 샤드된 노드로 분산되어,
   데이터 접근 속다가 빨라지고 응답 시간이 단축되었습니다.
   (병렬 처리를 통해 한 노드에서 병목 현상이 발생하는걸 방지)
3. **샤드 단위의 장애 복구**
   Sentinel 과 달리, 샤드 단위로 장애 복구가 이루어져,
   특정 노드 장애 시에도 서비스가 중단되지 않도록 설계되었습니다.
4. **정리**
   Sentinel은 소규모 트래픽과 단일 마스터 구조에서는 충분한 선택지지만,
   대규모 데이터와 고성능 요구사항을 고려해 Cluster를 선택했습니다.

### 기대 효과
- **안정적인 서비스 운영**
    - 대규모 트래픽에서도 데이터 접근 속도를 유지하며, 장애 발생 시에도 서비스 지속 가능.
- **확장성 확보**
    - 새로운 노드를 추가해도 성능 저하 없이 서비스 확장이 가능.
</details>

## 🌵 트러블슈팅
<details>
  <summary>좋아요 기능 성능 최적화</summary>

## 결과 먼저

DB 락을 통해 동시성 제어 문제를 해결했을때,
동시에 요청이 100개이상 들어오면 Error 가 발생하는 성능 이슈가 있었고,
이를 해결하기 위해 Kafka , Redis 를 사용하는 방식으로 변경했습니다.
개선 이후 성능을 확인하기 위해 테스트한 자료를 같이 보여드리겠습니다.
**감당할 수 있는** **동시 요청 수**가 약 **13배** 증가했습니다.
**동시에 여러개의 요청에 대한 응답 시간**도 약 **10배** 감소했습니다.
![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2Ff35dbdd7-feac-42dd-a0ea-3f8694559565%2Fimage.png?table=block&id=3ba4ceaa-4330-4494-b007-152801bdfba1&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1120&userId=&cache=v2)
![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2Feafb68c2-212f-4807-9467-ac1d45f1a5cb%2Fimage.png?table=block&id=16dbc647-3946-4779-adaf-82b609621980&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1120&userId=&cache=v2)
## 문제발생배경

- DB락을 사용해서 동시성 제어를 하는 방식으로
  좋아요 기능을 설계했습니다.
- **문제 1. 높은 요청 수 처리 시 성능 저하**
  동시 요청이 증가할수록 DB Lock 으로 인해
  요청 처리 시간이 급격히 증가했습니다.
- **문제 2. 트래픽 급증 시 장애 발생**
  초당 요청 수가 100개 이상일 때 DB로 부하로 인한 에러가 발생했습니다.

## 문제 해결을 위한 결정

Redis 를 통한 속도,성능 향상과 Kafka 를 통한 데이터 정합성을 보장

### 각 기술을 선택한 이유 (정리)

1. **Redis**
- 빠른 응답 속도를 제공하여 실시간으로 좋아요 데이터를 저장 및 조회
- 메모리 기반 데이터 저장소로 동시 요청 처리 능력 탁월

1. Kafka
- 이벤트 기반 비동기 처리로 데이터의 정합성을 보장
- 데이터 유실 방지 및 처리 안정성 향상

즉, Redis는 속도를 Kafka는 안정성을 담당하는 구조로 역할을 분리했습니다.

## 개선 방법

좋아요 요청 처리 방식을 개선하기 위해
**Kafka와 Redis**를 활용하여 아키텍처를 재설계하였습니다.

**개선된 아키텍처**

- 좋아요 요청 이벤트 발행
  좋아요 요청이 들어오면 Kafka Producer가 이벤트를 발행
- Kafka Consumer 처리
  2개의 Consumer group으로
  Consumer 1 :  Redis에 좋아요 정보를 캐싱
  → 실시간 데이터 조회 시 Redis를 통해 응답.
  Consumer 2 : 좋아요 정보를 DB에 동기화
  → 데이터 영구 저장
- 실시간으로 Redis 데이터를 활용하여 빠른 조회,
  비동기적 데이터 동기화를 동시에 달성
</details>
<details>
  <summary>게시판 검색 성능 최적화</summary>

**문제 발생 배경**

기존 Spring @Query 검색(JPQL)을 통해 게시글을 검색하는 과정에서, 수십만 건의 데이터가 쌓인 대규모 커뮤니티 환경에서는 검색 속도가 현저히 느려지는 문제가 발생했습니다.
특히, 기본적으로 제공되는 검색 메서드는 데이터 양이 많을수록 성능 저하가 두드러졌고, 이로 인해 사용자 경험에 불편함을 초래했습니다.

**해결 과정**

성능 향상을 위해 Elasticsearch를 도입하게 되었으며, 이를 통해 검색 속도 개선을 목표로 했습니다. Elasticsearch는 대규모 데이터 처리에서 탁월한 성능을 자랑하며, 효율적인 분산 검색을 가능하게 합니다.
Elasticsearch의 빠른 검색 속도와 유연한 분석 기능을 활용하여, 동의어 검색 기능을 커스터마이즈하고, 고유한 인덱스 설정과 최적화 작업을 통해 성능 문제를 해결했습니다.

**테스트 및 검증**

검색 성능을 검증하기 위해 동일한 단어에 대한 검색을 여러 차례 비교 테스트를 진행했습니다. 테스트 결과, Elasticsearch는 기존의 JPQL에 비해 월등한 검색 속도를 기록했으며, 검색 시간이 크게 단축된 것을 확인할 수 있었습니다.
특히, 대규모 데이터셋에서 Elasticsearch는 빠른 검색 속도와 높은 정확도로 성능을 향상 시켰으며, 이는 시스템의 전체적인 사용자 경험 개선에 기여했습니다.

**테스트 결과**

검색 검색 엔진 당 5개의 단어(“장미”,”히비스커스“,”부레“,”무궁화“,”식물“)를 5번씩 검색해서 나오는 시간을 측정하여 비교

같은 단어 장미 검색 시 소요 시간 측정

**JPQL**

![img](https://teamsparta.notion.site/image/https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fb4Eler%2FbtsKM1FSl5d%2FG0DqNrzneJixBn9H0nOiG0%2Fimg.png?table=block&id=f8588f8b-6802-45f8-b3c2-27522df00d3d&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1120&userId=&cache=v2)

**elasticsearch**

![img](https://teamsparta.notion.site/image/https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2F8oKm4%2FbtsKL3dD8dq%2F0UBY1QIBrBXkYrBiLMJBS1%2Fimg.png?table=block&id=3ce37458-e7cf-4490-afeb-05423d298fa5&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1120&userId=&cache=v2)

(검색 시간 단위 : ms)

|  | @Query를 이용한 검색 | 엘라스틱 서치 |
| --- | --- | --- |
| 1 | 651 | 79 |
| 2 | 421 | 39 |
| 3 | 425 | 44 |
| 4 | 415 | 45 |
| 5 | 385 | 38 |

**나머지 단어도 같은 방법으로 소요시간 측정 후 평균 값으로 성능 개선률 계산**

| 시행 횟수 | 1 | 2 | 3 | 4 | 5 |
| --- | --- | --- | --- | --- | --- |
| JPQL | 824.6 | 417.6 | 422.6 | 418.8 | 411.6 |
| 엘라스틱 서치 검색 | 118.4 | 70.4 | 58.4 | 55.6 | 54 |

![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F258ab359-d9dc-4423-b6ac-238342dc389c%2Fimage.png?table=block&id=5e3c9a21-ab8a-4b8e-8bf0-32c7eb88d9a4&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1120&userId=&cache=v2)

두 검색엔진을 비교해보니 생각보다 차이가 컸고 개선 비율 계산 결과 **약 86%** 의 성능 개선이 있었다.
</details>
<details>
  <summary>프론트엔드 페이지 토큰 유지 및 삭제</summary>

## **💡 배경**

- 프론트엔드를 간단하게 구현하는 과정에서 페이지 이동 시 Token 이 저장이 안돼서 로그인 정보를 유지할 수 없는 문제가 발생했다.
- 기존에 Postman 으로 테스트 했을 때 Authorization 에서 Jwt Token 값을 자동으로 생성해준 경우와 다르게 프론트를 구현해서 페이지마다 로그인을 유지시키기 위해 Cookie 에 Jwt Token 을 유지시킬 필요가 있었다.

### **📝 Token 유지**

***AuthService***

![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F77971cae-f978-4864-8b11-df8b89baea7e%2Fimage.png?table=block&id=529777b9-9d0e-49bd-9412-f6ba55ac7ac2&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1120&userId=&cache=v2)

***JwtSecurityFilter***

![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F1ac59d83-a30f-4eea-ad3a-e5400fdc3dab%2Fimage.png?table=block&id=25e81bd2-2eef-4622-930a-2549a9717b17&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=980&userId=&cache=v2)

- 로그인 진행 시 **Token 을 Cookie 에 저장**시키는 로직을 추가하고 Filter 에서 cookie 에 token 이 있을 시에 이를 읽어오는 코드를 추가했다.
![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F4f3740d8-a8e9-4734-bff3-a7107dae7207%2Fimage.png?table=block&id=db4d1492-5670-45c1-9b54-82e3433f783d&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=670&userId=&cache=v2)
![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2Fd0927d4a-891c-425d-b949-85825e8669b3%2Fimage.png?table=block&id=e7d899c1-7ef5-494f-bf23-2e8696b0feb3&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=920&userId=&cache=v2)
  개발자 모드에서 확인해본 결과 토큰이 잘 저장된 것을 확인할 수 있다.

### **📝 로그아웃 ( Token 삭제 )**
![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2F0523b901-5632-44a7-b220-ed83ba8dd269%2Fimage.png?table=block&id=c71bdc63-ac67-4660-bbe2-7b4be22d7df9&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1120&userId=&cache=v2)

Spring Security 의 logout 기능을 사용해 logout 시 Cookie 에 저장된 Token 을 삭제하고, 로그인 페이지로 redirect 시키는 로직을 구현했다.
</details>

# 🌲 와이어프레임
![image](https://github.com/user-attachments/assets/c991025f-8199-45ea-a32b-c181fe43b6d9)


# 🍀 ERD
![img](https://img.notionusercontent.com/s3/prod-files-secure%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2Ffa44ebc8-f5e2-431e-980a-7572af08ef15%2Fimage.png/size/w=2000?exp=1732326390&sig=BjZS7N1Vj3RGXYaI2pBsPmqljd11qcFqvxVSL5eoNJ4)

## 🌹 인프라 설계도
![image](https://github.com/user-attachments/assets/deeedb42-95e4-49cb-93cf-931933a4501f)


## 🌱 주요 기능
<details>
  <summary>🌳 유저 인증</summary>

    - 회원가입 : 운영자 또는 일반 사용자로 나뉜다.
    - 로그인
    - 회원 탈퇴
    - 유저 정보 수정 (비밀번호, 이메일 등)
</details>
<details>
  <summary>🌹 유저 프로필 및 친구 리스트</summary>

    - 프로필 사진 등록 및 수정
    - 내가 작성한 게시글 조회
    - 친구 리스트 관리 ( 친구 요청, 수락, 조회,  거절, 삭제 )
</details>
<details>
  <summary>🌻 게시글 작성</summary>

    - 게시글 작성시 공개 범위 설정 가능
    - 뉴스피드에서 나와 내 친구의 게시글들이 수정된 시간에 따라 내림차순으로 정렬됨.
    - 게시글 수정 및 삭제 - 사용자가 작성한 글과 사진 수정, 삭제
    - 악성 게시글 삭제 - 악성 게시글을 관리자가 직접 삭제 할 수 있음.
    - 식물 사진 등록 - 사용자가 키우는 식물 사진을 여러개 등록가능함
    - 검색 시 오타나 비슷한 단어를 검색해도 원하는 결과를 얻도록 구현
</details>
<details>
  <summary>🍀 게시글에 댓글 작성 기능</summary>

    - 게시물에 대한 의견과 조언 작성
    - 댓글 등록, 수정 및 삭제
</details>
<details>
  <summary>🌿 식물 도감 게시판 제공</summary>

    - 식물 정보 제공 게시판
        - 운영자만 작성할 수 있습니다.
        - 식물 사진 등록 - 정보 제공할 식물 사진과 자세한 설명 등록
        - 게시글 수정 및 삭제 - 작성한 글과 사진 수정, 삭제
        - 관심 식물도감 - 사용자가 관심 등록하고 싶은 식물도감을 등록, 해제
</details>
<details>
  <summary>🌵 나의 식물 관리</summary>

    - 내 식물 페이지
        - 본인만 등록하고 볼 수 있습니다.
        - 식물 사진 등록 - 본인의 식물 사진 및 애칭 등록
        - 식물 수정 및 삭제 - 등록한 식물에 대해 애칭과 사진 수정, 삭제
    - 내 식물 다이어리 페이지
        - 본인만 등록하고 볼 수 있습니다.
        - 그날의 식물 사진 등록 - 본인의 식물 사진 및 활동내용 등록
        - 다이어리 수정 및 삭제 - 다이어리에 활동내용과 사진을 수정하고 삭제
</details>
<details>
  <summary>🌻 실시간 알림기능</summary>

    - 알림 메시지 수신
        - 유저에게 발생한 알림을 수신하고 읽음/삭제 처리 할 수 있습니다.
</details>
