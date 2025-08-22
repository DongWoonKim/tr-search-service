## 🔎 tr-search-service (도서 검색 서비스)

`tr-search-service`는 **도서 검색(Search)** 기능을 제공하는 서비스로,  
사용자가 입력한 키워드를 기반으로 도서를 빠르고 효율적으로 탐색할 수 있도록 지원합니다.  

이 서비스는 **검색 최적화(Search Optimization)**에 초점을 두며,  
인기검색어 TOP10, 논리 연산자(OR/NOT), 페이지네이션 등 검색 기능을 제공합니다.  

이를 통해 사용자는 **도서 목록 검색** 및 **조건 기반 탐색** 기능을 편리하게 이용할 수 있습니다.

--- 

### 🔎 주요 특징 (tr-search-service)

1. **단순 검색**  
   - 사용자는 검색 쿼리를 생성하여 도서를 검색할 수 있습니다.  
   - 검색 쿼리는 키워드로 표현되며, 결과는 일치하는 도서 목록과 검색 메타데이터를 포함합니다.  

2. **복합 검색**  
   - 사용자는 검색 연산자를 포함한 복합 검색 쿼리를 생성할 수 있습니다.  
   - **OR 연산자 (`|`)**: 복수의 키워드에 대한 합집합 검색 전략을 적용합니다.  
   - **NOT 연산자 (`-`)**: 특정 키워드를 제외하는 검색 전략을 적용합니다.  
   - 검색 엔진은 **쿼리 파싱 → 검색 전략 결정 → 결과 집계**의 프로세스를 따릅니다.  

3. **검색 결과 페이징**  
   - 사용자는 대량의 검색 결과를 페이지 단위로 탐색할 수 있습니다.  
   - 각 페이지는 설정된 크기만큼의 도서 목록을 포함합니다.  

4. **인기 검색어**  
   - 사용자는 인기 검색어 TOP10을 확인할 수 있습니다.  

5. **검색 연산자 구현 예시**  
   - **OR 연산자 (`|`)**  
     - 예: `tdd|javascript` → *tdd* 또는 *javascript*가 포함된 모든 도서 검색  
   - **NOT 연산자 (`-`)**  
     - 예: `tdd-javascript` → *tdd*는 포함하지만 *javascript*는 제외된 도서 검색  
   - 키워드는 최대 2개까지 지원합니다.

--- 

### ⚙️ 내부 아키텍처

```bash
com.trevari.spring.trsearchservice
├─ application
│  ├─ QueryParser.java
│  └─ SearchService.java
├─ config
│  └─ OpenApiConfig.java
├─ domain
│  └─ search
│     ├─ ParsedQuery.java
│     ├─ SearchKeyword.java
│     └─ SearchKeywordRepository.java
├─ exception
│  ├─ BookNotFoundException.java
│  ├─ ErrorResponse.java
│  ├─ GlobalExceptionHandler.java
│  └─ SearchException.java
├─ infrastructure
│  └─ persistence
│     ├─ BookDoc.java
│     ├─ EsConfig.java
│     ├─ SearchKeywordEntity.java
│     ├─ SearchKeywordJpaRepository.java
│     └─ SearchKeywordRepositoryAdapter.java
├─ init
│  └─ BookIndexInitializer.java
├─ interfaces
│  ├─ dto
│  │  ├─ BookSummaryDto.java
│  │  └─ SearchResultDto.java
│  ├─ http
│  │  └─ SearchController.java
│  └─ mapper
│     └─ SearchKeywordMapper.java
└─ TrSearchServiceApplication.java

resources
├─ static/
├─ templates/
├─ application.yml
└─ books.json
```
 - application package : 위의 아키텍처에서 application package의 하위 항목들은 비즈니스 로직을 담는 서비스가 들어옵니다.
   - **QueryParser.java** : 사용자가 입력한 검색 문자열을 파싱하여 연산자(`OR`, `NOT`) 및 키워드 단위로 분리합니다.  
   - **SearchService.java**
     - `search()` : Elasticsearch를 활용하여 도서 검색을 수행합니다.  
      - 단순 키워드 검색부터 `OR(|)`, `NOT(-)` 연산자를 포함한 복합 검색까지 지원합니다.
      - 검색 키워드는 DB(`search_keyword` 테이블)에 저장·집계됩니다.
      - 검색 결과에는 도서 목록과 함께 페이징 메타데이터가 포함됩니다.  
    - `popularTop10()` : 사용자가 입력한 검색 키워드를 집계하여 **인기 검색어 TOP10**을 제공합니다.           

 - domain : search-service의 순수 도메인 모델을 정의하는 영역입니다.  
            이곳의 SearchKeywordRepository 인프라 기술에 의존하지 않는 순수 POJO 인터페이스로 작성되었습니다.
            -> 비즈니스 로직은 순수 도메인 모델에 의존하도록 설계하여, 외부 기술이나 환경 변화로부터 받는 영향을 최소화합니다.  
            - ParsedQuery : 사용자가 입력한 검색 문자열(예: "tdd|java -legacy")을 파싱한 결과를 표현하는 VO(Value Object).
            - SearchKeyword : 실제로 사용자가 입력한 검색어를 도메인 객체로 표현

 - infrastructure/persistence : 도메인 모델을 실제 저장소(Elasticsearch, DB)와 연결하는 역할을 담당합니다.
    - BookDoc : Elasticsearch(ES)에 색인되는 **도서 문서(Document)**를 정의합니다.
    - SearchKeywordRepositoryAdapter : 도메인 레벨에서 정의된 SearchKeywordRepository 인터페이스의 구현체(Adapter).
                              SearchKeywordJpaRepositor를 내부에서 사용하면서도, 도메인 계층은 오직 SearchKeywordRepository 인터페이스만 바라보도록 만들어 관심사 분리를 달성합니다.
                              -> 이를 통해 도메인 계층과 인프라 계층의 의존성 분리를 실현합니다.

 - interfaces : 외부와의 입출력 경계를 담당하는 영역으로, 도메인 로직을 그대로 노출하지 않고 DTO나 매퍼를 통해 안전하게 전달하는 역할을 합니다.
   - mapper/SearchKeywordMapper : 도메인 객체(SearchKeyword) ↔ DTO, Entity 간 변환을 담당합니다.
                         -> 서비스 계층과 컨트롤러/DB 계층을 느슨하게 연결해주며, 변환 로직을 모듈화하여 재사용성과 유지보수성을 높입니다.

- resources/books.json : 초기 검색 인덱스를 생성하기 위한 도서 데이터 시드 파일입니다. BookIndexInitializer 초기화 컴포넌트에서 읽어와 Elasticsearch에 색인할 때 사용됩니다.

### 📌 고민 사항
1. 내부 아키텍처 설계 및 Elastic Search 도입 : 이부분은 tr-store-infra에서 기재하였으므로 생략하겠습니다.

### 🔖 테스트 커버리지
 - 테스트 코드 비즈니스 계층(application/**)만 적용 : 일정적인 부분에 있어서 조금 빠듯해서 비즈스로직만 적용하였습니다 외부 입출력 영역은 web-service를 통해 확인할 수 있는 부분이기에 넣지 않았습니다.
 - search service 테스트 커버리지 결과
<img width="1057" height="172" alt="스크린샷 2025-08-22 오전 11 34 36" src="https://github.com/user-attachments/assets/d851c1d7-3ba9-4418-b897-532cdd4c67b7" />
