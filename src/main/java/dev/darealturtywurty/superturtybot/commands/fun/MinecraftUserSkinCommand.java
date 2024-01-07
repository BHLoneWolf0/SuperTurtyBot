package dev.darealturtywurty.superturtybot.commands.fun;

import com.google.gson.JsonObject;
import dev.darealturtywurty.superturtybot.core.command.CommandCategory;
import dev.darealturtywurty.superturtybot.core.command.CoreCommand;
import dev.darealturtywurty.superturtybot.core.util.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MinecraftUserSkinCommand extends CoreCommand {
    public MinecraftUserSkinCommand() {
        super(new Types(true, false, false, false));
    }

    @Override
    public List<OptionData> createOptions() {
        return List
                .of(new OptionData(OptionType.STRING, "username", "The username of which to get the skin for", true));
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public String getDescription() {
        return "Gets the Minecraft Skin from a username.";
    }

    @Override
    public String getHowToUse() {
        return "/mc-skin [username]";
    }

    @Override
    public String getName() {
        return "mc-skin";
    }

    @Override
    public String getRichName() {
        return "Minecraft Skin";
    }

    @Override
    public Pair<TimeUnit, Long> getRatelimit() {
        return Pair.of(TimeUnit.SECONDS, 5L);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        final Message message = event.getMessage();
        if (event.getComponentId().startsWith(message.getIdLong() + "-rotate-counter-clockwise")) {
            int rotation = Integer.parseInt(event.getComponentId().split("-")[4]) - 45;
            if (rotation <= 0) {
                rotation = 355;
            }
            if (rotation >= 360) {
                rotation = 5;
            }

            final MessageEmbed embed = message.getEmbeds().getFirst();
            final EmbedBuilder newEmbed = new EmbedBuilder(embed);

            MessageEmbed.ImageInfo image = embed.getImage();
            if (image == null) {
                event.deferEdit().queue();
                return;
            }

            String url = image.getProxyUrl();
            if (url == null) {
                event.deferEdit().queue();
                return;
            }

            final String filename = url.substring(url.lastIndexOf("/")).replace("/", "");
            final String username = filename.split(".png")[0];
            try {
                var url1 = new URI("https://minecraft-api.com/api/skins/%s/body/10.%d/10/json"
                        .formatted(username, rotation))
                        .toURL();
                final byte[] bytes = decodeURL(url1);

                newEmbed.setImage("attachment://" + username + ".png");
                final var atomicRotation = new AtomicInteger(rotation);
                event.deferEdit().setAttachments(message.getAttachments()).setCheck(event::isAcknowledged)
                        .queue(hook -> hook.editOriginalEmbeds(newEmbed.build())
                                .setComponents(createButtons(message.getIdLong(), atomicRotation.get()))
                                .setAttachments(FileUpload.fromData(bytes, username + ".png")).queue());
            } catch (final IOException | URISyntaxException exception) {
                Constants.LOGGER.error("Error getting skin for " + username, exception);
            }
        } else if (event.getComponentId().startsWith(message.getIdLong() + "-dismiss")) {
            if (message.getInteraction() == null && message.getMessageReference() != null) {
                message.getMessageReference().resolve().queue(msg -> {
                    if (event.getUser().getIdLong() != msg.getAuthor().getIdLong()) {
                        event.deferEdit().queue();
                    } else {
                        message.delete().queue();
                        msg.delete().queue();
                    }
                }, error -> message.delete().queue());

                return;
            }

            final User author = message.getInteraction().getUser();
            if (event.getUser().getIdLong() != author.getIdLong()) {
                event.deferEdit().queue();
                return;
            }

            message.delete().queue();
        } else if (event.getComponentId().startsWith(message.getIdLong() + "-rotate-clockwise")) {
            int rotation = Integer.parseInt(event.getComponentId().split("-")[3]) + 45;
            if (rotation <= 0) {
                rotation = 355;
            }
            if (rotation >= 360) {
                rotation = 5;
            }

            final MessageEmbed embed = message.getEmbeds().getFirst();
            final EmbedBuilder newEmbed = new EmbedBuilder(embed);

            MessageEmbed.ImageInfo image = embed.getImage();
            if (image == null) {
                event.deferEdit().queue();
                return;
            }

            String url = image.getProxyUrl();
            if (url == null) {
                event.deferEdit().queue();
                return;
            }

            final String filename = url.substring(url.lastIndexOf("/")).replace("/", "");
            final String username = filename.split(".png")[0];
            try {
                URL url1 = new URI("https://minecraft-api.com/api/skins/%s/body/10.%d/10/json"
                        .formatted(username, rotation))
                        .toURL();
                final byte[] bytes = decodeURL(url1);
                
                newEmbed.setImage("attachment://" + username + ".png");
                final var atomicRotation = new AtomicInteger(rotation);
                event.deferEdit().setAttachments(message.getAttachments()).setCheck(event::isAcknowledged)
                        .queue(hook -> hook.editOriginalEmbeds(newEmbed.build())
                                .setComponents(createButtons(message.getIdLong(), atomicRotation.get()))
                                .setFiles(FileUpload.fromData(bytes, username + ".png")).queue());
            } catch (final IOException | URISyntaxException exception) {
                Constants.LOGGER.error("Error getting skin for " + username, exception);
            }
        }
    }

    @Override
    protected void runSlash(SlashCommandInteractionEvent event) {
        final String username = URLEncoder.encode(
                event.getOption("username", "", OptionMapping::getAsString).trim(),
                StandardCharsets.UTF_8);
        try {
            final int rotation = 10;
            final var url = new URI("https://minecraft-api.com/api/skins/%s/body/10.%d/10/json"
                    .formatted(username, rotation))
                    .toURL();
            final byte[] bytes = decodeURL(url);

            final var embed = new EmbedBuilder().setTimestamp(Instant.now()).setColor(Color.BLUE)
                    .setDescription("The skin for `" + username + "` is:")
                    .setImage("attachment://" + username + ".png").build();
            event.deferReply().setFiles(FileUpload.fromData(bytes, username + ".png")).addEmbeds(embed)
                    .mentionRepliedUser(false).flatMap(InteractionHook::retrieveOriginal).queue(msg -> msg
                            .editMessageEmbeds(embed).setComponents(createButtons(msg.getIdLong(), rotation)).queue());
        } catch (final IOException | URISyntaxException exception) {
            event.deferReply(true)
                    .setContent("There has been an issue trying to gather this information from our database! "
                            + "This has been reported to the bot owner!")
                    .mentionRepliedUser(false).queue();
            Constants.LOGGER.error("Error getting skin for " + username, exception);
        } catch (final IllegalArgumentException exception) {
            Constants.LOGGER.error("Error getting skin for " + username, exception);
            event.deferReply(true).setContent("This player does not exist!").mentionRepliedUser(false).queue();
        }
    }

    private static ActionRow createButtons(long messageId, int rotation) {
        return ActionRow.of(Button.primary(messageId + "-rotate-counter-clockwise-" + rotation, "↩️"),
                Button.primary(messageId + "-dismiss", "🚮"),
                Button.primary(messageId + "-rotate-clockwise-" + rotation, "↪️"));
    }

    private static byte[] decodeURL(URL url) throws IOException {
        final URLConnection connection = url.openConnection();
        final String response = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
        if (response.contains("not found"))
            throw new IllegalArgumentException(response);
        final String base64 = Constants.GSON.fromJson(response, JsonObject.class).get("skin").getAsString();
        return Base64.decodeBase64(base64);
    }
}
