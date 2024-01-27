package dev.darealturtywurty.superturtybot.commands.fun;

import dev.darealturtywurty.superturtybot.core.command.CommandCategory;
import dev.darealturtywurty.superturtybot.core.command.CoreCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CoinFlipCommand extends CoreCommand {
    public CoinFlipCommand() {
        super(new Types(true, false, false, false));
    }

    @NotNull
    private static String constructChoiceReply(String botChoice, String choiceStr) {
        String reply;
        if (botChoice.equalsIgnoreCase(choiceStr)) {
            if (choiceStr.contains("head")) {
                reply = "You were correct! It was Heads 🗣.";
            } else if (choiceStr.contains("tail")) {
                reply = "You were correct! It was Tails 🐍.";
            } else {
                reply = "You were correct! It landed on it's side 😲.";
            }
        } else if (botChoice.contains("head")) {
            reply = "You were incorrect! It was Heads 🗣.";
        } else if (botChoice.contains("tail")) {
            reply = "You were incorrect! It was Tails 🐍.";
        } else {
            reply = "You were incorrect! It landed on it's side 😲.";
        }
        return reply;
    }

    @Override
    public List<OptionData> createOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "choice", "Whether you are choosing heads or tails", false)
                        .addChoice("heads", "heads")
                        .addChoice("tails", "tails"));
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Flips a coin";
    }

    @Override
    public String getHowToUse() {
        return "/coinflip\n/coinflip [heads or tails]";
    }

    @Override
    public String getName() {
        return "coinflip";
    }

    @Override
    public String getRichName() {
        return "Coin Flip";
    }

    @Override
    protected void runSlash(SlashCommandInteractionEvent event) {
        final OptionMapping choice = event.getOption("choice");
        if (choice == null) {
            if (ThreadLocalRandom.current().nextInt(1000) == 69) {
                reply(event, "It landed on it's side. It was neither heads or tails! 😔", false);
            } else {
                reply(event, "It was: " + (ThreadLocalRandom.current().nextBoolean() ? "Heads 🗣" : "Tails 🐍") + "!", false);
            }
        } else {
            String choiceStr = choice.getAsString();
            if (!choiceStr.contains("head") && !choiceStr.contains("tail") && !choiceStr.contains("side")) {
                reply(event, "You must supply either `heads` or `tails`!", false, true);
                return;
            }

            if (choiceStr.contains("head")) {
                choiceStr = "heads";
            } else if (choiceStr.contains("tail")) {
                choiceStr = "tails";
            } else {
                choiceStr = "side";
            }

            String botChoice;
            if (ThreadLocalRandom.current().nextInt(1000) == 69) {
                botChoice = "side";
            } else {
                botChoice = ThreadLocalRandom.current().nextBoolean() ? "heads" : "tails";
            }

            String reply = constructChoiceReply(botChoice, choiceStr);

            reply(event, "You chose `" + choiceStr + "`. " + reply, false);
        }
    }
}
