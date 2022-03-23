package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Transactional //반복 가능한 테스트 지원, 각각의 테스트를 실행할 때마다 강제로 롤백 -> DB 초기화
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em; //영속성 context 가 flush 하는 것을 확인하고 싶을 때

    @Test
//    @Rollback(value = false) //DB가 자동으로 롤백되는 것을 방지 -> H2 콘솔에서 DB 확인이 가능해진다. (메모리 모드가 아닐때)
    void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("yunho");

        //when
        Long savedId = memberService.join(member);

        //then
//        em.flush(); //DB 에 쿼리를 날려준다.
        assertEquals(member, memberRepository.findOne(savedId));
    }

    @Test
    void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("yunho");

        Member member2 = new Member();
        member2.setName("yunho");

        //when
        memberService.join(member1);

        //then
        try {
            memberService.join(member2);
            fail("예외가 발생해야 한다.");
        } catch (IllegalStateException e) {

        }
    }
}