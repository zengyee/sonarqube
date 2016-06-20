package pageobjects;

import com.codeborne.selenide.SelenideElement;

public class RuleItem {

  private final SelenideElement elt;

  public RuleItem(SelenideElement elt) {
    this.elt = elt;
  }

  public SelenideElement getTitle() {
    return elt.$(".coding-rule-title");
  }

  public SelenideElement getMetadata() {
    return elt.$(".coding-rule-meta");
  }


}
