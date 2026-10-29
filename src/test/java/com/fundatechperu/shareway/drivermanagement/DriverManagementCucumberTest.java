package com.fundatechperu.shareway.drivermanagement;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/** Runs the Driver Management acceptance scenarios of {@code features/drivermanagement}. */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/drivermanagement")
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME,
    value = "com.fundatechperu.shareway.drivermanagement.bdd")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "summary, html:target/cucumber-reports/drivermanagement.html")
class DriverManagementCucumberTest {}
