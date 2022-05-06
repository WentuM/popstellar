@ignore @report=false
Feature: Constants
  Scenario: Creates constants that will be used by other features
    # TODO: make the function depend on all the attributes the lao id depends on
    * def createLaoIdEmptyName =
      """
        function(){
          return "p8TW08AWlBScs9FGXK3KbLQX7Fbgz8_gLwX-B5VEWS0="
        }
      """
    * def createLaoIdNegativeTime =
      """
        function(){
          return "p8TW08AWlBScs9FGXK3KbLQX7Fbgz8_gLwX-B5VEWS0="
        }
      """
    * def createLaoValid =
      """
        function(){
          return "p_EYbHyMv6sopI5QhEXBf40MO_eNoq7V_LygBd4c9RA="
        }
      """
    * def createRollCallValid =
      """
        function(){
          return "Slj7C1LBEXlRC8ItV2B0zWfUSD6YiGJt6N_I_m02uw4="
        }
      """
    * def createRollCallInvalid =
      """
        function(){
          return "Dui7C1LBEXlRC8ItV2B0zWfUSD6YiGJt6N_I_m02uw4="
        }
      """
    * def createValidRollCallOpenId =
    """
        function(){
          return "VSsRrcHoOTQJ-nU_VT_FakiMkezZA86z2UHNZKCxbN8="
        }
      """
    * def createValidRollCallOpenUpdateId =
    """
        function(){
          return "l2OYtZueg1xkjvh3RCWw0nSZrrPNThuaz3U3ys7MjHI="
        }
      """
    * def createInvalidRollCallOpenUpdateId =
    """
        function(){
          return "krCHh6OFWIjSHQiUSrWyx1FV0Jp8deC3zUyelhPG-Yk="
        }
      """
    * def createValidRollCallCloseId =
    """
        function(){
          return "N9DNfliEA9lrcDNAnw5PXjOS84kbq2fLFz8GzIxzCwU="
        }
      """
    * def createValidRollCallCloseUpdateId =
    """
        function(){
          return "IGLB3pipK0p0G5E_wFxedEk4IpyM3L7XIQoFummhj0Y="
        }
      """
    * def createInvalidRollCallCloseUpdateId =
    """
        function(){
          return "lM5Lntpk4Y4SpKjzV2ICYpe4YnMOvWz1eeREB_RVVRg="
        }
      """
    * def createValidElectionSetupId =
    """
        function(){
          return "rdv-0minecREM9XidNxnQotO7nxtVVnx-Zkmfm7hm2w="
        }
      """
    * def createIsThisProjectFunQuestionId=
    """
        function(){
          return "3iPxJkdUiCgBd0c699KA9tU5U0zNIFau6spXs5Kw6Pg="
        }
      """
    * def organizerPk =
      """
        function(){
          return "J9fBzJV70Jk5c-i3277Uq4CmeL4t53WDfUghaK0HpeM="
        }
      """
    * def attendeePk =
      """
        function(){
          return "M5ZychEi5rwm22FjwjNuljL1qMJWD2sE7oX9fcHNMDU="
        }
      """
    * def getLaoIdEmptyName = call createLaoIdEmptyName
    * def getLaoIdNegativeTime = call createLaoIdNegativeTime
    * def getLaoValid = call createLaoValid

    * def getRollCallValidId = call createRollCallValid
    * def getRollCallInvalidId = call createRollCallInvalid

    * def getRollCallOpenValidId = call createValidRollCallOpenId
    * def getRollCallOpenValidUpdateId = call createValidRollCallOpenUpdateId
    * def getRollCallOpenInvalidUpdateId = call createInvalidRollCallOpenUpdateId

    * def getRollCallCloseValidId = call createValidRollCallCloseId
    * def getRollCallCloseValidUpdateId = call createValidRollCallCloseUpdateId
    * def getRollCallCloseInvalidUpdateId = call createInvalidRollCallCloseUpdateId

    * def getValidElectionSetupId = call createValidElectionSetupId
    * def getIsThisProjectFunQuestionId = call createIsThisProjectFunQuestionId

    * def getOrganizer = call organizerPk
    * def getAttendee = call attendeePk

    * def INVALID_ACTION =          {error: {code: -1, description: '#string'}}
    * def INVALID_RESOURCE =        {error: {code: -2, description: '#string'}}
    * def RESOURCE_ALREADY_EXISTS = {error: {code: -3, description: '#string'}}
    * def INVALID_MESSAGE_FIELD =   {error: {code: -4, description: '#string'}}
    * def ACCESS_DENIED =           {error: {code: -5, description: '#string'}}
    * def INTERNAL_SERVER_ERROR =   {error: {code: -6, description: '#string'}}
    * def VALID_MESSAGE =           {result: 0}
