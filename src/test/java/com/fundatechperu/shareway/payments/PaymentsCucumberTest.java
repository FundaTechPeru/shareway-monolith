package com.fundatechperu.shareway.payments;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/** Ejecuta los escenarios Gherkin de Payments &amp; Settlement en {@code features/payments}. */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/payments")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.fundatechperu.shareway.payments.bdd")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "summary, html:target/cucumber-reports/payments.html")
class PaymentsCucumberTest {}
