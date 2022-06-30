Feature: web test

  Background: App Preset
    * configure driver = { type: 'chrome', executable: 'C:/Program Files/Google/Chrome/Application/chrome.exe'}
    #* configure driver = { type: 'chrome' }
    * def driverOptions = karate.toAbsolutePath('file:../../fe1-web/web-build/index.html')

    # ================= Page Object Start ====================

    # Introduction screen
    * def exploring_selector = "[data-testid='exploring_selector']"

    #Home Screen
    * def tab_connect_selector = '{}Connect'
    * def launch_selector = "[data-testid='launch_selector']"

    # Launch screen
    * def tab_launch_lao_name_selector = "input[data-testid='launch_organization_name_selector']"
    * def backend_address_selector = "input[data-testid='launch_address_selector']"
    * def tab_launch_create_lao_selector = "[data-testid='launch_launch_selector']"

    # Lao Event List
    * def add_event_selector = "[data-testid='create_event_selector']"
    * def tab_events_selector = '{}Events'
    * def roll_call_title_selector = "input[data-testid='roll_call_name_selector']"
    * def roll_call_location_selector = "input[data-testid='roll_call_location_selector']"
    * def roll_call_confirm_selector = "[data-testid='roll_call_confirm_selector']"
    * def event_name_selector = "[data-testid='current_event_selector_0']"


  @name=basic_setup
  Scenario: Setup connection to the backend and complete on the home page
    Given driver driverOptions

    # Create and import mock backend
    And call read('classpath:fe/net/mockBackend.feature')
    * def backendURL = 'ws://localhost:' + backend.getPort()
    # Import message filters
    And call read('classpath:common/net/filters.feature')

    # The default input function is not consistent and does not work every time.
    # This replaces the input function with one that just tries again until it works.
    * def input =
          """
            function(selector, data) {
              tries = 0
              while (driver.attribute(selector, "value") != data) {
                if (tries++ >= max_input_retry)
                  throw "Could not input " + data + " - max number of retry reached."
                driver.clear(selector)
                driver.input(selector, data)
                delay(10)
              }
            }
          """

    # Click on the explore button of the intro screen
    * click(exploring_selector)
    # Click on the connect navigation item
    * click(tab_connect_selector)
    # Click on launch button
    * click(launch_selector)
    # Connect to the backend
    * input(backend_address_selector, backendURL)

  # Roll call web procedure
  @name=create_roll_call
  Scenario: Create a roll call for an already created LAO
    Given click(tab_events_selector)
    And click(add_event_selector)

    # Clicking on Create Roll-Call. This is because it is (as of now) an actionSheet element which does not have an id
    # If it breaks down, check that the name of the button has not changed, try to add more delay. Otherwise maybe karate
    # added a way to directly do that after the time of our writing.
    #
    # script allows the evaluation of arbitrary javascript code and document.evaluate
    # (https://developer.mozilla.org/en-US/docs/Web/API/Document/evaluate) allows the evaluation of an XPath expression.
    #
    # Somehow this turned out to work, at least if it was wrapped
    # in a setTimeout which delays the execution of the script.
    # The XPath selector is described here: https://stackoverflow.com/a/29289196/2897827
    * script("setTimeout(() => document.evaluate('//div[text()=\\'Create Roll-Call\\']', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.click(), 1000)")

    # Provide roll call required information
    And input(roll_call_title_selector, rc_name)
    And input(roll_call_location_selector, 'EPFL')