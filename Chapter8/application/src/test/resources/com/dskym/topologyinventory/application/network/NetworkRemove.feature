@NetworkRemove
Feature: 스위치에서 네트워크를 제거할 수 있는가?
  Scenario: 스위치에서 네트워크 제거하기
    Given 제거하기 원하는 네트워크를 알고 있다
    And 네트워크를 제거하려는 스위치를 알고 있다
    Then 스위치에서 네트워크를 제거한다