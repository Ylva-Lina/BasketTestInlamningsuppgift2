package stepDefs;

import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class MyStepdefs {

    private WebDriver driver;
    private boolean missingLastName = false;
    private boolean mismatchedPasswords = false;
    private boolean uncheckedTermsAndConditions = false;

    @After
    public void tearDown() {
        driver.quit();
    }

    @Given("I am using browser {string}")
    public void iAmUsingBrowser(String browser) {
        if (browser.equals("chrome")) {
            driver = new ChromeDriver();
        } else if (browser.equals("edge")) {
            driver = new EdgeDriver();
        }
        driver.get("https://membership.basketballengland.co.uk/NewSupporterAccount");
    }

    @And("I have entered date of birth")
    public void iHaveEnteredDateOfBirth() {
        driver.findElement(By.id("dp")).sendKeys("06/03/1979");
    }

    @And("I have entered first name")
    public void iHaveEnteredFirstName() {
        driver.findElement(By.id("member_firstname")).sendKeys("Linda");
    }

    @And("I have entered last name {string}")
    public void iHaveEnteredLastName(String lastName) {
        if (!lastName.isEmpty()) {
            driver.findElement(By.id("member_lastname")).sendKeys(lastName);
        } else {
            //Setting boolean in order to confirm correct error message in last step
            missingLastName = true;
        }
    }

    @And("I have entered and confirmed unique email")
    public void iHaveEnteredAndConfirmedEmail() {
        //Calling method that generates a unique email adress
        String randEmail = randomEmailGenerator();
        driver.findElement(By.id("member_emailaddress")).sendKeys(randEmail);
        driver.findElement(By.id("member_confirmemailaddress")).sendKeys(randEmail);
    }

    @And("I have entered and confirmed {string} passwords")
    public void iHaveEnteredAndConfirmedPassword(String passMatch) {
        //Calling method that generates a unique password
        String randPassword = randomPasswordGenerator();

        //If the scenario demands matching passwords, the saved one is used both times
        if (passMatch.equals("matching")) {
            driver.findElement(By.id("signupunlicenced_password")).sendKeys(randPassword);
            driver.findElement(By.id("signupunlicenced_confirmpassword")).sendKeys(randPassword);
        } //If the scenario demands mismatched passwords, the method is called again
        else if (passMatch.equals("not matching")) {
            driver.findElement(By.id("signupunlicenced_password")).sendKeys(randPassword);
            driver.findElement(By.id("signupunlicenced_confirmpassword")).sendKeys(randomPasswordGenerator());

            //Setting boolean in order to confirm correct error message in last step
            mismatchedPasswords = true;
        }
    }


    @And("I have {string} terms and conditions")
    public void iHaveTermsAndConditions(String checkedStatus) {
        if (checkedStatus.equals("checked")) {
            driver.findElement(By.cssSelector(".md-checkbox > .md-checkbox:nth-child(1) .box")).click();
        } else {
            //Setting boolean in order to confirm correct error message in last step
            uncheckedTermsAndConditions = true;
        }
    }

    @And("I have checked Adult and Code of conduct")
    public void iHaveCheckedAdultAndCodeOfConduct() {
        driver.findElement(By.cssSelector(".md-checkbox:nth-child(2) > label > .box")).click();
        driver.findElement(By.cssSelector(".md-checkbox:nth-child(7) .box")).click();
    }

    @When("I click the Join button")
    public void iClickTheJoinButton() {
        driver.findElement(By.name("join")).click();
    }

    @Then("I {string} a registered member")
    public void iARegisteredMember(String memberStatus) {
        //If the scenario demands a successful registration, confirm by asserting Thank you-message
        if (memberStatus.equals("am")) {
            String expected = "THANK YOU FOR CREATING AN ACCOUNT WITH BASKETBALL ENGLAND";
            String actual = driver.findElement(By.cssSelector(".bold:nth-child(1)")).getText();

            assertEquals(expected, actual);
        } //Otherwise if the scenario demands failed registration, confirm by asserting error messages
        else if (memberStatus.equals("am not")) {
            if (missingLastName) {
                //Expected error message
                String expected = "Last Name is required";

                //Waiting for error message to be visible
                wait(driver, By.cssSelector("#signup_form > div:nth-child(6) > div:nth-child(2) > div > span > span"));

                //Compare expected and actual error message
                String actual = driver.findElement(By.cssSelector("#signup_form > div:nth-child(6) > div:nth-child(2) > div > span > span")).getText();
                assertEquals(expected, actual);

            } else if (mismatchedPasswords) {
                String expected = "Password did not match";

                wait(driver, By.cssSelector("#signup_form > div:nth-child(9) > div > div.row > div:nth-child(2) > div > span > span"));
                String actual = driver.findElement(By.cssSelector("#signup_form > div:nth-child(9) > div > div.row > div:nth-child(2) > div > span > span")).getText();

                assertEquals(expected, actual);
            } else if (uncheckedTermsAndConditions) {
                String expected = "You must confirm that you have read and accepted our Terms and Conditions";

                wait(driver, By.cssSelector("#signup_form > div:nth-child(12) > div > div:nth-child(2) > div:nth-child(1) > span > span"));
                String actual = driver.findElement(By.cssSelector("#signup_form > div:nth-child(12) > div > div:nth-child(2) > div:nth-child(1) > span > span")).getText();

                assertEquals(expected, actual);
            }
        }
    }

    private String randomEmailGenerator() {
        return "LindaBelcher" + System.currentTimeMillis() + "!@burgers.com";
    }

    private String randomPasswordGenerator() {
        return "Belcher" + System.currentTimeMillis();
    }

    private void wait(WebDriver driver, By by) {
        (new WebDriverWait(driver, Duration.ofSeconds(10))).until(ExpectedConditions.
                presenceOfElementLocated(by));
    }
}
