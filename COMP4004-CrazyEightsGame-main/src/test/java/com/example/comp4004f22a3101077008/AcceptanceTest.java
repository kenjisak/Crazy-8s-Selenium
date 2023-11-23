package com.example.comp4004f22a3101077008;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
        SpringApplication.run(Application.class);

        System.setProperty("webdriver.chrome.driver",
                ".\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        allDrivers = new WebDriver[4];
        for (int i = 0; i < allDrivers.length; i++) {
            allDrivers[i] = new ChromeDriver(options);
            allDrivers[i].get("http://localhost:" + port);

            WebDriverWait wait = new WebDriverWait(allDrivers[i], Duration.ofSeconds(20));
            wait.until(ExpectedConditions.elementToBeClickable (By.id("usernameBtn"))).click();//waits till register button pops up then click
        }
    }
    @AfterEach
    @DirtiesContext
    public void closeGameDrivers(){
        for (WebDriver webDriver : allDrivers) {//close all drivers after
            webDriver.quit();
        }
    }
    @Test
    @DirtiesContext
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
    ////////////////////////////////RIGGING FUNCTIONS////////////////////////////////
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

        //add back the rigged cards
        ArrayList<Card> p1Cards = createCards(p1Card);
        for (Card card :
                p1Cards) {
            gd.getCards().add(0,card);
        }//set players hands, last player to first

        gd.getCards().add(0, createCards(topCard).get(0));//add top card back always at the end

        gd.setTopCard(game.startSetTopCard(gd.getCards()));//set the top card

        for (Player p : gd.getPlayers()) {//deal all players cards
            game.startDealCards(gd.getCards(), gd.getPlayers(), p.getID() - 1);
        }

    }
    public String removeCard(String gameDeck, String allCards){//
        String[] allGivenCards = allCards.split("\\s+");
        String updatedGD = "";

        for (String card :
                allGivenCards) {
            updatedGD = gameDeck.replace(card, "");
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
