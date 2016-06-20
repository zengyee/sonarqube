package pageobjects;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.page;

public class Navigation {

  public static Navigation openHomepage() {
    return open("/", Navigation.class);
  }

  public boolean isLoggedIn() {
    return !$(By.linkText("Log in")).exists();
  }

  public LoginPage clickOnLogIn() {
    $(By.linkText("Log in")).click();
    return Selenide.page(LoginPage.class);
  }

  public RulesPage clickOnRules() {
    $(By.linkText("Rules")).click();
    return page(RulesPage.class);
  }

  public SelenideElement clickOnQualityProfiles() {
    return $(By.linkText("Quality Profiles"));
  }

  public SelenideElement rightBar() {
    return $(By.cssSelector("#global-navigation .navbar-right"));
  }
}
