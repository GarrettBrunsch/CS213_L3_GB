// Garrett Brunsch
// Lab #3
// Due 7/13/25 with cut-off 7/20

import java.util.Scanner;
import java.util.Random;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main
{
    enum MenuOptions
    {
        INVALID, PLAY, QUIT
    }

    public static void main(String[] args) throws IOException
    {
        Scanner scanner = new Scanner(System.in);
        Game game = new Game();

        int choice = 0;
        MenuOptions menuChoice = MenuOptions.INVALID;

        while (menuChoice != MenuOptions.QUIT)
        {
            displayMenu();
            choice = getUserChoice(scanner);

            choice = (choice >= MenuOptions.PLAY.ordinal() && choice <= MenuOptions.QUIT.ordinal()) ? choice : 0;
            menuChoice = MenuOptions.values()[choice];

            switch (menuChoice)
            {
                case PLAY:
                    game.playGame(scanner);
                    break;
                case QUIT:
                    System.out.println("Now exiting program...");
                    break;
                default:
                    System.out.println("Invalid choice. Please select a valid menu option");
                    break;
            }
        }
        scanner.close();
    }

    public static void displayMenu()
    {
        System.out.print("\n\n=== MAIN MENU ===\n" +
                "1. Play\n" +
                "2. Quit\n" +
                "Choice: ");
    }

    public static int getUserChoice(Scanner scanner)
    {
        String input = scanner.nextLine().trim();
        int choice = 0;

        if (input != null && input.matches("[0-9]+"))
        {
            choice = Integer.parseInt(input);
        }

        return choice;
    }
}

class Constants
{
    // General Defaults
    public static final Random rand = new Random();

    public static final String HERO_NAMES_FILE = "src/in_hero_names.txt";
    public static final String VILLAIN_NAMES_FILE = "src/in_villain_names.txt";
    public static final String OUTPUT_FILE = "src/out_battle_results.txt";

    // Creature Defaults
    public static final int MIN_STRENGTH = 40;
    public static final int MAX_STRENGTH = 160;

    public static final int MIN_HEALTH = 40;
    public static final int MAX_HEALTH = 160;

    public static final String DEFAULT_NAME = "Unknown";
    public static final String DEFAULT_TYPE = "Unknown";

    public static final String[] CREATURE_TYPES = {"Bahamut", "Macara", "Nuggle", "Ceffyl"};

    public static final int BAHAMUT_BONUS_DAMAGE = 20;
    public static final int BAHAMUT_CHANCE = 10;

    public static final int NUGGLE_CHANCE = 15;

    // Army Defaults
    public static final int MIN_ARMY_SIZE = 1;
    public static final int MAX_ARMY_SIZE = 10;
    public static final int DEFAULT_ARMY_SIZE = MIN_ARMY_SIZE;
    public static final String ARMY_1_NAME = "Red Team";
    public static final String ARMY_2_NAME = "Blue Team";
}

class Game
{
    private StringBuilder battleLog;

    public Game()
    {
        battleLog = new StringBuilder();
    }

    private void printAndAppend(String message)
    {
        System.out.println(message);
        battleLog.append(message + "\n");
    }

    public void playGame(Scanner mainScanner) throws IOException
    {
        battleLog.setLength(0);
        int armySize = getArmySize(mainScanner);

        Army army1 = new Army(Constants.ARMY_1_NAME, armySize);
        Army army2 = new Army(Constants.ARMY_2_NAME, armySize);

        army1.loadCreatureNames(Constants.HERO_NAMES_FILE);
        army2.loadCreatureNames(Constants.VILLAIN_NAMES_FILE);

        printAndAppend("\n=== NEW BATTLE ===\n" + "Army Stats Before Battle:\n" +
                army1.toString() + "\n" + army2.toString());

        conductBattle(army1, army2);

        printAndAppend("\nArmy Stats After Battle:\n" + army1.toString() + "\n" + army2.toString());

        announceWinner(army1, army2);

        writeAllToFile();

        army1.resetArmy();
        army2.resetArmy();

        System.out.println("\nArmies have been reset for another battle");
    }

    private int getArmySize(Scanner mainScanner)
    {
        int size = 0;
        boolean validInput = false;

        while (!validInput)
        {
            System.out.print("Enter army size (1-10): ");
            String input = mainScanner.nextLine().trim();

            if (input != null && input.matches("[0-9]+"))
            {
                size = Integer.parseInt(input);
                if (size >= Constants.MIN_ARMY_SIZE && size <= Constants.MAX_ARMY_SIZE)
                {
                    validInput = true;
                    System.out.println(); // Avoids clutter after user-prompt but before table info
                }
                else
                {
                    System.out.println("Army size must be between " + Constants.MIN_ARMY_SIZE + " and " + Constants.MAX_ARMY_SIZE);
                }
            }
            else
            {
                System.out.println("Please enter a valid number");
            }
        }

        return size;
    }

    private void conductBattle(Army army1, Army army2)
    {
        System.out.println("\n--- BATTLE BEGINS ---");

        battleLog.append("\n--- BATTLE LOG ---\n");

        int armySize = army1.getArmySize();

        for (int position = 0; position < armySize; position++)
        {
            Creature creature1 = army1.getCreature(position);
            Creature creature2 = army2.getCreature(position);

            if (creature1 != null && creature2 != null && creature1.getHealth() > 0 && creature2.getHealth() > 0)
            {
                printAndAppend("\nBattle " + (position + 1) + ": " + creature1.getName() +
                        " vs " + creature2.getName());

                String battleHeader = String.format("%-20s | %-6s | %-10s | %-20s | %-15s | %-10s",
                        "Attacker", "Damage", "Army", "Defender", "Defender Health", "Army");
                String separator = "\n----------------------------------------------------------------------------------------";

                printAndAppend(battleHeader + separator);

                battleCreatures(creature1, creature2, army1.getArmyName(), army2.getArmyName());
            }
        }
    }

    private void battleCreatures(Creature creature1, Creature creature2, String army1Name, String army2Name)
    {
        int currentTurn = Constants.rand.nextInt(2);

        while (creature1.getHealth() > 0 && creature2.getHealth() > 0)
        {
            Creature attacker = (currentTurn == 0) ? creature1 : creature2;
            Creature defender = (currentTurn == 0) ? creature2 : creature1;
            String attackerArmy = (currentTurn == 0) ? army1Name : army2Name;
            String defenderArmy = (currentTurn == 0) ? army2Name : army1Name;

            int damage = attacker.getDamage();
            int newHealth = defender.getHealth() - damage;
            if (newHealth < 0)
            {
                newHealth = 0;
            }
            defender.setHealth(newHealth);

            String battleLine = String.format("%-20s | %6d | %-10s | %-20s | %15d | %-10s",
                    attacker.getNameAndType(), damage, attackerArmy,
                    defender.getNameAndType(), defender.getHealth(), defenderArmy);

            printAndAppend(battleLine);

            currentTurn = (currentTurn + 1) % 2;
        }

        Creature winner = (creature1.getHealth() > 0) ? creature1 : creature2;
        printAndAppend("Winner: " + winner.getNameAndType());
    }

    private void announceWinner(Army army1, Army army2)
    {
        int army1Health = army1.getTotalHealth();
        int army2Health = army2.getTotalHealth();

        String winnerMessage = (army1Health > army2Health) ? army1.getArmyName() + " wins the war!" :
                (army2Health > army1Health) ? army2.getArmyName() + " wins the war!" :
                        "The war ends in a tie!";

        String finalResults = "\n\n=== FINAL RESULTS ===\n" +
                army1.getArmyName() + " total health: " + army1Health + "\n" +
                army2.getArmyName() + " total health: " + army2Health + "\n" +
                winnerMessage;

        printAndAppend(finalResults);
    }

    private void writeAllToFile() throws IOException
    {
        FileWriter writer = new FileWriter(Constants.OUTPUT_FILE, true);
        writer.write(battleLog.toString());
        writer.close();
    }
}

class Creature
{
    private String name = Constants.DEFAULT_NAME;
    private String type = Constants.DEFAULT_TYPE;
    private int strength = Constants.MIN_STRENGTH;
    private int health = Constants.MIN_HEALTH;

    public Creature()
    {
        setCreature(Constants.DEFAULT_NAME, Constants.DEFAULT_TYPE, Constants.MIN_STRENGTH, Constants.MIN_HEALTH);
    }

    public Creature(String name, String type, int strength, int health)
    {
        setCreature(name, type, strength, health);
    }

    public void setCreature(String creatureName, String creatureType, int creatureStrength, int creatureHealth)
    {
        name = creatureName;
        type = creatureType;
        strength = (creatureStrength < Constants.MIN_STRENGTH) ? Constants.MIN_STRENGTH : creatureStrength;
        health = (creatureHealth < 0) ? 0 : creatureHealth;
    }

    public void reset()
    {
        setCreature(Constants.DEFAULT_NAME, Constants.DEFAULT_TYPE, Constants.MIN_STRENGTH, Constants.MIN_HEALTH);
    }

    public static int loadCreatureNames(String[] names, String filepath) throws IOException
    {
        final int MAX_NAMES = names.length;
        int count = 0;

        File inputFile = new File(filepath);

        if (inputFile.exists() && inputFile.canRead())
        {
            Scanner fileScanner = new Scanner(inputFile);

            while (fileScanner.hasNextLine() && count < MAX_NAMES)
            {
                String line = fileScanner.nextLine().trim();
                if (!line.isEmpty())
                {
                    names[count] = line;
                    count++;
                }
            }
            fileScanner.close();
        }
        else
        {
            System.out.println("Error: File not found: " + filepath);
        }
        return count;
    }

    public void setHealth(int newHealth)
    {
        health = (newHealth < 0) ? 0 : newHealth;
    }

    public int getHealth()
    {
        return health;
    }

    public int getStrength()
    {
        return strength;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public String getNameAndType()
    {
        return name + " the " + type;
    }

    public int getDamage()
    {
        int damage = 0;

        if (strength > 0)
        {
            damage = Constants.rand.nextInt(strength) + 1;

            switch (type.toLowerCase())
            {
                case "bahamut":
                    if ((Constants.rand.nextInt(100)) < Constants.BAHAMUT_CHANCE)
                    {
                        damage = damage + Constants.BAHAMUT_BONUS_DAMAGE;
                    }
                    break;

                case "macara":
                    damage = damage * 2;
                    break;

                case "nuggle":
                    if ((Constants.rand.nextInt(Constants.NUGGLE_CHANCE)) == 0)
                    {
                        damage = damage * 2;
                    }
                    break;

                default:
                    break;
            }
        }
        return damage;
    }

    public String toString()
    {
        return String.format("%-15s | %-15s | %10d | %10d", name, type, strength, health);
    }
}

class Army
{
    private String armyName = Constants.DEFAULT_NAME;
    private int armySize = Constants.DEFAULT_ARMY_SIZE;
    private Creature[] creatures = new Creature[Constants.MAX_ARMY_SIZE];

    public Army()
    {
        setArmy(Constants.DEFAULT_NAME, Constants.DEFAULT_ARMY_SIZE);
        initializeCreatures();
    }

    public Army(int size)
    {
        setArmy(Constants.DEFAULT_NAME, size);
        initializeCreatures();
    }

    public Army(String name)
    {
        setArmy(name, Constants.DEFAULT_ARMY_SIZE);
        initializeCreatures();
    }

    public Army(String name, int size)
    {
        setArmy(name, size);
        initializeCreatures();
    }

    public void setArmy(String name, int size)
    {
        setArmyName(name);
        setArmySize(size);
    }

    public void setArmyName(String name)
    {
        armyName = name;
    }

    public void setArmySize(int size)
    {
        armySize = (size >= Constants.MIN_ARMY_SIZE && size <= Constants.MAX_ARMY_SIZE) ? size : Constants.DEFAULT_ARMY_SIZE;
    }

    public int getArmySize()
    {
        return armySize;
    }

    public String getArmyName()
    {
        return armyName;
    }

    public Creature getCreature(int index)
    {
        Creature result = null;

        if (index >= 0 && index < armySize)
        {
            result = creatures[index];
        }

        return result;
    }

    private void initializeCreatures()
    {
        for (int i = 0; i < armySize; i++)
        {
            String creatureType = Constants.CREATURE_TYPES[Constants.rand.nextInt(Constants.CREATURE_TYPES.length)];
            int strength = Constants.rand.nextInt(Constants.MAX_STRENGTH - Constants.MIN_STRENGTH + 1) + Constants.MIN_STRENGTH;
            int health = Constants.rand.nextInt(Constants.MAX_HEALTH - Constants.MIN_HEALTH + 1) + Constants.MIN_HEALTH;

            creatures[i] = new Creature(Constants.DEFAULT_NAME, creatureType, strength, health);
        }
    }

    public void loadCreatureNames(String filepath) throws IOException
    {
        String[] names = new String[Constants.MAX_ARMY_SIZE];
        int nameCount = Creature.loadCreatureNames(names, filepath);

        assignNames(names, nameCount);
    }

    private void assignNames(String[] names, int nameCount)
    {
        for (int i = 0; i < armySize; i++)
        {
            String selectedName = Constants.DEFAULT_NAME;

            if (nameCount > 0)
            {
                selectedName = names[Constants.rand.nextInt(nameCount)];
            }
            creatures[i].setCreature(selectedName, creatures[i].getType(),
                    creatures[i].getStrength(), creatures[i].getHealth());
        }
    }

    public int getTotalHealth()
    {
        int totalHealth = 0;

        for (int i = 0; i < armySize; i++)
        {
            totalHealth += creatures[i].getHealth();
        }

        return totalHealth;
    }

    public void resetArmy()
    {
        for (int i = 0; i < armySize; i++)
        {
            creatures[i].reset();
        }
    }

    public String toString()
    {
        String headerFormat = "%-15s | %-15s | %10s | %10s";
        String result = armyName + " Stats\n";
        result += String.format(headerFormat, "Name", "Type", "Strength", "Health") + "\n";
        result += "----------------------------------------------------------------\n";

        for (int i = 0; i < armySize; i++)
        {
            result += creatures[i].toString() + "\n";
        }

        result += "Total Health: " + getTotalHealth() + "\n";

        return result;
    }
}

/*
=== MAIN MENU ===
1. Play
2. Quit
Choice: g
Invalid choice. Please select a valid menu option


=== MAIN MENU ===
1. Play
2. Quit
Choice: 3
Invalid choice. Please select a valid menu option


=== MAIN MENU ===
1. Play
2. Quit
Choice: 1
Enter army size (1-10): 4


=== NEW BATTLE ===
Army Stats Before Battle:
Red Team Stats
Name            | Type            |   Strength |     Health
----------------------------------------------------------------
Dumbledore      | Nuggle          |         56 |        130
Frodo           | Bahamut         |        123 |        123
Dumbledore      | Ceffyl          |         82 |         68
Dumbledore      | Nuggle          |        131 |         63
Total Health: 384

Blue Team Stats
Name            | Type            |   Strength |     Health
----------------------------------------------------------------
Voldemort       | Macara          |        134 |         80
Lucius          | Ceffyl          |         77 |        153
Smaug           | Macara          |        102 |        110
Hades           | Bahamut         |        140 |        111
Total Health: 454


--- BATTLE BEGINS ---

Battle 1: Dumbledore vs Voldemort
Attacker             | Damage | Army       | Defender             | Defender Health | Army
----------------------------------------------------------------------------------------
Dumbledore the Nuggle |     74 | Red Team   | Voldemort the Macara |               6 | Blue Team
Voldemort the Macara |    180 | Blue Team  | Dumbledore the Nuggle |               0 | Red Team
Winner: Voldemort the Macara

Battle 2: Frodo vs Lucius
Attacker             | Damage | Army       | Defender             | Defender Health | Army
----------------------------------------------------------------------------------------
Frodo the Bahamut    |     79 | Red Team   | Lucius the Ceffyl    |              74 | Blue Team
Lucius the Ceffyl    |     60 | Blue Team  | Frodo the Bahamut    |              63 | Red Team
Frodo the Bahamut    |     65 | Red Team   | Lucius the Ceffyl    |               9 | Blue Team
Lucius the Ceffyl    |     38 | Blue Team  | Frodo the Bahamut    |              25 | Red Team
Frodo the Bahamut    |     40 | Red Team   | Lucius the Ceffyl    |               0 | Blue Team
Winner: Frodo the Bahamut

Battle 3: Dumbledore vs Smaug
Attacker             | Damage | Army       | Defender             | Defender Health | Army
----------------------------------------------------------------------------------------
Smaug the Macara     |     28 | Blue Team  | Dumbledore the Ceffyl |              40 | Red Team
Dumbledore the Ceffyl |     45 | Red Team   | Smaug the Macara     |              65 | Blue Team
Smaug the Macara     |     96 | Blue Team  | Dumbledore the Ceffyl |               0 | Red Team
Winner: Smaug the Macara

Battle 4: Dumbledore vs Hades
Attacker             | Damage | Army       | Defender             | Defender Health | Army
----------------------------------------------------------------------------------------
Hades the Bahamut    |     33 | Blue Team  | Dumbledore the Nuggle |              30 | Red Team
Dumbledore the Nuggle |     91 | Red Team   | Hades the Bahamut    |              20 | Blue Team
Hades the Bahamut    |     69 | Blue Team  | Dumbledore the Nuggle |               0 | Red Team
Winner: Hades the Bahamut

Army Stats After Battle:
Red Team Stats
Name            | Type            |   Strength |     Health
----------------------------------------------------------------
Dumbledore      | Nuggle          |         56 |          0
Frodo           | Bahamut         |        123 |         25
Dumbledore      | Ceffyl          |         82 |          0
Dumbledore      | Nuggle          |        131 |          0
Total Health: 25

Blue Team Stats
Name            | Type            |   Strength |     Health
----------------------------------------------------------------
Voldemort       | Macara          |        134 |          6
Lucius          | Ceffyl          |         77 |          0
Smaug           | Macara          |        102 |         65
Hades           | Bahamut         |        140 |         20
Total Health: 91



=== FINAL RESULTS ===
Red Team total health: 25
Blue Team total health: 91
Blue Team wins the war!

Armies have been reset for another battle


=== MAIN MENU ===
1. Play
2. Quit
Choice: 1
Enter army size (1-10): 2


=== NEW BATTLE ===
Army Stats Before Battle:
Red Team Stats
Name            | Type            |   Strength |     Health
----------------------------------------------------------------
Snape           | Nuggle          |         56 |         80
Aragorn         | Macara          |         52 |        149
Total Health: 229

Blue Team Stats
Name            | Type            |   Strength |     Health
----------------------------------------------------------------
Gollum          | Bahamut         |         63 |        131
Gollum          | Macara          |         59 |         78
Total Health: 209


--- BATTLE BEGINS ---

Battle 1: Snape vs Gollum
Attacker             | Damage | Army       | Defender             | Defender Health | Army
----------------------------------------------------------------------------------------
Snape the Nuggle     |     28 | Red Team   | Gollum the Bahamut   |             103 | Blue Team
Gollum the Bahamut   |     38 | Blue Team  | Snape the Nuggle     |              42 | Red Team
Snape the Nuggle     |     40 | Red Team   | Gollum the Bahamut   |              63 | Blue Team
Gollum the Bahamut   |     34 | Blue Team  | Snape the Nuggle     |               8 | Red Team
Snape the Nuggle     |      1 | Red Team   | Gollum the Bahamut   |              62 | Blue Team
Gollum the Bahamut   |     37 | Blue Team  | Snape the Nuggle     |               0 | Red Team
Winner: Gollum the Bahamut

Battle 2: Aragorn vs Gollum
Attacker             | Damage | Army       | Defender             | Defender Health | Army
----------------------------------------------------------------------------------------
Aragorn the Macara   |     42 | Red Team   | Gollum the Macara    |              36 | Blue Team
Gollum the Macara    |     34 | Blue Team  | Aragorn the Macara   |             115 | Red Team
Aragorn the Macara   |     32 | Red Team   | Gollum the Macara    |               4 | Blue Team
Gollum the Macara    |    102 | Blue Team  | Aragorn the Macara   |              13 | Red Team
Aragorn the Macara   |     78 | Red Team   | Gollum the Macara    |               0 | Blue Team
Winner: Aragorn the Macara

Army Stats After Battle:
Red Team Stats
Name            | Type            |   Strength |     Health
----------------------------------------------------------------
Snape           | Nuggle          |         56 |          0
Aragorn         | Macara          |         52 |         13
Total Health: 13

Blue Team Stats
Name            | Type            |   Strength |     Health
----------------------------------------------------------------
Gollum          | Bahamut         |         63 |         62
Gollum          | Macara          |         59 |          0
Total Health: 62



=== FINAL RESULTS ===
Red Team total health: 13
Blue Team total health: 62
Blue Team wins the war!

Armies have been reset for another battle


=== MAIN MENU ===
1. Play
2. Quit
Choice: 2
Now exiting program...

Process finished with exit code 0

 */
