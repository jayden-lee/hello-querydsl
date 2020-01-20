package com.jayden.study.querydsl;

import com.jayden.study.querydsl.entity.Member;
import com.jayden.study.querydsl.entity.QMember;
import com.jayden.study.querydsl.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    @BeforeEach
    public void before() {
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
    public void jpql() {
        String qlString = "select m from Member m " +
            "where m.username = :username";
        String username = "member1";

        Member findMember = em.createQuery(qlString, Member.class)
            .setParameter("username", username)
            .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo(username);
    }

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
}
