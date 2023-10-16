# VISANG AI Model Showcase
## 비상교육의 데이터와 서비스를 한 곳에서 제공하고 통합하는 웹 기반의 플랫폼
> 최종목표 : 비상교육의 모든 데이터(정형, 비정형)를 통합하고 표준화하여 비상교육 임직원들에게 데이터를 손쉽게 검색 및 제공하기 위한 서비스

![image](https://github.com/shinsj4653/vs-data-service-backend/assets/49470452/a018f734-9114-4330-a257-0090f28e9a91)

접속 URL : http://13.209.224.148/main/  

## 목차
- [사용 기술 스택](#사용-기술-스택)
- [ERD](#erd)
- [나의 주요 구현 기능](#나의-주요-구현-기능)
  * [1. 메타 데이터 검색](#1-메타-데이터-검색)
  * [2. 실시간 검색어 순위](#2-실시간-검색어-순위)
  * [3. MockMvc 기반 Controller 테스팅](#5-mockmvc-기반-controller-테스팅)
  * [4. ControllerAdvice를 이용한 예외처리](#4-controlleradvice를-이용한-예외처리)

## 사용 기술 스택
- `Language` : Java 11, JUnit 4
- `Framework` : SpringBoot 2.7.14, MyBatis
- `Database` : PostgreSQL 42.5.0, AWS RDS
- `Deploy` : Github Actions, AWS S3 & CodeDeploy
- `API Docs` : Swagger 3.0.0
- `Logging` : Logback, Logstash
- `ElasticSearch` : RestHighLevelClient, Query DSL API
- `Controller Testing` : MockMVC

## ERD
![image](https://github.com/shinsj4653/vs-data-service-backend/assets/49470452/cb38098c-ac34-40f5-9c1f-11ce7010658e)

## 나의 주요 구현 기능

### 1. 메타 데이터 검색  

https://github.com/shinsj4653/vs-data-portal-backend/assets/49470452/86e0f1cd-6621-409f-b176-d5bd5f7b2a82  


*ElasticSearch 도입 전*  



https://github.com/shinsj4653/vs-data-portal-backend/assets/49470452/a82fe416-162c-4125-a922-1fb7e569e124

*ElasticSearch 도입 후*

- 메타 데이터 검색 기준 중, `한글로 구성된 테이블 설명 및 하위주제`에 `역색인화`를 도입한 빠른 검색 기능 구현을 위해, ElasticSearch의 한글 형태소 분석기인 `Nori Tokenizer` 를 설치 후, 메타 데이터 컬럼에 적용
- `문제은행` 키워드로 검색 시, 도입 전에는 해당 문자열이 그대로 포함된 결과값만 나왔지만 도입 이후에는 `문제`, `은행` 과 같이 더 세밀한 단위까지 나뉘어진 문자열 검색을 수행한 결과를 반환해줌

### 2. 실시간 검색어 순위


https://github.com/shinsj4653/vs-data-portal-backend/assets/49470452/1cd75b06-36e7-46c3-be88-bf176d95f53c


*Kibana Console에서 실행한 실시간 검색어 순위 집계 결과*  

- 검색 API 사용시, 다음 형식으로 로그를 전송하였고 이를 `logback-spring.xml` 파일을 이용하여 Logstash를 거친 후 ElasticSearch로 로그가 전송되도록 함
```java
log.info("{} {}", keyValue("requestURI", "/metadata/search/total"), keyValue("keyword", keyword));
```
- requestURI, 검색 키워드, 그리고 검색을 시도한 시간대 범위(gte, lte)를 지정한 후, `QueryDSL` 요청을 통해 조건에 맞는 로그를 필터링 함
- 이후, ElasticSearch의 `Bucket Aggregation` 기능을 통해 같은 검색 키워드 별로 `집계 수`를 계산하여 반환해주는 로직을 구현함


https://github.com/shinsj4653/vs-data-portal-backend/assets/49470452/109960da-a5fe-45a7-ac0c-4ebe86bf7b2b



*JAVA 프로젝트 기반 실시간 검색어 순위 결과 조회 API 구현*  

- Java 환경에서 ElasticSearch의 인스턴스 생성 및 활용, 그리고 QueryDSL 요청과 응답을 받기 위해 `RestHighLevelClient` 와 `검색 및 QueryDSL API` 사용하였고, JSON Object로 가공된 형태로 반환해주는 API를 완성시킴

### 3. MockMvc 기반 Controller 테스팅
- `@InjectMocks` 를 통해 테스트할 대상의 가짜 객체를 주입받을 수 있다는 점을 활용
- 컨트롤러 테스팅을 위한 Http 호출을 담당하는 `MockMvc` 객체를 중심으로 테스트 코드 작성

```java
@ExtendWith(MockitoExtension.class)
public class DataMapControllerTest {

    @Mock
    private DataMapService dataMapService;

    @InjectMocks
    private DataMapController dataMapController;

    private MockMvc mockMvc;


    @Test
    @DisplayName("데이터 맵 대분류 정보 반환 API 테스트")
    public void 데이터맵_대분류_반환() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(dataMapController).build();

        mockMvc.perform(get("/datamap/category/main"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("데이터 맵 대분류 단위까지의 데이터 조회에 성공하였습니다."));

    }

    @Test
    @DisplayName("데이터 맵 중분류 정보 반환 API 테스트")
    public void 데이터맵_중분류_반환() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(dataMapController).build();

        mockMvc.perform(get("/datamap/category/sub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("데이터 맵 중분류 단위까지의 데이터 조회에 성공하였습니다."));

    }

    @Test
    @DisplayName("데이터 맵 주요 데이터셋 반환 API 테스트")
    public void 데이터맵_주요_데이터셋_반환() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(dataMapController).build();

        mockMvc.perform(get("/datamap/dataset/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("데이터 맵 모든 주요 데이터 셋 조회에 성공하였습니다."));

    }
}
```

### 4. ControllerAdvice를 이용한 예외처리
- try, catch 문을 이용한 예외처리 대신, `@Controller` 어노테이션이 적용된 모든 곳에서 발생되는 예외에 대해 처리해주는 기능 구현
- `@ExceptionHandler` 어노테이션을 메서드에 선언하고 특정 예외 클래스 지정을 통해 해당 예외가 발생하였을 때 메서드에 정의한 로직을 처리할 수 있도록 해줌
```java
@Slf4j
@RestControllerAdvice
public class ControllerAdvice {
    private static final int FIELD_ERROR_CODE_INDEX = 0;
    private static final int FIELD_ERROR_MESSAGE_INDEX = 1;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInputFieldException(MethodArgumentNotValidException e) {
        FieldError mainError = e.getFieldErrors().get(0);
        String[] errorInfo = Objects.requireNonNull(mainError.getDefaultMessage()).split(":");

        int code = Integer.parseInt(errorInfo[FIELD_ERROR_CODE_INDEX]);
        String message = errorInfo[FIELD_ERROR_MESSAGE_INDEX];

        return ResponseEntity.badRequest().body(new ErrorResponse(code, message));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> sqlExceptionHandle(DataportalException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(DataportalException.class)
    public ResponseEntity<ErrorResponse> handleDataportalException(DataportalException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unhandledException(Exception e, HttpServletRequest request) {
        log.error("UnhandledException: {} {} errMessage={}\n",
                request.getMethod(),
                request.getRequestURI(),
                e.getMessage()
        );
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(9999, "일시적으로 접속이 원활하지 않습니다. 데이터 플랫폼 Cell 팀으로 문의 부탁드립니다."));
    }
}
```
- `BadRequest` 및 `NotFound` 카테고리에 속하는 예외를 생성한 후, 모든 예외는 `DataportalException` 예외 클래스를 상속받도록 하여 관리함

### 5. Filter 기반 XSS 공격 방지
- 기존에 사용한 `lucy-xss-servlet-filter`는 form data 전송 방식에는 적용되지만, `@RequestBody` 로 전달되는 JSON 요청은 처리해주지 않으므로, `MessageConverter`를 사용하는 방법을 택함.
```java
// HTMLCharacterEscapes.java
public class HTMLCharacterEscapes extends CharacterEscapes {
    private static final long serialVersionUID = 1L;
    private final int[] asciiEscapes;

    public HTMLCharacterEscapes() {
        //XSS 방지 처리할 특수 문자 지정
        asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
        asciiEscapes['<'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['>'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['&'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['\"'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['('] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes[')'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['#'] = CharacterEscapes.ESCAPE_CUSTOM;
        asciiEscapes['\''] = CharacterEscapes.ESCAPE_CUSTOM;

    }

    @Override
    public int[] getEscapeCodesForAscii() {
        return asciiEscapes;
    }

    @Override
    public SerializableString getEscapeSequence(int ch) {
        //Escape 처리
        return new SerializedString(StringEscapeUtils.escapeHtml4(Character.toString((char) ch)));
    }
}

// XSSConfig.java
@Configuration
@RequiredArgsConstructor
public class XssConfig implements WebMvcConfigurer {

    //이미 기존에 등록된 ObjectMapper Bean이 있다면 JSON 요청/응답에서 사용하기 위해 의존성 주입을 받아 사용한다.
    private final ObjectMapper objectMapper;

    // XSS 공격에 대한 Filter 적용
    @Bean
    public FilterRegistrationBean<XssEscapeServletFilter> getFilterRegistrationBean() {
        FilterRegistrationBean<XssEscapeServletFilter> xssRegistrationBean = new FilterRegistrationBean<>();
        xssRegistrationBean.setFilter(new XssEscapeServletFilter());
        xssRegistrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);
        xssRegistrationBean.addUrlPatterns("/*");
        return xssRegistrationBean;
    }

    // lucy-xss-servlet-filter는 @RequestBody로 전달되는 JSON 요청은 처리해주지 않는다.
    // 따라서, MessageConverter로 처리해줘야한다.
    @Bean
    public MappingJackson2HttpMessageConverter jsonEscapeConverter() {
        ObjectMapper copy = objectMapper.copy();
        copy.getFactory().setCharacterEscapes(new HTMLCharacterEscapes());
        return new MappingJackson2HttpMessageConverter(copy);
    }


}
```
- `CharacterEscapes` 를 상속하는 클래스 `HtmlCharacterEscapes` 를 만들어 처리해야 할 특수문자를 지정하고 변환한 후, `ObjectMapper`에 `HtmlCharacterEscapes` 를 설정하고 `MessageConverter`에 등록하여 Response가 클라이언트로 넘어가기 전에 처리해주는 로직 구현

## 향후 개선 사항
### 1. EC2 인스턴스에 ELK 플랫폼 성공적으로 연결
- EC2 인스턴스에서 `docker` 를 활용하여 ELK 연동에 성공하였지만, 인스턴스의 서버 용량 초과 문제 발생으로 EC2 실행 속도가 급격하게 느려지는 현상이 발생하여 현재는 로컬 환경으로 세팅해둔 상태이다.
- 현재의 `t3a.medium` 요금제에서 더 높은 성능의 요금제로 업그레이드 하거나 `AWS의 OpenSearch 서비스`를 활용해보는 방법을 도입해볼 예정이다.

## 참고 사항
- 회사 프로젝트의 접근 권한은 private이기 때문에, 제 리포지토리에 보이도록 하기 위해 `main 브랜치만 가져온 상태`입니다.
- 제 레포에서는 Github Actions의 Deploy 실패 문구가 보이지만, `실제 현업에서는 정상작동` 하고 있습니다.

![image](https://github.com/shinsj4653/vs-data-service-backend/assets/49470452/787fcdc3-686f-4363-9066-adcf37970793)
*현업에서 사용되었던, 혹은 사용중인 브랜치명 목록들*  



![image](https://github.com/shinsj4653/vs-data-service-backend/assets/49470452/811ab1df-f5c6-4c97-9fd3-b5f73935c673)
*정상 작동한 Github Actions의 Workflows 이력*

