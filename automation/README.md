_At this time, LC does not have the resources to offer support for this open source code. While LC will make the code available, the Library does not currently promise to address any issues which are pointed out by the community beyond what is needed for the Library's own usage._

## Automation

This is **FETCH **automation project. This repository demonstrates how to set up and use Cucumber with Selenium WebDriver for automated testing of web application using Java and Maven, integrated with IntelliJ IDEA.


## Prerequisites

Ensure you have the following installed and set up:

- Java Development Kit (JDK) installed (version 8 or higher)
- IntelliJ IDEA installed (or any other Java IDE)
- Git installed and configured
- Maven installed


## Getting Started

Follow these steps to get a local copy of the project and run the tests:

1. Clone the repository to your local machine:

   ```bash
   git clone https://git.example.com/fetch/automation.git
   ```

2. Open the project in IntelliJ IDEA:

   - Launch IntelliJ IDEA.
   - Select `File` -> `Open` and navigate to the cloned directory.

3. Resolve Dependencies:

   - IntelliJ IDEA should automatically detect and download the necessary dependencies specified in the `pom.xml` file. If not, you can manually trigger a Maven build to download them.

4. Usage:

   - Cucumber feature files are located in `src/test/resources/uiFeatures`.
   - Step definitions using Selenium WebDriver are located in `src/test/java/automation/step_definitions`.
   - Use Maven to build and manage dependencies.


## Test Execution

1. Run Individual Feature Files:

   - Cucumber feature files are located in `src/test/resources/uiFeatures`.
   - To run a specific feature file, locate it under `src/test/resources/uiFeatures`, right-click on it, and select `Run`'Feature: <name>'.

2. Running Test via TestRunner:

   - Open TestRunner class located in src/test/java/runner.
   - The `@CucumberOptions` `tags` annotation in this class can be modified to control which tests are run. `Tags` could be found in Feature Files in `src/test/resources/uiFeatures`.(E.g. tags = "@accession")
   - Right-click on the file and choose `Run``TestRunner`.

3. Viewing Test Results:

   - After running the tests, view the test results in the IntelliJ IDEA `Run` tool window.
   - The results will show the status (pass/fail) of each scenario and step defined in your feature files and corresponding step definitions.


## HTML report

- An HTML report can be found in `target` folder
- Right click `cucumber.html` -> `Open In` -> `Open In Browser`-> Browser of your choice


## Acknowledgements

- [Cucumber](https://cucumber.io/) - Used for behavior-driven development
- [Selenium WebDriver](https://www.selenium.dev/) - Used to automate browser actions
- [JUnit](https://junit.org/junit5/) - Used for unit testing
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) - Integrated Development Environment (IDEA)
- [Maven](https://maven.apache.org/) - Dependency Management
- [Java](https://www.java.com/) - The programming language used

