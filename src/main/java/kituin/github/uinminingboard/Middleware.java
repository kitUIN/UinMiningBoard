package kituin.github.uinminingboard;

import kituin.github.uinminingboard.data.FileData;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

import static kituin.github.uinminingboard.UinMiningBoard.*;

public class Middleware {
    public static ServerScoreboard SCOREBOARD;
    public static ScoreboardObjective MINE_OBJECTIVE;
    public static ScoreboardObjective DEATH_OBJECTIVE;
    public static FileData SCORE_DATA;
    public static FileData UUID_DATA;
    public static FileData IGNORE_DATA;
    public static FileData REDIRECT_DATA;


    public Middleware() {
        SCORE_DATA = FileData.load("data");
        UUID_DATA = FileData.load("uuid2name");
        IGNORE_DATA = FileData.load("ignore");
        REDIRECT_DATA = FileData.load("redirect");
    }


    public static void addScorePreBroken(String uuid) {
        if (IGNORE_DATA.containsKey(uuid)) {
            return;
        }
        String playerName = UUID_DATA.getItem(uuid);
        ScoreboardPlayerScore playerScore = SCOREBOARD.getPlayerScore(playerName, MINE_OBJECTIVE);
        int score = playerScore.getScore() + 1;
        SCORE_DATA.addItem(REDIRECT_DATA.getItemOrDefault(uuid), Integer.toString(score));
        playerScore.setScore(score);
        SCOREBOARD.updateScore(playerScore);
    }

    public void putRedirect(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        if (!REDIRECT_DATA.containsKey(uuid)) {
            REDIRECT_DATA.addItem(uuid, uuid);
            LOGGER.info("redirect.json <- " + uuid + "重定向");
        }
    }

    public void putUuid2Name(ServerPlayerEntity player) {
        putRedirect(player);
        String redirectUuid = player.getUuidAsString();
        String name = player.getEntityName();
        String uuid = REDIRECT_DATA.getItemOrDefault(redirectUuid);
        if (!UUID_DATA.containsKey(uuid)) {
            UUID_DATA.addItem(uuid, name);
            LOGGER.info("uuid2name.json <- " + uuid + " (" + name + ")");
        }
    }

    public static void updatePlayerScore(String redirectUuid) {
        String uuid = REDIRECT_DATA.getItemOrDefault(redirectUuid);
        String playerName = UUID_DATA.getItem(uuid);
        if (playerName == null) {
            return;
        }
        if (IGNORE_DATA.containsKey(uuid)) {
            SCOREBOARD.updatePlayerScore(playerName, MINE_OBJECTIVE);
        } else {
            ScoreboardPlayerScore playerScore = SCOREBOARD.getPlayerScore(playerName, MINE_OBJECTIVE);
            playerScore.setScore(Integer.parseInt(SCORE_DATA.getItem(uuid)));
            SCOREBOARD.updateScore(playerScore);
        }
    }

    public static void updateAll() {
        for (Map.Entry<String, String> entry : SCORE_DATA.getItems().entrySet()) {
            updatePlayerScore(entry.getKey());
        }
    }

    public static void show(MinecraftServer server) {
        ScoreboardObjective objectiveForSlot = server.getScoreboard().getObjectiveForSlot(1);
        if (objectiveForSlot == null) {
            server.getScoreboard().setObjectiveSlot(1, MINE_OBJECTIVE);
        } else if (objectiveForSlot.equals(MINE_OBJECTIVE)) {
            server.getScoreboard().setObjectiveSlot(1, MINE_OBJECTIVE);
        } else {
            server.getScoreboard().setObjectiveSlot(1, DEATH_OBJECTIVE);
        }
    }
}
