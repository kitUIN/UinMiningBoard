package kituin.github.uinminingboard;

import kituin.github.uinminingboard.data.FileData;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.scoreboard.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

public class Middleware {
    public static ServerScoreboard SCOREBOARD;
    public static ScoreboardObjective MINE_OBJECTIVE;
    public static ScoreboardObjective DEATH_OBJECTIVE;
    public static FileData IGNORE_DATA;


    public Middleware() {
        IGNORE_DATA = FileData.load("ignore");
    }


    public static void updateScorePreBroken(ServerPlayerEntity player) {
        if (IGNORE_DATA.containsKey(player.getUuidAsString())) return;
        String playerName = player.getDisplayName().getString();
        ScoreAccess playerScore = SCOREBOARD.getOrCreateScore(ScoreHolder.fromName(playerName), MINE_OBJECTIVE);
        playerScore.setScore(getAllMinedBlocksStats(player));
    }

    public static void updateScoreDeath(ServerPlayerEntity player) {
        if (IGNORE_DATA.containsKey(player.getUuidAsString())) return;
        String playerName = player.getDisplayName().getString();
        ScoreAccess playerScore = SCOREBOARD.getOrCreateScore(ScoreHolder.fromName(playerName), DEATH_OBJECTIVE);
        playerScore.setScore(getDeathStats(player));
    }

    public static void cleanScorePreBroken(ServerPlayerEntity player) {
        clean(player, MINE_OBJECTIVE);
    }

    public static void cleanScoreDeath(ServerPlayerEntity player) {
        clean(player, DEATH_OBJECTIVE);
    }

    public static int getScorePreBroken(ServerPlayerEntity player) {
        return score(player, MINE_OBJECTIVE);
    }

    public static int getScoreDeath(ServerPlayerEntity player) {
        return score(player, DEATH_OBJECTIVE);
    }

    private static void clean(ServerPlayerEntity player, ScoreboardObjective obj) {
        String playerName = player.getDisplayName().getString();
        SCOREBOARD.removeScore(ScoreHolder.fromName(playerName), obj);
    }
    private static int score(ServerPlayerEntity player, ScoreboardObjective obj) {
        String playerName = player.getDisplayName().getString();
        return SCOREBOARD.getScore(ScoreHolder.fromName(playerName), obj).getScore();
    }

    private  static int getAllMinedBlocksStats(ServerPlayerEntity player) {
        int total = 0;
        for (Identifier blockId : Registries.BLOCK.getIds()) {
            Stat<Block> minedStat = Stats.MINED.getOrCreateStat(Registries.BLOCK.get(blockId));
            int count = player.getStatHandler().getStat(minedStat);
            total += count;
        }

        return total;
    }

    private static int getDeathStats(ServerPlayerEntity player) {
        return player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS));
    }
}
