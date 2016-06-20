package pageobjects;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class RulesPage {

  public RulesPage() {
    $(By.cssSelector(".coding-rules")).should(Condition.exist);
  }

  public ElementsCollection getRules() {
    return $$(".coding-rules .coding-rule");
  }

  public List<RuleItem> getRulesAsItems() {
    return getRules()
      .stream()
      .map(elt -> new RuleItem(elt))
      .collect(Collectors.toList());
  }

  public int getTotal() {
    // warning - number is localized
    return Integer.parseInt($("#coding-rules-total").text());
  }
}
