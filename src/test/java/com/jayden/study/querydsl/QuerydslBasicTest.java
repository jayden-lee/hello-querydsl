package com.jayden.study.querydsl;

import com.jayden.study.querydsl.entity.Member;
import com.jayden.study.querydsl.entity.QMember;
import com.jayden.study.querydsl.entity.QTeam;
import com.jayden.study.querydsl.entity.Team;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static com.jayden.study.querydsl.entity.QMember.*;
import static com.jayden.study.querydsl.entity.QTeam.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("Querydsl 예제 테스트 클래스")
class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    void before() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("Team A");
        Team teamB = new Team("Team B");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    @DisplayName("JPQL 이용한 쿼리 테스트")
    void jpql() {
        String qlString = "select m from Member m " +
            "where m.username = :username";
        String username = "member1";

        Member findMember = em.createQuery(qlString, Member.class)
            .setParameter("username", username)
            .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("Querydsl를 이용한 쿼리 테스트")
    void querydsl() {
        String username = "member1";

        Member findMember = queryFactory
            .selectFrom(member)
            .where(member.username.eq(username))
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("검색 조건 쿼리 테스트")
    void search() {
        String username = "member1";
        int age = 10;

        Member findMember = queryFactory
            .selectFrom(member)
            .where(member.username.eq(username),
                member.age.eq(age))
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo(username);
        assertThat(findMember.getAge()).isEqualTo(age);
    }

    @Test
    @DisplayName("정렬 쿼리 테스트")
    void sort() {
        List<Member> members = queryFactory
            .selectFrom(member)
            .where(member.age.goe(10))
            .orderBy(member.age.desc(), member.username.asc().nullsLast())
            .fetch();

        assertThat(members.size()).isEqualTo(4);
    }

    @Test
    @DisplayName("페이징 쿼리 테스트")
    void paging() {
        List<Member> members = queryFactory
            .selectFrom(member)
            .orderBy(member.age.desc())
            .offset(0)
            .limit(4)
            .fetch();

        assertThat(members.size()).isEqualTo(4);
        assertThat(members.get(0).getUsername()).isEqualTo("member4");
    }

    @Test
    @DisplayName("조인 테스트")
    void join() {
        List<Member> result = queryFactory
            .selectFrom(member)
            .join(member.team, team)
            .where(team.name.eq("Team A"))
            .fetch();

        assertThat(result)
            .extracting("username")
            .containsExactly("member1", "member2");
    }

    @Test
    @DisplayName("조인 On 테스트")
    void join_on() {
        List<Tuple> result = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(member.team, team)
            .on(team.name.eq("Team A"))
            .fetch();

        for (Tuple tuple : result) {
            System.out.println(tuple);
        }

        assertThat(result.size()).isEqualTo(4);
    }

    @Test
    @DisplayName("연관 관계 없는 조인 On 테스트")
    void no_relation_join_on() {
        em.persist(new Member("Team A"));
        em.persist(new Member("Team B"));
        em.persist(new Member("Team C"));

        List<Tuple> result = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(team).on(member.username.eq(team.name))
            .fetch();

        for (Tuple tuple : result) {
            System.out.println(tuple);
        }
    }

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

    @Test
    @DisplayName("서브쿼리 테스트")
    void sub_query() {
        QMember memberSub = new QMember("memberSub");

        Member findMember = queryFactory
            .selectFrom(QMember.member)
            .where(QMember.member.age.eq(
                JPAExpressions
                    .select(memberSub.age.max())
                    .from(memberSub)
            )).fetchOne();

        assertThat(findMember.getAge()).isEqualTo(40);
    }
}
