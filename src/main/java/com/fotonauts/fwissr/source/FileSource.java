package com.fotonauts.fwissr.source;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fotonauts.fwissr.Fwissr;
import com.fotonauts.fwissr.FwissrRuntimeException;
import com.fotonauts.fwissr.SmarterMap;

/**
 * Actual implementation of a Source pulling its configuration content from a json or a yaml file, or
 * from a directory containing json and yaml file.
 *
 * <p>If a file basename is "fwissr", its content will be contributed at the top level. 
 * 
 * <p>In the other case, it will be mapped to an inner location on the document. For instance if the filename is
 * "foo.bar.json" and contains { "baz" : 42 }, it will contribute 42 at the /foo/bar/baz location. 
 * 
 * <p>As any other source, setting the option "top_level" to true will force all contributions to top level.
 * 
 * @author kali
 *
 */
public class FileSource extends Source {

    public final static Set<String> TOP_LEVEL_CONF_FILES = Collections.unmodifiableSet(Collections.singleton("fwissr"));

    private String path;

    /**
     * Constructor.
     * 
     * @param filename the filename (can be a directory)
     * @param options only top_level make sense
     */
    public FileSource(String filename, SmarterMap options) {
        super(options);
        if (!new File(filename).exists())
            throw new FwissrRuntimeException("File not found: " + filename);
        path = filename;
    }

    /**
     * Construct a FileSource from settings.
     * 
     * @param settings must contain "filepath", can contain "top_level".
     * @return the new Source
     */
    public static Source fromSettings(SmarterMap settings) {
        SmarterMap options = settings.clone();
        options.remove("filepath");
        options.freeze();
        return fromPath((String) settings.get("filepath"), options);
    }

    /**
     * Construct a FileSource from a file path.
     * 
     * @param filename the file name.
     * @return the new Source
     */
    public static FileSource fromPath(String filename) {
        return fromPath(filename, new SmarterMap());
    }

    /**
     * Construct a FileSource from a file path and ancillary options.
     * 
     * @param filename the file name
     * @param options the source options
     * @return the new Source
     */
    public static FileSource fromPath(String filename, SmarterMap options) {
        if (StringUtils.isBlank(filename))
            throw new FwissrRuntimeException("Unexpected file source path: " + filename);
        return new FileSource(filename, options);
    }

    /**
     * Will pull configuration payload from the file (or the files) and aggregated them.
     * 
     * This method should not be accessed by "client" code: the registry handles caching and
     * refreshing on top of it.
     *  
     */
    @Override
    public SmarterMap fetchConf() {
        SmarterMap result = new SmarterMap();

        List<File> files = new LinkedList<>();
        File conf = new File(path);
        if (conf.isDirectory()) {
            for (File f : conf.listFiles()) {
                if (f.getName().endsWith(".json") || f.getName().endsWith(".yaml") || f.getName().endsWith(".yml")) {
                    files.add(f);
                }
            }
        } else {
            files.add(conf);
        }

        for (File f : files) {
            if (f.isFile()) {
                String confFileName = f.getName().replaceAll("\\.[^.]*$", "");
                SmarterMap c = Fwissr.parseConfFile(f);
                mergeConf(result, c, confFileName.split("\\."), TOP_LEVEL_CONF_FILES.contains(confFileName));
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return "Configuration file source: " + path;
    }

    /**
     * Access the configuration path.
     * 
     * @return the configuration path
     */
    public String getPath() {
        return path;
    }
}
