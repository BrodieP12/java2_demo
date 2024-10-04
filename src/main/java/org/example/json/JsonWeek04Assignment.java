package org.example.json;

import com.google.gson.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.*;
import java.util.stream.Collectors;

// TODO: Connect database to add scores
public class JsonWeek04Assignment {
    public static void main(String[] args) throws IOException {
        GameLogic game = new GameLogic();
        game.Setup();

    }

    public static DrawCardsAPI getNewCard(String deckID, int totalToDraw) throws IOException {
        String url = "https://www.deckofcardsapi.com/api/deck/" + deckID + "/draw/?count=" + totalToDraw;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        JsonWeek04Assignment.DrawCardsAPI drawCardsAPI = null;
        try {
            drawCardsAPI = gson.fromJson(responseBody, JsonWeek04Assignment.DrawCardsAPI.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            System.out.println(e.getMessage());
            System.exit(0); // TODO: Figure out a different way to process this error
        }
        return drawCardsAPI;
    }
    // TODO: Refactor these two functions and combine them to comply with DRY (Don't repeat yourself) https://en.wikipedia.org/wiki/Don%27t_repeat_yourself
    public static NewDeckAPI getNewDeck(int totalDecks, boolean shuffled) throws IOException {
        String url = "";
        if(shuffled == true){
            url = "https://www.deckofcardsapi.com/api/deck/new/shuffle/?deck_count=" + totalDecks;
        } else {
            url = "https://www.deckofcardsapi.com/api/deck/new/?deck_count=" + totalDecks;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        JsonWeek04Assignment.NewDeckAPI newDeckAPI = null;
        try {
            newDeckAPI = gson.fromJson(responseBody, JsonWeek04Assignment.NewDeckAPI.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            System.out.println(e.getMessage());
            System.exit(0); // TODO: Figure out a different way to process this error
        }
        return newDeckAPI;
    }

    public static class Player {
        private int bank = 0;
        public List<Card> hand = new ArrayList<Card>();
        public int handTotal = 0;
        private int bet = 0;
        public String username;
        private Boolean busted = false;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        private void setBank(int bank) {
            this.bank = bank;
        }
        public void addHand(Card card){
            hand.add(card);
        }

        private boolean trySetBet(int bet) {
            if(bet > this.bet) return false;
            this.bet = bet;
            return true;
        }
        public String cardToString(Card card) {
            String cardString = "";
            if(!card.value.equals("10")){
                cardString += card.value.charAt(0); // To get K for King, Q for Queen, J for Jack, A for Ace
            } else {
                cardString += card.value;
            }
            cardString += card.getUnicodeDisplay(card.getSuit());
            return cardString;
        }

        public Boolean getBusted() {
            return busted;
        }

        public void setBusted(Boolean busted) {
            this.busted = busted;
        }

        public String handToString() {
            String cardString = "";
            for(Card card : hand) {
                cardString += cardToString(card);
                cardString += ", ";
            }
            cardString = cardString.substring(0, cardString.length() - 2); // To remove extra space and comma
            return cardString;
        }

        public int getHandTotal() {
            return this.handTotal;
        }
        public int getBet() {
            return this.bet;
        }
        public void setBet(int amt){
            this.bet = amt;
        }

        public int getBank() {
            return this.bank;
        }

        public int calculateHand() {
            int tempHandTotal = 0;
            int aceHandTotal = 0;

            for(Card card : hand) {
                if(card.getValue().equalsIgnoreCase("ACE")){
                    aceHandTotal += card.getNumValue(true);
                    tempHandTotal += card.getNumValue(false);
                } else {
                    aceHandTotal += card.getNumValue();
                    tempHandTotal += card.getNumValue();
                }
            }
            if(aceHandTotal > 21){
                handTotal = tempHandTotal;
                return tempHandTotal;
            }
            handTotal = aceHandTotal;
            return aceHandTotal;
        }
        public void clearHand(){
            this.hand.clear();
        }
    }

    public static class Dealer extends Player{
        public static NewDeckAPI deck;
        public String deckID;
        public int remainingCards;
        public static DrawCardsAPI drawnCard;

        public void NewDeck() {
            try{
                deck = getNewDeck(1, true);
                setDeckID();
                setRemainingCards();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            setDeckID();
            setRemainingCards();
        }
        public void setRemainingCards() {
            remainingCards = deck.getRemaining();
        }
        public void setDeckID() {
            deckID = deck.getDeckId();
        }
        public Card dealCard(){
            Card card;
            try{
                if(remainingCards == 0){ // case for deck running out of cards
                    NewDeck();
                }
                drawnCard = getNewCard(deckID, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            setRemainingCards();
            return drawnCard.cards[0];
        }
        private String displayHand() {
            String cardString = "";
            cardString += cardToString(hand.get(0));
            for(int i = 0; i < hand.size()-1; i++){ // Minus 1 from hand size because of line above
                cardString += hand.get(0).getHoleDisplay();
                cardString += ", ";
            }
            cardString = cardString.substring(0, cardString.length() - 2); // To remove extra space and coma
            return cardString;
        }
    }

    public static class UserInterface{
        Scanner scanner = new Scanner(System.in);
        public void displayOptions(String optionHeader,String[] options) {
            System.out.println(optionHeader);
            for(int i = 1; i < options.length+1; i++){
                System.out.println(i + ") " + options[i-1]);
            }
        }
        public String getUserInput(){
            return scanner.nextLine();
        }
        public boolean isValidInt(String input) {
            try {
                Integer.parseInt(input);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        public void printSeperator(int length) {
            for(int i = 0; i < length; i++){
                System.out.print("=");
            }
            System.out.println();
        }
        public void clearConsole(int printLines) {
            for(int i = 0; i < printLines; i++){
                System.out.println();
            }
        }
        public int getValidIntegerInput(){
            String input = "";
            Boolean validInput = false;
            while(!validInput){
                input = getUserInput();
                if(isValidInt(input)){
                    validInput = true;
                } else {
                    System.out.println("Please enter a valid number");
                }
            }
            return Integer.parseInt(input);
        }
        public int getValidIntegerInputInRange(int min, int max){
            String input = "";
            Boolean validInput = false;
            while(!validInput){
                input = getUserInput();
                if(isValidInt(input)){
                    if(Integer.parseInt(input) <= max && Integer.parseInt(input) >= min){
                        validInput = true;
                    } else {
                        System.out.println(String.format("Please enter in a number within %s and %s", min, max));
                    }
                } else {
                    System.out.println("Please enter in a valid number");
                }
            }
            return Integer.parseInt(input);
        }
    }

    // Steps of blackjack.
    // https://www.blackjackapprenticeship.com/how-to-play-blackjack/
    // Some steps skipped for brevity
    // Step 1. Get a deck
    // Step 2. Player places wager
    // Step 2. Dealer Deals cards to each player
    // Step 3. Player makes a decision:
    //      Stand: Move on to next player
    //      Hit: Dealt 1 card at a time until player "busts" or player stands
    // Step 4. Payouts
    //      Option 1: The dealer will bust
    //      Option 2: The dealer will make a hand (17 through 21)
    public static class GameLogic{
        public static String workingDeckID;
        public static List<Player> players = new ArrayList<>();
        public static int totalPlayers = 2;
        public static Dealer dealer = new Dealer();
        public UserInterface userInterface = new UserInterface();

        public void Setup(){
            System.out.println("Welcome to Blackjack!");
            System.out.println("How many people are playing?");
            totalPlayers = userInterface.getValidIntegerInput();

            dealer = new Dealer();
            for(int i = 0; i < totalPlayers; i++){
                players.add(new Player()); // https://www.digitalocean.com/community/tutorials/java-list-add-addall-methods
            }
            int playerNum = 1;
            for(Player player : players){
                String username;
                System.out.println("Player " + playerNum + " please enter a username: ");
                playerNum += 1;
                username = userInterface.getUserInput();
                player.setUsername(username);
                System.out.println("How much money would you like to start out with?");
                player.bank = userInterface.getValidIntegerInput();
                System.out.println("Added " + player.bank + " credits to your bank!");
            }
            userInterface.printSeperator(100);
            System.out.println("Getting a new shuffled deck");
            dealer.NewDeck();
            System.out.println("Remaining Cards: " + dealer.remainingCards);
            Play();
        }
        public void Play() {
            userInterface.printSeperator(100);
            for (Player player : players) {
                System.out.println(player.getUsername() + " your bank total is: $" + player.getBank() + ". Please place your wager: ");
                player.setBet(userInterface.getValidIntegerInputInRange(0, player.bank));
            }
            dealer.addHand(dealer.dealCard());
            dealer.addHand(dealer.dealCard());

            userInterface.printSeperator(100);

            System.out.print("Dealing cards");
            for (Player player : players) {
                player.addHand(dealer.dealCard());
                System.out.print(".");
                player.addHand(dealer.dealCard());
                System.out.print(".");
            }

            System.out.println();
            for (Player player : players) {
                userInterface.printSeperator(100);
                Boolean stand = false;
                System.out.println(player.getUsername() + "'s turn:");
                while (!stand) {
                    String[] options = {"Stand", "Hit"};
                    System.out.println("Cards: " + player.handToString());
                    System.out.println("Hand Total: " + player.calculateHand());
                    System.out.println("Dealer's hand: " + dealer.displayHand());
                    if (player.calculateHand() > 21) {
                        System.out.println(player.getUsername() + " you busted!");
                        player.setBusted(true);
                        break;
                    }
                    userInterface.displayOptions("What would you like to do? ", options);
                    int option = userInterface.getValidIntegerInputInRange(1, options.length);
                    switch (option) {
                        case 1:
                            stand = true;
                            break;
                        case 2:
                            player.addHand(dealer.dealCard());
                            stand = false;
                    }
                }
            }
            userInterface.printSeperator(100);
            System.out.println("Dealer's turn:");
            Boolean dealerStand = false;
            while (!dealerStand) {
                System.out.println("Dealer's hand: " + dealer.handToString());
                System.out.println("Dealer's total: " + dealer.calculateHand());
                if(dealer.calculateHand() > 21){
                    dealer.setBusted(true);
                    System.out.println("Dealer busted");
                    break;
                }
                if (dealer.calculateHand() < 17){
                    System.out.println("Dealer hits");
                    dealer.addHand(dealer.dealCard());
                } else {
                    System.out.println("Dealer stands");
                    dealerStand = true;
                }
                userInterface.printSeperator(100);
            }

            // Determine dealer's hand total and bust status
            int dealerHandTotal = dealer.calculateHand();
            boolean dealerHasBusted = dealer.getBusted();

            // Separate players into non-busted and busted players
            List<Player> nonBustedPlayers = players.stream()
                    .filter(player -> !player.busted)   // Players who have not busted
                    .collect(Collectors.toList());

            List<Player> bustedPlayers = players.stream()
                    .filter(player -> player.busted)    // Players who have busted
                    .collect(Collectors.toList());

            // Initialize lists for winners and losers
            List<Player> winningPlayers = new ArrayList<>();
            List<Player> tiedPlayers = new ArrayList<>();
            List<Player> losingPlayers = new ArrayList<>();

            int maxHandTotal = 0;

            // Process each non-busted player to determine if they win or lose
            for (Player player : nonBustedPlayers) {
                int playerHandTotal = player.getHandTotal();
                if(maxHandTotal < playerHandTotal){
                    maxHandTotal = playerHandTotal;
                }
                if (dealerHasBusted) {
                    // Dealer busted, player wins
                    winningPlayers.add(player);
                } else if (playerHandTotal > dealerHandTotal) {
                    // Player's hand is greater than dealer's, player wins
                    winningPlayers.add(player);
                } else if (playerHandTotal == dealerHandTotal) {
                    // Tie with dealer, player doesnt win or lose
                    tiedPlayers.add(player);
                } else {
                    // Player loses (hand total less than dealer's)
                    losingPlayers.add(player);
                }
            }

            // Sort winning players based on their hand totals in descending order
            winningPlayers.sort(Comparator.comparingInt(Player::getHandTotal).reversed());

            // Process winnings for winning players
            for (Player player : winningPlayers) {
                int playerHandTotal = player.getHandTotal();
                if(playerHandTotal == maxHandTotal){
                    // Adjust payout based on hand value
                    // Base payout is 2x bet, with a bonus for higher hand values
                    int basePayout = player.getBet() * 2;
                    int bonus = 0;

                    // Bonus: For each point above 18, add 10% of the bet
                    if (playerHandTotal > 18) {
                        bonus = (int) ((playerHandTotal - 18) * player.getBet() * 0.1);
                    }

                    int totalPayout = basePayout + bonus;
                    player.setBank(player.getBank() + totalPayout);  // Update player's bank

                    System.out.println(player.getUsername() + " wins with a hand total of " + playerHandTotal
                            + "! Your payout is: $" + totalPayout + ". Your new bank total is: $" + player.getBank());
                } else {
                    losingPlayers.add(player);
                }
            }

            for (Player player : tiedPlayers){
                System.out.println(player.getUsername() + " has tied! Your wager has been returned to you. Your bank total is: $" + player.getBank());
            }

            // Process losing players
            for (Player player : losingPlayers) {
                player.setBank(player.getBank()-player.getBet());
                System.out.println(player.getUsername() + " loses with a hand total of " + player.getHandTotal()
                        + ". Your new bank total is: $" + player.getBank());
            }

            // Output results for busted players
            for (Player player : bustedPlayers) {
                player.setBank(player.getBank()-player.getBet());
                System.out.println(player.getUsername() + " busted with a hand total of " + player.getHandTotal()
                        + ". Your new bank total is: $" + player.getBank());
            }
            List<Player> playersStaying = new ArrayList<>();
            List<Player> playersLeaving = new ArrayList<>();
            String[] yesNo = {"Yes", "No"};
            for(Player player : players){
                userInterface.printSeperator(100);
                if(player.getBank() <= 0) {
                    userInterface.displayOptions(player.getUsername() + " you ran out of money! Would you like to reload your bank?", yesNo);
                    int option = userInterface.getValidIntegerInputInRange(1, yesNo.length);
                    if(option == 1){
                        System.out.println("How much money would you like to reload your bank with?");
                        player.setBank(userInterface.getValidIntegerInput());
                        System.out.println("Added $" + player.getBank() + " to your bank!");
                        playersStaying.add(player);
                    } else {
                        playersLeaving.add(player);
                    }
                } else {
                    playersStaying.add(player);
                }
            }
            if(playersStaying.size() == 0){
                System.out.println("Thanks for playing!");
            }

            for(Player player : playersStaying){
                userInterface.displayOptions(player.getUsername() + " would you like to play again?", yesNo);
                int option = userInterface.getValidIntegerInputInRange(1, yesNo.length);
                if(option == 1){
                    ResetPlayer(player);
                } else {
                    playersLeaving.add(player);
                    System.out.println(player.getUsername() + "Thanks for playing!");
                }
            }
            if(playersStaying.size() > 0){
                players = playersStaying;
                for(Player player : players){
                    ResetPlayer(player);
                }
                ResetDealer();
                Play();
            } else {
                System.out.println("Not enough players to start a new game!");
                userInterface.displayOptions("Would you like to add new players?", yesNo);
                int option = userInterface.getValidIntegerInputInRange(1, yesNo.length);
                if(option == 1){
                    Setup();
                } else {
                    System.out.println("Goodbye");
                }
            }
        }
        public void ResetPlayer(Player player){
            player.clearHand();
            player.setBusted(false);
        }
        public void ResetDealer(){
            dealer.clearHand();
        }
    }

    private static class NewDeckAPI {
        public String deck_id; // Unique identifier for the deck
        public int remaining; // Remaining cards to draw
        public boolean shuffled; // Shuffled deck

        public String getDeckId() {
            return deck_id;
        }

        public int getRemaining() {
            return remaining;
        }

        public boolean getShuffled() {
            return shuffled;
        }

        @Override
        public String toString() {
            return "Deck[" +
                    "deck_id='" + deck_id + '\'' +
                    ", remaining=" + remaining +
                    ", shuffled=" + shuffled +
                    ']';
        }
    }
    private static class DrawCardsAPI {
        public String deck_id; // Unique identifier for deck drew from
        public Card[] cards; // List of cards drawn from the deck
        public int remaining; // Available cards remaining to draw from the deck
    }

    private static class Card {
        public String code; // Number then Suit
        public String image; // Image of the card
        public Images images; // Class for storing different links of image formats for the card
        public String value; // Value of the card Ace, 1, 2, 3 King, Queen, etc.
        public String suit; // Suit of the card SPADES, CLUBS, HEARTS, DIAMONDS

        public String getCode() {
            return code;
        }

        public String getImage() {
            return image;
        }

        public Images getImages() {
            return images;
        }

        public String getValue() {
            return value;
        }

        public String getSuit() {
            return suit;
        }

        public String getHoleDisplay(){
            return "▯"; // U+25AF
        }

        public int getNumValue() {
            switch(value.toUpperCase()) { // Uppercase to avoid formatting problems with API
                case "KING", "QUEEN", "JACK":
                    return 10;
                default:
                    return Integer.parseInt(value);
            }
        }

        public int getNumValue(boolean aceHigh) {
            switch(value.toUpperCase()) { // Uppercase to avoid formatting problems with API
                case "ACE":
                    if(aceHigh) {
                        return 11;
                    } else {
                        return 1;
                    }
                case "KING", "QUEEN", "JACK":
                    return 10;
                default:
                    return Integer.parseInt(value);
            }
        }

        public String getUnicodeDisplay(String suit) {
            switch (suit) {
                case "SPADES":
                    return "♠"; // U+2660
                case "HEARTS":
                    return "♡"; // U+2661
                case "CLUBS":
                    return "♣"; // U+2663
                case "DIAMONDS":
                    return "♢"; // U+2662
                default:
                    throw new IllegalArgumentException("Not a valid suit");
            }
        }

        @Override
        public String toString() {
            return value + getUnicodeDisplay(suit);
        }
    }
    private static class Images { // TODO: Implement this into JSP
        public String svg; // URL for card image in SVG file format
        public String png; // URL for card image in PNG file format
    }
}
