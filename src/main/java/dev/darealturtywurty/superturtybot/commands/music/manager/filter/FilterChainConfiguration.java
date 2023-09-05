package dev.darealturtywurty.superturtybot.commands.music.manager.filter;

import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public class FilterChainConfiguration {
    private final ChannelMixConfig channelMix = new ChannelMixConfig();
    private final EqualizerConfig equalizer = new EqualizerConfig();
    private final KaraokeConfig karaoke = new KaraokeConfig();
    private final LowPassConfig lowPass = new LowPassConfig();
    private final RotationConfig rotation = new RotationConfig();
    private final TimescaleConfig timescale = new TimescaleConfig();
    private final TremoloConfig tremolo = new TremoloConfig();
    private final VibratoConfig vibrato = new VibratoConfig();
    private final Map<Class<? extends FilterConfig>, FilterConfig> filters = new HashMap<>();

    public FilterChainConfiguration() {
        filters.put(channelMix.getClass(), channelMix);
        filters.put(equalizer.getClass(), equalizer);
        filters.put(karaoke.getClass(), karaoke);
        filters.put(lowPass.getClass(), lowPass);
        filters.put(rotation.getClass(), rotation);
        filters.put(timescale.getClass(), timescale);
        filters.put(tremolo.getClass(), tremolo);
        filters.put(vibrato.getClass(), vibrato);
    }

    /**
     * Returns true if a configuration of the provided class is present.
     *
     * @param clazz Class of the configuration.
     *
     * @return True if a configuration of the provided class is present.
     */
    public boolean hasConfig(Class<? extends FilterConfig> clazz) {
        return filters.containsKey(clazz);
    }

    /**
     * Returns a configuration of the provided class, if it exists. May return null.
     *
     * @param clazz Class of the configuration.
     * @param <T>   Type of the configuration.
     *
     * @return The existing instance, or null if there is none.
     */
    @SuppressWarnings("unchecked")
    @CheckReturnValue
    public <T extends FilterConfig> T getConfig(@Nonnull Class<T> clazz) {
        return (T) filters.get(clazz);
    }

    /**
     * Returns a configuration of the provided class if it exists, or creates
     * a new instance of it with the provided supplier.
     *
     * @param clazz    Class of the configuration.
     * @param supplier Supplier for creating a new instance of the configuration.
     * @param <T>      Type of the configuration.
     *
     * @return An instance of the provided class stored. If none is stored, a new
     * one is created and stored.
     */
    @SuppressWarnings("unchecked")
    @CheckReturnValue
    public <T extends FilterConfig> T getOrPutConfig(@Nonnull Class<T> clazz, @Nonnull Supplier<T> supplier) {
        return (T) filters.computeIfAbsent(clazz, ignored -> {
            var config = Objects.requireNonNull(supplier.get(), "Provided configuration may not be null");
            if(!clazz.isInstance(config)) {
                throw new IllegalArgumentException("Config not instance of provided class");
            }

            for(FilterConfig filterConfig : filters.values()) {
                if(filterConfig.name().equals(config.name())) {
                    throw new IllegalArgumentException("Duplicate configuration name " + filterConfig.name());
                }
            }

            return config;
        });
    }

    public void putConfig(@Nonnull Class<? extends FilterConfig> clazz, @Nonnull FilterConfig config) {
         filters.put(clazz, config);
    }

    /**
     * Returns whether or not this configuration has filters enabled.
     *
     * <br>This method returns true if any of the configurations reports
     * it's enabled.
     *
     * @return True if this configuration is enabled.
     */
    @CheckReturnValue
    public boolean isEnabled() {
        for(var config : filters.values()) {
            if(config.enabled()) return true;
        }

        return false;
    }

    /**
     * Returns a filter factory with the currently enabled filters.
     *
     * <br>If no configuration is enabled, this method returns null.
     *
     * @return A filter factory for the currently enabled filters,
     * or null if none are enabled.
     */
    @Nullable
    @CheckReturnValue
    public PcmFilterFactory factory() {
        return isEnabled() ? new Factory(this) : null;
    }

    /**
     * Encodes the state of this configuration and all filters in it.
     *
     * @return The encoded state.
     */
    @Nonnull
    @CheckReturnValue
    public JsonObject encode() {
        var obj = new JsonObject();
        for(FilterConfig config : filters.values()) {
            JsonObject encoded = config.encode();
            encoded.addProperty("enabled", config.enabled());
            obj.add(config.name(), encoded);
        }

        return obj;
    }

    @Nonnull
    @CheckReturnValue
    public ChannelMixConfig channelMix() {
        return channelMix;
    }

    @Nonnull
    @CheckReturnValue
    public EqualizerConfig equalizer() {
        return equalizer;
    }

    @Nonnull
    @CheckReturnValue
    public KaraokeConfig karaoke() {
        return karaoke;
    }

    @Nonnull
    @CheckReturnValue
    public LowPassConfig lowPass() {
        return lowPass;
    }

    @Nonnull
    @CheckReturnValue
    public RotationConfig rotation() {
        return rotation;
    }

    @Nonnull
    @CheckReturnValue
    public TimescaleConfig timescale() {
        return timescale;
    }

    @Nonnull
    @CheckReturnValue
    public TremoloConfig tremolo() {
        return tremolo;
    }

    @Nonnull
    @CheckReturnValue
    public VibratoConfig vibrato() {
        return vibrato;
    }

    public void disableAll() {
        for(FilterConfig config : filters.values()) {
            config.setEnabled(false);
        }
    }

    public Map<Class<? extends FilterConfig>, ? extends FilterConfig> getFilters() {
        return Map.copyOf(filters);
    }

    private record Factory(FilterChainConfiguration configuration) implements PcmFilterFactory {
        @Override
            public List<AudioFilter> buildChain(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter output) {
                var list = new ArrayList<AudioFilter>();
                list.add(output);
                for (var config : configuration.filters.values()) {
                    AudioFilter filter = config.enabled() ? config.create(format, (FloatPcmAudioFilter) list.get(0)) : null;
                    if (filter != null) {
                        list.add(0, filter);
                    }
                }
                return list.subList(0, list.size() - 1);
            }
        }
}