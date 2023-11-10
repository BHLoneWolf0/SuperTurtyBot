package dev.darealturtywurty.superturtybot.commands.util;

import com.google.gson.JsonObject;
import dev.darealturtywurty.superturtybot.Environment;
import dev.darealturtywurty.superturtybot.core.command.CommandCategory;
import dev.darealturtywurty.superturtybot.core.command.CoreCommand;
import dev.darealturtywurty.superturtybot.core.util.Constants;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SteamGameDetailsCommand extends CoreCommand {
    public SteamGameDetailsCommand() {
        super(new Types(true,false,false,false));
    }

    @Override
    public List<OptionData> createOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "app-id", "The steam app id.", true));
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UTILITY;
    }

    @Override
    public String getDescription() {
        return "Returns the app/game details of the specified app id";
    }

    @Override
    public String getName() {
        return "game-details";
    }

    @Override
    public String getRichName() {
        return "game details";
    }

    @Override
    public Pair<TimeUnit, Long> getRatelimit() {
        return Pair.of(TimeUnit.SECONDS, 10L);
    }

    @Override
    protected void runSlash(SlashCommandInteractionEvent event) {

        if(Environment.INSTANCE.steamKey().isEmpty()) {
            reply(event, "❌ This command has been disabled by the bot owner!", false, true);
            Constants.LOGGER.warn("Steam API key is not set!");
            return;
        }

        String appID = event.getOption("app-id", null, OptionMapping::getAsString);

        if (appID == null) {
            reply(event, "❌ You must provide a Steam appid!", false, true);
            return;
        }
        event.deferReply().queue();

        String getGameDetailsUrl = "https://store.steampowered.com/api/appdetails?appids=%s".formatted(appID);

        String getCurrentPlayedGameApi = "https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?key=%s&appid=%s"
                .formatted(Environment.INSTANCE.steamKey().get(), appID);


        final Request request = new Request.Builder().url(getGameDetailsUrl).get().build();

        final Request request1 = new Request.Builder().url(getCurrentPlayedGameApi).get().build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            Response getSteamPlayerCount = new OkHttpClient().newCall(request1).execute();

            if (!response.isSuccessful()) {
                event.getHook()
                        .sendMessage("❌ Failed to get response!")
                        .mentionRepliedUser(false)
                        .queue();
                return;
            }

            if (!getSteamPlayerCount.isSuccessful()) {
                event.getHook()
                        .sendMessage("❌ Failed to get response!")
                        .mentionRepliedUser(false)
                        .queue();
                return;
            }

            ResponseBody body = response.body();
            ResponseBody steamPlayerCountBody = getSteamPlayerCount.body();
            if (body == null) {
                event.getHook()
                        .sendMessage("❌ Failed to get response!")
                        .mentionRepliedUser(false)
                        .queue();
                return;
            }

            if (steamPlayerCountBody == null) {
                event.getHook()
                        .sendMessage("❌ Failed to get response!")
                        .mentionRepliedUser(false)
                        .queue();
                return;
            }

            String bodyString = body.string();
            String SteamPlayerCountBodyString = body.string();

            if (bodyString.isBlank()) {
                event.getHook()
                        .sendMessage("❌ Failed to get response!")
                        .mentionRepliedUser(false)
                        .queue();
                return;
            }

            if (SteamPlayerCountBodyString.isBlank()) {
                event.getHook()
                        .sendMessage("❌ Failed to get response!")
                        .mentionRepliedUser(false)
                        .queue();
                return;
            }

            StoreSteamApiResponse steamResponse = StoreSteamApiResponse.fromJsonString(bodyString, appID);

            GetPlayerCountApi getCurrentPlayerCountApi = GetPlayerCountApi.fromJsonString(SteamPlayerCountBodyString);

            SteamData getSteamGameDetails = steamResponse.data;
            int PlayerCount = getCurrentPlayerCountApi.player_count;

            if(!steamResponse.success) {
                event.getHook()
                        .sendMessage("❌ Failed to get steam appid!")
                        .mentionRepliedUser(false)
                        .queue();
                return;
            }

            if(getCurrentPlayerCountApi.result == 0) {
                event.getHook()
                        .sendMessage("❌ Failed to get steam player count!")
                        .mentionRepliedUser(false)
                        .queue();
                return;
            }

            StringBuilder categories = new StringBuilder();

            for(Categories category : getSteamGameDetails.categories){
                categories.append(category.description + ", ");
            }

            StringBuilder genres = new StringBuilder();

            for(Genres genre : getSteamGameDetails.genres){
                genres.append(genre.description + ", ");
            }

            String genre = String.valueOf(genres);
            String category = String.valueOf(categories);
            String recommendationTotal = String.valueOf(getSteamGameDetails.recommendations.total);
            String playerCount = String.valueOf(PlayerCount);

            var embed = new EmbedBuilder()
                    .setTitle("%s (%s)".formatted(getSteamGameDetails.name , appID))
                    .addField("🟦 Description", getSteamGameDetails.short_description, false)
                    .addField("🟦 Categories", category, true)

                    .addField("🟦 Recommendations\n" + recommendationTotal + "\n" + "🟦 Release Date", getSteamGameDetails.release_date.date, true)
                    .addField("🟦 Publishers\n"+ getSteamGameDetails.developers.toString() + "\n🟦 Developers", getSteamGameDetails.publishers.toString(),true)

                    .addField("🟦Platforms\nWindows: %b\nMac: %b\nLinux: %b".formatted(getSteamGameDetails.platforms.windows, getSteamGameDetails.platforms.mac, getSteamGameDetails.platforms.linux), "",true)
                    .addField("🟦 Genre", genre, true)

                    .addField("🟦 Players playing", playerCount,true)
                    .build();

            event.getHook()
                    .sendMessageEmbeds(embed)
                    .mentionRepliedUser(false)
                    .queue();

        }catch (IOException exception) {
            reply(event, "Failed to response!");
            Constants.LOGGER.error("Failed to get response!", exception);
        }
    }
    @Data
    public static class StoreSteamApiResponse{
        private boolean success;
        private SteamData data;

        private static StoreSteamApiResponse fromJsonString(String json, String appid) {
            JsonObject object = Constants.GSON.fromJson(json, JsonObject.class);
            return Constants.GSON.fromJson(object.get(appid), StoreSteamApiResponse.class);
        }
    }
    @Data
    public static class SteamData{
        private String type;
        private String name;
        private int steam_appid;
        private boolean is_free;
        private String about_the_game;
        private String short_description;
        private List<String> publishers;
        private List<String> developers;
        private Platforms platforms;
        private List<Categories> categories;
        private List<Genres> genres;
        private Recommendations recommendations;
        private ReleaseDate release_date;

    }

    @Data
    public static class Platforms {
        private boolean windows;
        private boolean mac;
        private boolean linux;
    }
    @Data

    public static class Categories {
        private int id;
        private String description;
    }
    @Data

    public static class Genres {
        private int id;
        private String description;
    }
    @Data
    public static class Recommendations {
        private int total;
    }
    @Data
    public static class ReleaseDate {
        private boolean coming_soon;
        private String date;
    }
    @Data
    public static class GetPlayerCountApi{
        private int player_count;
        private int result;
        private static GetPlayerCountApi fromJsonString(String json) {
            JsonObject object = Constants.GSON.fromJson(json, JsonObject.class);
            return Constants.GSON.fromJson(object.get("response"), GetPlayerCountApi.class);
        }
    }

}
