import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testng.keyword.TestNGBuiltinKeywords as TestNGKW
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys

WebUI.openBrowser(null)

WebUI.maximizeWindow()

WebUI.navigateToUrl('http://localhost:8080/')

WebUI.click(findTestObject('Page_Testing-Site  a Spring Framework demonstration/span_Find Owners'))
WebUI.click(findTestObject('Page_Testing-Site  a Spring Framework demonstration/a_Add Owner'))
WebUI.setText(findTestObject('Page_Testing-Site  a Spring Framework demonstration/input_First Name'), firstName)
WebUI.setText(findTestObject('Page_Testing-Site  a Spring Framework demonstration/input_Last Name'), lastName)
WebUI.setText(findTestObject('Page_Testing-Site  a Spring Framework demonstration/input_Address'), address)
WebUI.setText(findTestObject('Page_Testing-Site  a Spring Framework demonstration/input_City'), city)
WebUI.setText(findTestObject('Page_Testing-Site  a Spring Framework demonstration/input_Telephone'), telephone)
WebUI.click(findTestObject('Page_Testing-Site  a Spring Framework demonstration/button_Add Owner'))

// Overall check: form should not save
boolean successShown = WebUI.waitForElementPresent(findTestObject('Page_Testing-Site  a Spring Framework demonstration/div_success-message'), 3, FailureHandling.OPTIONAL)
WebUI.verifyEqual(successShown, false)

// Specific check: the correct field's error message should appear
switch (expectedErrorField) {
	case 'firstName':
		WebUI.verifyElementPresent(findTestObject('Page_Testing-Site  a Spring Framework demonstration/lbl_FirstNameError'), 3)
		break
	case 'lastName':
		WebUI.verifyElementPresent(findTestObject('Page_Testing-Site  a Spring Framework demonstration/lbl_LastNameError'), 3)
		break
	case 'address':
		WebUI.verifyElementPresent(findTestObject('Page_Testing-Site  a Spring Framework demonstration/lbl_AddressError'), 3)
		break
	case 'city':
		WebUI.verifyElementPresent(findTestObject('Page_Testing-Site  a Spring Framework demonstration/lbl_CityError'), 3)
		break
	case 'telephone':
		WebUI.verifyElementPresent(findTestObject('Page_Testing-Site  a Spring Framework demonstration/lbl_TelephoneEmptyError'), 3)
		break
}