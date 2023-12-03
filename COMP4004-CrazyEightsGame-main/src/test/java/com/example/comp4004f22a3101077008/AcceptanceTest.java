package com.example.comp4004f22a3101077008;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
    ////////////////////////////////TEST RIG FUNCTIONS////////////////////////////////
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
    ////////////////////////////////RIGGING HELPER FUNCTIONS////////////////////////////////
    public String removeCard(String gameDeck, String allCards){//
        String[] allGivenCards = allCards.split("\\s+");
        String updatedGD = "";

        for (String card :
                allGivenCards) {
            updatedGD = gameDeck.replace(card, "");
        }

        if(updatedGD.charAt(0) == ' '){//removes extra spacing if the card was the first or last card
            updatedGD = updatedGD.substring(1);
        } else if (updatedGD.charAt(updatedGD.length() - 1) == ' ') {
            updatedGD = updatedGD.substring(0,updatedGD.length() - 2);
        }
        return updatedGD;
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
}
