package com.legends.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legends.server.api.GameController;
import com.legends.server.game.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The HTTP API end to end, without starting a real server: MockMvc sends requests
 * straight into Spring and checks the responses.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GameApiTest {

    private static final String FOUR_PLAYERS = """
            {"seed": 3, "players": [
              {"name": "Ana", "hero": "ARCHER"},
              {"name": "Ben", "hero": "KNIGHT"},
              {"name": "Cleo", "hero": "CASTLE"},
              {"name": "Dev", "hero": "PAWN"}]}""";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private String gameId;
    private String[] tokens;

    @BeforeEach
    void createGame() throws Exception {
        String body = mvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON).content(FOUR_PLAYERS))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode created = json.readTree(body);
        gameId = created.get("gameId").asText();
        tokens = new String[4];
        for (JsonNode seat : created.get("seats")) {
            tokens[seat.get("seat").asInt()] = seat.get("token").asText();
        }
    }

    private JsonNode view(int seat) throws Exception {
        String body = mvc.perform(get("/api/games/" + gameId).header(GameController.TOKEN_HEADER, tokens[seat]))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    private int currentSeat() throws Exception {
        return view(0).get("currentSeat").asInt();
    }

    @Test
    void creatingAGameGivesEachSeatItsOwnToken() {
        assertThat(tokens).doesNotContainNull().doesNotHaveDuplicates();
    }

    @Test
    void aRandomSeedIsSmallEnoughForABrowserToKeepExactly() throws Exception {
        String twoPlayers = """
                {"players": [{"name": "Ana", "hero": "ARCHER"}, {"name": "Ben", "hero": "KNIGHT"}]}""";
        for (int i = 0; i < 20; i++) {
            String body = mvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON).content(twoPlayers))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            assertThat(json.readTree(body).get("seed").asLong()).isBetween(0L, GameService.RANDOM_SEED_LIMIT - 1);
        }
    }

    @Test
    void playerSeesTheBoardAndAllHeroes() throws Exception {
        JsonNode view = view(1);
        assertThat(view.get("yourSeat").asInt()).isEqualTo(1);
        assertThat(view.get("board")).hasSize(12);
        assertThat(view.get("board").get(0)).hasSize(12);
        assertThat(view.get("heroes")).hasSize(4);
        assertThat(view.get("heroes").get(1).get("type").asText()).isEqualTo("KNIGHT");
    }

    @Test
    void onlyThePlayerWhoseTurnItIsGetsMoveOptions() throws Exception {
        int current = currentSeat();
        int other = (current + 1) % 4;
        assertThat(view(current).get("yourMoves")).isNotEmpty();
        assertThat(view(other).get("yourMoves")).isEmpty();
    }

    @Test
    void currentPlayerCanMoveAndGetsEventsBack() throws Exception {
        int current = currentSeat();
        JsonNode move = view(current).get("yourMoves").get(0);
        String action = "{\"type\": \"MOVE\", \"to\": " + move + "}";

        mvc.perform(post("/api/games/" + gameId + "/actions")
                        .header(GameController.TOKEN_HEADER, tokens[current])
                        .contentType(MediaType.APPLICATION_JSON).content(action))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events[0].type").value("HeroMoved"))
                .andExpect(jsonPath("$.events[0].data.to.row").value(move.get("row").asInt()))
                .andExpect(jsonPath("$.view.heroes[" + current + "].position.row").value(move.get("row").asInt()));
    }

    @Test
    void actingOutOfTurnIsAConflict() throws Exception {
        int other = (currentSeat() + 1) % 4;
        mvc.perform(post("/api/games/" + gameId + "/actions")
                        .header(GameController.TOKEN_HEADER, tokens[other])
                        .contentType(MediaType.APPLICATION_JSON).content("{\"type\": \"END_TURN\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("It's not your turn"));
    }

    @Test
    void illegalMoveIsAConflictWithTheReason() throws Exception {
        int current = currentSeat();
        mvc.perform(post("/api/games/" + gameId + "/actions")
                        .header(GameController.TOKEN_HEADER, tokens[current])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"MOVE\", \"to\": {\"row\": 6, \"col\": 6}}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void wrongTokenIsForbidden() throws Exception {
        mvc.perform(get("/api/games/" + gameId).header(GameController.TOKEN_HEADER, "not-a-token"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/games/" + gameId))
                .andExpect(status().isForbidden());
    }

    @Test
    void unknownGameIsNotFound() throws Exception {
        mvc.perform(get("/api/games/nope").header(GameController.TOKEN_HEADER, tokens[0]))
                .andExpect(status().isNotFound());
    }

    @Test
    void moveWithoutADestinationIsABadRequest() throws Exception {
        mvc.perform(post("/api/games/" + gameId + "/actions")
                        .header(GameController.TOKEN_HEADER, tokens[currentSeat()])
                        .contentType(MediaType.APPLICATION_JSON).content("{\"type\": \"MOVE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("'to' is required for this action"));
    }

    @Test
    void tooFewPlayersIsABadRequest() throws Exception {
        mvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"players\": [{\"name\": \"Solo\", \"hero\": \"ARCHER\"}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownHeroIsABadRequest() throws Exception {
        mvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"players\": [{\"name\": \"A\", \"hero\": \"WIZARD\"}, {\"name\": \"B\", \"hero\": \"PAWN\"}]}"))
                .andExpect(status().isBadRequest());
    }
}
