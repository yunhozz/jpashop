package jpabook.jpashop.service.query;

import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {

    /**
     * 화면이나 API 에 맞춘 서비스 (주로 읽기 전용 트랜잭션 사용)
     */

    private final OrderRepository orderRepository;
}
