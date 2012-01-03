package com.imaginea.brightest.driver;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.imaginea.brightest.ExecutionContext;
import com.thoughtworks.selenium.Selenium;

public class BaseClass {
	protected Selenium selenium;
	protected ExecutionContext context = ExecutionContext.getNonStaticInstance();
	@Parameters({"browser"})
	@BeforeMethod
	public void beforeTest(@Optional("firefox") String browser){
		DesiredCapabilities capability= new DesiredCapabilities();
		capability.setBrowserName(browser);
		capability.setPlatform(Platform.ANY);
		//context.startServer();
		try {
			WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capability);
			selenium=new WebDriverBackedSelenium(driver, "http://www.google.com");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Assert.fail("Failed due to malformed URL exception.");
		} catch (Exception e) {
			Assert.fail("Failed due to: "+ e.toString());
		}
		
	}
	@AfterMethod
	public void afterTest(){
		selenium.stop();
		//context.stopServer();
	}
}
