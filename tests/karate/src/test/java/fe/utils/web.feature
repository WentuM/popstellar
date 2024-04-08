@ignore @report=false
Feature: web page object
  Background:
    # Functions

    # Wallet screen
    * def wallet_seed_wallet_text = "[data-testid='seed_wallet_text']"
    * def wallet_new_wallet_button = "[data-testid='exploring_selector']"
    * def wallet_goto_restore_wallet_button = "{}Restore"
    * def wallet_restore_wallet_button = "{}Restore Wallet"
    * def wallet_restore_input = "input"

    # Lao screen
    * def lao_create_button = "{}Create"
    * def lao_join_button = "{^}Join"
    * def lao_organization_name_input = "input[data-testid='launch_organization_name_selector']"
    * def lao_server_url_input = "input[data-testid='launch_address_selector']"
    * def lao_launch_button = "[data-testid='launch_launch_selector']"

    # Event screen
    * def event_create_button = "[data-testid='create_event_selector']"

  @name=open_app
  Scenario:
    Given driver webDriverOptions
    Given driver 'about:blank'
    And driver.dimensions = { left: 0, top: 0, width: screenWidth, height: screenHeight }
    Then driver frontendURL

  @name=create_new_wallet
  Scenario:
    Given call read('web.feature@name=open_app')
    When waitFor(wallet_new_wallet_button)
    Then click(wallet_new_wallet_button)

  @name=restore_wallet
  Scenario:
    Given call read('web.feature@name=open_app')
    When waitFor(wallet_goto_restore_wallet_button).click()
    Then input(wallet_restore_input, params.seed)
    Then click(wallet_restore_wallet_button)

