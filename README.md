# 🎮 Crazy Eights

This project involved working with a web-based implementation of the Crazy Eights card game backed by a Spring Boot backend. The primary goals were to identify and fix a deliberately introduced bug, and to develop comprehensive automated acceptance tests to verify the game’s functionality.

## ▶️ Video Demo

[![Crazy Eights Demo](https://img.youtube.com/vi/KNOGn1ahCrA/maxresdefault.jpg)](https://www.youtube.com/watch?v=KNOGn1ahCrA)

## 🛠️ My Contributions

- Analyzed the existing codebase and gameplay mechanics to identify the introduced bug in the core game logic (`GameLogic.java`), then implemented an effective fix to ensure correct game behavior.
- Designed and implemented a suite of model-based automated acceptance tests using **JUnit 5** and **Selenium WebDriver**. These tests covered a wide range of gameplay scenarios specified in the provided test plan, including special card behaviors (Queens, Aces, Twos), turn order changes, scoring, and player actions.
- Created reusable test setup methods to rig the initial state of the game for each scenario, improving test modularity and maintainability.
- Used Selenium WebDriver to automate browser interactions with the web UI, verifying that game states, scores, and player turns were properly reflected in the interface.
- Executed the full test suite against the Spring Boot web application, ensuring end-to-end functionality and integration between frontend and backend components.
- Produced a video walkthrough demonstrating repository cloning, test execution, and passing results to facilitate assessment and reproducibility.

## 🧰 Technologies Used

- **Java** for backend game logic and test development
- **JUnit 5** for writing and structuring test cases
- **Selenium WebDriver** for browser-based UI automation
- **Spring Boot** as the web application framework
- **Maven** for project build and dependency management

## 🎓 Skills Developed

- Industrial-strength debugging and bug fixing within a moderately complex Java application
- Model-based test design and automated acceptance testing of web applications
- Selenium WebDriver usage for reliable and maintainable UI test automation
- Understanding of multiplayer game mechanics and special rule implementations
- Practical experience working with Spring Boot web applications and Maven build tools

---

This project sharpened my ability to analyze, test, and improve real-world software applications, bridging backend logic with frontend behavior through automated testing frameworks.
