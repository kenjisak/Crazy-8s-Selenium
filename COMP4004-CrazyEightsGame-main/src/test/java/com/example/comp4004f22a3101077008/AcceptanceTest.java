package com.example.comp4004f22a3101077008;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

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
    WebDriver[] allDrivers;

    @BeforeEach
    @DirtiesContext
    public void setupGameDrivers(){
//        SpringApplication.run(Application.class);

        System.setProperty("webdriver.chrome.driver",
                ".\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        allDrivers = new WebDriver[4];
        for (int i = 0; i < allDrivers.length; i++) {
            allDrivers[i] = new ChromeDriver(options);
            allDrivers[i].get("http://localhost:" + port);
            //arranges and moves windows to left screen
            int screenWidth = 1920;
            int screenHeight = 1080;
            int x,y;
            if (i == 0){
                x = -screenWidth;//0
                y = 0;
            } else if (i == 1) {
                x = screenWidth/2 - screenWidth;//screenWidth/2
                y = 0;
            } else if (i == 2) {
                x = -screenWidth;//0
                y = screenHeight/2;
            }else{
                x = screenWidth/2 - screenWidth;//screenWidth/2
                y = screenHeight/2;
            }
            allDrivers[i].manage().window().setPosition(new org.openqa.selenium.Point(x, y));
            allDrivers[i].manage().window().setSize(new org.openqa.selenium.Dimension(screenWidth/2, screenHeight/2));

            WebDriverWait wait = new WebDriverWait(allDrivers[i], Duration.ofSeconds(20));
            wait.until(ExpectedConditions.elementToBeClickable (By.id("usernameBtn"))).click();//waits till register button pops up then click
        }
    }
    @AfterEach
    @DirtiesContext
    public void closeGameDrivers() throws InterruptedException {
        for (WebDriver webDriver : allDrivers) {//close all drivers after
            webDriver.quit();
        }
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 25: Next Player")
    //p1 plays 3C: assert next player is player 2
    public void testRow25() throws InterruptedException {
        rigTestRow25();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable (By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (WebDriver playerBrowser : allDrivers) {//check all players windows they display the correct starting top card
            String topCard = playerBrowser.findElement(By.className("topCard")).getAttribute("id");
            assertEquals("5C",topCard);
        }

        allDrivers[0].findElement(By.id("3C")).click();//P1 plays 3C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("3C",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 2",playerTurn);//check all players windows they display player 2 as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 1){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 2 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }

    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 27: Direction Reversal with Ace")
    //p1 plays 1H assert next player is player 4 AND interface must show now playing in opposite direction (i.e., going right)
    //player 4 plays 7H and next player is player 3
    public void testRow27() throws InterruptedException {
        rigTestRow27();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable (By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (WebDriver playerBrowser : allDrivers) {//check all players windows they display the correct starting top card
            String topCard = playerBrowser.findElement(By.className("topCard")).getAttribute("id");
            assertEquals("5H",topCard);

        }

        String originalDirection = allDrivers[0].findElement(By.id("direction")).getText();//get original direction from p1
        allDrivers[0].findElement(By.id("AH")).click();//P1 plays 3C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("AH",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 4",playerTurn);//check all players windows they display player 4 as current turn

            String updatedDirection = allDrivers[i].findElement(By.id("direction")).getText();
            assertTrue(updatedDirection.equals("right") && !updatedDirection.equals(originalDirection));//check all players windows they display direction change

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 3){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 4 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }

        allDrivers[3].findElement(By.id("7H")).click();//P4 plays 7H
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("7H",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 3",playerTurn);//check all players windows they display player 3 as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 2){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 3 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }
    }

    @Test
    @DirtiesContext
    @DisplayName("Test Row 28: Player turn Skipped with Queen")
    //p1 plays QC assert next player is player 3 (because player 2 is notified and loses their turn)
    public void testRow28() throws InterruptedException{
        rigTestRow28();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable (By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (WebDriver playerBrowser : allDrivers) {//check all players windows they display the correct starting top card
            String topCard = playerBrowser.findElement(By.className("topCard")).getAttribute("id");
            assertEquals("5C",topCard);
        }

        allDrivers[0].findElement(By.id("QC")).click();//P1 plays QC
        TimeUnit.SECONDS.sleep(7);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("QC",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 3",playerTurn);//check all players windows they display player 3 as current turn since player 2 was skipped

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 2){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 3 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 29: Next Turn Loops back to Player 1")
    //p4 plays 3C: assert next player is player 1
    public void testRow29() throws InterruptedException {
        rigTestRow29();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable (By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (WebDriver playerBrowser : allDrivers) {//check all players windows they display the correct starting top card
            String topCard = playerBrowser.findElement(By.className("topCard")).getAttribute("id");
            assertEquals("4C",topCard);
        }

        allDrivers[0].findElement(By.id("5C")).click();//P1 plays 5C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("5C",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 2",playerTurn);//check all players windows they display player 2 as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 1){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 2 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }
        allDrivers[1].findElement(By.id("6C")).click();//P2 plays 6C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("6C",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 3",playerTurn);//check all players windows they display player 3 as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 2){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 3 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }
        allDrivers[2].findElement(By.id("7C")).click();//P3 plays 7C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("7C",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 4",playerTurn);//check all players windows they display player 4 as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 3){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 4 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }
        allDrivers[3].findElement(By.id("3C")).click();//P4 plays 3C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("3C",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 1",playerTurn);//check all players windows they display player 1 as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 0){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 1 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 31: Turn Reversal and continuing player turn checks")
    //p4 plays 1H: assert next player is player 3 AND interface must show now playing in opposite direction (i.e., right)
    //player 3 plays 7H and next player is player 2
    public void testRow31() throws InterruptedException {
        rigTestRow31();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable (By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (WebDriver playerBrowser : allDrivers) {//check all players windows they display the correct starting top card
            String topCard = playerBrowser.findElement(By.className("topCard")).getAttribute("id");
            assertEquals("4H",topCard);
        }

        allDrivers[0].findElement(By.id("5H")).click();//P1 plays 5H
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("5H",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 2",playerTurn);//check all players windows they display player 2 as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 1){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 2 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }
        allDrivers[1].findElement(By.id("6H")).click();//P2 plays 6H
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("6H",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 3",playerTurn);//check all players windows they display player 3 as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 2){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 3 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }
        allDrivers[2].findElement(By.id("9H")).click();//P3 plays 9H
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("9H",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 4",playerTurn);//check all players windows they display player 4 as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 3){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 4 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }
        String originalDirection = allDrivers[3].findElement(By.id("direction")).getText();//get original direction from p4
        allDrivers[3].findElement(By.id("AH")).click();//P4 plays AH
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("AH",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 3",playerTurn);//check all players windows they display player 3 as current turn

            String updatedDirection = allDrivers[i].findElement(By.id("direction")).getText();
            assertTrue(updatedDirection.equals("right") && !updatedDirection.equals(originalDirection));//check all players windows they display direction change

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 2){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 3 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }

        allDrivers[2].findElement(By.id("7H")).click();//P3 plays 7H
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("7H",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 2",playerTurn);//check all players windows they display player 2 as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 1){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 2 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 32: Player loop and skipping player turn with Queen")
    //p4 plays 1H: assert next player is player 3 AND interface must show now playing in opposite direction (i.e., right)
    //player 3 plays 7H and next player is player 2
    public void testRow32() throws InterruptedException {
        rigTestRow32();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable (By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (WebDriver playerBrowser : allDrivers) {//check all players windows they display the correct starting top card
            String topCard = playerBrowser.findElement(By.className("topCard")).getAttribute("id");
            assertEquals("4C",topCard);
        }

        allDrivers[0].findElement(By.id("5C")).click();//P1 plays 5C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("5C",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 2",playerTurn);//check all players windows they display player 2 as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 1){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 2 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }
        allDrivers[1].findElement(By.id("6C")).click();//P2 plays 6C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("6C",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 3",playerTurn);//check all players windows they display player 3 as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 2){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 3 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }
        allDrivers[2].findElement(By.id("7C")).click();//P3 plays 7C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("7C",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 4",playerTurn);//check all players windows they display player 4 as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 3){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 4 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }

        allDrivers[3].findElement(By.id("QC")).click();//P4 plays QC
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("QC",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 2",playerTurn);//check all players windows they display player 3 as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == 1){
                assertTrue(drawBtn.isEnabled());
            }//assert only player 2 draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }
    }
    ////////////////////////////////////////////////////////////////////
    @Test
    @DirtiesContext
    @DisplayName("Test Row 35: Playability of Cards with Kings")
    //top card is KC and player 1 plays KH
    public void testRow35() throws InterruptedException {
        rigTestRow35();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable (By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (WebDriver playerBrowser : allDrivers) {//check all players windows they display the correct starting top card
            String topCard = playerBrowser.findElement(By.className("topCard")).getAttribute("id");
            assertEquals("KC",topCard);
        }

        allDrivers[0].findElement(By.id("KH")).click();//P1 plays KH
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("KH",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 2",playerTurn);//check all players windows they display player 2 as current turn
        }

    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 36: Playability of Cards with King and a Numbered Card")
    //top card is KC and player 1 plays 7C
    public void testRow36() throws InterruptedException {
        rigTestRow36();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable (By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (WebDriver playerBrowser : allDrivers) {//check all players windows they display the correct starting top card
            String topCard = playerBrowser.findElement(By.className("topCard")).getAttribute("id");
            assertEquals("KC",topCard);
        }

        allDrivers[0].findElement(By.id("7C")).click();//P1 plays 7C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("7C",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 2",playerTurn);//check all players windows they display player 2 as current turn
        }

    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 37: Playability of an 8 card")
    //top card is KC and player1 plays 8H and interface prompts for new suit
    public void testRow37() throws InterruptedException {
        rigTestRow37();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable (By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (WebDriver playerBrowser : allDrivers) {//check all players windows they display the correct starting top card
            String topCard = playerBrowser.findElement(By.className("topCard")).getAttribute("id");
            assertEquals("KC",topCard);
        }

        allDrivers[0].findElement(By.id("8H")).click();//P1 plays 8H
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("8H",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 1",playerTurn);//check all players windows they display player 1 as still the same turn since has to choose a suit

            WebElement suitBtns = allDrivers[i].findElement(By.id("8Played"));
            if (i == 0){
                assertTrue(suitBtns.findElement(By.id("spade")).isDisplayed());
                assertTrue(suitBtns.findElement(By.id("heart")).isDisplayed());
                assertTrue(suitBtns.findElement(By.id("club")).isDisplayed());
                assertTrue(suitBtns.findElement(By.id("diamond")).isDisplayed());
            }//assert only player 1 has the suits button displayed after playing an 8 card, its always enabled, even if its hidden
            else{
                assertFalse(suitBtns.findElement(By.id("spade")).isDisplayed());
                assertFalse(suitBtns.findElement(By.id("heart")).isDisplayed());
                assertFalse(suitBtns.findElement(By.id("club")).isDisplayed());
                assertFalse(suitBtns.findElement(By.id("diamond")).isDisplayed());
            }
        }

    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 38: Playability of trying to play a non playable card")
    //top card is KC and player1 tries to play 5S and interface prohibits this card being played (disabled or message)
    public void testRow38() throws InterruptedException {
        rigTestRow38();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable (By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (WebDriver playerBrowser : allDrivers) {//check all players windows they display the correct starting top card
            String topCard = playerBrowser.findElement(By.className("topCard")).getAttribute("id");
            assertEquals("KC",topCard);
        }

        allDrivers[0].findElement(By.id("5S")).click();//P1 tries to play 5S
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        for (int i = 0; i < allDrivers.length; i++) {
            WebDriverWait waitAlert = new WebDriverWait(allDrivers[i], Duration.ofSeconds(1));
            Alert invalidCard = null;

            if(i == 0) {//check if alert pops up only for player 1
                try{
                    invalidCard = waitAlert.until(ExpectedConditions.alertIsPresent());
                }catch (Exception e){
                    //timeout and no alert;
                }
                assertNotNull(invalidCard);
                assertEquals("Invalid Selection",invalidCard.getText());

                invalidCard.dismiss();
            } else {
                try{
                    invalidCard = waitAlert.until(ExpectedConditions.alertIsPresent());
                }catch (Exception e){
                    //timeout and no alert;
                }
                assertNull(invalidCard);
            }

            String topCard = allDrivers[i].findElement(By.className("topCard")).getAttribute("id");
            assertEquals("KC",topCard);//check all players windows they display the correct starting top card

            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: 1",playerTurn);//check all players windows they display player 1 as still the same turn since invalid card was chosen

        }

    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 42: Drawing Rules, draws 1 card and has to play it.")
    //top card is 7C and p1 has only {3H} as hand: must draw, draws 6C and must play it
    public void testRow42() throws InterruptedException {
        rigTestRow42();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("3D");//check all players windows they display the correct top card for the start of the scenario

        int numLoopPlayed = 4;
        for (int i = 0; i < numLoopPlayed; i++) {//Set up of playing their cards until we get to 7C as top card
            for (WebDriver currPlayer : allDrivers) {
                List<WebElement> plyrHand = currPlayer.findElement(By.id("hand")).findElements(By.className("card"));
                plyrHand.get(0).click();
                TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
            }
        }

        assertTopCard("7C");//check all players windows they display the correct top card for the start of the scenario

        assertDrawnCard(0,"6C",true);//assert playable drawn card is in hand, the only one enabled, and draw button is disabled

        allDrivers[0].findElement(By.id("6C")).click();//P1 had to play 6C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("6C");//check all players windows they display the correct top card after it was played
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 43: Drawing Rules, draws 2 cards and has to play it")
    //top card is 7C and p1 has {3H} as hand: must draw, draws 6D then 5C and must play it
    public void testRow43() throws InterruptedException {
        rigTestRow43();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("3D");//check all players windows they display the correct starting top card

        int numLoopPlayed = 4;
        for (int i = 0; i < numLoopPlayed; i++) {//Set up of playing their cards until we get to 7C as top card
            for (WebDriver currPlayer : allDrivers) {
                List<WebElement> plyrHand = currPlayer.findElement(By.id("hand")).findElements(By.className("card"));
                plyrHand.get(0).click();
                TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
            }
        }

        assertTopCard("7C");//check all players windows they display the correct top card for the start of the scenario

        assertDrawnCard(0,"6D",false);//assert non playable drawn card is in hand and draw button is still enabled

        assertDrawnCard(0,"5C",true);//assert playable drawn card is in hand, the only one enabled, and draw button is disabled

        allDrivers[0].findElement(By.id("5C")).click();//P1 had to play 6C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("5C");//check all players windows they display the correct top card after it was played
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 44: Drawing Rules, draws 3 cards and has to play it")
    //top card is 7C and p1 has {3H} as hand: must draw, draws 6D, 5S then 7H and must play it
    public void testRow44() throws InterruptedException {
        rigTestRow44();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("3D");//check all players windows they display the correct starting top card

        int numLoopPlayed = 4;
        for (int i = 0; i < numLoopPlayed; i++) {//Set up of playing their cards until we get to 7C as top card
            for (WebDriver currPlayer : allDrivers) {
                List<WebElement> plyrHand = currPlayer.findElement(By.id("hand")).findElements(By.className("card"));
                plyrHand.get(0).click();
                TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
            }
        }

        assertTopCard("7C");//check all players windows they display the correct top card for the start of the scenario

        assertDrawnCard(0,"6D",false);//assert non playable drawn card is in hand and draw button is still enabled

        assertDrawnCard(0,"5S",false);//assert non playable drawn card is in hand and draw button is still enabled

        assertDrawnCard(0,"7H",true);//assert playable drawn card is in hand, the only one enabled, and draw button is disabled

        allDrivers[0].findElement(By.id("7H")).click();//P1 had to play 7H
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("7H");//check all players windows they display the correct top card after it was played
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 45: Drawing Rules, draws 3 cards and ends turn")
    //top card is 7C and p1 has {3H} as hand: must draw, draws 6D, 5S, 4H; still can't play: turn ends (ie max 3 cards drawn)
    public void testRow45() throws InterruptedException {
        rigTestRow45();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("3D");//check all players windows they display the correct starting top card

        int numLoopPlayed = 4;
        for (int i = 0; i < numLoopPlayed; i++) {//Set up of playing their cards until we get to 7C as top card
            for (WebDriver currPlayer : allDrivers) {
                List<WebElement> plyrHand = currPlayer.findElement(By.id("hand")).findElements(By.className("card"));
                plyrHand.get(0).click();
                TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
            }
        }
        String savedTopCard = allDrivers[0].findElement(By.className("topCard")).getAttribute("id");
        assertTopCard("7C");//check all players windows they display the correct top card for the start of the scenario

        assertDrawnCard(0,"6D",false);//assert non playable drawn card is in hand and draw button is still enabled

        assertDrawnCard(0,"5S",false);//assert non playable drawn card is in hand and draw button is still enabled

        assertTurn("1");//used the manual code since we drawbtn will be disabled since end of turn, instead of enabled since its still a non playable card
        allDrivers[0].findElement(By.id("draw")).click();
        assertNotNull(allDrivers[0].findElement(By.id("hand")).findElement(By.id("4H")));//assert the card is in player's hand
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTurn("2");//assert current turn is the next player
        assertTopCard("7C");//check all players windows the top card is the same
        assertEquals(savedTopCard,allDrivers[0].findElement(By.className("topCard")).getAttribute("id"));//check the top card is still set to 7C
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 46: Drawing Rules, draws 2 cards and has to play an 8 card")
    //top card is 7C and p1 has {3H} as hand: must draw, draws 6D then 8H; must play 8H and declare new suit
    public void testRow46() throws InterruptedException {
        rigTestRow46();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("3D");//check all players windows they display the correct starting top card

        int numLoopPlayed = 4;
        for (int i = 0; i < numLoopPlayed; i++) {//Set up of playing their cards until we get to 7C as top card
            for (WebDriver currPlayer : allDrivers) {
                List<WebElement> plyrHand = currPlayer.findElement(By.id("hand")).findElements(By.className("card"));
                plyrHand.get(0).click();
                TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
            }
        }

        assertTopCard("7C");//check all players windows they display the correct top card for the start of the scenario

        assertDrawnCard(0,"6D",false);//assert non playable drawn card is in hand and draw button is still enabled

        assertDrawnCard(0,"8H",true);//assert playable drawn card is in hand, the only one enabled, and draw button is disabled

        allDrivers[0].findElement(By.id("8H")).click();//P1 had to play 8H
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("8H");//check all players windows they display the correct top card after it was played
        assert8PlayedBtns(0);//assert only Player 1 has the suit buttons when 8 was played
        allDrivers[0].findElement(By.id("diamond")).click();//declare a suit
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("D");//assert the correct suit was set as a top card
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 47: Drawing Rules, has playable cards, but chooses to draw anyway")
    //top card is 7C and p1 has {KS, 3C} as hand: chooses to draw, draws 6C and must play 6C
    public void testRow47() throws InterruptedException {
        rigTestRow47();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("3D");//check all players windows they display the correct top card for the start of the scenario

        int numLoopPlayed = 3;
        for (int i = 0; i < numLoopPlayed; i++) {//Set up of playing their cards until we get to 7C as top card
            for (WebDriver currPlayer : allDrivers) {
                List<WebElement> plyrHand = currPlayer.findElement(By.id("hand")).findElements(By.className("card"));
                plyrHand.get(0).click();
                TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
            }
        }

        assertTopCard("7C");//check all players windows they display the correct top card for the start of the scenario

        //chooses to draw anyway
        assertDrawnCard(0,"6C",true);//assert playable drawn card is in hand, the only one enabled, and draw button is disabled

        allDrivers[0].findElement(By.id("6C")).click();//P1 had to play 6C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("6C");//check all players windows they display the correct top card after it was played
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 51: Playing Twos, immediately draws 2 cards and is able to play the first one")
    //p1 plays 2C, p2 has only {4H} thus must draw 2 cards {6C and 9D} then plays 6C
    public void testRow51() throws InterruptedException {
        rigTestRow51();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("3D");//check all players windows they display the correct top card for the start of the scenario

        int numLoopPlayed = 3;
        for (int i = 0; i < numLoopPlayed; i++) {//Set up of playing their cards until we get to 7C as top card
            for (WebDriver currPlayer : allDrivers) {
                List<WebElement> plyrHand = currPlayer.findElement(By.id("hand")).findElements(By.className("card"));
                String plyrCardPlayed = plyrHand.get(0).getAttribute("id");
                plyrHand.get(0).click();
                assertTopCard(plyrCardPlayed);//asserts each card played is the new top card
                TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
            }
        }

        allDrivers[1].findElement(By.id("5S")).click();//P2 Plays 5S since P1 was skipped
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("5S");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[2].findElement(By.id("9S")).click();//P3 Plays 9S
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("9S");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[3].findElement(By.id("9C")).click();//P4 Plays 9C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("9C");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[0].findElement(By.id("2C")).click();//P1 Plays 2C so itll be the topCard after P4 played
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("2C");//check all players windows they display the correct top card for the start of the scenario

        //2C was played so assert P2 received the correct cards
        assertOnlyDrawn(1,"6C");//P2 draw card and assert drawn card is in hand
        assertOnlyDrawn(1,"9D");//P2 draw card and assert drawn card is in hand
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        allDrivers[1].findElement(By.id("6C")).click();//P2 plays 6C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("6C");//check all players windows they display the correct top card after it was played
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 52: Playing Twos, immediately draws 2 cards and has to draw 2 more")
    //p1 plays 2C, p2 has only {4H}, draws {6S and 9D}, still can't play, then draws 9H then 6C and then must play 6C
    public void testRow52() throws InterruptedException {
        rigTestRow52();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("3D");//check all players windows they display the correct top card for the start of the scenario

        int numLoopPlayed = 3;
        for (int i = 0; i < numLoopPlayed; i++) {//Set up of playing their cards until we get to 7C as top card
            for (WebDriver currPlayer : allDrivers) {
                List<WebElement> plyrHand = currPlayer.findElement(By.id("hand")).findElements(By.className("card"));
                String plyrCardPlayed = plyrHand.get(0).getAttribute("id");
                plyrHand.get(0).click();
                assertTopCard(plyrCardPlayed);//asserts each card played is the new top card
                TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
            }
        }

        allDrivers[1].findElement(By.id("5S")).click();//P2 Plays 5S since P1 was skipped
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("5S");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[2].findElement(By.id("9S")).click();//P3 Plays 9S
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("9S");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[3].findElement(By.id("9C")).click();//P4 Plays 9C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("9C");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[0].findElement(By.id("2C")).click();//P1 Plays 2C so itll be the topCard after P4 played
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("2C");//check all players windows they display the correct top card for the start of the scenario

        //2C was played so assert P2 received the correct cards
        assertOnlyDrawn(1,"6S");//P2 assert drawn card is in hand
        assertOnlyDrawn(1,"9D");//P2 assert drawn card is in hand


        List<WebElement> plyrHand = allDrivers[1].findElement(By.id("hand")).findElements(By.className("card"));
        for (WebElement plyrCard : plyrHand) {
            plyrCard.click();

            WebDriverWait waitAlert = new WebDriverWait(allDrivers[1], Duration.ofSeconds(1));
            Alert invalidCard = null;
            try{
                invalidCard = waitAlert.until(ExpectedConditions.alertIsPresent());
            }catch (Exception e){
                //timeout and no alert;
            }
            assertNotNull(invalidCard);
            assertEquals("Invalid Selection",invalidCard.getText());
            invalidCard.dismiss();
            TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        }

        assertDrawnCard(1,"9H",false);//P2 draw card and assert drawn card is in hand
        assertDrawnCard(1,"6C",true);//P2 draw card and assert is the only enabled card
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        allDrivers[1].findElement(By.id("6C")).click();//P2 plays 6C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("6C");//check all players windows they display the correct top card after it was played
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 53: Playing Twos, immediately draws 2 cards and has to draw 3 more and ends turn")
    //p1 plays 2C, p2 has only {4H} draws {6S and 9D} then draws 9H, 7S, 5H and then  turns end (without playing a card)
    public void testRow53() throws InterruptedException {
        rigTestRow53();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("3D");//check all players windows they display the correct top card for the start of the scenario

        int numLoopPlayed = 3;
        for (int i = 0; i < numLoopPlayed; i++) {//Set up of playing their cards until we get to 7C as top card
            for (WebDriver currPlayer : allDrivers) {
                List<WebElement> plyrHand = currPlayer.findElement(By.id("hand")).findElements(By.className("card"));
                String plyrCardPlayed = plyrHand.get(0).getAttribute("id");
                plyrHand.get(0).click();
                assertTopCard(plyrCardPlayed);//asserts each card played is the new top card
                TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
            }
        }

        allDrivers[1].findElement(By.id("5S")).click();//P2 Plays 5S since P1 was skipped
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("5S");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[2].findElement(By.id("9S")).click();//P3 Plays 9S
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("9S");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[3].findElement(By.id("9C")).click();//P4 Plays 9C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("9C");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[0].findElement(By.id("2C")).click();//P1 Plays 2C so itll be the topCard after P4 played
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("2C");//check all players windows they display the correct top card for the start of the scenario
        String savedTopCard = allDrivers[0].findElement(By.className("topCard")).getAttribute("id");

        //2C was played so assert P2 received the correct cards
        assertOnlyDrawn(1,"6S");//P2 assert drawn card is in hand
        assertOnlyDrawn(1,"9D");//P2 assert drawn card is in hand


        assertDrawnCard(1,"9H",false);//P2 draw card and assert drawn card is in hand
        assertDrawnCard(1,"7S",false);//P2 draw card and assert drawn card is in hand

        assertTurn("2");//used the manual code since we drawbtn will be disabled since end of turn, instead of enabled since its still a non playable card
        allDrivers[1].findElement(By.id("draw")).click();
        assertNotNull(allDrivers[1].findElement(By.id("hand")).findElement(By.id("5H")));//assert the card is in player 2's hand
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTurn("3");//assert current turn is the next player since P2 couldn't play a card
        assertTopCard("2C");//check all players windows the top card is the same
        assertEquals(savedTopCard,allDrivers[1].findElement(By.className("topCard")).getAttribute("id"));//check the top card is still set to 2C
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 55: Playing Twos, immediately draws 2 cards and plays a 2, so next player draws 4")
    //p1 plays 2C, p2 has {4H} draws {2H and 9D} then plays 2H (forcing next player to immediately play or draw 4 cards)
    //then p3 having only {7D} p3 draws {5S, 6D, 6H and 7C} and then  plays 6H
    public void testRow55() throws InterruptedException {
        rigTestRow55();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("3D");//check all players windows they display the correct top card for the start of the scenario

        int numLoopPlayed = 3;
        for (int i = 0; i < numLoopPlayed; i++) {//Set up of playing their cards until we get to 7C as top card
            for (WebDriver currPlayer : allDrivers) {
                List<WebElement> plyrHand = currPlayer.findElement(By.id("hand")).findElements(By.className("card"));
                String plyrCardPlayed = plyrHand.get(0).getAttribute("id");
                plyrHand.get(0).click();
                assertTopCard(plyrCardPlayed);//asserts each card played is the new top card
                TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
            }
        }

        allDrivers[1].findElement(By.id("7S")).click();//P2 Plays 7S since P1 was skipped
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("7S");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[2].findElement(By.id("9S")).click();//P3 Plays 9S
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("9S");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[3].findElement(By.id("9C")).click();//P4 Plays 9C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("9C");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[0].findElement(By.id("2C")).click();//P1 Plays 2C so itll be the topCard after P4 played
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("2C");//check all players windows they display the correct top card for the start of the scenario

        //2C was played so assert P2 received the correct cards
        assertOnlyDrawn(1,"2H");//P2 draw card and assert drawn card is in hand
        assertOnlyDrawn(1,"9D");//P2 draw card and assert drawn card is in hand
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        allDrivers[1].findElement(By.id("2H")).click();//P2 plays 2H
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("2H");//check all players windows they display the correct top card after it was played

        //2H was played so assert P3 received the correct cards
        assertOnlyDrawn(2,"5S");//P3 draw card and assert drawn card is in hand
        assertOnlyDrawn(2,"6D");//P3 draw card and assert drawn card is in hand
        assertOnlyDrawn(2,"6H");//P3 draw card and assert drawn card is in hand
        assertOnlyDrawn(2,"7C");//P3 draw card and assert drawn card is in hand
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        allDrivers[2].findElement(By.id("6H")).click();//P3 plays 6H
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("6H");//check all players windows they display the correct top card after it was played
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 56: Playing Twos, but is able to play 2 cards immediately")
    //p1 plays 2C, p2 has {4C, 6C, 9D} then p2 plays 4C and 6C (ie 2 legal cards) and ends their turn
    public void testRow56() throws InterruptedException {
        rigTestRow56();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("3D");//check all players windows they display the correct top card for the start of the scenario

        int numLoopPlayed = 1;
        for (int i = 0; i < numLoopPlayed; i++) {//Set up of playing their cards until we get to 2C as top card
            for (WebDriver currPlayer : allDrivers) {
                List<WebElement> plyrHand = currPlayer.findElement(By.id("hand")).findElements(By.className("card"));
                String plyrCardPlayed = plyrHand.get(0).getAttribute("id");
                plyrHand.get(0).click();
                assertTopCard(plyrCardPlayed);//asserts each card played is the new top card
                TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
            }
        }

        allDrivers[0].findElement(By.id("7C")).click();//P1 Plays 7C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("7C");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[1].findElement(By.id("5C")).click();//P2 Plays 5C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("5C");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[2].findElement(By.id("QC")).click();//P3 Plays QC
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("QC");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[0].findElement(By.id("2C")).click();//P1 Plays 2C so itll be the topCard since P4 is skipped
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("2C");//check all players windows they display the correct top card for the start of the scenario

        /////////////////////////////////P2 Plays 2 cards consecutively///////////////////////////
        allDrivers[1].findElement(By.id("4C")).click();//P2 Plays 4C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("4C");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[1].findElement(By.id("6C")).click();//P2 Plays 6C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("6C");//check all players windows they display the correct top card for the start of the scenario

        assertTurn("3");//assert the next correct player is displayed
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 57: Playing Twos, but is able to play 2 cards immediately but ends the round")
    //p1 plays 2C, p2 has {4C, 4S} then p2 plays 4C and 4S and ends round (because p2 played all their cards)
    public void testRow57() throws InterruptedException {
        rigTestRow57();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("3D");//check all players windows they display the correct top card for the start of the scenario

        int numLoopPlayed = 2;
        for (int i = 0; i < numLoopPlayed; i++) {//Set up of playing their cards until we get to 2C as top card
            for (WebDriver currPlayer : allDrivers) {
                List<WebElement> plyrHand = currPlayer.findElement(By.id("hand")).findElements(By.className("card"));
                String plyrCardPlayed = plyrHand.get(0).getAttribute("id");
                plyrHand.get(0).click();
                assertTopCard(plyrCardPlayed);//asserts each card played is the new top card
                TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
            }
        }

        allDrivers[0].findElement(By.id("TC")).click();//P1 Plays TC
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("TC");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[1].findElement(By.id("9C")).click();//P2 Plays 9C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("9C");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[2].findElement(By.id("QC")).click();//P3 Plays QC
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("QC");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[0].findElement(By.id("2C")).click();//P1 Plays 2C so itll be the topCard since P4 is skipped
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("2C");//check all players windows they display the correct top card for the start of the scenario

        /////////////////////////////////P2 Plays 2 cards consecutively///////////////////////////
        allDrivers[1].findElement(By.id("4C")).click();//P2 Plays 4C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("4C");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[1].findElement(By.id("4S")).click();//P2 Plays 4S
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        //cant assert this as top card since round is over

        //assert Round ended
        assertTurn("2");//assert Player 2 is still the Correct Turn since they Won the Round
        for (int i = 0; i < allDrivers.length; i++) {
            WebElement startBtn = allDrivers[i].findElement(By.id("startBtn"));
            List<WebElement> scoreList = allDrivers[i].findElements(By.id("scoreList"));
            if (i == 1){
                assertTrue(startBtn.isDisplayed());//assert that the start game button is only on player 2 browser
                for(int scorei = 1; scorei < scoreList.size();scorei++){//start at index 1 since 0 is Score title
                    //assert that player 2 score is still 0 while everyone else is above 0, in every browser
                    String plyrScore = scoreList.get(i).getText();
                    int plyrScoreNum = Character.getNumericValue(plyrScore.charAt(plyrScore.length() - 1));

                    if(scorei == 2){//Player 2, check if the
                        assertEquals(0,plyrScoreNum);
                    } else {
                        assertTrue(0 < plyrScoreNum );
                    }
                }
            }else{
                assertFalse(startBtn.isDisplayed());
            }
        }

        //potential bug
        //even though 4S and 4C is in hand (in that order), will still be forced to draw 2 cards after a 2 is played.
        //so its not iterating through the whole hand to see if there is the first card thats playable,
        //then a second card thats playable based on the first card that can potentially be played
        // but is still able to play the 4C after, just not the 4S
    }
    @Test
    @DirtiesContext
    @DisplayName("Test Row 61: Scoring with a single round, ending with a specific set of cards and points")
    //first round ends with p1 with {1S}, p2 with no cards, p3 with {8H, JH, 6H, KH, KS}, p4 with {8C, 8D, 2D}
    //then game is over with p1 scores 1, p2 scores 0, p3 scores 86 and p4 scores 102
    public void testRow61() throws InterruptedException {
        rigTestRow61();//rigs deck for this test

        WebDriverWait wait = new WebDriverWait(allDrivers[0], Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("startBtn"))).click();//waits till start button pops up and starts the game with the rigged deck

        verifyDeckCount();

        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay

        assertTopCard("3H");//check all players windows they display the correct top card for the start of the scenario

        int numLoopPlayed = 2;
        for (int i = 0; i < numLoopPlayed; i++) {//Set up of playing their cards until we get to 2C as top card
            for (WebDriver currPlayer : allDrivers) {
                List<WebElement> plyrHand = currPlayer.findElement(By.id("hand")).findElements(By.className("card"));
                String plyrCardPlayed = plyrHand.get(0).getAttribute("id");
                plyrHand.get(0).click();
                assertTopCard(plyrCardPlayed);//asserts each card played is the new top card
                TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
            }
        }

        allDrivers[0].findElement(By.id("4C")).click();//P1 Plays 4C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("4C");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[1].findElement(By.id("JC")).click();//P2 Plays JC
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("JC");//check all players windows they display the correct top card for the start of the scenario

        //chooses to draw KH,KS,KC
        assertDrawnCard(2,"KH",false);
        assertDrawnCard(2,"KS",false);
        assertDrawnCard(2,"KC",true);
        allDrivers[2].findElement(By.id("KC")).click();//P3 Plays KC
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("KC");//check all players windows they display the correct top card for the start of the scenario

        assertDrawnCard(3,"9C",true);
        allDrivers[3].findElement(By.id("9C")).click();//P4 Plays 9C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("9C");//check all players windows they display the correct top card for the start of the scenario

        allDrivers[0].findElement(By.id("2C")).click();//P1 Plays 2C so itll be the topCard since P4 is skipped
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("2C");//check all players windows they display the correct top card for the start of the scenario

        /////////////////////////////////P2 Plays 2 cards consecutively and ends the game///////////////////////////
        allDrivers[1].findElement(By.id("5C")).click();//P2 Plays 5C
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        assertTopCard("5C");//check all players windows they display the correct top card for the start of the scenario

        //can only assert ending hands before the last card is played here. Since hands are not displayed anymore after that.
        assertHand(0,"AS");
        assertHand(2,"8H,JH,6H,KH,KS");
        assertHand(3,"8C,8D,2D");
        assertTurn("2");//assert it's still Player 2's turn to be able to play the last card to end the game

        allDrivers[1].findElement(By.id("TC")).click();//P2 Plays TC
        TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
        //cant assert this as top card since game is over

        //assert Round ended
        assertTurn("2");//assert Player 2 is still the Correct Turn since they Won the Round
        for (int i = 0; i < allDrivers.length; i++) {
            List<WebElement> scoreList = allDrivers[i].findElements(By.id("scoreList"));
            WebElement winMSG = allDrivers[i].findElement(By.id("winMSG"));//assert winner message for all players, winMSG = WINNER IS PLAYER 2
            assertEquals("WINNER IS PLAYER 2",winMSG.getText());
            for(int scorei = 1; scorei < scoreList.size();scorei++){//start at index 1 since 0 is Score title
                //assert all scores on browsers are 1,0,86,102 and winner msg is displayed correctly
                String plyrScore = scoreList.get(i).getText();
                int plyrScoreNum = Character.getNumericValue(plyrScore.charAt(plyrScore.length() - 1));

                if(scorei == 1){//Player 1, check if the
                    assertEquals(1,plyrScoreNum);
                } else if(scorei == 2){//Player 2, check if the
                    assertEquals(0,plyrScoreNum);
                } else if(scorei == 3){//Player 3, check if the
                    assertEquals(86,plyrScoreNum);
                } else if(scorei == 4){//Player 4, check if the
                    assertEquals(102,plyrScoreNum);
                }

            }
        }

    }
    ////////////////////////////////TEST RIG FUNCTIONS(NEXT TURN)////////////////////////////////
    public void rigTestRow25(){
        String topCard = "5C";
        String p1Card = "3C";

        String rig = "AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS AC 2C 3C 4C 5C 6C 7C 8C 9C TC JC QC KC AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD";//populated deck
        rig = removeCard(rig,topCard);//remove cards we want to rig
        rig = removeCard(rig,p1Card);
        assertTrue(!rig.contains(topCard) && !rig.contains(p1Card));

        ArrayList<Card> gameDeck = createCards(rig);
        gd.setCards(gameDeck);//setCards with populated Deck
        game.shuffleDeck(gd.getCards());//shuffle the deck, simulate more realism

        //add top card back
        gd.getCards().add(0, createCards(topCard).get(0));

        //add back player rigged cards in correct spots
        ArrayList<Card> p1Cards = createCards(p1Card);
        gd.getCards().add(1,p1Cards.get(0));

//        for (Card card: gd.getCards()) {
//            System.out.print(card.getRank() + card.getSuit() + ", ");
//        }
        gd.setTopCard(game.startSetTopCard(gd.getCards()));//set the top card

        for (Player p : gd.getPlayers()) {//deal all players cards
            game.startDealCards(gd.getCards(), gd.getPlayers(), p.getID() - 1);
        }

    }
    public void rigTestRow27(){
        String topCard = "5H";
        String p1Card = "AH";
        String p4Card = "7H";

        String rig = "AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS AC 2C 3C 4C 5C 6C 7C 8C 9C TC JC QC KC AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD";//populated deck
        rig = removeCard(rig,topCard);//remove cards we want to rig
        rig = removeCard(rig,p1Card);
        rig = removeCard(rig,p4Card);

//        System.out.println(rig);
//        assertTrue(!rig.contains(topCard) && !rig.contains(p1Card));

        ArrayList<Card> gameDeck = createCards(rig);
        gd.setCards(gameDeck);//setCards with populated Deck
        game.shuffleDeck(gd.getCards());//shuffle the deck, simulate more realism

        gd.getCards().add(0, createCards(topCard).get(0));//add top card back

        //add back player rigged cards in correct spots
        ArrayList<Card> p1Cards = createCards(p1Card);
        gd.getCards().add(1,p1Cards.get(0));

        ArrayList<Card> p4Cards = createCards(p4Card);
        gd.getCards().add(16,p4Cards.get(0));

        gd.setTopCard(game.startSetTopCard(gd.getCards()));//set the top card

        for (Player p : gd.getPlayers()) {//deal all players cards
            game.startDealCards(gd.getCards(), gd.getPlayers(), p.getID() - 1);
        }

    }
    public void rigTestRow28(){
        String topCard = "5C";
        String p1Card = "QC";

        String rig = "AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS AC 2C 3C 4C 5C 6C 7C 8C 9C TC JC QC KC AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD";//populated deck
        rig = removeCard(rig,topCard);//remove cards we want to rig
        rig = removeCard(rig,p1Card);
        assertTrue(!rig.contains(topCard) && !rig.contains(p1Card));

        ArrayList<Card> gameDeck = createCards(rig);
        gd.setCards(gameDeck);//setCards with populated Deck
        game.shuffleDeck(gd.getCards());//shuffle the deck, simulate more realism

        //add top card back
        gd.getCards().add(0, createCards(topCard).get(0));

        //add back player rigged cards in correct spots
        ArrayList<Card> p1Cards = createCards(p1Card);
        gd.getCards().add(1,p1Cards.get(0));

        gd.setTopCard(game.startSetTopCard(gd.getCards()));//set the top card

        for (Player p : gd.getPlayers()) {//deal all players cards
            game.startDealCards(gd.getCards(), gd.getPlayers(), p.getID() - 1);
        }

    }
    public void rigTestRow29(){
        String topCard = "4C";
        String p1Card = "5C";
        String p2Card = "6C";
        String p3Card = "7C";
        String p4Card = "3C";

        String rig = "AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS AC 2C 3C 4C 5C 6C 7C 8C 9C TC JC QC KC AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD";//populated deck
        rig = removeCard(rig,topCard);//remove cards we want to rig
        rig = removeCard(rig,p1Card);
        rig = removeCard(rig,p2Card);
        rig = removeCard(rig,p3Card);
        rig = removeCard(rig,p4Card);

        ArrayList<Card> gameDeck = createCards(rig);
        gd.setCards(gameDeck);//setCards with populated Deck
        game.shuffleDeck(gd.getCards());//shuffle the deck, simulate more realism

        gd.getCards().add(0, createCards(topCard).get(0));//add top card back

        //add back player rigged cards in correct spots
        ArrayList<Card> p1Cards = createCards(p1Card);
        gd.getCards().add(1,p1Cards.get(0));

        ArrayList<Card> p2Cards = createCards(p2Card);
        gd.getCards().add(6,p2Cards.get(0));

        ArrayList<Card> p3Cards = createCards(p3Card);
        gd.getCards().add(11,p3Cards.get(0));

        ArrayList<Card> p4Cards = createCards(p4Card);
        gd.getCards().add(16,p4Cards.get(0));

        gd.setTopCard(game.startSetTopCard(gd.getCards()));//set the top card

        for (Player p : gd.getPlayers()) {//deal all players cards
            game.startDealCards(gd.getCards(), gd.getPlayers(), p.getID() - 1);
        }

    }
    public void rigTestRow31(){
        String topCard = "4H";
        String p1Card = "5H";
        String p2Card = "6H";
        String p3Card = "9H 7H";
        String p4Card = "AH";

        String rig = "AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS AC 2C 3C 4C 5C 6C 7C 8C 9C TC JC QC KC AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD";//populated deck
        rig = removeCard(rig,topCard);//remove cards we want to rig
        rig = removeCard(rig,p1Card);
        rig = removeCard(rig,p2Card);
        rig = removeCard(rig,p3Card);
        rig = removeCard(rig,p4Card);

        ArrayList<Card> gameDeck = createCards(rig);
        gd.setCards(gameDeck);//setCards with populated Deck
        game.shuffleDeck(gd.getCards());//shuffle the deck, simulate more realism

        gd.getCards().add(0, createCards(topCard).get(0));//add top card back

        //add back player rigged cards in correct spots
        ArrayList<Card> p1Cards = createCards(p1Card);
        gd.getCards().add(1,p1Cards.get(0));

        ArrayList<Card> p2Cards = createCards(p2Card);
        gd.getCards().add(6,p2Cards.get(0));

        ArrayList<Card> p3Cards = createCards(p3Card);
        for (int i = 0; i < p3Cards.size(); i++) {//add all of player 3 cards
            gd.getCards().add(11 + i,p3Cards.get(i));
        }

        ArrayList<Card> p4Cards = createCards(p4Card);
        gd.getCards().add(16,p4Cards.get(0));

        gd.setTopCard(game.startSetTopCard(gd.getCards()));//set the top card

        for (Player p : gd.getPlayers()) {//deal all players cards
            game.startDealCards(gd.getCards(), gd.getPlayers(), p.getID() - 1);
        }

    }
    public void rigTestRow32(){
        String topCard = "4C";
        String p1Card = "5C";
        String p2Card = "6C";
        String p3Card = "7C";
        String p4Card = "QC";

        String rig = "AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS AC 2C 3C 4C 5C 6C 7C 8C 9C TC JC QC KC AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD";//populated deck
        rig = removeCard(rig,topCard);//remove cards we want to rig
        rig = removeCard(rig,p1Card);
        rig = removeCard(rig,p2Card);
        rig = removeCard(rig,p3Card);
        rig = removeCard(rig,p4Card);

        ArrayList<Card> gameDeck = createCards(rig);
        gd.setCards(gameDeck);//setCards with populated Deck
        game.shuffleDeck(gd.getCards());//shuffle the deck, simulate more realism

        gd.getCards().add(0, createCards(topCard).get(0));//add top card back

        //add back player rigged cards in correct spots
        ArrayList<Card> p1Cards = createCards(p1Card);
        gd.getCards().add(1,p1Cards.get(0));

        ArrayList<Card> p2Cards = createCards(p2Card);
        gd.getCards().add(6,p2Cards.get(0));

        ArrayList<Card> p3Cards = createCards(p3Card);
        for (int i = 0; i < p3Cards.size(); i++) {//add all of player 3 cards
            gd.getCards().add(11 + i,p3Cards.get(i));
        }

        ArrayList<Card> p4Cards = createCards(p4Card);
        gd.getCards().add(16,p4Cards.get(0));

        gd.setTopCard(game.startSetTopCard(gd.getCards()));//set the top card

        for (Player p : gd.getPlayers()) {//deal all players cards
            game.startDealCards(gd.getCards(), gd.getPlayers(), p.getID() - 1);
        }

    }
    ////////////////////////////////TEST RIG FUNCTIONS(PLAYABILITY)////////////////////////////////
    public void rigTestRow35(){
        String topCard = "KC";
        String p1Card = "KH";

        String rig = "AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS AC 2C 3C 4C 5C 6C 7C 8C 9C TC JC QC KC AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD";//populated deck
        rig = removeCard(rig,topCard);//remove cards we want to rig
        rig = removeCard(rig,p1Card);
        assertTrue(!rig.contains(topCard) && !rig.contains(p1Card));

        ArrayList<Card> gameDeck = createCards(rig);
        gd.setCards(gameDeck);//setCards with populated Deck
        game.shuffleDeck(gd.getCards());//shuffle the deck, simulate more realism

        //add top card back
        gd.getCards().add(0, createCards(topCard).get(0));

        //add back player rigged cards in correct spots
        ArrayList<Card> p1Cards = createCards(p1Card);
        gd.getCards().add(1,p1Cards.get(0));

        gd.setTopCard(game.startSetTopCard(gd.getCards()));//set the top card

        for (Player p : gd.getPlayers()) {//deal all players cards
            game.startDealCards(gd.getCards(), gd.getPlayers(), p.getID() - 1);
        }

    }
    public void rigTestRow36(){
        String topCard = "KC";
        String p1Card = "7C";

        String rig = "AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS AC 2C 3C 4C 5C 6C 7C 8C 9C TC JC QC KC AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD";//populated deck
        rig = removeCard(rig,topCard);//remove cards we want to rig
        rig = removeCard(rig,p1Card);
        assertTrue(!rig.contains(topCard) && !rig.contains(p1Card));

        ArrayList<Card> gameDeck = createCards(rig);
        gd.setCards(gameDeck);//setCards with populated Deck
        game.shuffleDeck(gd.getCards());//shuffle the deck, simulate more realism

        //add top card back
        gd.getCards().add(0, createCards(topCard).get(0));

        //add back player rigged cards in correct spots
        ArrayList<Card> p1Cards = createCards(p1Card);
        gd.getCards().add(1,p1Cards.get(0));

        gd.setTopCard(game.startSetTopCard(gd.getCards()));//set the top card

        for (Player p : gd.getPlayers()) {//deal all players cards
            game.startDealCards(gd.getCards(), gd.getPlayers(), p.getID() - 1);
        }

    }
    public void rigTestRow37(){
        String topCard = "KC";
        String p1Card = "8H";

        String rig = "AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS AC 2C 3C 4C 5C 6C 7C 8C 9C TC JC QC KC AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD";//populated deck
        rig = removeCard(rig,topCard);//remove cards we want to rig
        rig = removeCard(rig,p1Card);
        assertTrue(!rig.contains(topCard) && !rig.contains(p1Card));

        ArrayList<Card> gameDeck = createCards(rig);
        gd.setCards(gameDeck);//setCards with populated Deck
        game.shuffleDeck(gd.getCards());//shuffle the deck, simulate more realism

        //add top card back
        gd.getCards().add(0, createCards(topCard).get(0));

        //add back player rigged cards in correct spots
        ArrayList<Card> p1Cards = createCards(p1Card);
        gd.getCards().add(1,p1Cards.get(0));

        gd.setTopCard(game.startSetTopCard(gd.getCards()));//set the top card

        for (Player p : gd.getPlayers()) {//deal all players cards
            game.startDealCards(gd.getCards(), gd.getPlayers(), p.getID() - 1);
        }

    }
    public void rigTestRow38(){
        String topCard = "KC";
        String p1Card = "5S";

        String rig = "AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS AC 2C 3C 4C 5C 6C 7C 8C 9C TC JC QC KC AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD";//populated deck
        rig = removeCard(rig,topCard);//remove cards we want to rig
        rig = removeCard(rig,p1Card);
        assertTrue(!rig.contains(topCard) && !rig.contains(p1Card));

        ArrayList<Card> gameDeck = createCards(rig);
        gd.setCards(gameDeck);//setCards with populated Deck
        game.shuffleDeck(gd.getCards());//shuffle the deck, simulate more realism

        //add top card back
        gd.getCards().add(0, createCards(topCard).get(0));

        //add back player rigged cards in correct spots
        ArrayList<Card> p1Cards = createCards(p1Card);
        gd.getCards().add(1,p1Cards.get(0));

        gd.setTopCard(game.startSetTopCard(gd.getCards()));//set the top card

        for (Player p : gd.getPlayers()) {//deal all players cards
            game.startDealCards(gd.getCards(), gd.getPlayers(), p.getID() - 1);
        }

    }
    ///////////////////////////////////////////
    public void rigTestRow42(){
        String topCard = "3D";
        String p1Card = "5D 9H 5S 9C 3H";
        String p2Card = "6D 7H 6S 5C";
        String p3Card = "7D 6H 7S 3C";
        String p4Card = "9D 5H 9S 7C";
        String drawCard = "6C";

        rigAllPlayers(topCard,p1Card,p2Card,p3Card,p4Card,drawCard);
    }
    public void rigTestRow43(){
        String topCard = "3D";
        String p1Card = "4D 9H 5S 9C 3H";
        String p2Card = "5D 7H 6S 6C";
        String p3Card = "7D 6H 7S 3C";
        String p4Card = "9D 5H 9S 7C";
        String drawCard = "6D 5C";

        rigAllPlayers(topCard,p1Card,p2Card,p3Card,p4Card,drawCard);
    }
    public void rigTestRow44(){
        String topCard = "3D";
        String p1Card = "4D 9H 4S 9C 3H";
        String p2Card = "5D 6H 6S 6C";
        String p3Card = "7D 5H 7S 5C";
        String p4Card = "9D 4H 9S 7C";
        String drawCard = "6D 5S 7H";

        rigAllPlayers(topCard,p1Card,p2Card,p3Card,p4Card,drawCard);
    }
    public void rigTestRow45(){
        String topCard = "3D";
        String p1Card = "4D 9H 6S 9C 3H";
        String p2Card = "5D 7H 4S 6C";
        String p3Card = "7D 5H 7S 5C";
        String p4Card = "9D 6H 9S 7C";
        String drawCard = "6D 5S 4H";

        rigAllPlayers(topCard,p1Card,p2Card,p3Card,p4Card,drawCard);
    }
    public void rigTestRow46(){
        String topCard = "3D";
        String p1Card = "4D 9H 5S 9C 3H";
        String p2Card = "5D 7H 6S 6C";
        String p3Card = "7D 6H 7S 3C";
        String p4Card = "9D 5H 9S 7C";
        String drawCard = "6D 8H";

        rigAllPlayers(topCard,p1Card,p2Card,p3Card,p4Card,drawCard);
    }
    public void rigTestRow47(){
        String topCard = "3D";
        String p1Card = "9D 5S 9C KS 3C";
        String p2Card = "7D 6S 5C";
        String p3Card = "6D 7S 4C";
        String p4Card = "5D 9S 7C";
        String drawCard = "6C";

        rigAllPlayers(topCard,p1Card,p2Card,p3Card,p4Card,drawCard);
    }
    ////////////////////////////////////////////
    public void rigTestRow51(){
        String topCard = "3D";
        String p1Card = "4D 7H TS 2C";
        String p2Card = "5D 6H 6S 5S 4H";
        String p3Card = "6D 5H 7S 9S";
        String p4Card = "7D TH QS 9C";
        String drawCard = "6C 9D";

        rigAllPlayers(topCard,p1Card,p2Card,p3Card,p4Card,drawCard);
    }
    public void rigTestRow52(){
        String topCard = "3D";
        String p1Card = "4D 7H TS 2C";
        String p2Card = "5D 6H 3S 5S 4H";
        String p3Card = "6D 5H 7S 9S";
        String p4Card = "7D TH QS 9C";
        String drawCard = "6S 9D 9H 6C";

        rigAllPlayers(topCard,p1Card,p2Card,p3Card,p4Card,drawCard);
    }
    public void rigTestRow53(){
        String topCard = "3D";
        String p1Card = "4D 7H TS 2C";
        String p2Card = "5D 6H 3S 5S 4H";
        String p3Card = "6D 3H 4S 9S";
        String p4Card = "7D TH QS 9C";
        String drawCard = "6S 9D 9H 7S 5H";

        rigAllPlayers(topCard,p1Card,p2Card,p3Card,p4Card,drawCard);
    }
    public void rigTestRow55(){
        String topCard = "3D";//1
        String p1Card = "4D KH TS 2C";//4
        String p2Card = "5D 5H 3S 7S 4H";//5
        String p3Card = "TD 3H 4S 9S 7D";//5
        String p4Card = "KD TH QS 9C";//4
        String drawCard = "2H 9D 5S 6D 6H 7C";//6

        rigAllPlayers(topCard,p1Card,p2Card,p3Card,p4Card,drawCard);
    }
    public void rigTestRow56(){
        String topCard = "3D";
        String p1Card = "4D 7C 2C";
        String p2Card = "5D 5C 4C 6C 9D";
        String p3Card = "6D QC";
        String p4Card = "7D";
        String drawCard = "";

        rigAllPlayers(topCard,p1Card,p2Card,p3Card,p4Card,drawCard);
    }
    public void rigTestRow57(){
        String topCard = "3D";
        String p1Card = "4D 7H TC 2C";
        String p2Card = "5D 6H 9C 4C 4S";
//        String p2Card = "5D 6H 9C 4S 4C";//replace with this to reproduce bug
        String p3Card = "6D 5H QC";
        String p4Card = "7D TH";
        String drawCard = "";

        rigAllPlayers(topCard,p1Card,p2Card,p3Card,p4Card,drawCard);
    }
    ///////////////////////////////////
    public void rigTestRow61(){
        String topCard = "3H";
        String p1Card = "5H 9S 4C 2C AS";
        String p2Card = "4H 7S JC 5C TC";
        String p3Card = "7H 6S 8H JH 6H";//chooses to draw,KH KS KC, plays KC
        String p4Card = "9H 4S 8C 8D 2D";//chooses to draw and plays 9C
        String drawCard = "KH KS KC 9C";

        rigAllPlayers(topCard,p1Card,p2Card,p3Card,p4Card,drawCard);
    }
    ////////////////////////////////RIGGING HELPER FUNCTIONS////////////////////////////////
    public String removeCard(String gameDeck, String allCards){//
        String[] allGivenCards = allCards.split("\\s+");
        String updatedGD = gameDeck;

        for (String card :
                allGivenCards) {
            updatedGD = updatedGD.replace(card, "");
        }

//        if(updatedGD.charAt(0) == ' '){//removes extra spacing if the card was the first or last card
//            updatedGD = updatedGD.substring(1);
//        } else if (updatedGD.charAt(updatedGD.length() - 1) == ' ') {
//            updatedGD = updatedGD.substring(0,updatedGD.length() - 1);
//        }

        return updatedGD.trim();//remove leading or trailing spaces
    }
    public ArrayList<Card> createCards(String cards){

        ArrayList<Card> cardsList = new ArrayList<>();

        String[] allCards = cards.split("\\s+");
        for (String card :
                allCards) {
            cardsList.add(new Card(card.substring(1,2),card.substring(0,1)));//second letter is the Suit and first letter is the value
        }

        return cardsList;
    }

    public void verifyDeckCount(){//just to double check the deck dealt added up to 52
        int gameDeckCount = gd.getCards().size() + 1;//current size after cards dealt, plus top card.

        for(Player p :gd.getPlayers()){
            gameDeckCount += p.handSize();
            assertEquals(5,p.handSize());
        }
        assertEquals(52,gameDeckCount);

    }
    public void rigAllPlayers(String topCard, String p1Card, String p2Card, String p3Card, String p4Card, String drawCard){

        String rig = "AH 2H 3H 4H 5H 6H 7H 8H 9H TH JH QH KH AS 2S 3S 4S 5S 6S 7S 8S 9S TS JS QS KS AC 2C 3C 4C 5C 6C 7C 8C 9C TC JC QC KC AD 2D 3D 4D 5D 6D 7D 8D 9D TD JD QD KD";//populated deck
        //remove cards we want to rig
        rig = removeCard(rig,topCard);
//        assertEquals(52 - 1, createCards(rig).size());
        rig = removeCard(rig,p1Card);
//        assertEquals(52 - 1 - 5, createCards(rig).size());
        rig = removeCard(rig,p2Card);
//        assertEquals(52 - 1 - 5 - 5, createCards(rig).size());
        rig = removeCard(rig,p3Card);
//        assertEquals(52 - 1 - 5 - 5 - 5, createCards(rig).size());
        rig = removeCard(rig,p4Card);
//        assertEquals(52 - 1 - 5 - 5 - 5 - 5, createCards(rig).size());
        if(!drawCard.isEmpty()){
            rig = removeCard(rig,drawCard);
//            assertEquals(52 - 1 - 5 - 5 - 5 - 5 - 3, createCards(rig).size());
        }

        ArrayList<Card> gameDeck = createCards(rig);
//        int cardSize = 1 + 4 + 5 + 5 + 4 + 6;
//        assertEquals(52 - cardSize, gameDeck.size());

        gd.setCards(gameDeck);//setCards with populated Deck
        game.shuffleDeck(gd.getCards());//shuffle the deck, simulate more realism

        gd.getCards().add(0, createCards(topCard).get(0));//add top card back
//        assertEquals(52 - cardSize + 1, gd.getCards().size());

        //add back player rigged cards in correct spots
        ArrayList<Card> p1Cards = createCards(p1Card);
        for (int i = 0; i < p1Cards.size(); i++) {//add all of player 1 cards
            gd.getCards().add(1 + i,p1Cards.get(i));
        }
//        assertEquals(52 - cardSize + 1 + p1Cards.size(), gd.getCards().size());

        ArrayList<Card> p2Cards = createCards(p2Card);
        for (int i = 0; i < p2Cards.size(); i++) {//add all of player 2 cards
            gd.getCards().add(6 + i,p2Cards.get(i));
        }
//        assertEquals(52 - cardSize + 1 + p1Cards.size() + p2Cards.size(), gd.getCards().size());

        ArrayList<Card> p3Cards = createCards(p3Card);
        for (int i = 0; i < p3Cards.size(); i++) {//add all of player 3 cards
            gd.getCards().add(11 + i,p3Cards.get(i));
        }
//        assertEquals(52 - cardSize + 1 + p1Cards.size() + p2Cards.size() + p3Cards.size(), gd.getCards().size());

        ArrayList<Card> p4Cards = createCards(p4Card);
        for (int i = 0; i < p4Cards.size(); i++) {//add all of player 4 cards
            gd.getCards().add(16 + i,p4Cards.get(i));
        }
//        assertEquals(52 - cardSize + 1 + p1Cards.size() + p2Cards.size() + p3Cards.size() + p4Cards.size(), gd.getCards().size());

        if(!drawCard.isEmpty()){
            ArrayList<Card> drawCards = createCards(drawCard);
            for (int i = 0; i < drawCards.size(); i++) {//add all of drawCards
                gd.getCards().add( 21 + i,drawCards.get(i));
            }
//            assertEquals(52 - cardSize + 1 + p1Cards.size() + p2Cards.size() + p3Cards.size() + p4Cards.size() + drawCards.size(), gd.getCards().size());
        }


        gd.setTopCard(game.startSetTopCard(gd.getCards()));//set the top card

        for (Player p : gd.getPlayers()) {//deal all players cards
            game.startDealCards(gd.getCards(), gd.getPlayers(), p.getID() - 1);
        }
    }
    ////////////////////////////////ASSERT FUNCTIONS////////////////////////////////
    public void assertDrawnCard(int plyrIndex, String card, Boolean playable) throws InterruptedException {
        for (int i = 0; i < allDrivers.length; i++) {//check all players windows if draw button is only enabled for that player and the drawed card is in their hand, but is not playable
            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == plyrIndex) {
                assertTrue(drawBtn.isEnabled());
                drawBtn.click();
                assertNotNull(allDrivers[plyrIndex].findElement(By.id("hand")).findElement(By.id(card)));//assert the card is in player's hand
                TimeUnit.SECONDS.sleep(3);//slow down to see gameplay
            }//assert only that player's draw button is enabled and in hand
            else {
                assertFalse(drawBtn.isEnabled());
            }
        }

        if(playable){
            assertFalse(allDrivers[plyrIndex].findElement(By.id("draw")).isEnabled());//assert draw button is disabled since card is playable

            List<WebElement> plyrHand = allDrivers[plyrIndex].findElement(By.id("hand")).findElements(By.className("card"));
            for (WebElement plyrCard : plyrHand) {
                if (!Objects.equals(plyrCard.getAttribute("id"), card)) {
                    assertFalse(plyrCard.isEnabled());
                } else {//check if only the playable card they drawed is clickable
                    assertTrue(plyrCard.isEnabled());
                }
            }
        }else{
            assertTrue(allDrivers[plyrIndex].findElement(By.id("draw")).isEnabled());//assert draw button is enabled since still no playable cards in hand
        }

    }
    public void assertTopCard(String correctTopCard){
        for (WebDriver playerBrowser : allDrivers) {//check all players windows they display the correct starting top card
            String topCard = playerBrowser.findElement(By.className("topCard")).getAttribute("id");
            assertEquals(correctTopCard, topCard);
        }
    }
    public void assertTurn(String correctPlyrTurn){
        for (int i = 0; i < allDrivers.length; i++) {
            String playerTurn = allDrivers[i].findElement(By.id("turnID")).getText();
            assertEquals("Turn: " + correctPlyrTurn,playerTurn);//check all players windows they display that player as current turn

            WebElement drawBtn = allDrivers[i].findElement(By.id("draw"));
            if (i == Integer.parseInt(correctPlyrTurn) - 1){
                assertTrue(drawBtn.isEnabled());
            }//assert only that player's draw button is enabled
            else{
                assertFalse(drawBtn.isEnabled());
            }
        }
    }
    public void assert8PlayedBtns(int plyrIndex){
        for (int i = 0; i < allDrivers.length; i++) {
            WebElement suitBtns = allDrivers[i].findElement(By.id("8Played"));
            if (i == plyrIndex){
                assertTrue(suitBtns.findElement(By.id("spade")).isDisplayed());
                assertTrue(suitBtns.findElement(By.id("heart")).isDisplayed());
                assertTrue(suitBtns.findElement(By.id("club")).isDisplayed());
                assertTrue(suitBtns.findElement(By.id("diamond")).isDisplayed());
            }//assert only player 1 has the suits button displayed after playing an 8 card, its always enabled, even if its hidden
            else{
                assertFalse(suitBtns.findElement(By.id("spade")).isDisplayed());
                assertFalse(suitBtns.findElement(By.id("heart")).isDisplayed());
                assertFalse(suitBtns.findElement(By.id("club")).isDisplayed());
                assertFalse(suitBtns.findElement(By.id("diamond")).isDisplayed());
            }
        }
    }
    public void assertOnlyDrawn(int plyrIndex, String card) throws InterruptedException {
        assertNotNull(allDrivers[plyrIndex].findElement(By.id("hand")).findElement(By.id(card)));//assert the card is in player's hand
    }
    public void assertHand(int plyrIndex,String endHand){
        allDrivers[plyrIndex].findElement(By.id("hand"));
        List<WebElement> plyrHand = allDrivers[plyrIndex].findElement(By.id("hand")).findElements(By.className("card"));
        String[] plyrEndHand = endHand.split(",");
        for (int i = 0; i < plyrHand.size();i++) {
            assertEquals(plyrEndHand[i],plyrHand.get(i).getAttribute("id"));
        }
    }
}
