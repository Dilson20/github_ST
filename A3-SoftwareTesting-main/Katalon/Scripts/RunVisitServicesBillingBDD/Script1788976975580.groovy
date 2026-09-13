import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import internal.GlobalVariable as GlobalVariable

CucumberKW.GLUE = ['common', 'operations', 'billing']

CucumberKW.runFeatureFile('Include/features/visit_services_billing.feature')
