package kituin.github.uinminingboard.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import kituin.github.uinminingboard.Middleware;
import kituin.github.uinminingboard.data.FileData;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;

import static kituin.github.uinminingboard.Middleware.*;
import static net.minecraft.text.HoverEvent.Action.SHOW_TEXT;

public class UinMiningBoardCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static int help(CommandContext<ServerCommandSource> context) {
        MutableText help = Text.literal("UinMiningBoard 挖掘榜帮助:\n").setStyle(Style.EMPTY.withColor(Formatting.GREEN));
        MutableText playerMessage = Text.literal(" <player> ").setStyle(Style.EMPTY.withColor(Formatting.AQUA));
        help.append(Text.literal("/uinminingboard help ").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/uinminingboard help")).withHoverEvent(new HoverEvent(SHOW_TEXT, Text.literal("点击选择该命令")))))
                .append(Text.literal("查看挖掘榜帮助\n").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
                .append(Text.literal("/uinminingboard score").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/uinminingboard score")).withHoverEvent(new HoverEvent(SHOW_TEXT, Text.literal("点击选择该命令")))))
                .append(playerMessage)
                .append(Text.literal("查看玩家挖掘量\n").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
                .append(Text.literal("/uinminingboard death").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/uinminingboard death")).withHoverEvent(new HoverEvent(SHOW_TEXT, Text.literal("点击选择该命令")))))
                .append(playerMessage)
                .append(Text.literal("查看玩家死亡次数\n").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
                .append(Text.literal("/uinminingboard show ").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/uinminingboard show")).withHoverEvent(new HoverEvent(SHOW_TEXT, Text.literal("点击选择该命令")))))
                .append(Text.literal("重载计分板\n").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
        if (context.getSource().hasPermissionLevel(4)) {
            help.append(Text.literal("/uinminingboard ban").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/uinminingboard ban")).withHoverEvent(new HoverEvent(SHOW_TEXT, Text.literal("点击选择该命令"))))
                    .append(playerMessage)
                    .append(Text.literal("禁止玩家上挖掘榜\n").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
                    .append(Text.literal("/uinminingboard unban").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/uinminingboard unban")).withHoverEvent(new HoverEvent(SHOW_TEXT, Text.literal("点击选择该命令")))))
                    .append(playerMessage)
                    .append(Text.literal("允许玩家上挖掘榜\n").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
                    .append(Text.literal("/uinminingboard reload ").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/uinminingboard reload")).withHoverEvent(new HoverEvent(SHOW_TEXT, Text.literal("点击选择该命令")))))
                    .append(Text.literal("重载计分板数据\n").setStyle(Style.EMPTY.withColor(Formatting.GREEN))));
        }
        context.getSource().sendFeedback(()->help, false);
        return 1;
    }

    public static int ban(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            IGNORE_DATA.addItem(player.getUuidAsString(), String.valueOf(System.currentTimeMillis()));
            Middleware.cleanScorePreBroken(player);
            Middleware.cleanScoreDeath(player);
            context.getSource().sendFeedback(() -> Text.literal("忽略计算").append(getPlayerText(player)).append("的榜单值"), false);
            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendError(Text.of(e.getMessage()));
            return 0;
        }
    }

    public static int unBan(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            IGNORE_DATA.removeItem(player.getUuidAsString());
            Middleware.updateScorePreBroken(player);
            Middleware.updateScoreDeath(player);
            context.getSource().sendFeedback(() -> Text.literal("重新计算").append(getPlayerText(player)).append("的榜单值"), false);
            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendError(Text.of(e.getMessage()));
            return 0;
        }
    }

    public static MutableText getPlayerText(ServerPlayerEntity player) {
        return Text.literal(player.getDisplayName().getString()).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withHoverEvent(new HoverEvent(SHOW_TEXT, Text.literal("UUID:" + player.getUuidAsString()))));
    }
    private static int getPlayerMineScore(CommandContext<ServerCommandSource> context, boolean find) {
        try {
            ServerPlayerEntity player;
            if(find){
                player = EntityArgumentType.getPlayer(context, "player");
            }else{
                player = context.getSource().getPlayer();
            }
            if (player == null)
                return 0;
            context.getSource().sendFeedback(() -> getPlayerText(player)
                    .append(Text.literal("的挖掘量为: "))
                    .append(Text.literal(String.valueOf(Middleware.getScorePreBroken(player)))
                            .fillStyle(Style.EMPTY.withColor(Formatting.GREEN))), false);
            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendError(Text.of(e.getMessage()));
            return 0;
        }
    }
    private static int getPlayerDeathScore(CommandContext<ServerCommandSource> context, boolean find) {
        try {
            ServerPlayerEntity player;
            if(find){
                player = EntityArgumentType.getPlayer(context, "player");
            }else{
                player = context.getSource().getPlayer();
            }
            if (player == null)
                return 0;
            context.getSource().sendFeedback(() -> getPlayerText(player)
                    .append(Text.literal("的死亡次数为: "))
                    .append(Text.literal(String.valueOf(Middleware.getScoreDeath(player)))
                            .fillStyle(Style.EMPTY.withColor(Formatting.GREEN))), false);
            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendError(Text.of(e.getMessage()));
            return 0;
        }
    }
    public static int findScore(CommandContext<ServerCommandSource> context) {
        return getPlayerMineScore(context, true);
    }

    public static int findMyScore(CommandContext<ServerCommandSource> context) {
        return getPlayerMineScore(context, false);
    }

    public static int findDeath(CommandContext<ServerCommandSource> context) {
        return getPlayerDeathScore(context, true);
    }

    public static int findMyDeath(CommandContext<ServerCommandSource> context) {
        return getPlayerDeathScore(context, false);
    }

    public static int reload(CommandContext<ServerCommandSource> context) {
        IGNORE_DATA = FileData.load("ignore");
        context.getSource().sendFeedback(() -> Text.literal("数据文件已经重载"), false);
        return 1;
    }

    public static int show(CommandContext<ServerCommandSource> context) {
        //Middleware.show(context.getSource().getServer());
        context.getSource().sendFeedback(() -> Text.literal("已经重载计分板"), false);
        return 1;
    }
}
