@ignore @report=false
Feature: android page object
  Background:
    # Wallet screen
    * def wallet_button_empty_ok = '//*[@text="OK"]'
    * def wallet_seed_wallet_text = '#com.github.dedis.popstellar:id/seed_wallet_text'
    * def wallet_new_wallet_button = "#com.github.dedis.popstellar:id/button_confirm_seed"
    * def wallet_confirm_new_wallet_button = '//*[@text="YES"]'
    * def wallet_restore_button = '#com.github.dedis.popstellar:id/import_seed_button'
    * def wallet_restore_input = '#com.github.dedis.popstellar:id/import_seed_entry_edit_text'

    # Lao screen
    * def lao_create_button = "#com.github.dedis.popstellar:id/home_create_button"
    * def lao_join_button = '#com.github.dedis.popstellar:id/home_join_button'
    * def lao_organization_name_input = "#com.github.dedis.popstellar:id/lao_name_entry_edit_text"
    * def lao_server_url_input = "#com.github.dedis.popstellar:id/server_url_entry_edit_text"
    * def lao_launch_button = "#com.github.dedis.popstellar:id/button_create"

    # Event screen
    * def event_create_button = "#com.github.dedis.popstellar:id/add_event"

  @name=open_app
  Scenario:
    Given driver webDriverOptions
    Then waitFor(wallet_button_empty_ok).click()

  @name=create_new_wallet
  Scenario:
    Given call read('android.feature@name=open_app')
    When waitFor(wallet_new_wallet_button)
    Then click(wallet_new_wallet_button)
    Then waitFor(wallet_confirm_new_wallet_button).click()

  @name=restore_wallet
  Scenario:
    Given call read('android.feature@name=open_app')
    When input(wallet_restore_input, params.seed)
    Then click(wallet_restore_button)
