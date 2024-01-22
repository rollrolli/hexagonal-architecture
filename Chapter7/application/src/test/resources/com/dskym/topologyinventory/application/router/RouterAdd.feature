@RouterAdd
Feature: 코어 라우터에 에지 라우터를 추가할 수 있는가?
  Scenario: 코어 라우터에 에지 라우터 추가
    Given 에지 라우터가 있다
    And 코어 라우터가 있다
    Then 코어 라우터에 에지 라우터를 추가한다

  Scenario: 또 다른 코어 라우터에 코어 라우터를 추가한다
    Given 코어 라우터가 있다
    And 또 다른 코어 라우터가 있다
    Then 코어 라우터에 이 코어 라우터를 추가한다