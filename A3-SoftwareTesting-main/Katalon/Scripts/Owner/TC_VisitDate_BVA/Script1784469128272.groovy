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
WebUI.setText(findTestObject('Page_Testing-Site  a Spring Framework demonstration/input_First Name'), 'Trong')
WebUI.setText(findTestObject('Page_Testing-Site  a Spring Framework demonstration/input_Last Name'), 'Tran')
WebUI.setText(findTestObject('Page_Testing-Site  a Spring Framework demonstration/input_Address'), 'Brown street')
WebUI.setText(findTestObject('Page_Testing-Site  a Spring Framework demonstration/input_City'), 'Ho Chi Minh city')
WebUI.setText(findTestObject('Page_Testing-Site  a Spring Framework demonstration/input_Telephone'), '0123456789')
WebUI.click(findTestObject('Page_Testing-Site  a Spring Framework demonstration/button_Add Owner'))

// Compute today's date fresh, inline (no caching risk)
def todayDate = new Date().format('MM/dd/yyyy')

WebUI.click(findTestObject('Page_Testing-Site  a Spring Framework demonstration/a_Add New Pet'))
WebUI.setText(findTestObject('Page_Testing-Site  a Spring Framework demonstration/input_Name'), 'Dirty')
WebUI.selectOptionByValue(findTestObject('Page_Testing-Site  a Spring Framework demonstration/select_Type'), 'cat', false)
WebUI.setText(findTestObject('Page_Testing-Site  a Spring Framework demonstration/input_Birth Date'), todayDate)
WebUI.click(findTestObject('Page_Testing-Site  a Spring Framework demonstration/button_Add Pet'))

// Compute the visit date from the data-driven offset, fresh, inline
def cal = Calendar.getInstance()
cal.add(Calendar.DATE, Integer.parseInt(dateOffset))
def visitDate = cal.getTime().format('MM/dd/yyyy')

WebUI.click(findTestObject('Page_Testing-Site  a Spring Framework demonstration/a_Add Visit'))
WebUI.setText(findTestObject('Page_Testing-Site  a Spring Framework demonstration/input_Date_date'), visitDate)
WebUI.setText(findTestObject('Page_Testing-Site  a Spring Framework demonstration/input_Description'), 'Checkup - offset ' + dateOffset + ' day(s) test')
WebUI.click(findTestObject('Page_Testing-Site  a Spring Framework demonstration/button_Add Visit'))

boolean successShown = WebUI.waitForElementPresent(findTestObject('Page_Testing-Site  a Spring Framework demonstration/div_success-message'), 3, FailureHandling.OPTIONAL)

if (expectedResult == 'valid') {
	WebUI.verifyEqual(successShown, true)
} else {
	WebUI.verifyEqual(successShown, false)
}