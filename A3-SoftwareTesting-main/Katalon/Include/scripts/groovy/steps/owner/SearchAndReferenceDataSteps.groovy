package common
import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint

import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.checkpoint.Checkpoint
import com.kms.katalon.core.checkpoint.CheckpointFactory
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testcase.TestCase
import com.kms.katalon.core.testcase.TestCaseFactory
import com.kms.katalon.core.testdata.TestData
import com.kms.katalon.core.testdata.TestDataFactory
import com.kms.katalon.core.testobject.ObjectRepository
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import internal.GlobalVariable

import org.openqa.selenium.WebElement
import org.openqa.selenium.WebDriver
import org.openqa.selenium.By

import com.kms.katalon.core.mobile.keyword.internal.MobileDriverFactory
import com.kms.katalon.core.webui.driver.DriverFactory

import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.TestObjectProperty

import com.kms.katalon.core.mobile.helper.MobileElementCommonHelper
import com.kms.katalon.core.util.KeywordUtil

import com.kms.katalon.core.webui.exception.WebElementNotFoundException

import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When



class SearchAndReferenceDataSteps {
	
	String currentLastName

	@Given("the receptionist has more than one owner registered")
	def hasMultipleOwners() {
		WebUI.openBrowser('')
		WebUI.navigateToUrl('http://localhost:8080')
	}
	
	@When("the receptionist searchs for owners without any lastname entered")
	def searchWithEmptyLastName() {
		WebUI.navigateToUrl('http://localhost:8080/owners/find')
		WebUI.setText(findTestObject('Object Repository/Page_Testing-Site  a Spring Framework demonstration/input_Last Name_lastName'), '')
		WebUI.click(findTestObject('Object Repository/Page_Testing-Site  a Spring Framework demonstration/button_Find Owner'))
	}
	
	@Then("all registered owners should be displayed as a paginated list")
	def allOwnersDisplayed() {
		WebUI.verifyElementText(findTestObject('Object Repository/Page_Testing-Site  a Spring Framework demonstration/th_Name'), 'Name')
	}
	
	@Given("no owner is registered under the lastname {string}")
	def noOwnerWithLastName(String lastname) {
		WebUI.openBrowser('')
		currentLastName = lastname
	}
	
	@Given("only one owner is registered under the lastname {string}")
	def onlyOneOwnerUnderLastName(String lastname) {
		WebUI.openBrowser('')
		currentLastName = lastname
	}
	
	@Given("multiple owners are registered under the lastname {string}")
	def multipleOwnerUnderLastName(String lastname) {
		WebUI.openBrowser('')
		currentLastName = lastname
	}

	@When("the receptionist searchs for an owner by that lastname")
	def searchByStoredLastName() {
		WebUI.navigateToUrl('http://localhost:8080/owners/find')
		WebUI.setText(findTestObject('Object Repository/Page_Testing-Site  a Spring Framework demonstration/input_Last Name_lastName'), currentLastName)
		WebUI.click(findTestObject('Object Repository/Page_Testing-Site  a Spring Framework demonstration/button_Find Owner'))	
	}
	
	@When("the receptionist searchs for an owner using {string}")
	def searchUsingGivenLastName(String lastname) {
		WebUI.navigateToUrl('http://localhost:8080/owners/find')
		WebUI.setText(findTestObject('Object Repository/Page_Testing-Site  a Spring Framework demonstration/input_Last Name_lastName'), lastname)
		WebUI.click(findTestObject('Object Repository/Page_Testing-Site  a Spring Framework demonstration/button_Find Owner'))
	}
	
	@Then("a {string} message should be displayed")
	def noOwnerListShown(String message) {
		String actualMessage = WebUI.getText(findTestObject('Object Repository/Page_Testing-Site  a Spring Framework demonstration/p_has not been found')) 
		assert actualMessage.toLowerCase().contains(message.toLowerCase())
	}
	
	@Then("the receptionsist should be navigated directly to that owner detail page")
	def navigatedDirectlyToOwnerDetail() {
		WebUI.verifyElementText(findTestObject('Object Repository/Page_Testing-Site  a Spring Framework demonstration/h2_Owner Information'), 'Owner Information')
		String currentUrl = WebUI.getUrl()
		assert currentUrl.matches(".*\\/owners\\/\\d+")
	}
	
	@Then("a owner pagination list is shown")
	def ownerPaginationListShown() {
		WebUI.verifyElementText(findTestObject('Object Repository/Page_Testing-Site  a Spring Framework demonstration/th_Name'), 'Name')
	}
	
	@Given("a veterinarian is registered with no assigned specialty")
	def vetWithNoSpecialtyRegistered() {
		WebUI.openBrowser('')
	}
	
	@When("the receptionist views the veterinarian directory")
	def viewVetDirectory() {
		WebUI.navigateToUrl('http://localhost:8080/vets.html')
	}
	
	@Then("that veterinarian should appear in the list without an error")
	def vetAppearWithoutAnError() {
		WebUI.verifyElementText(findTestObject('Object Repository/Page_Testing-Site  a Spring Framework demonstration/span_James Carter'), 'James Carter')
	}
	
}