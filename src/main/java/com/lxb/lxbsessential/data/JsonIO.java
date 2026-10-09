package com.lxb.lxbsessential.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Supplier;

/** JSON persistence: atomic writes, and corrupt files are moved aside instead of crashing the server. */
public final class JsonIO {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT);

    private JsonIO() {
    }

    public static final class Loaded<T> {
        public final T value;
        /** Where a corrupt file was moved to, or null. */
        public final Path quarantined;
        /** Why the file could not be read, or null when it loaded fine. */
        public final Exception error;

        Loaded(T value, Path quarantined, Exception error) {
            this.value = value;
            this.quarantined = quarantined;
            this.error = error;
        }
    }

    public static <T> Loaded<T> read(Path file, Type type, Supplier<T> empty, LocalDateTime now) {
        if (!Files.exists(file)) {
            return new Loaded<>(empty.get(), null, null);
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            T value = GSON.fromJson(reader, type);
            return new Loaded<>(value != null ? value : empty.get(), null, null);
        } catch (IOException | JsonParseException e) {
            Path target = file.resolveSibling(file.getFileName() + ".corrupt-" + STAMP.format(now));
            try {
                Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException moveFailed) {
                e.addSuppressed(moveFailed);
                target = null;
            }
            return new Loaded<>(empty.get(), target, e);
        }
    }

    public static void write(Path file, Object value) throws IOException {
        Files.createDirectories(file.getParent());
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            GSON.toJson(value, writer);
        }
        try {
            Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
