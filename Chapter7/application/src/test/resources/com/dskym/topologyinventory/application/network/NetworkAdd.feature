@NetworkAdd
Feature: 스위치에 네트워크를 추가할 수 있는가?
  Scenario: 스위치에 네트워크 추가하기
    Given 네트워크가 있다
    And 스위치에 추가할 네트워크가 있다
    Then 스위치에 네트워크를 추가한다