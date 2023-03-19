package kituin.github.uinminingboard.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import kituin.github.uinminingboard.Middleware;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;

import java.util.Map;

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
                .append(Text.literal("查看玩家挖掘量\n").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
        if (context.getSource().hasPermissionLevel(4)) {
            help.append(Text.literal("/uinminingboard redirect").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/uinminingboard redirect")).withHoverEvent(new HoverEvent(SHOW_TEXT, Text.literal("点击选择该命令")))))
                    .append(playerMessage)
                    .append(Text.literal("将玩家重定向到已有数据中\n").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
                    .append(Text.literal("/uinminingboard ban").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/uinminingboard ban")).withHoverEvent(new HoverEvent(SHOW_TEXT, Text.literal("点击选择该命令")))))
                    .append(playerMessage)
                    .append(Text.literal("禁止玩家上榜\n").setStyle(Style.EMPTY.withColor(Formatting.GREEN)))
                    .append(Text.literal("/uinminingboard unban").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/uinminingboard unban")).withHoverEvent(new HoverEvent(SHOW_TEXT, Text.literal("点击选择该命令")))))
                    .append(playerMessage)
                    .append(Text.literal("允许玩家上榜\n").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
        }
        context.getSource().sendFeedback(help, false);
        return 1;
    }


    public static int redirect(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            if (UUID_DATA.containsKey(player.getUuidAsString())) {
                LOGGER.info(player.getEntityName() + "无需重定向");
                return 1;
            }
            // Boolean flag = false;
            for (Map.Entry<String, String> entry : UUID_DATA.getItems().entrySet()) {
                if (entry.getValue().equals(player.getEntityName())) {
                    REDIRECT_DATA.addItem(player.getUuidAsString(), entry.getKey());
                    LOGGER.info(player.getEntityName() + "重定向:\n" + player.getUuidAsString() + "\n->" + entry.getKey());
                    return 1;
                }
            }
            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendError(Text.of(e.getMessage()));
            return 0;
        }

    }

    public static int ban(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            IGNORE_DATA.addItem(player.getUuidAsString(), String.valueOf(System.currentTimeMillis()));
            String message = "榜单忽略玩家:" + player.getEntityName() + "(" + player.getUuidAsString() + ")的榜单值";
            Middleware.updateDefault();
            context.getSource().sendFeedback(Text.of(message), false);
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
            Middleware.updateDefault();
            String message = "重新计算玩家:" + player.getEntityName() + "(" + player.getUuidAsString() + ")的榜单值";
            // LOGGER.info(message);
            context.getSource().sendFeedback(Text.of(message), false);
            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendError(Text.of(e.getMessage()));
            return 0;
        }
    }

    public static String score(ServerPlayerEntity player) {
        return SCORE_DATA.getItem(REDIRECT_DATA.getItemOrDefault(player.getUuidAsString()));
    }

    public static int findScore(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
            context.getSource().sendFeedback(Text.literal(player.getEntityName() + " 的挖掘量为: ").append(Text.literal(score(player)).fillStyle(Style.EMPTY.withColor(Formatting.GREEN))), false);
            return 1;
        } catch (CommandSyntaxException e) {
            context.getSource().sendError(Text.of(e.getMessage()));
            return 0;
        }
    }

    public static int findMyScore(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null)
            return 0;
        context.getSource().sendFeedback(Text.literal("您的挖掘量为: ").append(Text.literal(score(player)).fillStyle(Style.EMPTY.withColor(Formatting.GREEN))), false);
        return 1;
    }
}
