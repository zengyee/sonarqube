package pageobjects;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

public class LoginPage {

  public LoginPage() {
    $("#login_form").should(Condition.exist);
  }

  public Navigation submitCredentials(String login, String password) {
    return submitCredentials(login, password, Navigation.class);
  }

  public LoginPage submitWrongCredentials(String login, String password) {
    return submitCredentials(login, password, LoginPage.class);
  }

  public SelenideElement getErrorMessage() {
    return $(By.cssSelector("#login_form .alert"));
  }

  private <T> T submitCredentials(String login, String password, Class<T> expectedResultPage) {
    $("#login").val(login);
    $("#password").val(password);
    $(By.name("commit")).click();
    return page(expectedResultPage);
  }
}
