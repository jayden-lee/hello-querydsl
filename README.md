# Querydsl Study Repo
> 인프런 실전 Querydsl 강좌를 학습하고 정리한 내용입니다

## Gradle에서 Querydsl 설정
build.gradle 파일에 querydsl 설정을 추가한다. 설정을 추가하고 <code>build</code> 또는
<code>compileQuerydsl</code>을 하면, 빌드 폴더에 Entity 클래스에 매핑되는 QEntity 클래스가 생성된다.

```
plugins {
    id 'org.springframework.boot' version '2.2.2.RELEASE'
    id 'io.spring.dependency-management' version '1.0.8.RELEASE'
    //querydsl 추가
    id "com.ewerk.gradle.plugins.querydsl" version "1.0.10"
    id 'java'
}

group = 'com.jayden.study'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'

    //querydsl 추가
    implementation 'com.querydsl:querydsl-jpa'

    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'mysql:mysql-connector-java'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation('org.springframework.boot:spring-boot-starter-test') {
        exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
    }
}

test {
    useJUnitPlatform()
}

//querydsl 추가 시작
def querydslDir = "$buildDir/generated/querydsl"
querydsl {
    jpa = true
    querydslSourcesDir = querydslDir
}
sourceSets {
    main.java.srcDir querydslDir
}
configurations {
    querydsl.extendsFrom compileClasspath
}
compileQuerydsl {
    options.annotationProcessorPath = configurations.querydsl
}
//querydsl 추가 끝
```

### Querydsl 라이브러리
- querydsl-apt: Querydsl 관련 코드(ex: QHello) 생성 기능 제공
- querydsl-jpa: Querydsl JPA 관련 기능 제공

## JPQL vs Querydsl
> 아래는 JPQL과 Querydsl에서 동일한 작업(특정 회원 1명 조회)를 하는 코드이다. 두 개의 큰 차이점으로 쿼리 문법 오류를 JPQL은 
실행 시점에 발견할 수 있으며, Querydsl은 컴파일 시점에 발견할 수 있다.

### JPQL
```java
@Test
public void jpql() {
    String qlString = "select m from Member m " +
        "where m.username = :username";
    String username = "member1";

    Member findMember = em.createQuery(qlString, Member.class)
        .setParameter("username", username)
        .getSingleResult();

    assertThat(findMember.getUsername()).isEqualTo(username);
}
```

### Querydsl
```java
@Test
public void querydsl() {
    String username = "member1";

    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    QMember m = new QMember("m");

    Member findMember = queryFactory
        .select(m)
        .from(m)
        .where(m.username.eq(username))
        .fetchOne();

    assertThat(findMember.getUsername()).isEqualTo(username);
}
```

## Q-Type 인스턴스 사용 방법
1. 직접 QClass 인스턴스를 생성해서 사용하기
    ```java
    QMember qMember = new QMember("m");
    ```
   
2. QClass에 이미 생성된 기본 인스턴스 사용하기
    ```java
    QMember qMember = QMember.member;
    ```
   
   <img width="632" alt="QMember" src="https://user-images.githubusercontent.com/43853352/72715544-00b16800-3bb4-11ea-9a9c-b2d592ba2c38.png">

## 검색 조건 쿼리
Querydsl은 JPQL과 Criteria 쿼리를 모두 대체할 수 있다

```java
member.username.eq("member1") // username = 'member1'
member.username.ne("member1") //username != 'member1'
member.username.eq("member1").not() // username != 'member1'

member.username.isNotNull() // username is not null
member.age.in(10, 20) // age in (10,20)
member.age.notIn(10, 20) // age not in (10, 20)
member.age.between(10,30) //between 10, 30

member.age.goe(30) // age >= 30
member.age.gt(30) // age > 30
member.age.loe(30) // age <= 30
member.age.lt(30) // age < 30

member.username.like("member%") // like 'member%' 검색
member.username.contains("member") // like '%member%' 검색
member.username.startsWith("member") //like 'member%' 검색
```

### And 연산자 사용 방법
And 연산자를 적용하는 경우에 체인닝하는 방법과 파라미터로 적용하는 방법이 있다.

```java
List<Member> result = queryFactory
    .selectFrom(member)
    .where(member.username.eq("member1")
            .and(member.age.eq(10)))
    .fetch();
```

```java
List<Member> result = queryFactory
    .selectFrom(member)
    .where(member.username.eq("member1"),
            member.age.eq(10))
    .fetch();
```

<code>QueryBase</code> 클래스에는 <code>where</code> 메서드가 오버로딩 되어 있다. 파라미터 1개만 받는 메서드와 여러 개를 받을 수 있는
메서드가 있기 때문에 조건절에 해당하는 Predicate를 두 가지 방법으로 전달할 수 있다. 

그리고 Predicate 인자를 받는 곳에 null 값을 넘기면 조건절을
만들 때 무시한다. 이런 특성으로 인해 동적 쿼리를 만들 때 유용하다.

<img width="452" alt="querydsl_where" src="https://user-images.githubusercontent.com/43853352/72876934-084f4900-3d3b-11ea-9946-ad0c02e9ed07.png">

## 결과 조회
### 1. fetch()
리스트를 조회한다. 만약 데이터가 없으면 빈 리스트 반환한다.

```java
List<Member> members = queryFactory
    .selectFrom(member)
    .fetch();
```

### 2. fetchOne()
단 건 조회할 때 사용한다. 결과가 없으면 null을 반환하고, 결과 값이 두 개 이상이면 에러가 발생한다.

```java
Member findMember = queryFactory
    .selectFrom(member)
    .where(member.username.eq("member1"),
            member.age.eq(10))
    .fetchOne();
```

### 3. fetchFirst()
limit 구문을 붙여서 쿼리가 실행되고 단 건 조회할 때 사용한다.

```java
Member findMember = queryFactory
    .selectFrom(member)
    .where(member.username.eq("member1"),
            member.age.eq(10))
    .fetchFirst();
```

<img width="251" alt="querydsl_fetchfirst" src="https://user-images.githubusercontent.com/43853352/72878478-56b21700-3d3e-11ea-942e-200a744208d7.png">

### 4. fetchResults()
결과값으로 반환되는 <code>QueryResults</code>에는 페이징 정보와 목록을 가지고 있다.

```java
QueryResults<Member> results = queryFactory
    .selectFrom(member)
    .fetchResults();
```

### 5. fetchCount()
count 쿼리로 변경되어 실행되어서 결과값으로는 count를 반환한다

```java
long count = queryFactory
    .selectFrom(member)
    .fetchCount();
```

## 정렬
정렬은 <code>orderBy</code> 메서드를 사용해서 설정할 수 있다. orderBy 메서드 인자에는 여러 인자를 넘길 수 있다.
원하는 정렬 순서에 맞게 <code>OrderSpecifier</code>를 넣어주면 된다.

```java
List<Member> members = queryFactory
    .selectFrom(member)
    .where(member.age.goe(10))
    .orderBy(member.age.desc(), member.username.asc().nullsLast())
    .fetch();
```

<img width="488" alt="querydsl_desc" src="https://user-images.githubusercontent.com/43853352/72880665-a692dd00-3d42-11ea-8ccf-15f4e3adcc1a.png">

## 페이징
Querydsl에서는 <code>offset</code>과 <code>limit</code>을 이용하여 페이징 기능을 지원한다. offset은 시작 지점이며 0부터 시작한다.
limit은 개수 제한을 지정한다.

```java
List<Member> members = queryFactory
    .selectFrom(member)
    .orderBy(member.age.desc())
    .offset(0)
    .limit(4)
    .fetch();
```

## References
- [인프런 실전! Querydsl 강좌](https://www.inflearn.com/course/Querydsl-%EC%8B%A4%EC%A0%84/dashboard)
- [Querydsl Reference Guide](http://www.querydsl.com/static/querydsl/4.1.3/reference/html_single)
