package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    /**
     * JPA 스펙 상 엔티티나 임베디드 타입은 자바 기본 생성자(default constructor)를 public 또는 protected 로 설정해야한다.
     * protected 로 설정하는 것이 그나마 더 안전하다.
     */
    protected Address() {

    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
