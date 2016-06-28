/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package it.allInOne;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.sonar.orchestrator.Orchestrator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import pageobjects.LoginPage;
import pageobjects.Navigation;
import pageobjects.RuleItem;
import pageobjects.RulesPage;

import static com.codeborne.selenide.Condition.hasText;
import static com.codeborne.selenide.Condition.or;
import static com.codeborne.selenide.Selenide.open;
import static org.assertj.core.api.Assertions.assertThat;

public class AllInOneTest {

  @ClassRule
  public static final Orchestrator ORCHESTRATOR = Orchestrator.builderEnv()
    .setOrchestratorProperty("javaVersion", "LATEST_RELEASE")
    .addPlugin("java")
    .setOrchestratorProperty("cssVersion", "LATEST_RELEASE")
    .addPlugin("css")
    .build();

  @BeforeClass
  public static void configureBrowser() {
    Configuration.baseUrl = ORCHESTRATOR.getServer().getUrl();
    Configuration.reportsFolder = "target/screenshots";
  }

  @Before
  public void resetSession() {
    open("/sessions/logout");
  }

  @Test
  public void log_in_with_correct_credentials() {
    Navigation nav = Navigation.openHomepage();
    assertThat(nav.isLoggedIn()).isFalse();

    Navigation page = nav.clickOnLogIn().submitCredentials("admin", "admin");
    page.rightBar().shouldHave(Condition.text("Administrator"));
    assertThat(nav.isLoggedIn()).isTrue();
  }

  @Test
  public void log_in_with_wrong_credentials() {
    Navigation nav = Navigation.openHomepage();

    LoginPage page = nav.clickOnLogIn().submitWrongCredentials("admin", "wrong");
    page.getErrorMessage().shouldHave(Condition.text("Authentication failed"));

    assertThat(Navigation.openHomepage().isLoggedIn()).isFalse();
  }

  @Test
  public void test_page_rules() {
    RulesPage rulesPage = Navigation.openHomepage().clickOnRules();

    // wait for rules to be displayed
    rulesPage.getRules().shouldHave(CollectionCondition.sizeGreaterThan(0));

    assertThat(rulesPage.getTotal()).isGreaterThan(0);
    for (RuleItem ruleItem : rulesPage.getRulesAsItems()) {
      ruleItem.getTitle().should(Condition.visible);
      ruleItem.getMetadata().should(or("have type", hasText("Bug"), hasText("Code Smell"), hasText("Vulnerability")));
    }
  }
}
