package kituin.github.uinminingboard;

import com.mojang.logging.LogUtils;
import kituin.github.uinminingboard.data.FileData;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.util.Map;

import static kituin.github.uinminingboard.UinMiningBoard.CONFIG;
import static kituin.github.uinminingboard.UinMiningBoard.MOD_ID;

public class Middleware {
    public static ServerScoreboard SCOREBOARD;
    public static ScoreboardObjective SCOREBOARD_OBJECTIVE;
    public static FileData SCORE_DATA;
    public static FileData UUID_DATA;
    public static FileData IGNORE_DATA;
    public static FileData REDIRECT_DATA;
    private static final Logger LOGGER = LogUtils.getLogger();

    public Middleware() {
        SCOREBOARD_OBJECTIVE = SCOREBOARD.addObjective(MOD_ID, ScoreboardCriterion.DUMMY, Text.literal(CONFIG.displayName), ScoreboardCriterion.RenderType.HEARTS);
        SCOREBOARD.updateObjective(SCOREBOARD_OBJECTIVE);
        SCORE_DATA = FileData.load("data");
        UUID_DATA = FileData.load("uuid2name");
        IGNORE_DATA = FileData.load("ignore");
        REDIRECT_DATA = FileData.load("redirect");
    }


    public static void addScorePreBroken(String uuid) {
        if (IGNORE_DATA.containsKey(uuid)) {
            return;
        }
        String playerName = UUID_DATA.getItem(REDIRECT_DATA.getItemOrDefault(uuid));
        ScoreboardPlayerScore playerScore = SCOREBOARD.getPlayerScore(playerName, SCOREBOARD_OBJECTIVE);
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
        String uuid = player.getUuidAsString();
        String name = player.getEntityName();
        if (!UUID_DATA.containsKey(REDIRECT_DATA.getItemOrDefault(uuid))) {
            UUID_DATA.addItem(REDIRECT_DATA.getItemOrDefault(uuid), name);
            LOGGER.info("uuid2name.json <- " + uuid + " (" + name + ")");
        }
    }

    public static void update(Map.Entry<String, String> entry) {
        ScoreboardPlayerScore playerScore = SCOREBOARD.getPlayerScore(UUID_DATA.getItem(REDIRECT_DATA.getItemOrDefault(entry.getKey())), SCOREBOARD_OBJECTIVE);
        if (IGNORE_DATA.containsKey(entry.getKey())) {
            playerScore.setScore(0);
        } else {
            playerScore.setScore(Integer.parseInt(entry.getValue()));
        }
        SCOREBOARD.updateScore(playerScore);
    }

    public static void updateDefault() {
        for (Map.Entry<String, String> entry : SCORE_DATA.getItems().entrySet()) {
            update(entry);
        }
    }

//    public static void updateSorted() {
//        for (Map.Entry<String, String> entry : SCORE_DATA.sort()) {
//            update(entry);
//        }
//    }
}
