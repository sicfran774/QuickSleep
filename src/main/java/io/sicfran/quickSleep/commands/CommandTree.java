package io.sicfran.quickSleep.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.sicfran.quickSleep.QuickSleep;

@SuppressWarnings("UnstableApiUsage")
public class CommandTree {

    private final SleepCommand sleepCommand;
    private final MessageCommand messageCommand;

    public CommandTree(QuickSleep plugin){
        sleepCommand = new SleepCommand(plugin);
        messageCommand = new MessageCommand(plugin);
    }

    public LiteralArgumentBuilder<CommandSourceStack> createCommand(){
        return Commands.literal("sleep") // sleep
                .then(Commands.literal("confirm") // sleep confirm
                    .requires(ctx -> ctx.getSender().hasPermission(CommandsPermissions.CONFIRM))
                    .executes(sleepCommand::sleepConfirm)
                )
                .then(Commands.literal("cancel") // sleep cancel
                    .requires(ctx -> ctx.getSender().hasPermission(CommandsPermissions.CANCEL))
                    .executes(sleepCommand::sleepCancel)
                ).then(Commands.literal("timer")
                        .executes(sleepCommand::getSleepTimer)
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(3,60))
                            .requires(ctx -> ctx.getSender().hasPermission(CommandsPermissions.TIMER))
                            .executes(sleepCommand::sleepTimer)
                        )
                )
                .then(Commands.literal("message") // sleep message
                        .requires(ctx -> ctx.getSender().hasPermission(CommandsPermissions.MESSAGE_SELF))
                        .then(Commands.literal("wakeup") // sleep message wakeup
                                .then(Commands.argument("message", StringArgumentType.greedyString()) // sleep message wakeup {message}
                                        .executes(ctx ->
                                                messageCommand.changeMessage(ctx, "Wake up"))
                                )
                        )
                        .then(Commands.literal("cancel") // sleep message cancel
                                .then(Commands.argument("message", StringArgumentType.greedyString()) // sleep message cancel {message}
                                        .executes(ctx ->
                                                messageCommand.changeMessage(ctx, "Cancel"))
                                )
                        )

                        .requires(ctx -> ctx.getSender().hasPermission(CommandsPermissions.MESSAGE_ALL))
                        .then(Commands.argument("player", ArgumentTypes.player()) // sleep message {player}
                                .then(Commands.literal("wakeup") // sleep message {player} wakeup
                                        .then(Commands.argument("message", StringArgumentType.greedyString()) // sleep message {player} wakeup {message}
                                                .executes(ctx ->
                                                        messageCommand.changeMessage(ctx, "Wake up"))
                                        )
                                )
                                .then(Commands.literal("cancel") // sleep message {player} cancel
                                        .then(Commands.argument("message", StringArgumentType.greedyString()) // sleep message {player} cancel {messsage}
                                                .executes(ctx ->
                                                        messageCommand.changeMessage(ctx, "Cancel"))
                                        )
                                )
                        )
                );
    }
}
