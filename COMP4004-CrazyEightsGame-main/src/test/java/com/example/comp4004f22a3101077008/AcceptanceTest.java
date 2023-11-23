package com.example.comp4004f22a3101077008;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;


@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = Application.class)
public class AcceptanceTest {
    @Autowired
    GameData gd;
    @Autowired
    GameLogic game;
    @LocalServerPort
    int port;

    @Test
    public void testGameDrivers(){
        SpringApplication.run(Application.class);

        System.setProperty("webdriver.chrome.driver",
                ".\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        WebDriver[] multiDriver = new WebDriver[4];
        for (int i = 0; i < multiDriver.length; i++) {
            multiDriver[i] = new ChromeDriver(options);
            multiDriver[i].get("http://localhost:" + port);

            WebDriverWait wait = new WebDriverWait(multiDriver[i], Duration.ofSeconds(10));
            wait.until(ExpectedConditions.elementToBeClickable (By.id("usernameBtn"))).click();//waits till register button pops up before closing
        }

        for (WebDriver webDriver : multiDriver) {//close all drivers after
            webDriver.close();
        }
    }
}
