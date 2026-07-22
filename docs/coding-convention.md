# bandi 코딩 컨벤션

> Spring Boot 3.5.x · Java 17 · MyBatis · MySQL · JSP · Flyway
> 최종 수정: 2026-07-12 (v0.7 — 뷰 기술 Thymeleaf → JSP 전환: 12장 전면 개정, 6.6/7.2/13/18장 연동 수정, war 패키징)

---

## 0. 규칙 등급

모든 규칙은 MUST/SHOULD 중 한 등급을 가지며, DOMAIN 태그가 조합될 수 있다. 등급 표기가 없으면 **MUST**로 간주한다.

| 등급 | 의미 |
|------|------|
| **MUST** | 반드시 지킨다. 위반 시 코드리뷰에서 반려 |
| **SHOULD** | 지키기 위해 노력한다. 합리적 사유가 있으면 예외 허용 (PR에 사유 명시) |
| **DOMAIN** | 등급이 아닌 **적용 범위 태그** — 도메인 객체(핵심 비즈니스 로직)에만 적용, DTO/설정/매핑 객체는 예외. MUST/SHOULD와 조합해 쓰며, DOMAIN 단독 표기는 (DOMAIN, MUST)를 의미한다 |

---

## 1. Java 네이밍

### 1.1 식별자에는 영문/숫자/언더스코어만 허용
변수명, 클래스명, 메소드명에는 영어와 숫자만 사용한다. 언더스코어는 상수와 테스트 메소드명에서만 사용한다.

### 1.2 한국어 발음대로 표기 금지
식별자는 한글 발음이 아닌 영어로 표기한다. 단, 한국어 고유명사(예: `bandi`)는 예외다.

```java
// 나쁜 예
String juso;
// 좋은 예
String address;
```

### 1.3 패키지 이름은 소문자로만 구성
언더스코어나 대문자를 섞지 않는다.

```java
// 나쁜 예: com.bandi.clubMember
// 좋은 예: com.bandi.member
```

### 1.4 클래스/인터페이스/record/enum 이름은 대문자 카멜 케이스(파스칼) 사용
```java
public class MemberService {}
public record MemberResponse() {}
public enum ClubRole {}
```

### 1.5 클래스 이름은 명사 사용
```java
// 나쁜 예: public class ManageMember
// 좋은 예: public class MemberManager
```

### 1.6 인터페이스 이름은 명사/형용사 사용
```java
public interface MemberMapper {}   // 명사
public interface Searchable {}     // 형용사
```

### 1.7 메소드 이름은 소문자 카멜 케이스 사용
테스트 클래스의 메소드 이름에서는 언더스코어를 허용한다.

```java
// 프로덕션
public void openSession() {}
// 테스트
void 회원가입_시_이메일이_중복되면_예외가_발생한다() {}
```

### 1.8 메소드 이름은 동사/전치사로 시작
동사는 [15. 동사 사전]을 따른다. 형 변환 메소드는 전치사(`to~`)로 시작할 수 있다.

```java
// 나쁜 예: memberCreate()
// 좋은 예: createMember(), toResponse()
```

### 1.9 변수 이름은 소문자 카멜 케이스 사용
멤버변수/지역변수/메소드 인자 모두 해당.

### 1.10 임시 변수 외에는 한 글자 이름 사용 금지
반복문 인덱스, 람다 파라미터 등 짧은 범위의 임시 변수만 예외.

### 1.11 테스트 클래스 이름은 `Test`로 끝남
`MemberServiceTest`

### 1.12 상수 이름은 대문자 + 언더스코어
```java
public static final int MAX_CLUB_MEMBER_COUNT = 100;
```

### 1.13 약어는 가급적 사용 지양 **(SHOULD)**
단어가 지나치게 길 경우 아래 합의된 약어만 사용한다. 목록에 없는 약어가 필요하면 이 문서에 추가한 뒤 사용한다.

| 원어 | 약어 |
|------|------|
| description | desc |
| datetime | dttm |
| identifier | id |
| request | req (변수명 한정, 클래스명은 `~Request`) |
| response | res (변수명 한정, 클래스명은 `~Response`) |

### 1.14 DTO 클래스 접미사 규칙
| 용도 | 접미사 | 패키지 | 예시 |
|------|--------|--------|------|
| 요청(폼 바인딩 포함) | `~Request` | `dto.request` | `MemberCreateRequest` |
| 응답/뷰 렌더링 | `~Response` | `dto.response` | `MemberResponse` |
| 검색 조건 | `~Condition` | `dto.request` | `MemberSearchCondition` |
| MyBatis 전용 파라미터 | `~Param` | `dto.request` | `MemberUpdateParam` |

`~Dto`, `~VO` 접미사는 사용하지 않는다.

---

## 2. 선언

### 2.1 소스 파일당 1개의 탑레벨 클래스
한 파일에 여러 클래스가 필요하면 내부 클래스(또는 내부 record)로 선언한다.

### 2.2 static import에만 와일드카드(*) 허용
```java
// 나쁜 예: import java.util.*;
// 좋은 예
import java.util.List;
import static org.assertj.core.api.Assertions.*;  // 테스트에서 허용
```

### 2.3 Annotation 선언 후 새 줄 사용
파라미터가 없는 애노테이션 1개는 같은 줄 선언을 허용한다.

```java
@PostMapping("/members")
public String createMember() {}

@Override public void destroy() {}   // 허용
```

### 2.4 한 줄에 한 문장
변수 선언도 한 문장에 하나의 변수만 다룬다.

### 2.5 배열의 대괄호는 타입 뒤에 선언
```java
String[] names;   // O
String names[];   // X
```

### 2.6 var 사용 규칙 **(SHOULD)**
우변에서 타입이 명백할 때만 `var`를 허용한다. 메소드 반환값을 받는 경우에는 명시적 타입을 쓴다.

```java
var members = new ArrayList<Member>();          // 허용
List<Member> members = memberMapper.searchAll();  // 반환값은 명시적 타입
```

---

## 3. 들여쓰기 / 포맷

### 3.1 소프트탭(공백) 4칸 사용
탭 문자를 사용하지 않는다. IntelliJ 설정: `Use tab character` 해제, `Tab size / Indent = 4`.
프로젝트 루트에 `.editorconfig`를 두어 강제한다. ([16. 도구 설정] 참조)

### 3.2 블럭 들여쓰기
클래스, 메소드, 제어문 등 코드 블럭이 생길 때마다 1단계씩 들여쓴다.

### 3.3 한 줄 최대 길이는 120자 **(SHOULD)**

---

## 4. 중괄호

### 4.1 K&R 스타일로 중괄호 선언
```java
public class MemberService {
    public boolean isActive(Member member) {
        if (member == null) {
            return false;
        }
        return member.isActive();
    }
}
```

### 4.2 닫는 중괄호와 같은 줄에 catch, finally, while(do-while), else 선언
else 자체는 6.2에 따라 지양하되, 부득이하게 쓸 경우 이 스타일을 따른다.

```java
try {
    writeLog();
} catch (IOException e) {
    reportFailure(e);
} finally {
    writeFooter();
}
```

### 4.3 빈 블럭은 같은 줄에서 중괄호 닫기 허용
```java
public void commit() {}
```

### 4.4 조건/반복문에 중괄호 사용
한 줄로 끝나더라도 중괄호를 쓴다.

```java
if (exp == null) {
    return false;
}
```

### 4.5 switch는 화살표(->) 표현식을 기본으로 사용
Java 17이므로 전통 콜론 스타일 switch문 대신 switch 표현식을 우선한다. 콜론 스타일이 불가피한 경우 콜론 앞에 공백을 넣지 않는다(`case GOLD:`).

```java
String label = switch (role) {
    case ADMIN -> "운영진";
    case LEADER -> "팀장";
    case MEMBER -> "일반 부원";
};
```

---

## 5. 공백

### 5.1 공백으로 줄을 끝내지 않음
### 5.2 닫는 대괄호 뒤에 다른 선언이 올 경우 공백 삽입 (`String[] names;`)
### 5.3 여는 중괄호 앞, 닫는 중괄호 뒤 키워드 앞에 공백 삽입
### 5.4 제어문 키워드와 여는 소괄호 사이에 공백 삽입 (`if (`, `for (`, `switch (`)
### 5.5 식별자와 여는 소괄호 사이에 공백 미삽입 (`createMember(`, `@Cached(`)
### 5.6 타입 캐스팅 소괄호 내부 공백 미삽입 (`(String) rawLine`)
### 5.7 콤마/구분자 세미콜론의 뒤에만 공백 삽입 (`display(level, message, i)`)
### 5.8 향상된 for문과 삼항연산자의 콜론 앞뒤에 공백 삽입
switch case의 콜론은 4.5에 따라 앞 공백 없음. (전통 switch 자체를 지양)

### 5.9 주석 기호 전후 공백 삽입
```java
System.out.print(true); // 주석 기호 앞뒤 공백
/* 블록 주석도 동일 */
```

---

## 6. 클린 코드 규칙 (우테코 수정판)

> 원본은 우아한테크코스 미션용 훈련 규칙이다. JSP SSR 웹 프로젝트 현실에 맞게 적용 범위를 조정했다.

### 6.1 한 메소드에 한 단계의 들여쓰기만 허용 **(SHOULD)**
중첩이 2단계 이상이면 private 메소드 추출 또는 Stream API로 리팩토링을 시도한다. 프레임워크 콜백/람다 내부는 예외.

### 6.2 else 예약어 사용 지양 **(SHOULD)**
early return(가드 절)을 기본 패턴으로 한다. 삼항연산자나 switch 표현식으로 자연스럽게 표현되는 경우는 위반이 아니다.

```java
// 나쁜 예
if (member.isActive()) {
    return doSomething();
} else {
    throw new InactiveMemberException();
}
// 좋은 예
if (!member.isActive()) {
    throw new InactiveMemberException();
}
return doSomething();
```

### 6.3 원시값과 문자열 포장 **(DOMAIN)**
도메인 개념을 가진 값(예: 이메일, 공연 회차, 회비 금액)은 record로 포장한다. Controller 파라미터, DTO 필드, MyBatis 매핑 필드는 원시값을 허용한다.

```java
public record Email(String value) {
    public Email {
        if (!value.matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("이메일 형식이 아닙니다: " + value);
        }
    }
}
```

### 6.4 일급 컬렉션 적용 **(DOMAIN)**
도메인 로직이 컬렉션을 다룰 때(예: 출연진 목록, 회비 납부 내역) 일급 컬렉션으로 포장한다. 단순 조회 결과 전달(`List<MemberResponse>`)에는 강제하지 않는다.

### 6.5 인스턴스 변수 개수 최소화 **(DOMAIN, SHOULD)**
도메인 객체는 인스턴스 변수를 줄이도록 노력한다. 3개를 넘으면 응집도 있는 값들을 묶어 별도 객체로 추출할 수 있는지 검토한다. DTO, 설정 클래스, 테이블 매핑 객체는 예외.

### 6.6 getter/setter 규칙 — JSP 수정판
**JSP EL의 `${member.name}`은 내부적으로 getter를 호출하므로, 뷰에 전달되는 객체는 getter가 필수다.** 따라서 계층별로 규칙을 나눈다.

| 계층 | getter | setter |
|------|--------|--------|
| model (도메인 객체) | **허용** — 단, 아래 "꺼내서 판단 금지" 원칙 적용 | 금지 |
| 응답 DTO (`~Response`) | 필수 (record 사용 시 자동) | 금지 (불변) |
| 요청 DTO (`~Request`, 폼 바인딩) | 필수 | 허용 (7.2 참조) |
| MyBatis 매핑 객체 | 필수 | 원칙적으로 금지, resultMap 매핑 문제 시 허용 |

> `MyBatis 매핑 객체` 행은 9.3의 비대화 경고에 따라 Persistence/Domain을 **분리한 이후의 Persistence 객체에만** 적용한다. 분리 전 기본 구조에서 model은 매핑 대상을 겸하더라도 `model` 행(setter 금지)을 따른다 — 매핑 때문에 setter가 필요해지는 순간이 곧 9.3의 분리 트리거다.

**핵심은 getter의 존재가 아니라 사용 방식이다.** `getName()`으로 값을 꺼내 표시·전달하는 것은 문제가 아니다. 문제는 **꺼낸 값으로 외부에서 판단(분기)하는 것**이며, 이런 코드가 보이면 판단 로직을 model의 메소드로 옮긴다.

```java
// 허용 — 값을 꺼내 전달/표시
String name = member.getName();

// 나쁜 예 — 꺼낸 값으로 외부에서 판단
if (member.getRole() == ClubRole.ADMIN) { ... }
// 좋은 예 — 객체에 묻기
if (member.isAdmin()) { ... }
```

Lombok 규칙:
- `@Data` 금지 (equals/hashCode/setter가 무분별하게 생성됨)
- `@Setter` 클래스 레벨 사용은 요청 DTO에만 허용
- `@Getter`는 전 계층 허용. 단 model에서는 위 원칙에 따라 판단 메소드를 함께 제공한다
- DI는 `@RequiredArgsConstructor` + `private final` 필드로 통일 (필드 주입 `@Autowired` 금지)

### 6.7 메소드 인자 수 제한 **(SHOULD)**
3개 이하를 기본으로 하고, 4개 이상이면 `~Condition`/`~Param` 객체로 묶는 것을 먼저 검토한다.
단, **같은 타입이 연속되는 시그니처**(`String, String, ...`)나 boolean 플래그가 섞인 시그니처는 개수와 무관하게 객체로 묶는다 — 호출부에서 순서 실수가 나는 유형이기 때문이다. **(MUST)**

### 6.8 디미터의 법칙 — 한 줄에 점 하나 **(SHOULD)**
객체 그래프를 파고드는 체이닝(`order.getMember().getClub().getName()`)은 금지. 단, 아래는 예외다.
- Stream API 체이닝
- 빌더 패턴
- fluent API (StringBuilder, MockMvc, AssertJ 등)
- record 컴포넌트 접근은 1단계(`response.member()`)까지 예외로 허용. 2단계 체이닝(`response.member().name()`)은 지양하되, Service의 DTO 조립 코드에서만 1회 허용

### 6.9 메소드는 한 가지 일만 담당
메소드가 "~하고 ~한다"로 설명되면 분리를 검토한다.

### 6.10 클래스를 작게 유지 **(SHOULD)**
분리 기준은 줄 수가 아니라 **책임이 2개 이상 느껴지는가**다. 300줄은 분리를 강제하는 기준이 아니라 "책임을 점검해보라"는 신호로만 쓴다 — 300줄인데 응집도 높은 Service는 유지하고, 200줄이어도 책임이 섞였으면 분리한다.

---

## 7. record 사용 규칙

### 7.1 record를 기본으로 사용하는 곳
- 응답 DTO (`~Response`)
- 검색 조건 (`~Condition`)
- MyBatis 파라미터 객체 (`~Param`)
- 도메인 값 객체 (Email, Money 등)

```java
public record MemberResponse(Long memberId, String name, String email, ClubRole role) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(...);
    }
}
```

### 7.2 record를 쓰지 않는 곳 — 폼 바인딩 객체 ⚠️
`<form:form modelAttribute>` + `<form:input path>`로 바인딩되는 **요청 DTO는 setter가 있는 일반 클래스**로 작성한다.

**이유 (알려진 이슈):**
1. Spring MVC의 `@ModelAttribute` 생성자 바인딩이 record를 지원하긴 하지만, **검증 실패 후 폼 재렌더링** 시 `BindingResult`와 함께 부분적으로 채워진 객체를 다시 모델에 넣는 흐름에서 프로퍼티 단위 바인딩(setter 방식)보다 동작이 미묘하다. 특히 타입 변환 실패 필드가 있으면 생성자 바인딩은 객체 생성 자체가 어그러질 수 있다.
2. Spring form 태그(`<form:input path>`)는 프로퍼티 접근으로 값을 다시 읽는데, record는 문제없지만 **바인딩 방향(쓰기)** 이 없어 GET 폼 초기값 세팅 패턴이 제한된다.
3. 중첩 폼 객체, 리스트 바인딩(`members[0].name`)은 setter 방식에서만 안정적이다.

`~Condition`도 `<form:form>`으로 폼에 바인딩한다면 setter 클래스로 작성한다. record `~Condition`은 form 태그를 쓰지 않는 쿼리 파라미터 직접 바인딩(`@ModelAttribute` 생성자 바인딩) 전용이다.

```java
@Getter @Setter
public class MemberCreateRequest {

    @NotBlank(message = "{member.name.required}")
    private String name;

    @Email
    @NotBlank
    private String email;
}
```

### 7.3 MyBatis + record 매핑 이슈 ⚠️
MyBatis 3.5.x는 record를 **생성자 기반**으로 매핑한다. 아래 함정에 주의한다.
1. `resultType`으로 record를 지정하면 **생성자 파라미터 이름과 컬럼(또는 alias) 이름이 일치**해야 한다. `map-underscore-to-camel-case: true` 상태에서 `member_id → memberId` 변환은 적용된다.
2. 컴포넌트가 많은 record에서 SELECT 컬럼 누락 시 컴파일 타임에 잡히지 않고 **null이 아니라 매핑 실패 예외 또는 엉뚱한 값**이 들어갈 수 있다. 컬럼 수가 5개를 넘는 조회 결과는 명시적 `<resultMap>` + `<constructor>` 사용을 권장한다. **(SHOULD)**
3. record에 컴팩트 생성자 검증 로직이 있으면 매핑 시에도 실행된다. 조회 전용 record에는 검증 로직을 넣지 않는다.

### 7.4 record 공통 규칙
- 컴포넌트가 4개를 넘으면 정적 팩토리(`from`, `of`)를 두어 호출부 가독성을 확보한다. **(SHOULD)**
- record에 비즈니스 로직 메소드를 넣지 않는다. 변환(`toXxx`)과 단순 파생값 계산만 허용.
- `@Builder`를 record에 붙이지 않는다. 컴포넌트가 많아 빌더가 필요하다는 것은 분리 신호다.
- **record → 일반 클래스 전환은 컨벤션 위반이 아니다.** 상속, 프록시, 빌더, 가변성 등 record로 감당할 수 없는 요구가 생기면 전환하고, PR에 전환 사유를 한 줄 명시한다. 반대로 "나중에 필요할지 모르니" 미리 클래스로 만드는 것은 금지 — 필요해진 시점에 바꾼다.

---

## 8. 패키지 / 리소스 구조

베이스 패키지는 `kr.ac.tukorea.bandi`. feature 패키지는 `domain` 아래에 두는 package-by-feature 구조를 따른다.

### 8.1 Java 패키지 구조

```
kr.ac.tukorea.bandi
├── BandiApplication.java
│
├── domain
│   └── member                             # ── feature 단위 (club, performance, fee ...) ──
│       ├── controller
│       │   ├── MemberController.java          # SSR (JSP)
│       │   └── MemberApiController.java       # /api — global.swagger.MemberApiDocs 구현
│       ├── service
│       │   └── MemberService.java
│       ├── mapper
│       │   └── MemberMapper.java              # MyBatis 인터페이스
│       ├── model                              # 도메인 객체 (비즈니스 로직, DB 매핑 대상)
│       │   ├── Member.java
│       │   └── ClubRole.java
│       ├── dto
│       │   ├── request                        # 폼 바인딩 / 검색 조건
│       │   │   ├── MemberCreateRequest.java
│       │   │   ├── MemberUpdateRequest.java
│       │   │   └── MemberSearchCondition.java
│       │   └── response                       # 뷰/API 렌더링용
│       │       └── MemberResponse.java
│       └── exception                          # feature별 커스텀 예외 (9.4)
│           └── MemberNotFoundException.java
│
└── global
    ├── config                             # WebConfig, MyBatisConfig ...
    ├── security                           # SecurityConfig, @LoginMember, ArgumentResolver
    ├── swagger                            # OpenApiConfig, ApiTag, 기능별 ~ApiDocs 인터페이스
    ├── exception                          # ErrorCode, BusinessException, ErrorResponse, ApiExceptionHandler, SsrExceptionHandler
    └── response                           # /api 공통 응답 객체 (현재 미사용 — 도입 여부는 16.3)
```

### 8.2 리소스 구조

```
src/main/resources
├── application.yaml
├── messages.properties                    # 검증/화면 메시지 (하드코딩 금지)
│
├── db
│   └── migration                          # Flyway — V{yyyyMMddHHmm}__{설명}.sql (11.4)
│
├── mapper
│   └── member                             # feature별 하위 폴더, 인터페이스와 1:1
│       └── MemberMapper.xml
│
├── static
│   ├── css
│   │   ├── common.css                     # 리셋, CSS 변수, 공통 컴포넌트
│   │   ├── layout.css                     # 헤더/푸터/그리드 골격
│   │   └── member                         # 페이지별 (템플릿 1:1)
│   │       ├── list.css
│   │       └── form.css
│   └── js
│       ├── common
│       │   └── api.js                     # CSRF fetch 래퍼
│       └── member
│           └── list.js
│
src/main/webapp
└── WEB-INF
    ├── tags                               # 공용 태그 파일 — layout, header, footer, pagination ...
    │   └── layout.tag                     # 기본 레이아웃 (공유 자원, 22.5)
    └── views                              # JSP 뷰 (spring.mvc.view.prefix)
        ├── member/                        # list, detail, form (뷰 이름 = member/list)
        └── error/                         # 404, 500
```

테스트 패키지는 main과 동일한 구조로 미러링한다 (`domain.member.service.MemberServiceTest`).

### 8.3 패키지 규칙 / 의존성 방향

**허용되는 의존성 방향 (이 방향만 존재해야 한다):**

```
[feature 내부]                       [feature 간]

Controller                           member.Service ──→ club.Service   O
    │  (dto만 주고받음)
    ▼
Service ──→ 다른 feature의 Service
    │
    ▼
Mapper ──→ model
```

**금지 방향:**

| 금지 | 이유 |
|------|------|
| Controller → Mapper ❌ | Service를 건너뛰면 트랜잭션/변환 경계가 무너짐 |
| Controller → model ❌ | Controller는 dto만 다룬다 (9.1) |
| Mapper → Service ❌ | 역방향 의존 |
| model → Service/Mapper/Controller ❌ | model은 어떤 상위 계층도 모른다 (순수 객체) |
| dto → Service ❌ | dto는 데이터 운반만, 빈 주입 금지 |
| feature.mapper/model → 다른 feature ❌ | feature 간 통로는 Service뿐 |

- feature 패키지 간 직접 참조는 **service → 다른 feature의 service**까지만 허용한다. 다른 feature의 mapper/model을 직접 사용하지 않는다.
- 순환 참조가 생기면 공통 개념을 별도 feature로 추출한다.
- `global`에 특정 feature 전용 로직을 넣지 않는다. 인증 관련 클래스는 `config`가 아닌 `security`에 응집시킨다.
- **Swagger 문서 예외**: `global.swagger`는 API 문서의 단일 관리 지점이다. `~ApiDocs`가 HTTP 계약을 표현하기 위해 feature의 request/response DTO를 참조하는 것만 허용하며, feature의 Service·Mapper·model 참조와 비즈니스 로직은 금지한다.

## 9. 레이어 규칙

### 9.1 Controller
- 요청 바인딩, 검증 트리거(`@Valid`), 뷰/리다이렉트 결정만 담당한다. 비즈니스 로직 금지.
- 반환 타입은 뷰 이름 `String` 또는 `ResponseEntity`(API 한정)로 통일한다.
- **PRG 패턴 필수**: 폼 POST 성공 시 `redirect:`로 응답한다. 사용자 피드백은 `RedirectAttributes.addFlashAttribute`를 사용한다.
- URL은 소문자 케밥 케이스, 복수형 리소스: `/members`, `/members/{memberId}/fee-records`
- 화면용 컨트롤러와 API 컨트롤러(`@RestController`)를 분리한다. API는 `/api` prefix.

### 9.2 Service
- `@Transactional`은 클래스 레벨 `readOnly = true` + 쓰기 메소드에 `@Transactional` 재선언을 기본으로 한다.
- model ↔ DTO 변환은 Service 경계에서 수행한다. Controller에 model 객체를 노출하지 않는다.
- 비즈니스 판단은 model의 메소드에 위임한다. Service는 흐름 조율(트랜잭션, 변환, 다른 feature 호출)을 담당하고, `if (member.getRole() == ...)` 식의 판단 로직을 직접 갖지 않는다.
- 조회 전용 로직이 커지면 `~QueryService`로 분리를 검토한다. **(SHOULD)**

### 9.3 Model (도메인 객체)
- 데이터 흐름: `Request(dto) → [Service에서 변환] → Model → Mapper 저장/조회 → [Service에서 변환] → Response(dto) → View`
- model은 비즈니스 로직과 판단 메소드를 가진다 (`isAdmin()`, `canEditPerformance()`). 6장의 DOMAIN 등급 규칙(getter 지양, 원시값 포장, 일급 컬렉션)이 적용되는 계층이다.
- model은 MyBatis 조회 결과가 매핑되는 대상이기도 하다. 매핑을 위한 기본 생성자/접근이 필요한 경우 최소한으로 허용한다.
- **예외**: 로직이 전혀 없는 단순 코드성 테이블(코드-이름 매핑 등)은 model을 생략하고 dto만 사용할 수 있다. 단, 이후 판단 로직이 생기면 즉시 model로 승격한다.
- **비대화 경고**: 현재 구조에서 model은 DB 매핑 대상과 비즈니스 객체 역할을 겸한다. 이 규모에선 단순함이 이득이지만, **매핑 요구사항(기본 생성자, 매핑 전용 필드, resultMap 편의를 위한 setter)이 도메인 설계를 침식하기 시작하면** 해당 feature에 한해 Persistence 객체(매핑 전용)와 Domain 객체(로직 전용)로 분리한다. 전 feature 일괄 분리가 아니라 비대해진 곳만 분리하며, 분리 시 아래 목록에 해당 feature를 기록한다.
- **Persistence/Domain 분리된 feature 목록: (현재 없음)**

### 9.4 예외 처리 (공통 / 커스텀)

구조: **ErrorCode(enum) → BusinessException(공통 부모) → feature별 커스텀 예외 → 핸들러 2개(SSR/API)**

```java
// global/exception/ErrorCode.java — 에러의 단일 출처
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통 (C)
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C002", "권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "일시적인 오류가 발생했습니다."),
    // member (M)
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 단원입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M002", "이미 사용 중인 이메일입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
```

- 코드 접두사는 feature별 고정: `C` 공통, `A` auth, `M` member, `CA` calendar, `FI` file, `PN` public notice, `NI` internal notice, `RS` resource, `AR` activity record, `EV` event, `F` fee, `PO` policy, `P` performance, `AS` asset, `R` reservation... 새 feature 추가 시 이 문서에 접두사를 등록한다.
- `message`는 **사용자에게 그대로 보여줄 문장**으로 작성한다. 내부 사정("DB 커넥션 실패")을 노출하지 않는다.

```java
// global/exception/BusinessException.java
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

// domain/member/exception/MemberNotFoundException.java — feature별 커스텀 예외
public class MemberNotFoundException extends BusinessException {

    public MemberNotFoundException(Long memberId) {
        super(ErrorCode.MEMBER_NOT_FOUND);
    }
}
```

- 커스텀 예외 위치: `domain.{feature}.exception`. Service에서 `throw new BusinessException(ErrorCode.X)`를 직접 던지지 말고 **의미 있는 커스텀 예외 클래스**를 만든다 (호출부 가독성 + 테스트에서 예외 타입 단언 가능).
- Service에서 `null` 반환 금지. Mapper의 `Optional`은 Service에서 `orElseThrow(() -> new MemberNotFoundException(id))`로 끊고, Controller까지 끌고 가지 않는다.

**핸들러는 SSR용/API용 2개로 분리한다.**

```java
// global/exception/ApiExceptionHandler.java — /api 전용
@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        log.warn("비즈니스 예외 - code={}, message={}", e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(ErrorResponse.from(e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        log.debug("검증 실패 - {}", e.getBindingResult());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, e.getBindingResult()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("예상치 못한 예외", e);   // 유일하게 스택트레이스 포함
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.from(ErrorCode.INTERNAL_ERROR));
    }
}
```

```java
// global/exception/ErrorResponse.java
public record ErrorResponse(String code, String message, List<FieldError> fieldErrors) {

    public record FieldError(String field, String reason) {}
}
```

SSR용 `@ControllerAdvice`(`SsrExceptionHandler`)는 동일한 분기로 에러 페이지(`error/404.html`, `error/500.html`)를 반환한다. 단, **폼 검증 실패는 전역 핸들러로 보내지 않는다** — 12.4대로 해당 Controller에서 `BindingResult`를 받아 폼을 재렌더링한다.

**예외 로깅 레벨 규칙:**
| 상황 | 레벨 | 스택트레이스 |
|------|------|--------------|
| BusinessException (예상된 실패 흐름) | `warn` 한 줄 | X |
| 입력 검증 실패 (흔한 사용자 실수) | `debug` | X |
| 그 외 모든 예외 (버그/장애) | `error` | **O** |

- 예외를 잡아서 로그만 찍고 삼키는 것 금지. 처리 못 하면 다시 던진다.
- `e.printStackTrace()` 금지, 같은 예외를 여러 계층에서 중복 로깅 금지 (핸들러에서 한 번만).

### 9.5 Spring 공통 기본 규칙
- **DI는 생성자 주입만** 사용한다: `private final` + `@RequiredArgsConstructor`. 필드 주입(`@Autowired`)·세터 주입 금지.
- 설정값 주입은 `@Value` 대신 **`@ConfigurationProperties` + record**를 사용한다. 설정 클래스는 `global.config`에 둔다.
```java
@ConfigurationProperties(prefix = "bandi.upload")
public record UploadProperties(String basePath, long maxSizeBytes) {}
```
- 매직 넘버/문자열 하드코딩 금지. 의미 있는 상수 또는 설정값으로 추출한다.
- `@Transactional`은 public 메소드에만 동작한다. **같은 클래스 내부 호출(self-invocation)에는 적용되지 않으므로**, 트랜잭션 경계가 필요한 메소드를 내부에서 호출하는 구조를 만들지 않는다.
- 현재 시각은 `LocalDateTime.now()` 직접 호출 대신 `Clock`을 주입받아 사용한다. (테스트에서 시간 고정 가능) **(SHOULD)**
- `@Component` 남발 금지. 상태 없는 순수 함수 유틸은 static 메소드로, 의존성이 필요하면 빈으로.
- Bean 간 순환 참조가 발생하면 `@Lazy`로 덮지 말고 설계를 수정한다.

---

## 10. MyBatis

### 10.1 파일 배치와 네이밍
- 인터페이스: `domain.{feature}.mapper.{도메인}Mapper` (예: `MemberMapper`)
- XML: `src/main/resources/mapper/{feature}/{도메인}Mapper.xml` — 인터페이스와 1:1
- `@MapperScan("kr.ac.tukorea.bandi.domain.**.mapper")` 방식으로 통일 (개별 `@Mapper` 생략)
- Mapper의 조회 결과는 model(또는 record 응답 전용 객체)로 매핑한다. dto.request 객체를 resultType으로 쓰지 않는다.

### 10.2 메소드 네이밍 — 동사 사전 적용
[15. 동사 사전]의 동사를 그대로 쓴다. 특히 아래 구분을 지킨다.

| 동사 | 의미 | 반환 |
|------|------|------|
| `search~` | 조건 검색 (결과 여러 개) | `List<T>` |
| `lookup~` | 단건 조회 | `Optional<T>` |
| `exists~` | 존재 확인 | `boolean` |
| `count~` | 개수 | `int` / `long` |
| `insert~` | 저장 | `int` (영향 행 수) |
| `update~` | 수정 | `int` |
| `delete~` | 소프트 삭제 (deleted_dttm 세팅) | `int` |
| `remove~` | 하드 삭제 (실제 DELETE) | `int` |

```java
public interface MemberMapper {
    Optional<Member> lookupById(Long memberId);
    List<Member> searchByCondition(MemberSearchCondition condition);
    boolean existsByEmail(String email);
    int insert(Member member);
    int delete(Long memberId);   // UPDATE ... SET deleted_dttm = NOW()
}
```

### 10.3 XML 작성 규칙
- SQL 키워드는 대문자 (`SELECT`, `FROM`, `LEFT JOIN`)
- `parameterType`은 생략한다 (타입 추론에 맡김). `resultType`/`resultMap`은 명시한다.
- **`${}` 사용 금지.** 동적 정렬이 필요하면 `<choose>` 화이트리스트로 처리한다. `#{}`만 사용.
- 소프트 삭제 대상 테이블 조회에는 `deleted_dttm IS NULL` 조건을 항상 포함한다. 누락 방지를 위해 공통 `<sql id="notDeleted">` fragment를 사용한다.
- 동적 SQL `<if>` 조건에는 `@` 없이 널/공백 체크를 일관되게: `<if test="name != null and name != ''">`
- 조인 결과 매핑은 `<resultMap>` + `<association>`/`<collection>`을 쓰고, 컬럼 alias 남발로 평탄화하지 않는다. **(SHOULD)**

### 10.4 application.yaml 설정 고정값
실제 `src/main/resources/application.yaml`의 mybatis 블록이 이 절과 글자 단위로 일치해야 한다. (17.1의 공통 설정 예시는 이 절을 참조만 한다)

```yaml
mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    return-instance-for-empty-row: true   # LEFT JOIN의 빈 행을 null 대신 인스턴스로 매핑
    default-fetch-size: 100
    jdbc-type-for-null: "NULL"            # 반드시 문자열로 인용 — 인용 없으면 YAML null로 파싱되어 설정이 사라짐
```

---

## 11. MySQL / Flyway

### 11.1 테이블/컬럼 네이밍 — 소문자 snake_case
> IRM 컨벤션(테이블 대문자 카멜)은 채택하지 않는다. MySQL은 `lower_case_table_names` 설정과 OS에 따라 테이블명 대소문자 동작이 달라져(macOS 개발 → Linux 배포 시 사고 위험) 소문자 snake_case로 통일한다.

- 테이블: 소문자 snake_case 단수형 — `member`, `club`, `fee_record`, `performance_cast`
- 컬럼: 소문자 snake_case — `member_id`, `created_dttm`
- 연결 테이블: 두 테이블명 조합 — `club_member`
- **예외**: 프레임워크가 스키마를 제공·강제하는 테이블(spring-session의 `SPRING_SESSION`/`SPRING_SESSION_ATTRIBUTES` — 18.4)은 공식 스키마 원문을 그대로 사용하며, 11.1~11.3 네이밍 규칙의 예외다.

### 11.2 공통 컬럼
모든 테이블에 아래 컬럼을 둔다.

```sql
created_dttm    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
updated_dttm    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
deleted_dttm    DATETIME(6) NULL     -- 소프트 삭제 대상 테이블만
```

- PK는 `{테이블명}_id BIGINT AUTO_INCREMENT` 로 통일 (`id` 단독 금지 — 조인 시 모호함 방지)
- boolean 컬럼은 `is_` 접두사 + `TINYINT(1)` — `is_active`
- enum 값은 DB에 `VARCHAR`로 저장 (MySQL ENUM 타입 금지 — 마이그레이션 지옥)

### 11.3 제약조건/인덱스 네이밍
| 종류 | 형식 | 예시 |
|------|------|------|
| PK | `pk_{table}` | `pk_member` |
| FK | `fk_{table}_{ref_table}` | `fk_club_member_member` |
| UNIQUE | `uk_{table}_{columns}` | `uk_member_email` |
| INDEX | `idx_{table}_{columns}` | `idx_fee_record_member_id` |
| CHECK | `ck_{table}_{meaning}` | `ck_member_role_code` |

- 코드 컬럼의 허용값은 Java enum과 `CHECK` 제약을 함께 사용한다. enum·제약·관련 문서는 같은 커밋에서 변경한다.
- generated column은 MySQL 8의 결정적 표현식으로 계산할 수 있고 UNIQUE·조회 인덱스 등 DB 정합성에 필요한 경우에만 사용한다.
- generated column 이름과 계산식, `VIRTUAL`/`STORED` 선택 이유를 스키마 정본에 기록한다. 애플리케이션에서 직접 값을 INSERT·UPDATE하지 않는다.
- NULL을 예약값으로 치환하는 generated key를 사용하면 예약값이 실제 PK·코드로 생성될 수 없다는 제약을 함께 명시한다.
- 서비스에서만 판단 가능한 권한·상태 전이·외부 시스템 결과를 generated column이나 복잡한 `CHECK`로 대신하지 않는다.

### 11.4 Flyway
- 파일명: `V{yyyyMMddHHmm}__{설명_스네이크}.sql` — `V202607071930__create_fee_record.sql` (언더스코어 2개 주의)
- **날짜 기반 버전을 쓰는 이유**: 순차 정수(`V3`)는 여러 브랜치/여러 AI 에이전트가 병렬로 마이그레이션을 추가할 때 번호가 충돌한다. 날짜 버전은 충돌 확률이 사실상 없다.
- merge 후 내 마이그레이션 버전이 이미 적용된 버전보다 과거가 되면(out-of-order), `spring.flyway.out-of-order`를 켜지 말고 **파일명을 현재 시각으로 리네임 후 리베이스**한다. (아직 dev/prod에 미적용인 경우에만)
- 위치: `src/main/resources/db/migration`
- **dev/prod에 적용된 마이그레이션 파일은 절대 수정하지 않는다.** 변경은 새 버전 파일로.
- **'적용됨' 판정 기준** (에이전트/리뷰 공통 — dev/prod DB를 직접 조회할 수 없으므로 git을 대리 기준으로 쓴다): `git fetch origin` 후 `git log origin/dev -- src/main/resources/db/migration/<파일>` 결과가 있으면(= dev에 머지됨) 적용된 것으로 간주한다. 수정·리네임이 허용되는 파일은 **현재 브랜치에서 내가 추가했고 아직 dev에 머지되지 않은 파일**뿐이다. origin/dev가 아직 없으면 현재 브랜치 밖(다른 브랜치/원격)에 존재한 적 있는 파일은 모두 적용된 것으로 보수적으로 간주한다.
- 하나의 파일은 하나의 논리적 변경만 담는다.
- 데이터 보정(DML)도 마이그레이션으로 관리하되 파일명에 명시: `V202607081000__backfill_member_role.sql`
- spring-session-jdbc의 세션 테이블(`SPRING_SESSION`)도 Flyway로 관리한다. `spring.session.jdbc.initialize-schema: never` 설정 후 스키마 SQL을 마이그레이션 파일로 추가한다. ([18.4] 참조)

---

## 12. JSP

> **스크립틀릿(`<% %>`, `<%= %>`) 전면 금지.** 뷰에는 JSTL/EL/태그 파일만 사용한다. 로직이 필요하면 컨트롤러에서 모델로 가공해 내려보낸다. 유일한 예외는 페이지/태그 최상단의 지시자(`<%@ page %>`, `<%@ tag %>`, `<%@ taglib %>`, `<%@ attribute %>`)다.

### 12.1 디렉토리 구조
```
src/main/webapp/WEB-INF
├── tags                        # 공용 태그 파일 (.tag — 공유 자원, 22.5)
│   ├── head.tag                # 공통 head — 폰트/토큰/Tailwind 매핑 (단일 원본)
│   ├── layout.tag              # 관리자 셸 (사이드바+탑바)
│   ├── layoutPublic.tag        # 공개 셸 (헤더+푸터) — header.tag/footer.tag 사용
│   ├── layoutAuth.tag          # 인증 셸 (중앙 카드)
│   └── pageHead·card·statCard·badge·emptyState·modal·formField.tag  # 컴포넌트 (명세: design-guide.md 6장)
└── views                       # spring.mvc.view: prefix=/WEB-INF/views/, suffix=.jsp
    ├── member
    │   ├── list.jsp            # GET /members
    │   ├── detail.jsp          # GET /members/{id}
    │   └── form.jsp            # 등록/수정 공용 폼
    └── error
        ├── 404.jsp
        └── 500.jsp
```

- 뷰 이름 = `{feature}/{용도}` 소문자. 컨트롤러 반환값과 파일 경로(prefix/suffix 제외)가 그대로 일치해야 한다.
- 등록/수정 폼은 가능하면 `form.jsp` 하나로 공용화한다. **(SHOULD)**
- 모든 JSP/태그 파일은 첫 줄에 인코딩·공백 제거 지시자를 고정한다:
  `<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>`
- taglib prefix는 프로젝트 전체에서 고정: `c`(jakarta.tags.core), `fn`(jakarta.tags.functions), `fmt`(jakarta.tags.fmt — 날짜/금액 포맷, design-guide 10장), `form`(spring form), `sec`(spring security), `t`(tagdir `/WEB-INF/tags`). 다른 prefix를 만들지 않는다.

### 12.2 레이아웃/태그 파일
- 모든 페이지는 `<t:layout title="...">`로 감싼다. 페이지별 css/script는 fragment attribute로 주입한다:

```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout title="단원 목록">
    <jsp:attribute name="script">
        <script type="module" src="<c:url value='/js/member/list.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <section>...</section>
    </jsp:body>
</t:layout>
```

- 반복되는 UI 블록(페이지네이션, 카드, 폼 필드)은 **태그 파일**로 추출하고, 파라미터는 `<%@ attribute %>`로 명시한다: `<%@ attribute name="page" required="true" type="..." %>`
- `<jsp:include>`는 태그 파일로 표현할 수 없는 경우에만 예외적으로 사용한다. **(SHOULD)**

### 12.3 출력/보안 규칙
- **JSP EL `${}`는 자동 이스케이프가 없다** (Thymeleaf `th:text`와 정반대 — 전환 시 최다 실수 지점). **사용자 유래 데이터 출력은 반드시 `<c:out value="${...}"/>`** 를 거친다. HTML 속성 값 내부도 동일하다.
- `<c:out escapeXml="false">` 등 **원문 HTML 출력 금지.** 서버에서 검증된 HTML 렌더링이 정말 필요해지면 그때 화이트리스트 sanitizer와 함께 예외 승인.
- 인라인 `<script>` 블록에 EL로 서버 데이터를 직접 조립하지 않는다. 서버 데이터 → JS 전달은 **`data-*` 속성 + `<c:out>`** 으로 마크업에 싣고, JS에서 `dataset`으로 읽는다.
- 폼은 `<form:form>` 사용 시 CSRF hidden 필드가 자동 삽입된다(`CsrfRequestDataValueProcessor`). **JS fetch로 POST할 때는** layout의 `<meta name="_csrf">`를 헤더로 전달한다. ([13.4] 참조)
- 인증/인가 분기는 `<sec:authorize access="...">`를 사용하고, 컨트롤러/서비스 레벨 인가와 항상 이중으로 건다 (뷰 숨김은 보안이 아님).

### 12.4 폼 바인딩 패턴 (고정)
```jsp
<c:url var="memberFormUrl" value="/members"/>
<form:form modelAttribute="memberCreateRequest" action="${memberFormUrl}" method="post">
    <form:label path="name">이름</form:label>
    <form:input path="name"/>
    <form:errors path="name" element="p" cssClass="field-error"/>
    <button type="submit">등록</button>
</form:form>
```

- 모델 attribute 이름은 요청 DTO 클래스명의 카멜 케이스 그대로 (`memberCreateRequest`)
- 검증 메시지는 하드코딩하지 않고 `messages.properties` 키로 관리: `@NotBlank(message = "{member.name.required}")`
- `<form:input path>`를 쓰고 `name`/`value` 수동 지정을 하지 않는다. Spring form 태그는 값을 자동 이스케이프하므로 `<c:out>` 불필요.
- CSRF hidden 필드를 수동으로 넣지 않는다 (자동 삽입과 중복).

### 12.5 URL/정적 리소스
- 모든 링크는 `<c:url>` 사용 (컨텍스트 패스 대응). 문자열 하드코딩 금지.
- 정적 리소스도 `href="<c:url value='/css/tokens.css'/>"` 형태로 참조.

---

## 13. CSS / JS

### 13.1 파일 배치
```
src/main/resources/static
├── css
│   └── tokens.css              # shadcn 디자인 토큰 (:root CSS 변수 — 공유 자원, 22.5)
└── js
    ├── common
    │   └── api.js              # fetch 래퍼 (CSRF 포함)
    └── member
        └── list.js
```
- **페이지별 CSS 파일은 만들지 않는다.** 스타일은 뷰 안의 Tailwind 유틸리티 클래스로 작성하고, 반복되는 UI 블록은 태그 파일 컴포넌트로 추출한다(13.2).
- JS는 기존 규칙 유지: 템플릿 1개당 JS 파일 1:1 대응, 페이지에서 필요한 파일만 로드한다.

### 13.2 CSS 규칙 — Tailwind + shadcn 토큰

스타일링은 **Tailwind CSS v4 유틸리티 클래스**로만 한다. 디자인 시스템은 **shadcn 토큰 체계**(라이트 전용)를 따른다.

**로딩 구조 (고정)**
- Tailwind는 **Play CDN**(`@tailwindcss/browser`, 버전·SRI 고정)으로 로드한다 — Node 빌드 없음. **prod 포함 유지로 확정** (근거·재검토 조건: design-guide 10장)
- 토큰 값(`:root` CSS 변수)은 `static/css/tokens.css`에서만 관리한다
- `@theme inline` 매핑(토큰→유틸리티)과 `@layer base`는 `WEB-INF/tags/head.tag`의 `<style type="text/tailwindcss">` 블록에만 둔다. 새 토큰은 tokens.css의 `:root`와 이 매핑에 **함께** 추가한다
- 폰트는 **Noto Sans KR**(Google Fonts, head.tag에서 로드) — 폰트 변경도 head.tag에서만
- tokens.css와 head.tag의 Tailwind 블록은 공유 자원이다(22.5) — 수정 전 보고
- 팔레트 값·용도·컴포넌트 레시피의 정본은 `docs/design-guide.md`다. 폐기된 화면 데모는 `docs/archive/style-guide.html`에만 보관한다

**색상 (MUST)**
- 색상은 **shadcn 시맨틱 토큰 유틸리티만** 사용한다: `bg-background`, `text-foreground`, `text-muted-foreground`, `bg-primary`/`text-primary-foreground`/`bg-primary-strong`(hover), `bg-secondary`, `bg-accent`/`text-accent-foreground`, `text-destructive`, `border-border`(기본 `border`), `bg-card`, `ring-ring`, 상태색 `success`/`warning`/`destructive`/`info`(+`*-soft` 배경), 네이비 셸 `sidebar` 계열 — 값과 용도는 `docs/design-guide.md` 2장
- Tailwind 팔레트 유틸리티(`bg-red-500`, `text-gray-600`)와 임의 값(`bg-[#4f46e5]`, `text-[13px]`) **금지** — 새 색이 필요하면 토큰을 추가한다

**크기/간격/타이포 (MUST)**
- 간격·크기는 Tailwind 기본 스케일(`p-4`, `gap-2`, `h-9`)만, 임의 값(`p-[18px]`) 금지
- 모서리는 `rounded-sm/md/lg/xl`(`--radius` 파생)만, 폰트 크기는 `text-sm/base/lg` 등 프리셋만
- z-index는 `z-10/20/30/40/50` 프리셋만, 임의 숫자 금지

**컴포넌트 패턴**
- 반복되는 UI 블록(버튼, 카드, 폼 필드)은 CSS 클래스로 추상화하지 않고 **JSP 태그 파일 컴포넌트**로 추출한다(12.2 — shadcn이 컴포넌트에 유틸리티를 인라인하는 방식과 동일). `@apply`로 컴포넌트 클래스를 만드는 것은 지양 **(SHOULD)**
- 클래스 나열 순서: 레이아웃(flex/grid/position) → 박스(크기/여백) → 시각(배경/테두리/모서리) → 타이포 → 상태 변형(`hover:`, `focus-visible:`, `disabled:`) **(SHOULD)**

```html
<!-- 버튼 예시 — shadcn default 버튼과 동일한 유틸리티 조합 -->
<button type="submit"
        class="inline-flex h-9 items-center justify-center rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground hover:bg-primary/90 focus-visible:ring-2 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50">
    등록
</button>
```

**기타**
- 인라인 `style` 속성 금지, `!important`(`!` 접두 유틸리티) 금지 — 단 **서버 데이터로 결정되는 연속값**(진행 바 `width:N%` 등)은 인라인 style을 예외 허용 (design-guide.md 7.2)
- id는 JS 훅/폼 label 연결 전용 — 스타일에 쓰지 않는다
- 상태 변화는 JS에서 Tailwind 클래스 토글로만: `el.classList.toggle('hidden')`. `el.style.*` 직접 조작 금지
- 반응형은 **모바일 퍼스트**, 브레이크포인트는 Tailwind 기본 `md`(768px) / `lg`(1024px) 두 개만 사용하고 임의 브레이크포인트(`min-[900px]:`) 금지

### 13.3 JS 규칙

**기본 문법**
- Vanilla JS + ES Module (`<script type="module">`). 전역 변수 금지
- `const` 기본, 재할당 필요 시에만 `let`. `var` 금지
- 비교는 `===` / `!==`만. 세미콜론 필수, 문자열은 작은따옴표, 조립은 템플릿 리터럴
- 네이밍: 변수/함수 lowerCamelCase, 상수 UPPER_SNAKE, 파일명 kebab-case. 함수는 동사로 시작하며 [15. 동사 사전]을 준용한다
- 비동기는 `async/await`로 통일, `.then()` 체이닝 지양. 함수 내 중첩은 early return으로 풀어낸다 (6.1/6.2 준용)
- 모듈은 **named export만** 사용, default export 금지 (임포트 이름 통일)

**DOM / 이벤트**
- **인라인 이벤트 핸들러(`onclick=""`) 금지.** JS 훅은 `data-*` 속성으로 연결: `data-action="delete-member"`
- 스타일용 BEM 클래스를 JS 셀렉터로 쓰지 않는다 (스타일과 동작 결합 금지)
- 반복 목록의 이벤트는 아이템마다 리스너를 달지 않고 **이벤트 위임**으로 처리한다

```javascript
memberList.addEventListener('click', (event) => {
    const button = event.target.closest('[data-action="delete-member"]');
    if (!button) {
        return;
    }
    deleteMember(button.dataset.memberId);
});
```

- 같은 요소를 반복 조회하지 않는다 — 파일 상단에서 한 번 조회해 상수로 보관
- `data-action` 값 같은 매직 문자열은 파일 상단 상수 객체로 모은다

**보안 (XSS)**
- **사용자 입력이 섞인 값을 `innerHTML`에 넣지 않는다.** 텍스트는 `textContent`, 요소 생성은 `createElement` 사용
- 서버 데이터를 JS로 넘길 때는 12.3의 `data-*` 속성 + `<c:out>` 방식만 사용

**에러 처리 / UX**
- `await` 호출부는 try/catch로 감싸고 **사용자에게 보이는 피드백**(에러 영역 표시, 알림)을 준다. `console.error`만 찍고 끝내는 침묵 처리 금지
- 상태 변경 요청은 반드시 13.4의 fetch 래퍼 경유 (CSRF)
- 삭제 등 파괴적 동작은 실행 전 확인을 거친다
- 검색 입력 등 연속 발생 이벤트는 debounce(300ms 기준) 적용

**폼 검증**
- 1차는 HTML 속성(`required`, `maxlength`, `type="email"`)으로, 최종 검증은 항상 서버(Bean Validation). **JS 검증은 UX 보조일 뿐 보안이 아니다**
- JS 검증 메시지는 서버 검증 메시지와 문구를 일치시킨다 (`messages.properties` 기준)

### 13.4 fetch 공통 래퍼 (CSRF)
```javascript
// static/js/common/api.js
const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

async function request(method, url, body) {
    const response = await fetch(url, {
        method,
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken,
        },
        body: body === undefined ? undefined : JSON.stringify(body),
    });
    if (!response.ok) {
        throw new Error(`요청 실패: ${response.status}`);
    }
    if (response.status === 204) {
        return null;
    }
    return response.json();
}

export const post = (url, body) => request('POST', url, body);
export const put = (url, body) => request('PUT', url, body);
export const patch = (url, body) => request('PATCH', url, body);
export const del = (url) => request('DELETE', url);
```
JS에서의 상태 변경 요청(POST/PUT/PATCH/DELETE)은 반드시 이 래퍼를 통한다.

---

## 14. 테스트

- **model/Service는 TDD로 개발한다.** 절차와 적용 범위는 [22.6] — 사람과 에이전트 모두 동일하게 적용한다
- 클래스명 `~Test`, 메소드명은 한글 + 언더스코어 허용: `이메일이_중복되면_예외가_발생한다()`
- given / when / then 주석 구조를 기본으로 한다.
- 단언은 AssertJ(`assertThat`)로 통일. JUnit `assertEquals` 금지.
- Mapper 테스트는 `@MybatisTest` + 테스트용 스키마(Flyway 적용)로 실제 SQL을 검증한다. 클래스에 반드시 3종을 함께 붙인다: `@MybatisTest` + `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)` + `@ActiveProfiles("test")` — `replace = NONE`이 없으면 임베디드 DB 교체 시도로 실패하고(클래스패스에 H2 없음), `@ActiveProfiles("test")`가 없으면 기본 프로파일 dev의 `bandi` 스키마에 붙어 개발 데이터를 오염시킨다. 이 3종을 묶은 커스텀 애노테이션(`@MapperTest`)을 global 인프라로 두면 반복을 줄일 수 있다.
- Service 단위 테스트는 Mockito, Controller는 `@WebMvcTest` + `spring-security-test`(`@WithMockUser`, `csrf()`)

---

## 15. 동사 사전

가급적 서로 짝이 되는 동사를 사용한다.

**CRUD/조회**
- `create` 생성 · `insert` 저장(DB) · `update` 수정 · `modify` 변경
- `search` 검색(결과 여러 개) · `lookup` 조회(결과 1개) · `select` 선택 · `count` 개수 · `exists` / `has` 존재 확인
- `delete` 가상 삭제(플래그) → `remove` 실제 삭제(DB) → `purge` 완전 삭제(파일까지) · `erase` 그 외 삭제

**추가/연결**
- `insert` 앞에 추가 · `append` 뒤에 추가 · `add` 추가
- `attach` 보조 객체 연결 · `detach` 보조 객체 분리
- `enqueue` 큐에 넣기 · `dequeue` 큐에서 빼기 · `push` 넣기 · `pull` 빼기

**상태/제어**
- `enable` 켜기 · `disable` 끄기 · `toggle` 토글 · `activate` 활성화
- `init` 시작 · `initialize` 초기화 · `exit` 종료 · `finalize` 마지막 정리
- `open` 열기 · `close` 닫기 · `save` 저장 · `load` 불러오기 · `preload` 미리 불러오기

**기타**
- `get`/`set` 프로퍼티 접근 · `take` 획득 · `keep` 보관 · `free` 해제
- `show` 보여주기 · `hide` 숨기기 · `view` 보기 · `preview` 미리보기 · `draw` 그리기
- `merge` 병합 · `print` 인쇄 · `fix`/`patch` 수정 · `prepare` 준비
- `include` 포함 · `exclude` 제외 · `generate` 생성 · `validate` 검증 · `serialize` 직렬화

---

## 16. 도구 설정

### 16.1 .editorconfig (프로젝트 루트)
```ini
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.java]
indent_style = space
indent_size = 4
max_line_length = 120

[*.{xml,html}]
indent_style = space
indent_size = 4

[*.{yml,yaml}]
indent_style = space
indent_size = 2

[*.{js,css}]
indent_style = space
indent_size = 4

[*.md]
trim_trailing_whitespace = false
```

### 16.2 IntelliJ
- `Editor > Code Style > Java`: Use tab character 해제, Tab/Indent 4
- 커밋 전 `Reformat code` + `Optimize imports` 활성화
- Save Actions 또는 커밋 훅으로 포맷 강제 검토 **(SHOULD)**

### 16.3 향후 검토 항목 (미확정)
- [ ] Checkstyle/Spotless 도입 여부 (naver-checkstyle-rules 커스텀)
- [ ] /api 성공 응답 공통 래퍼(ApiResponse) 도입 여부 — 현재는 래핑 없이 `ResponseEntity<T>` 반환, 에러만 `ErrorResponse`(9.4)로 통일
- [ ] (장기) 폼 바인딩 객체의 record 전환 — **현재는 setter 클래스로 확정(7.2)**. Spring이 record 폼 바인딩의 검증 실패 재렌더링/중첩 바인딩 문제를 해결한 버전이 나오면 그때만 재논의
- [ ] CI (GitHub Actions) 빌드/테스트 파이프라인 구성

---

## 17. 환경 설정 / 프로파일 (팀 개발 필수)

### 17.1 프로파일 구성
실행 프로파일은 `dev` / `prod` 2단계, 여기에 테스트 전용 `test`가 추가된다. **dev가 로컬 개발 기본 프로파일**이며, 프로파일 미지정 실행은 `dev`로 동작하도록 기본값을 지정한다. (별도 개발 서버 환경이 생기면 그때 프로파일을 추가한다)

```yaml
# application.yaml (공통 — 모든 환경에서 동일한 값만)
spring:
  profiles:
    default: dev
  config:
    import: "optional:file:.env[.properties]"   # DB 접속값 로딩 (17.2)
  session:
    jdbc:
      initialize-schema: never
    timeout: 30m                                # 18.4
# mybatis 고정값은 10.4, springdoc은 19.2가 정본 — 여기 중복 기재하지 않는다
```

| 파일 | 용도 | Git 커밋 |
|------|------|----------|
| `application.yaml` | 환경 무관 공통 설정 | O |
| `application-dev.yaml` | 로컬 개발 기본 (.env로 접속값 로딩, 로컬 도커 3307) | O (접속값은 환경변수 참조) |
| `application-prod.yaml` | 운영 | O (비밀값은 환경변수 참조) |
| `src/test/resources/application-test.yaml` | 테스트 전용 (`bandi_test` 스키마 — 14장, 17.3) | O |
| `.env` | 개인/서버별 접속값·비밀값 | **X (gitignore)** |
| `.env.example` | 필요한 환경변수 키 목록 (로컬 팀 공통 더미값 포함) | O |

### 17.2 비밀값 규칙
- 비밀번호, API 키, 세션 시크릿 등은 **어떤 yaml에도 실값을 넣지 않는다.** `${DB_PASSWORD}` 형태로 환경변수를 참조한다.
- 새 환경변수를 추가하면 `.env.example`에 키를 반드시 추가한다. (팀원 온보딩 깨짐 방지)
- 로컬 Docker MySQL 계정(`bandi`/`bandi1234` 등)은 팀 공통 더미값이므로 `.env.example`에 실값 기재를 허용한다. 이 값은 절대 prod에서 재사용하지 않는다.

### 17.3 로컬 인프라 — docker-compose
프로젝트 루트에 `docker-compose.yaml`을 커밋한다. 팀원 온보딩은 아래 두 명령으로 끝나야 한다.

```bash
cp .env.example .env      # 최초 1회 (로컬은 더미값 그대로 사용)
docker compose up -d      # MySQL 8.x (포트/계정/DB명 팀 고정)
./gradlew bootRun         # dev 프로파일(기본), Flyway 자동 적용
```

- **호스트 포트는 3307 고정** (3306은 로컬의 다른 MySQL과 충돌 방지). 계정 `bandi`/`bandi1234`, 스키마 `bandi`(개발)·`bandi_test`(테스트)
- MySQL 컨테이너에 `--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci` 고정
- 테스트도 동일 컨테이너의 별도 스키마(`bandi_test`)를 사용하고 `application-test.yaml`(포트 3307)로 연결한다. H2 호환 모드는 MySQL 전용 SQL(Flyway)과 어긋나므로 쓰지 않는다.

### 17.4 시간대
- DB 연결: `serverTimezone=Asia/Seoul`, JVM 기본 시간대 `Asia/Seoul` 통일 (국내 단일 서비스이므로 KST 단순화 우선)
- 코드에서는 `LocalDateTime` 사용, `Date`/`Calendar` 금지

### 17.5 devtools
- `developmentOnly` 스코프이므로 배포 아티팩트에 포함되지 않는다 — 별도 프로파일 처리 불필요
- LiveReload는 개인 선택. 단, devtools의 restart가 MyBatis 캐시/세션과 얽혀 이상 동작하면 `spring.devtools.restart.enabled=false` 후 수동 재시작으로 전환

---

## 18. Spring Security / 세션

### 18.1 기본 방침
- **세션 기반 인증** (JWT 아님 — 결정 사항). 커스텀 학교 SSO 로그인 페이지(`/login`)와 인증 어댑터를 사용하며 로컬 아이디·비밀번호 계정을 만들지 않는다
- 인증/인가 실패 흐름: 미인증 → `/login` 리다이렉트, 권한 부족 → 403 에러 페이지
- `SecurityConfig`, 인증 필터, `@LoginMember` + `ArgumentResolver`는 전부 `global.security`에 응집
- 학교 비밀번호와 학교 세션은 인증 요청 처리 중에만 사용하고 DB·세션 저장소·캐시·로그에 남기지 않는다

### 18.2 비밀번호 / 권한
- 1차 서비스는 로컬 비밀번호를 저장하지 않는다. 향후 로컬 자격증명이 승인되더라도 평문 비교·자체 해시 구현은 금지하고 `PasswordEncoderFactories.createDelegatingPasswordEncoder()`를 사용한다
- `ClubRole` enum은 `ADMIN`, `LEADER`, `MEMBER` 세 값만 사용한다
- Spring Security 권한 문자열은 `ROLE_` 접두사 + enum 이름인 `ROLE_ADMIN`, `ROLE_LEADER`, `ROLE_MEMBER`로 통일한다
- `ADMIN`은 전체 운영, `LEADER`는 소속 팀 관리, `MEMBER`는 일반 부원 권한이다. 팀 범위 인가는 역할 문자열만으로 끝내지 않고 대상 `teamId`와 로그인 멤버의 현재 팀을 Service에서 함께 검증한다
- URL 단위 인가는 `SecurityConfig`에서 중앙 관리하고, 메소드 단위 세밀 인가가 필요할 때만 `@PreAuthorize`를 병행한다. 뷰의 `sec:authorize`는 표시 제어일 뿐 보안이 아니다 — 서버 인가와 항상 이중으로 건다

### 18.3 CSRF
- **CSRF 보호를 끄지 않는다.** `/api`도 같은 세션 기반이므로 예외(ignoring) 처리하지 않는다
- 폼은 `<form:form>`으로 자동 삽입(`CsrfRequestDataValueProcessor`), JS는 13.4의 fetch 래퍼로 헤더 전달 — 이 두 경로 외의 상태 변경 요청 금지

### 18.4 spring-session-jdbc
- 세션 저장소는 DB(spring-session-jdbc) — 서버 재시작에도 로그인 유지, 추후 다중 인스턴스 대비
- `spring.session.jdbc.initialize-schema: never` 고정, `SPRING_SESSION`/`SPRING_SESSION_ATTRIBUTES` 테이블은 Flyway 마이그레이션으로 생성 (spring-session이 제공하는 `schema-mysql.sql` 내용을 복사)
- 세션 타임아웃: `spring.session.timeout: 30m` (변경 시 이 문서 갱신)
- 세션에는 **로그인 식별 정보 최소한만** 저장한다 (memberId, role 정도). 조회 결과 객체를 세션에 캐싱하지 않는다 — 직렬화되어 DB에 들어가므로 무겁고, model 클래스 변경 시 역직렬화가 깨진다
- 쿠키: `httpOnly` 기본 유지, prod에서 `server.servlet.session.cookie.secure: true`

### 18.5 인증 정보 접근
- Controller에서 로그인 사용자는 `@LoginMember Long memberId` 커스텀 리졸버로 받는다
- Service/Mapper에서 `SecurityContextHolder` 직접 접근 금지 — 인증 정보는 Controller에서 꺼내 파라미터로 전달한다 (테스트 용이성)

---

## 19. API 문서화 (springdoc / Swagger)

### 19.1 ApiDocs 인터페이스 패턴
springdoc 애노테이션(`@Tag`, `@Operation`, `@Parameter`)이 컨트롤러를 오염시키지 않도록, **문서 애노테이션과 HTTP 매핑을 `global.swagger` 인터페이스에 두고 컨트롤러가 구현**한다.

- 위치: `global.swagger.{도메인}ApiDocs` — Swagger 관련 설정과 문서 계약은 이 패키지를 단일 정본으로 사용한다
- `@RequestMapping`/`@GetMapping` 등 매핑 애노테이션과 `@Valid`, 검증 애노테이션까지 인터페이스에 선언
- 구현 컨트롤러(`~ApiController`)에는 `@RestController` + `@RequiredArgsConstructor` + 로직만. 메소드에 `@Override` 필수
- `~ApiDocs`는 feature의 request/response DTO만 참조할 수 있고 Service·Mapper·model은 참조하지 않는다
- 공통 태그명은 `ApiTag`, 세션 인증 스키마명은 `OpenApiConfig.SESSION_COOKIE_SCHEME`을 사용한다

```java
// global/swagger/MemberApiDocs.java
@RequestMapping("/api/members")
@Tag(name = ApiTag.MEMBER, description = "단원 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface MemberApiDocs {

    @Operation(summary = "단원 검색", description = "조건에 맞는 단원 목록을 조회합니다.")
    @GetMapping
    ResponseEntity<List<MemberResponse>> searchMembers(
            @Parameter(description = "단원 이름 (부분 일치)") @RequestParam(required = false) String name);
}

// domain/member/controller/MemberApiController.java
@RestController
@RequiredArgsConstructor
public class MemberApiController implements MemberApiDocs {

    private final MemberService memberService;

    @Override
    public ResponseEntity<List<MemberResponse>> searchMembers(String name) {
        return ResponseEntity.ok(memberService.searchMembers(name));
    }
}
```

### 19.2 문서화 범위와 설정
- 문서화 대상은 `/api/**`만. SSR 컨트롤러는 springdoc 스캔에서 제외한다
- **prod에서는 Swagger UI와 api-docs를 비활성화한다** (내부 관리 도구가 아닌 이상 운영 노출 금지)

```yaml
# application.yaml — Swagger UI는 /docs, OpenAPI JSON은 /api-docs (확정)
springdoc:
  paths-to-match: /api/**
  swagger-ui:
    path: /docs
  api-docs:
    path: /api-docs

# application-prod.yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

- 성공 응답은 공통 래퍼 없이 `ResponseEntity<T>`를 그대로 반환한다. 에러 포맷만 9.4의 `ErrorResponse`로 통일한다. (공통 래퍼 도입 여부는 16.3 미확정 항목)

- `@Operation`의 summary는 명사형 종결("단원 검색"), description은 문장으로. example 값은 실제 형식과 일치시킨다
- 에러 응답 스펙은 `@ApiResponse`로 케이스별 나열하기보다 `ApiExceptionHandler`의 공통 에러 포맷(`ErrorResponse`)을 문서 서두에 한 번 정의한다 **(SHOULD)**

---

## 20. 로깅

### 20.1 기본 규칙
- Lombok `@Slf4j` 사용. `System.out.println` / `printStackTrace()` 금지
- 플레이스홀더 사용, 문자열 연결 금지: `log.info("회원 가입 완료 - memberId={}", memberId)`
- 예외 로깅은 스택트레이스 포함: `log.error("파일 업로드 실패 - fileName={}", fileName, e)` (마지막 인자로 예외 전달)
- **민감정보 로그 금지**: 비밀번호, 세션 ID, 개인 연락처. 회원 식별은 memberId로만
- 레벨 기준: `error` 즉시 확인 필요한 장애 / `warn` 이상 징후지만 동작 계속 / `info` 주요 비즈니스 이벤트(가입, 결제 등) / `debug` 개발 진단용

### 20.2 log4jdbc (SQL 로깅)
log4jdbc는 **dev 전용**이다. prod는 순정 드라이버를 쓴다 — 프로파일별로 datasource가 갈린다.

```yaml
# application-dev.yaml
spring:
  datasource:
    driver-class-name: net.sf.log4jdbc.sql.jdbcapi.DriverSpy
    url: jdbc:log4jdbc:mysql://${DB_HOST}:${DB_PORT}/bandi?serverTimezone=Asia/Seoul

# application-prod.yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_HOST}:3306/bandi?serverTimezone=Asia/Seoul
```

로거 레벨 (`logback-spring.xml`의 `<springProfile name="dev">` 안에서):

| 로거 | dev | 설명 |
|------|-------|------|
| `jdbc.sqltiming` | INFO | 파라미터 바인딩된 실제 SQL + 실행 시간 |
| `jdbc.resultsettable` | INFO | 조회 결과 테이블 (시끄러우면 OFF) |
| `jdbc.sqlonly` `jdbc.audit` `jdbc.resultset` `jdbc.connection` | OFF | sqltiming과 중복 |

### 20.3 logback-spring.xml (전체 설정)
파일명은 `logback.xml`이 아닌 **`logback-spring.xml`** (`<springProfile>` 사용을 위해). 아래를 기준 설정으로 커밋한다.

```xml
<configuration>
    <property name="LOG_PATTERN"
              value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"/>
    <property name="LOG_DIR" value="${LOG_DIR:-logs}"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_DIR}/bandi.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_DIR}/bandi.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <!-- log4jdbc 공통: sqltiming만 쓰고 나머지는 끔 (dev에서만 의미 있음) -->
    <logger name="log4jdbc.log4j2" level="ERROR"/>  <!-- 드라이버 내부 로그 억제 -->
    <logger name="jdbc.sqlonly" level="OFF"/>
    <logger name="jdbc.audit" level="OFF"/>
    <logger name="jdbc.resultset" level="OFF"/>
    <logger name="jdbc.connection" level="OFF"/>

    <springProfile name="dev">
        <logger name="kr.ac.tukorea.bandi" level="DEBUG"/>
        <logger name="jdbc.sqltiming" level="INFO"/>
        <logger name="jdbc.resultsettable" level="INFO"/>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>

    <springProfile name="prod">
        <logger name="kr.ac.tukorea.bandi" level="INFO"/>
        <root level="INFO">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

핵심: **앱 패키지(`kr.ac.tukorea.bandi`)가 dev에서 DEBUG, prod에서 INFO.** 이 한 줄 차이가 20.4 규칙의 전제다.

### 20.4 디버그 로깅 규칙
- **개발 진단용 정보는 전부 `debug`로 남긴다.** prod에서는 앱 로거가 INFO라 debug는 출력되지 않으므로, "개발 끝났으니 로그 지우기"를 하지 않는다 — 지우지 말고 debug로 남겨두는 것이 규칙이다.
- debug로 남길 것: 분기 판단 근거(어떤 조건으로 이 흐름을 탔는지), 외부 연동 요청/응답 요약, 배치성 처리의 중간 카운트
- `info`는 비즈니스 이벤트(가입, 공연 등록, 회비 납부)만. 디버깅 정보를 info로 올리지 않는다 — prod 로그가 오염된다.
- 로그 인자에 비싼 연산(대형 객체 직렬화, 컬렉션 정렬 등)이 들어가면 supplier 또는 `log.isDebugEnabled()` 가드를 쓴다. 단순 값 전달은 플레이스홀더면 충분하므로 가드 불필요.
- 임시 확인용 `log.info("여기 옴")`, `System.out` 류는 커밋 금지. 리뷰에서 발견 시 반려.

---

## 21. Git 컨벤션

### 21.1 브랜치 전략
```
master ────────●───────────●──── (배포, 보호 브랜치)
                \         /
dev     ──●──●──●──●──●──●────── (통합 개발)
           \       /
feature/12-member-signup         (작업 단위)
```

| 브랜치 | 규칙 |
|--------|------|
| `master` | 배포 기준. **직접 push 금지**, dev에서 PR로만 병합 |
| `dev` | 통합 브랜치. **직접 push 금지**, feature에서 PR로만 병합 |
| `feature/{이슈번호}-{설명}` | `dev`에서 분기. 케밥 케이스 영문: `feature/12-member-signup` |
| `fix/{이슈번호}-{설명}` | dev 대상 버그 수정 |
| `hotfix/{설명}` | master 긴급 수정. 병합 후 dev에도 반영 |

- PR은 최소 1인 리뷰 승인 후 병합. feature → dev는 **squash merge**(커밋 정리), dev → master는 merge commit
- 하나의 PR은 하나의 이슈만 다룬다. 300줄(diff)을 넘으면 분할을 검토 **(SHOULD)**

### 21.2 커밋 메시지 — Conventional Commits
형식: `type(scope): 제목` — scope는 feature 패키지명, 생략 가능. 제목은 한글 허용, 명령형, 마침표 없음, 50자 이내.

| type | 용도 |
|------|------|
| `feat` | 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 동작 변화 없는 구조 개선 |
| `style` | 포맷, 세미콜론 등 (코드 동작 무관) |
| `docs` | 문서 |
| `test` | 테스트 추가/수정 |
| `chore` | 빌드, 설정, 의존성 |
| `db` | Flyway 마이그레이션 추가 (팀 커스텀 타입 — 스키마 변경을 히스토리에서 바로 찾기 위함) |

```
feat(member): 단원 가입 폼과 이메일 중복 검증 추가

- MemberCreateRequest 검증 규칙 추가
- existsByEmail 매퍼 쿼리 추가

Closes #12
```

- 본문은 "무엇을"이 아니라 "왜"를 적는다. 이슈 연결은 푸터에 `Closes #번호`
- 마이그레이션 파일과 그것을 사용하는 코드는 **같은 커밋**에 담는다
- 커밋의 author/committer는 저장소에 설정된 개발자 identity를 사용한다. 자동화 도구·모델은 공동 작성자로 기록하지 않는다
- 커밋 메시지·PR·이슈 등 GitHub 기록에 `Co-Authored-By: Claude...`, `Generated with ...`, 도구명·모델명·봇 이모지 등 자동 생성 주체 표기를 추가하지 않는다

---

## 22. AI 에이전트 (Claude Code / 서브에이전트) 개발 컨벤션

### 22.1 컨텍스트 파일 구성
```
bandi/
├── AGENTS.md                  # 에이전트 공통 규약 원본(source of truth) — 스택·명령어·색인·MUST 요약·금지 목록·DoD
├── CLAUDE.md                  # @AGENTS.md import + Claude Code 전용 규칙만 (80줄 이내 유지)
├── CLAUDE.local.md            # 개인 로컬 설정 (gitignore)
└── docs/
    ├── coding-convention.md   # 이 문서 — 조항 번호의 출처
    ├── database-schema.md     # 스키마·ERD·마이그레이션 분할 정본
    └── feature-spec.md        # 1차 기능 범위 정본
```

- 이 문서를 CLAUDE.md/AGENTS.md에 `@import`하거나 통째로 넣지 않는다 — 1300줄 이상이 매 세션 주입되면 요약 설계가 무너진다. AGENTS.md에 요약과 장 번호 색인을 두고, 에이전트는 작업에 해당하는 장만 골라 읽는다
- CLAUDE.md는 `@AGENTS.md`만 import한다. 스택/명령어/MUST 요약/금지 목록/DoD는 AGENTS.md가 담는다
- `.claude/` 디렉토리는 gitignore 대상(개인 로컬 전용)이므로 팀 공유 규칙을 그 안에 두지 않는다

### 22.2 작업 단위와 브랜치
- 에이전트 작업 1개 = feature 브랜치 1개 = PR 1개. 사람 작업과 동일하게 21장 규칙을 적용한다
- 에이전트가 만든 커밋도 Conventional Commits와 21.2의 Git identity 규칙을 따른다. 자동화 도구·모델을 author·committer·공동 작성자 또는 생성 주체로 기록하지 않는다
- **병합 전 사람 리뷰 필수.** 에이전트 산출물이라고 리뷰를 생략하지 않는다 — 특히 SecurityConfig, 마이그레이션, 인가 로직

### 22.3 에이전트 금지 목록 (AGENTS.md에 명시)
- 적용된 Flyway 마이그레이션 파일 수정 (새 버전 파일만 허용)
- `application-prod.yaml`, `SecurityConfig`의 인가 규칙, `.github/` 워크플로우 무단 변경
- 비밀값(.env, 실제 비밀번호/키) 생성·커밋
- `build.gradle.kts`/`settings.gradle.kts` 등 Gradle 빌드 스크립트의 의존성·플러그인 추가/변경 (사람 승인 후에만)
- `git push --force`, master/dev 직접 push
- 테스트를 통과시키기 위한 테스트 삭제·주석 처리·`@Disabled`

### 22.4 검증 게이트
- 에이전트는 커밋 전 반드시 `./gradlew build`(테스트 포함)를 실행하고, 실패 상태로 커밋하지 않는다
- 마이그레이션을 추가했다면 로컬 DB에 Flyway 적용까지 확인한다
- 새 화면을 만들었다면 해당 URL의 렌더링 확인(최소한 컨트롤러 테스트)까지가 작업 완료 기준

### 22.5 서브에이전트 병렬 작업 규칙
- 분할 기준은 **feature 패키지 단위** (member 에이전트, fee 에이전트...). 8.3의 참조 규칙 덕에 병렬 충돌이 최소화된다
- **공유 자원 수정은 병렬 금지**: `global.**`, `common.css`/`layout.css`, `WEB-INF/tags/` 태그 파일, `messages.properties`. 이 파일들을 건드리는 작업은 단일 에이전트(또는 사람)가 직렬로 처리한다
- Flyway 버전은 날짜 기반(11.4)이라 충돌하지 않지만, **같은 테이블을 두 에이전트가 동시에 변경하는 작업은 배정하지 않는다**
- 에이전트 간 인터페이스(다른 feature의 service 시그니처)가 필요하면, 먼저 시그니처만 정의·커밋한 후 병렬 작업을 시작한다

### 22.6 TDD 개발 규칙
에이전트는 **테스트 주도(TDD)** 로 개발한다. 작업 지시(프롬프트)에 이 절차를 명시한다.

**작업 절차 (Red → Green → Refactor)**
1. **테스트 목록 먼저**: 구현 시작 전, 요구사항에서 테스트 케이스 목록(정상/경계/예외)을 도출해 제시한다. 이 목록이 사람이 리뷰하는 첫 산출물이다 — 케이스가 틀리면 구현 전체가 틀린다
2. **Red**: 목록에서 하나를 골라 실패하는 테스트를 먼저 작성하고, **실제로 실행해 실패를 확인**한다. 작성 직후 통과하는 테스트는 아무것도 검증하지 않는다는 신호이므로 테스트를 의심한다
3. **Green**: 그 테스트를 통과시키는 최소한의 구현만 작성한다. 목록에 없는 기능을 미리 만들지 않는다
4. **Refactor**: 테스트가 초록인 상태에서 6장 규칙(들여쓰기 1단계, else 제거, 메소드 분리)에 맞게 정리한다. **이 단계에서 테스트 코드를 수정하지 않는다** — 테스트가 바뀌어야 한다면 동작 명세가 바뀐 것이므로 1번으로 돌아간다
5. 목록의 다음 케이스로 반복

**적용 범위**
| 대상 | TDD |
|------|-----|
| model (도메인 로직) | **필수** — 순수 객체라 TDD 효율이 가장 높은 곳 |
| Service | **필수** (Mapper는 Mockito 모킹) |
| Mapper (`@MybatisTest`) | 구현 후 테스트 허용 — SQL은 선행 작성이 비효율적 |
| Controller (`@WebMvcTest`) | 구현 후 테스트 허용 — 바인딩/인가/상태코드 검증 중심 |
| JSP 뷰, CSS, JS | TDD 제외 (수동 확인 + 컨트롤러 테스트로 커버) |

**커밋/금지 규칙**
- **Red 상태로 커밋하지 않는다.** 테스트와 그것을 통과시키는 구현은 같은 커밋에 담는다 (모든 커밋은 `./gradlew build` 통과 상태 — 21장과 일관)
- 구현을 통과시키기 위한 단언(assertion) 완화·삭제·`@Disabled`는 금지 (22.3 재확인). 테스트가 요구사항과 다르다고 판단되면 수정하지 말고 **사람에게 보고**한다
- 커버리지 수치를 목표로 삼지 않는다. 기준은 "테스트 목록 = 요구사항 명세"의 소진 여부다
- 버그 수정(`fix:`)은 반드시 **버그를 재현하는 실패 테스트 추가 → 수정** 순서로 진행한다 (회귀 방지)
