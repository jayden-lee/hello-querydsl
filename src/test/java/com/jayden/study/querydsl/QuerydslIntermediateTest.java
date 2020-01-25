package com.jayden.study.querydsl;

import com.jayden.study.querydsl.dto.MemberDto;
import com.jayden.study.querydsl.dto.QMemberDto;
import com.jayden.study.querydsl.entity.Member;
import com.jayden.study.querydsl.entity.Team;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static com.jayden.study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
@DisplayName("Querydsl 중급 예제 테스트 클래스")
class QuerydslIntermediateTest {

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
    @DisplayName("튜플 프로젝션 테스트")
    void tuple_projection() {
        List<Tuple> result = queryFactory
            .select(member.username, member.age)
            .from(member)
            .orderBy(member.age.asc())
            .fetch();

        for (Tuple tuple : result) {
            System.out.println(tuple);
        }
    }

    @Test
    @DisplayName("DTO 프로젝션 프로퍼티 테스트")
    void dto_projection_property() {
        List<MemberDto> result = queryFactory
            .select(Projections.bean(MemberDto.class,
                member.username,
                member.age))
            .from(member)
            .fetch();

        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }

    @Test
    @DisplayName("DTO 프로젝션 필드 테스트")
    void dto_projection_field() {
        List<MemberDto> result = queryFactory
            .select(Projections.fields(MemberDto.class,
                member.username,
                member.age))
            .from(member)
            .fetch();

        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }

    @Test
    @DisplayName("DTO 프로젝션 생성자 테스트")
    void dto_projection_constructor() {
        List<MemberDto> result = queryFactory
            .select(Projections.constructor(MemberDto.class,
                member.username,
                member.age))
            .from(member)
            .fetch();

        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }

    @Test
    @DisplayName("DTO QueryProjection 테스트")
    void dto_query_projection() {
        List<MemberDto> result = queryFactory
            .select(new QMemberDto(member.username, member.age))
            .from(member)
            .fetch();

        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }
}
