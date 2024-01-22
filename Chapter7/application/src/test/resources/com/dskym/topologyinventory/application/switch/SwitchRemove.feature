@SwitchRemove
Feature: 에지 라우터에서 스위치를 제거할 수 있는가?
  Scenario: 에지 라우터에서 스위치 제거하기
    Given 제거하기 원하는 스위치를 알고 있다
    And 스위치는 아무런 네트워크도 갖고 있지 않다
    Then 에지 라우터에서 스위치를 제거한다