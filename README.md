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