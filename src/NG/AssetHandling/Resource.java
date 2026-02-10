package NG.AssetHandling;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Geert van Ieperen created on 26-2-2020.
 */
public class Resource<T> extends Asset<T> {
    private static final Map<Path, Resource<?>> allFileResources = new HashMap<>();

    /**
     * must be relative to file directory, for the sake of serialisation
     */
    private final Path fileLocation;
    private final FileLoader<T> loader;

    private Resource(FileLoader<T> loader, Path relativePath) {
        super();
        this.loader = loader;
        this.fileLocation = relativePath;
    }

    @Override
    protected T reload() throws AssetException {
        try {
            return loader.apply(fileLocation);

        } catch (IOException e) {
            throw new AssetException(e, fileLocation + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Resource<T> get(FileLoader<T> loader, Path path) {
        //noinspection unchecked
        return (Resource<T>) allFileResources.computeIfAbsent(
                path, (p) -> new Resource<>(loader, p)
        );
    }

    public interface FileLoader<T> extends Serializable {
        T apply(Path path) throws IOException;
    }

    public static class Path {
        String path;

        public Path(String path) {
            this.path = path;
        }

        public Path resolve(String... relative) {
            StringBuilder pathBuilder = new StringBuilder(path);
            for (String s : relative) {
                pathBuilder.append("/");
                pathBuilder.append(s);
            }
            return new Path(pathBuilder.toString());
        }

        public Path resolve(Path relative) {
            String newPath = this.path + "/" + relative.path;
            return new Path(newPath);
        }

        public static Path get(String... elements) {
            StringBuilder pathBuilder = new StringBuilder(elements[0]);
            for (int i = 1; i < elements.length; i++) {
                pathBuilder.append("/");
                pathBuilder.append(elements[i]);
            }
            return new Path(pathBuilder.toString());
        }

        /**
         * creates a file input stream of the resource indicated by this path.
         * Intended for use as part of a {@link FileLoader}
         *
         * @return this resource as input stream
         */
        public InputStream asStream() throws AssetException {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
            if (stream == null) {
                throw new AssetException("Could not find resource " + path);
            }
            return stream;
        }

        public String asText() throws IOException, AssetException {
            String result;
            try (InputStream in = this.asStream();
                    Scanner scanner = new Scanner(in, StandardCharsets.UTF_8)) {
                result = scanner.useDelimiter("\\A").next();
            }
            return result;
        }

        @Override
        public String toString() {
            return path;
        }
    }
}
