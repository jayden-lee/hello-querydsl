package com.jayden.study.querydsl.repository;

import com.jayden.study.querydsl.dto.MemberSearchCondition;
import com.jayden.study.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition condition);
}
