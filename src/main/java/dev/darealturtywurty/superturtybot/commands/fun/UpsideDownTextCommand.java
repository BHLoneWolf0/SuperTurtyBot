package dev.darealturtywurty.superturtybot.commands.fun;

import dev.darealturtywurty.superturtybot.core.command.CommandCategory;
import dev.darealturtywurty.superturtybot.core.command.CoreCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class UpsideDownTextCommand extends CoreCommand {
    private static final String NORMAL_CHARS = "abcdefghijklmnopqrstuvwxyz_,;.?!/\\'ABCDEFGHIJKLMNOPQRSTUVWXYZ'˙/[]-=`1234567890~@#$%^&*()‾_+{}|:\"<>";
    private static final String UPSIDEDOWN_CHARS = "ɐqɔpǝɟƃɥıɾʞןɯuodbɹsʇnʌʍxʎz‾'؛˙¿¡/,∀𐐒Ɔ◖ƎℲ⅁HIſ⋊˥WNOԀΌᴚS⊥∩ΛMX⅄Z,.][-=,ƖᄅƐㄣϛ9ㄥ860~@#$%^⅋*)(_‾+}{|:„><";

    public UpsideDownTextCommand() {
        super(new Types(true, false, false, false));
    }

    @Override
    public List<OptionData> createOptions() {
        return List.of(new OptionData(OptionType.STRING, "text", "The text to put upside-down", true));
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Puts the given piece of text upside-down";
    }

    @Override
    public String getHowToUse() {
        return "/upsidedowntext [text]";
    }

    @Override
    public String getName() {
        return "upsidedowntext";
    }

    @Override
    public String getRichName() {
        return "Upside-Down Text";
    }

    @Override
    protected void runSlash(SlashCommandInteractionEvent event) {
        final String text = event.getOption("text", "", OptionMapping::getAsString);
        final var newText = new StringBuilder();
        for (int charIndex = 0; charIndex < text.length(); charIndex++) {
            final char letter = text.charAt(charIndex);
            final int normalIndex = NORMAL_CHARS.indexOf(letter);
            newText.append(normalIndex != -1 ? UPSIDEDOWN_CHARS.charAt(normalIndex) : letter);
        }

        reply(event, newText.reverse().toString(), false);
    }
}
