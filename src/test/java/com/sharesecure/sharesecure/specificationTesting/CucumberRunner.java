package com.sharesecure.sharesecure.specificationTesting;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features", glue = "src/test/java/com/sharesecure/sharesecure/specificationTesting/stepDefinitions")
public class CucumberRunner {
}