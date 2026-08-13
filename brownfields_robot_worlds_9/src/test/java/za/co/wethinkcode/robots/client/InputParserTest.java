package za.co.wethinkcode.robots.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

// Tests for InputParser.
//
// Each test group covers one command.
// We check three things for every command:
//   1. A correct input is accepted (isValid() == true)
//   2. A wrong input is rejected (isValid() == false)
//   3. The error message clearly tells the user what went wrong
class InputParserTest {

    // BLANK / NULL INPUT

    @Test
    void blankInputIsInvalid() {
        InputParser parser = new InputParser("   ");
        assertFalse(parser.isValid());
        assertNotNull(parser.getError());
    }

    @Test
    void emptyStringIsInvalid() {
        InputParser parser = new InputParser("");
        assertFalse(parser.isValid());
    }

    @Test
    void nullInputIsInvalid() {
        InputParser parser = new InputParser(null);
        assertFalse(parser.isValid());
    }

    // UNKNOWN COMMAND

    @Test
    void unknownCommandIsInvalid() {
        InputParser parser = new InputParser("dance");
        assertFalse(parser.isValid());
    }

    @Test
    void unknownCommandErrorMentionsTheWord() {
        InputParser parser = new InputParser("dance");
        assertTrue(parser.getError().contains("dance"));
    }

    @Test
    void unknownCommandErrorListsValidCommands() {
        InputParser parser = new InputParser("fly");
        assertTrue(parser.getError().contains("forward"));
    }

    // LAUNCH

    @Test
    void launchWithMakeAndNameIsValid() {
        InputParser parser = new InputParser("launch Scout Hal");
        assertTrue(parser.isValid());
    }

    @Test
    void launchIsCaseInsensitive() {
        InputParser parser = new InputParser("LAUNCH Scout Hal");
        assertTrue(parser.isValid());
    }

    @Test
    void launchWithNoArgumentsIsInvalid() {
        InputParser parser = new InputParser("launch");
        assertFalse(parser.isValid());
    }

    @Test
    void launchWithOnlyOneArgumentIsInvalid() {
        InputParser parser = new InputParser("launch Scout");
        assertFalse(parser.isValid());
    }

    @Test
    void launchErrorContainsUsageHint() {
        InputParser parser = new InputParser("launch");
        assertTrue(parser.getError().contains("launch"));
    }

    @Test
    void launchGetArgReturnsCorrectMake() {
        InputParser parser = new InputParser("launch Tank Hal");
        assertEquals("Tank", parser.getArg(1));
    }

    @Test
    void launchGetArgReturnsCorrectName() {
        InputParser parser = new InputParser("launch Tank Hal");
        assertEquals("Hal", parser.getArg(2));
    }

    // FORWARD

//    @Test
//    void forwardWithPositiveNumberIsValid() {
//        InputParser parser = new InputParser("forward 5");
//        assertTrue(parser.isValid());
//    }
//
//    @Test
//    void forwardWithOneStepIsValid() {
//        InputParser parser = new InputParser("forward 1");
//        assertTrue(parser.isValid());
//    }
//
//    @Test
//    void forwardWithNoArgumentIsInvalid() {
//        InputParser parser = new InputParser("forward");
//        assertFalse(parser.isValid());
//    }
//
//    @Test
//    void forwardWithZeroStepsIsInvalid() {
//        InputParser parser = new InputParser("forward 0");
//        assertFalse(parser.isValid());
//    }
//
//    @Test
//    void forwardWithNegativeStepsIsInvalid() {
//        InputParser parser = new InputParser("forward -3");
//        assertFalse(parser.isValid());
//    }
//
//    @Test
//    void forwardWithWordInsteadOfNumberIsInvalid() {
//        InputParser parser = new InputParser("forward lots");
//        assertFalse(parser.isValid());
//    }
//
//    @Test
//    void forwardErrorMentionsInvalidInput() {
//        InputParser parser = new InputParser("forward lots");
//        assertTrue(parser.getError().contains("lots"));
//    }
//
//    @Test
//    void forwardGetCommandReturnsForward() {
//        InputParser parser = new InputParser("forward 3");
//        assertEquals("forward", parser.getCommand());
//    }
//
//    @Test
//    void forwardGetArgReturnsStepCount() {
//        InputParser parser = new InputParser("forward 7");
//        assertEquals("7", parser.getArg(1));
//    }

    @Test
    void forwardWithPositiveNumberIsValid() {
        assertTrue(new InputParser("forward Hal 5").isValid());
    }

    @Test
    void forwardWithOneStepIsValid() {
        assertTrue(new InputParser("forward Hal 1").isValid());
    }

    @Test
    void forwardWithNoArgumentIsInvalid() {
        assertFalse(new InputParser("forward").isValid());
    }

    @Test
    void forwardWithRobotButNoStepsIsInvalid() {
        assertFalse(new InputParser("forward Hal").isValid());
    }

    @Test
    void forwardWithZeroStepsIsInvalid() {
        assertFalse(new InputParser("forward Hal 0").isValid());
    }

    @Test
    void forwardWithNegativeStepsIsInvalid() {
        assertFalse(new InputParser("forward Hal -3").isValid());
    }

    @Test
    void forwardWithWordInsteadOfNumberIsInvalid() {
        assertFalse(new InputParser("forward Hal lots").isValid());
    }

    @Test
    void forwardErrorMentionsInvalidInput() {
        InputParser parser = new InputParser("forward Hal lots");
        assertTrue(parser.getError().contains("lots"));
    }

    @Test
    void forwardGetCommandReturnsForward() {
        assertEquals("forward", new InputParser("forward Hal 3").getCommand());
    }

    @Test
    void forwardGetArgReturnsRobotName() {
        InputParser parser = new InputParser("forward Hal 7");
        assertEquals("Hal", parser.getArg(1));
        assertEquals("7", parser.getArg(2));
    }
    //BACK
//
//    @Test
//    void backWithPositiveNumberIsValid() {
//        InputParser parser = new InputParser("back 2");
//        assertTrue(parser.isValid());
//    }
//
//    @Test
//    void backWithNoArgumentIsInvalid() {
//        InputParser parser = new InputParser("back");
//        assertFalse(parser.isValid());
//    }
//
//    @Test
//    void backWithZeroStepsIsInvalid() {
//        InputParser parser = new InputParser("back 0");
//        assertFalse(parser.isValid());
//    }
//
//    @Test
//    void backWithNegativeStepsIsInvalid() {
//        InputParser parser = new InputParser("back -1");
//        assertFalse(parser.isValid());
//    }
//
//    @Test
//    void backWithWordInsteadOfNumberIsInvalid() {
//        InputParser parser = new InputParser("back fast");
//        assertFalse(parser.isValid());
//    }
    @Test
    void backWithPositiveNumberIsValid() {
        assertTrue(new InputParser("back Hal 2").isValid());
    }

    @Test
    void backWithNoArgumentIsInvalid() {
        assertFalse(new InputParser("back").isValid());
    }

    @Test
    void backWithRobotButNoStepsIsInvalid() {
        assertFalse(new InputParser("back Hal").isValid());
    }

    @Test
    void backWithZeroStepsIsInvalid() {
        assertFalse(new InputParser("back Hal 0").isValid());
    }

    @Test
    void backWithNegativeStepsIsInvalid() {
        assertFalse(new InputParser("back Hal -1").isValid());
    }

    @Test
    void backWithWordInsteadOfNumberIsInvalid() {
        assertFalse(new InputParser("back Hal fast").isValid());
    }
    // TURN

    @Test
    void turnLeftIsValid() {
        assertTrue(new InputParser("turn Hal left").isValid());
    }

    @Test
    void turnRightIsValid() {
        assertTrue(new InputParser("turn Hal right").isValid());
    }

    @Test
    void turnIsCaseInsensitive() {
        assertTrue(new InputParser("turn Hal LEFT").isValid());
    }

    @Test
    void turnWithNoDirectionIsInvalid() {
        assertFalse(new InputParser("turn").isValid());
    }

    @Test
    void turnWithRobotButNoDirectionIsInvalid() {
        assertFalse(new InputParser("turn Hal").isValid());
    }

    @Test
    void turnWithInvalidDirectionIsInvalid() {
        assertFalse(new InputParser("turn Hal sideways").isValid());
    }

    @Test
    void turnErrorMentionsInvalidDirection() {
        InputParser parser = new InputParser("turn Hal sideways");
        assertTrue(parser.getError().contains("sideways"));
    }

    @Test
    void turnGetArgReturnsDirection() {
        InputParser parser = new InputParser("turn Hal right");
        assertEquals("right", parser.getArg(2));
    }
    // NO-ARGUMENT COMMANDS
    @Test
    void lookAloneIsValid() {
        assertTrue(new InputParser("look Hal").isValid());
    }

    @Test
    void fireAloneIsValid() {
        assertTrue(new InputParser("fire Hal").isValid());
    }

    @Test
    void repairAloneIsValid() {
        assertTrue(new InputParser("repair Hal").isValid());
    }

    @Test
    void reloadAloneIsValid() {
        assertTrue(new InputParser("reload Hal").isValid());
    }

    @Test
    void stateAloneIsValid() {
        assertTrue(new InputParser("state Hal").isValid());
    }

    @Test
    void lookWithExtraArgumentIsInvalid() {
        assertFalse(new InputParser("look Hal around").isValid());
    }

    @Test
    void fireWithExtraArgumentIsInvalid() {
        assertFalse(new InputParser("fire Hal now").isValid());
    }

    @Test
    void repairWithExtraArgumentIsInvalid() {
        assertFalse(new InputParser("repair Hal shields").isValid());
    }

    @Test
    void reloadWithExtraArgumentIsInvalid() {
        assertFalse(new InputParser("reload Hal gun").isValid());
    }

    @Test
    void stateWithExtraArgumentIsInvalid() {
        assertFalse(new InputParser("state Hal now").isValid());
    }

    @Test
    void noArgumentCommandErrorContainsCommandName() {
        InputParser parser = new InputParser("look Hal around");
        assertTrue(parser.getError().contains("look"));
    }

//    @Test
//    void lookAloneIsValid() {
//        assertTrue(new InputParser("look").isValid());
//    }
//
//    @Test
//    void fireAloneIsValid() {
//        assertTrue(new InputParser("fire").isValid());
//    }
//
//    @Test
//    void repairAloneIsValid() {
//        assertTrue(new InputParser("repair").isValid());
//    }
//
//    @Test
//    void reloadAloneIsValid() {
//        assertTrue(new InputParser("reload").isValid());
//    }
//
//    @Test
//    void stateAloneIsValid() {
//        assertTrue(new InputParser("state").isValid());
//    }
//
//    @Test
//    void lookWithExtraArgumentIsInvalid() {
//        InputParser parser = new InputParser("look around");
//        assertFalse(parser.isValid());
//    }
//
//    @Test
//    void fireWithExtraArgumentIsInvalid() {
//        InputParser parser = new InputParser("fire now");
//        assertFalse(parser.isValid());
//    }
//
//    @Test
//    void repairWithExtraArgumentIsInvalid() {
//        InputParser parser = new InputParser("repair shields");
//        assertFalse(parser.isValid());
//    }
//
//    @Test
//    void reloadWithExtraArgumentIsInvalid() {
//        InputParser parser = new InputParser("reload gun");
//        assertFalse(parser.isValid());
//    }
//
//    @Test
//    void stateWithExtraArgumentIsInvalid() {
//        InputParser parser = new InputParser("state now");
//        assertFalse(parser.isValid());
//    }
//
//    @Test
//    void noArgumentCommandErrorContainsCommandName() {
//        InputParser parser = new InputParser("look around");
//        assertTrue(parser.getError().contains("look"));
//    }

    // EXTRA WHITESPACE

//    @Test
//    void extraSpacesBetweenWordsIsStillValid() {
//        // "forward   5" should parse just like "forward 5"
//        InputParser parser = new InputParser("forward   5");
//        assertTrue(parser.isValid());
//    }
//
//    @Test
//    void leadingAndTrailingSpacesAreIgnored() {
//        InputParser parser = new InputParser("  look  ");
//        assertTrue(parser.isValid());
//    }
    @Test
    void extraSpacesBetweenWordsIsStillValid() {
        assertTrue(new InputParser("forward   Hal   5").isValid());
    }

        @Test
        void leadingAndTrailingSpacesAreIgnored() {
            assertTrue(new InputParser("  look   Hal  ").isValid());
        }
    // CASE INSENSITIVITY

//    @Test
//    void commandNamesAreNotCaseSensitive() {
//        assertTrue(new InputParser("LOOK").isValid());
//        assertTrue(new InputParser("Forward 3").isValid());
//        assertTrue(new InputParser("TURN right").isValid());
//        assertTrue(new InputParser("FIRE").isValid());
//    }
//
//    @Test
//    void getCommandAlwaysReturnsLowercase() {
//        InputParser parser = new InputParser("FORWARD 3");
//        assertEquals("forward", parser.getCommand());
//    }
    @Test
    void commandNamesAreNotCaseSensitive() {
        assertTrue(new InputParser("LOOK Hal").isValid());
        assertTrue(new InputParser("Forward Hal 3").isValid());
        assertTrue(new InputParser("TURN Hal right").isValid());
        assertTrue(new InputParser("FIRE Hal").isValid());
    }

        @Test
        void getCommandAlwaysReturnsLowercase() {
            assertEquals("forward", new InputParser("FORWARD Hal 3").getCommand());
        }

    //getParts
//
//    @Test
//    void getPartsReturnsAllWords() {
//        InputParser parser = new InputParser("forward 5");
//        String[] parts = parser.getParts();
//        assertEquals(2, parts.length);
//        assertEquals("forward", parts[0]);
//        assertEquals("5",       parts[1]);
//    }
//
//    @Test
//    void getArgOutOfRangeReturnsNull() {
//        InputParser parser = new InputParser("look");
//        assertNull(parser.getArg(5));
//    }
    @Test
    void getPartsReturnsAllWords() {
        InputParser parser = new InputParser("forward Hal 5");
        String[] parts = parser.getParts();

        assertEquals(3, parts.length);
        assertEquals("forward", parts[0]);
        assertEquals("Hal", parts[1]);
        assertEquals("5", parts[2]);
    }

    @Test
    void getArgOutOfRangeReturnsNull() {
        InputParser parser = new InputParser("look Hal");
        assertNull(parser.getArg(5));
    }
}
