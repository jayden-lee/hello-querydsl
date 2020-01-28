# Querydsl Study Repo
> 인프런 실전 Querydsl 강좌를 학습하고 정리한 내용입니다

## Docker MySQL 설치
```shell script
docker run -d --name test_mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=admin007! mysql:5.7 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
```

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

# Querydsl 기본 문법

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

## 그룹핑
그룹핑 기능은 <code>groupBy</code> 메서드를 사용해서 설정할 수 있다. 필터 조건으로 <code>having</code>도 사용 가능하다. 
반환되는 <code>Tuple</code> 타입은 쿼리 실행 결과값으로 반환되는 서로 다른 타입을 함께 처리할 수 있도록 Querydsl에서 지원하는 타입이다.

```java
List<Tuple> result = queryFactory
    .select(team.name, member.age.avg())
    .from(member)
    .join(member.team, team)
    .groupBy(team.name)
    .fetch();

Tuple teamA = result.get(0);

assertThat(teamA.get(team.name)).isEqualTo("teamA");
assertThat(teamA.get(member.age.avg())).isEqualTo(15);
```

## 조인
Querydsl은 **Inner Join**, **Join**, **Left Join**, **Right Join**을 지원한다. 원하는 조인 메서드를 선택하고 첫 번째 파라미터에는
조인 대상을 지정하고, 두 번째 파라미터에는 별칭으로 사용할 Q 타입을 지정하면 된다. 그리고 조인할 때, <code>on</code> 구문을 직접 지정할 수도 있다.

### 조인 예제 (Inner Join)

```java
List<Member> result = queryFactory
    .selectFrom(member)
    .join(member.team, team)
    .where(team.name.eq("Team A"))
    .fetch();
```

실제 수행되는 쿼리는 아래와 같다.

```sql
select
    member0_.member_id as member_i1_0_,
    member0_.age as age2_0_,
    member0_.team_id as team_id4_0_,
    member0_.username as username3_0_ 
from
    member member0_ 
inner join
    team team1_ 
        on member0_.team_id=team1_.team_id 
where
    team1_.name=?
```

### 조인 On절
1. 조인 대상 필터링
Team 테이블은 이름이 "Team A"인 행만 필터링 되고 나서 Member 테이블과 Left Join을 한다. Left Join처럼 외부조인의 경우에는
On을 이용해서 필터링 하는 효과가 있지만, 내부조인의 경우에는 익숙한 Where절에 필터링 조건을 적용하는 것이 낫다.

```java
List<Tuple> result = queryFactory
    .select(member, team)
    .from(member)
    .leftJoin(member.team, team)
    .on(team.name.eq("Team A"))
    .fetch();
```

실제 수행되는 쿼리는 아래와 같다.

```sql
select
    member0_.member_id as member_i1_0_0_,
    team1_.team_id as team_id1_1_1_,
    member0_.age as age2_0_0_,
    member0_.team_id as team_id4_0_0_,
    member0_.username as username3_0_0_,
    team1_.name as name2_1_1_ 
from
    member member0_ 
left outer join
    team team1_ 
        on member0_.team_id=team1_.team_id 
        and (
            team1_.name=?
        )
```

2. 연관관계 없는 엔티티 외부조인

```java
List<Tuple> result = queryFactory
    .select(member, team)
    .from(member)
    .leftJoin(team).on(member.username.eq(team.name))
    .fetch();
```

실제 수행되는 쿼리는 아래와 같다.

```sql
select
    member0_.member_id as member_i1_0_0_,
    team1_.team_id as team_id1_1_1_,
    member0_.age as age2_0_0_,
    member0_.team_id as team_id4_0_0_,
    member0_.username as username3_0_0_,
    team1_.name as name2_1_1_ 
from
    member member0_ 
left outer join
    team team1_ 
        on (
            member0_.username=team1_.name
        )
```

### 페치 조인
SQL 조인을 활용해서 연관된 엔티티를 한번의 SQL로 모두 조회하는 기능이다. Inner Join의 경우에 페치 조인을 사용하면
데이터가 중복되어 조회된다. 이런 경우에는 <code>distinct()</code>를 사용해서 중복되는 데이터를 제거할 수 있다.

```java
class Test {
    
    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    @DisplayName("페치 조인 테스트")
    void fetch_join() {
        Member findMember = queryFactory
            .selectFrom(member)
            .join(member.team, team).fetchJoin()
            .where(member.username.eq("member1"))
            .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).isTrue();
    }
}
```

실제 수행되는 쿼리는 아래와 같다.

```sql
select
    member0_.member_id as member_i1_0_0_,
    team1_.team_id as team_id1_1_1_,
    member0_.age as age2_0_0_,
    member0_.team_id as team_id4_0_0_,
    member0_.username as username3_0_0_,
    team1_.name as name2_1_1_ 
from
    member member0_ 
inner join
    team team1_ 
        on member0_.team_id=team1_.team_id 
where
    member0_.username=?
```

## 서브쿼리
Querydsl에서 서브쿼리를 사용하려면 <code>JPAExpressios</code> 클래스의 팩토리 메서드를 이용해야 한다.

```java
// 서브 쿼리에서 사용되는 QMember 인스턴스 생성
QMember memberSub = new QMember("memberSub");

Member findMember = queryFactory
    .selectFrom(member)
    .where(member.age.eq(
        JPAExpressions
            .select(memberSub.age.max())
            .from(memberSub)
    )).fetchOne();
```


```sql
select
    member0_.member_id as member_i1_0_,
    member0_.age as age2_0_,
    member0_.team_id as team_id4_0_,
    member0_.username as username3_0_ 
from
    member member0_ 
where
    member0_.age=(
        select
            max(member1_.age) 
        from
            member member1_
    )
```

> JPA JPQL 서브쿼리 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다. 따라서 Querydsl에서도 인라인 뷰를
지원하지 않는다. 해결 방법으로는 서브쿼리를 조인으로 변경 또는 Native SQL을 사용해야 한다.

## Case 문
간단한 Case 문을 사용하는 경우에 <code>when</code>, <code>then</code>, <code>otehrwise</code> 메서드를 체이닝해서 사용할 수 있다. 복잡한
Case 문을 작성하는 경우에는 <code>CaseBuilder</code>를 사용한다. 

```java
List<String> result = queryFactory
    .select(member.age
        .when(10).then("열살")
        .when(20).then("스무살")
        .otherwise("기타"))
    .from(member)
    .fetch();
```

실제 수행되는 쿼리는 아래와 같다.

```sql
select
    case 
        when member0_.age=? then ? 
        when member0_.age=? then ? 
        else '기타' 
    end as col_0_0_ 
from
member member0_
```

## 상수, 문자 합치기

### 상수
조회 결과로 상수 표현식을 나타내기 위해서는 <code>Expressions</code> 클래스의 <code>constant</code> 메서드를 사용한다. 상수는 실제 쿼리가 실행될 때
사용되지 않고 결과에 추가되어 나타난다.

```java
List<Tuple> result = queryFactory
    .select(member.username, Expressions.constant("Dev"))
    .from(member)
    .fetch();
```


```sql
select
    member0_.username as col_0_0_ 
from
    member member0_
```

### 문자 합치기
문자를 합쳐서 하나의 값으로 표현할 때는 <code>concat</code> 메서드를 사용한다. 타입이 문자열이 아닌 경우에는 <code>stringValue</code>
메서드를 호출해서 타입을 문자열로 변경해서 사용하면 된다. 

```java
String result = queryFactory
    .select(member.username.concat("_").concat(member.age.stringValue()))
    .from(member)
    .where(member.username.eq("member1"))
    .fetchOne();
```

실제 수행되는 쿼리는 아래와 같다.

```sql
select
    concat(concat(member0_.username,
    ?),
    cast(member0_.age as char)) as col_0_0_ 
from
    member member0_ 
where
    member0_.username=?
```

# Querydsl 중급 문법

## 프로젝션
Select 구문에 어떤 필드를 가져올지 지정하는 것을 프로젝션이라고 한다. 사용자 이름만 반환하는 경우에는 프로젝션 대상이 사용자 이름 1개이다.
1개이기 때문에 타입을 명확하게 지정할 수 있다. 하지만, 프로젝션 대상이 여러개인 경우에는 Tuple 또는 DTO로 조회해야 한다.

### 프로젝션 대상 하나
사용자 이름 목록을 조회하는 예제

```java
List<String> result = queryFactory
    .select(member.username)
    .from(member)
    .fetch();
```

### Tuple
프로젝션이 여러개인 결과 값을 처리할 수 있도록 Querydsl에서는 <code>Tuple</code> 클래스를 제공한다.

```java
List<Tuple> result = queryFactory
    .select(member.username, member.age)
    .from(member)
    .orderBy(member.age.asc())
    .fetch();
```

### DTO로 조회
DTO 클래스로 결과값을 받는 방법은 프로퍼티, 필드, 생성자 접근 방법이 있다.

1. 프로퍼티 접근 방법
2. 필드 접근 방법
3. 생성자 접근 방법

#### 1. 프로퍼티 접근 방법
Querydsl은 <code>MemberDto</code> 객체를 **기본 생성자**로 생성하고 값을 Setter로 설정한다.

```java
List<MemberDto> result = queryFactory
    .select(Projections.bean(MemberDto.class,
            member.username,
            member.age))
    .from(member)
    .fetch();
```

#### 2. 필드 접근 방법
Getter, Setter 없이 바로 필드에 접근해서 값을 설정하는 방법이다.
 
```java
List<MemberDto> result = queryFactory
    .select(Projections.fields(MemberDto.class,
        member.username,
        member.age))
    .from(member)
    .fetch();
```

#### 3. 생성자 접근 방법
MemberDto 클래스의 생성자에 값을 설정하는 방법이다.

```java
List<MemberDto> result = queryFactory
    .select(Projections.constructor(MemberDto.class,
        member.username,
        member.age))
    .from(member)
    .fetch();
```

## @QueryProjection
프로젝션 결과 값을 DTO로 설정하는 3가지 방법을 살펴봤는데, 또 다른 방법으로 <code>@QueryProjection</code>이 있다. **DTO 생성자 위에
어노테이션을 붙임으로써 컴파일 결과로 QDto 파일이 생성**된다.

DTO 클래스 생성자에 <code>QueryProjection</code> 어노테이션을 추가하고 compileQuerydsl을 수행해서 QMemberDto 클래스를 얻는다.

```java
@Data
@NoArgsConstructor
public class MemberDto {

    private String username;
    private int age;

    @QueryProjection
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
```

생성된 <code>QMemberDto</code> 클래스의 생성자에 프로젝션을 설정한다.

```java
List<MemberDto> result = queryFactory
    .select(new QMemberDto(member.username, member.age))
    .from(member)
    .fetch();
```

## 동적 쿼리
복잡한 조건에 따른 동적 쿼리를 만들기 위해서는 다음 두 가지 방식을 사용할 수 있다. 

- BooleanBuilder
- Where

### BooleanBuilder
전달 받은 이름 개수만큼 <code>BooleanBuilder</code>를 사용해서 **or** 구문을 생성한다. or 뿐만 아니라 **and** 메서드도 지원한다.

```java
public List<Customer> getCustomer(String... names) {
    QCustomer customer = QCustomer.customer;
    JPAQuery<Customer> query = queryFactory.selectFrom(customer);
    BooleanBuilder builder = new BooleanBuilder();
    for (String name : names) {
        builder.or(customer.name.eq(name));
    }
    query.where(builder); // customer.name eq name1 OR customer.name eq name2 OR ...
    return query.fetch();
}
```

### Where
where 메서드는 Predicate를 여러 개 받을 수 있도록 선언되어 있다. **null 값을 전달하면 무시**하므로 검색 조건에 해당하는
값을 각 파라미터에 맞게 조건절을 생성하고 동적 쿼리를 쉽게 만들 수 있다. usernameEq 메서드의 경우에 username 값이 null 이면 null을 반환한다. 값이
있는 경우에는 조건절을 생성하고 BooleanExpression(Predicate 인터페이스를 구현한 클래스)를 반환한다.

```java
private List<Member> searchMember(String username, Integer age) {
    return queryFactory
        .selectFrom(member)
        .where(usernameEq(username), ageEq(age))
        .fetch();
}

private BooleanExpression usernameEq(String username) {
    if (username == null) {
        return null;
    }

    return member.username.eq(username);
}

private BooleanExpression ageEq(Integer age) {
    if (age == null) {
        return null;
    }

    return member.age.eq(age);
}
```

## 수정, 삭제 처리
하나의 쿼리로 대량의 데이터를 수정, 삭제 (DML) 처리하는 방법에 대해서 알아보자.

### 데이터 수정 쿼리
데이터 수정 쿼리를 다음과 같이 실행하게 되면, 영속성 컨택스트를 무시하고 바로 DB에 쿼리를 날린다. 그래서
영속성 컨텍스트와 DB 간에 데이터 불일치가 발생한다. 따라서 영속성 컨텍스트 내용을 초기하해서 DB와 일치시킨다.

```java
long count = queryFactory.update(member)
    .set(member.username, "비회원")
    .where(member.age.lt(29))
    .execute();

em.flush();
em.clear();
```

실제 수행되는 쿼리는 아래와 같다.

```sql
update
    member 
set
    username=? 
where
    age<?
```

### 데이터 삭제 쿼리
```java
long count = queryFactory.
    delete(member)
    .where(member.age.lt(30))
    .execute();
```

실제 수행되는 쿼리는 아래와 같다.

```sql
delete 
from
    member 
where
    age<?
```

## SQL 함수 호출
현재 사용하고 있는 DB에 맞는 Dialect에 이미 함수로 등록되어 있는 경우에만 사용할 수 있다. 아래 이미지는 MySQLDialect 클래스에 등록된 함수
일부분이다.

<img width="893" alt="mysql-dialect" src="https://user-images.githubusercontent.com/43853352/73242578-be59dd80-41e8-11ea-9b75-e32de4f1daac.png">

### SQL 함수 예제
```java
List<String> result = queryFactory
    .select(Expressions.stringTemplate("function('concat', {0}, {1}, {2}, {3})"
        , "Name: ", member.username, " Age: ", member.age.stringValue()))
    .from(member)
    .fetch();
```

실제 수행되는 쿼리는 아래와 같다.

```sql
select
    concat(?,
    member0_.username,
    ?,
    cast(member0_.age as char)) as col_0_0_ 
from
    member member0_
```

## References
- [인프런 실전! Querydsl 강좌](https://www.inflearn.com/course/Querydsl-%EC%8B%A4%EC%A0%84/dashboard)
- [Querydsl Reference Guide](http://www.querydsl.com/static/querydsl/4.1.3/reference/html_single)