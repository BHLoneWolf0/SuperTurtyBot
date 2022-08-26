package io.github.darealturtywurty.superturtybot.commands.util;

import java.awt.Color;
import java.time.Instant;

import io.github.darealturtywurty.superturtybot.core.command.CommandCategory;
import io.github.darealturtywurty.superturtybot.core.command.CoreCommand;
import io.github.darealturtywurty.superturtybot.core.util.BotUtils;
import io.github.darealturtywurty.superturtybot.core.util.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ServerInfoCommand extends CoreCommand {
    public ServerInfoCommand() {
        super(new Types(true, false, false, false));
    }
    
    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }
    
    @Override
    public String getDescription() {
        return "Retrieves information about this server";
    }
    
    @Override
    public String getName() {
        return "serverinfo";
    }
    
    @Override
    public String getRichName() {
        return "Server Info";
    }
    
    @Override
    public boolean isServerOnly() {
        return true;
    }
    
    @Override
    protected void runSlash(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.deferReply().setContent("This command can only be used inside a server!").mentionRepliedUser(false)
                .queue();
            return;
        }
        
        final Guild guild = event.getGuild();
        final EmbedBuilder embed = createEmbed(guild);
        event.deferReply().addEmbeds(embed.build()).mentionRepliedUser(false).queue();
    }
    
    private static EmbedBuilder createEmbed(Guild guild) {
        final var embed = new EmbedBuilder();
        embed.setTimestamp(Instant.now());
        embed.setColor(Color.RED);
        embed.setThumbnail(guild.getIconUrl());
        embed.setTitle(guild.getName());
        embed.setDescription(guild.getDescription());
        
        embed.addField("Boost Count", guild.getBoostCount() + "", true);
        embed.addField("Categories", guild.getCategories().size() + "", true);
        embed.addField("Text Channels", guild.getTextChannels().size() + "", true);
        embed.addField("Voice Channels", guild.getVoiceChannels().size() + "", true);
        embed.addField("Emotes", guild.getEmojis().size() + "", true);
        embed.addField("Members", guild.getMemberCount() + "", true);
        
        embed.addField("Created At", StringUtils.formatTime(guild.getTimeCreated()), false);
        
        embed.addField("Notification Level", BotUtils.convertNotificationLevel(guild.getDefaultNotificationLevel()),
            true);
        embed.addField("Explicit Content Level", BotUtils.convertExplicitContentLevel(guild.getExplicitContentLevel()),
            true);
        embed.addField("NSFW Level", BotUtils.convertNSFWLevel(guild.getNSFWLevel()), true);
        embed.addField("Verification Level", BotUtils.convertVerificationLevel(guild.getVerificationLevel()), true);
        
        embed.addField("Owner", guild.getOwner().getAsMention(), true);
        embed.addField("Max File Size", String.format("%.2g", guild.getMaxFileSize() / 1000000f) + "MB", true);
        
        if (guild.getBoostCount() > 0) {
            final var boosters = new StringBuilder();
            guild.getBoosters().forEach(booster -> boosters.append(booster.getAsMention() + ", "));
            boosters.delete(boosters.length() - 1, boosters.length());
            embed.addField("Boosters", boosters.toString(), false);
        }
        
        final var roles = new StringBuilder();
        guild.getRoles().forEach(role -> roles.append(role.getAsMention() + ", "));
        roles.delete(roles.length() - 2, roles.length());
        embed.addField("Roles", roles.toString(), false);
        
        return embed;
    }
}
