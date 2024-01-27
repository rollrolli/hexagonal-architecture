@RouterCreate
Feature: 새로운 라우터를 만들 수 있는가?
  Scenario: 새로운 코어 라우터 만들기
    Given 코어 라우터를 만들기 위해 필요한 모든 데이터를 제공한다
    Then 새로운 코어 라우터가 생성된다
  Scenario: 새로운 에지 라우터 생성하기
    Given 에지 라우터를 만들기 위해 필요한 모든 데이터를 제공한다
    Then 새로운 에지 라우터가 생성된다