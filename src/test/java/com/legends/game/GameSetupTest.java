package com.legends.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.legends.board.BoardLayout;
import com.legends.hero.Hero;
import com.legends.hero.HeroType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GameSetupTest {

    private static final List<PlayerChoice> FOUR_PLAYERS = List.of(
            new PlayerChoice("Ana", HeroType.ARCHER),
            new PlayerChoice("Ben", HeroType.KNIGHT),
            new PlayerChoice("Cleo", HeroType.CASTLE),
            new PlayerChoice("Dev", HeroType.PAWN));

    private final GameSetup setup = new GameSetup();

    @Test
    void seatsFollowTheChoicesInOrder() {
        GameState game = setup.newGame(1, FOUR_PLAYERS);
        for (int seat = 0; seat < FOUR_PLAYERS.size(); seat++) {
            Hero hero = game.heroes().get(seat);
            assertEquals(FOUR_PLAYERS.get(seat).hero(), hero.type());
            assertEquals(seat, hero.owner().seat());
            assertEquals(FOUR_PLAYERS.get(seat).name(), hero.owner().name());
        }
    }

    @Test
    void heroesStartOnTheirCornersClockwise() {
        GameState game = setup.newGame(1, FOUR_PLAYERS);
        for (int seat = 0; seat < FOUR_PLAYERS.size(); seat++) {
            assertEquals(
                    BoardLayout.startSquare(GameSetup.CLOCKWISE.get(seat)),
                    game.positionOf(game.heroes().get(seat)).orElseThrow());
        }
    }

    @Test
    void sameSeedGivesSameFirstPlayer() {
        assertEquals(
                setup.newGame(99, FOUR_PLAYERS).currentHero().owner().seat(),
                setup.newGame(99, FOUR_PLAYERS).currentHero().owner().seat());
    }

    @Test
    void everySeatCanGoFirst() {
        Set<Integer> firstSeats = new HashSet<>();
        for (long seed = 0; seed < 100; seed++) {
            firstSeats.add(setup.newGame(seed, FOUR_PLAYERS).currentHero().owner().seat());
        }
        assertEquals(Set.of(0, 1, 2, 3), firstSeats);
    }

    @Test
    void everyHeroCanMoveAtTheStart() {
        for (long seed = 0; seed < 200; seed++) {
            GameState game = setup.newGame(seed, FOUR_PLAYERS);
            for (Hero hero : game.heroes()) {
                assertTrue(!game.legalMoves(hero).isEmpty(), "seed " + seed + ": " + hero + " is stuck");
            }
        }
    }

    @Test
    void playerCountMustBeTwoToFour() {
        assertThrows(IllegalArgumentException.class, () -> setup.newGame(1, FOUR_PLAYERS.subList(0, 1)));
        List<PlayerChoice> five = new java.util.ArrayList<>(FOUR_PLAYERS);
        five.add(new PlayerChoice("Eve", HeroType.ARCHER));
        assertThrows(IllegalArgumentException.class, () -> setup.newGame(1, five));
    }

    @Test
    void twoPlayerGameWorks() {
        GameState game = setup.newGame(5, FOUR_PLAYERS.subList(0, 2));
        assertEquals(2, game.heroes().size());
    }
}
