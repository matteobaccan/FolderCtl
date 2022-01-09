/*
 * Matteo Baccan
 * http://www.baccan.it
 *
 * Distributed under the GPL v3 software license, see the accompanying
 * file LICENSE or http://www.gnu.org/licenses/gpl.html.
 *
 */
package it.baccan.folderctl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Matteo
 */
@Slf4j
public class FolderCtl {

    private String folder = "";
    private boolean update = false;
    private boolean validate = false;
    private List<String> exclude = new ArrayList<>();

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        log.info("  ______    _     _            _____ _   _ ");
        log.info(" |  ____|  | |   | |          / ____| | | |");
        log.info(" | |__ ___ | | __| | ___ _ __| |    | |_| |");
        log.info(" |  __/ _ \\| |/ _` |/ _ \\ '__| |    | __| |");
        log.info(" | | | (_) | | (_| |  __/ |  | |____| |_| |");
        log.info(" |_|  \\___/|_|\\__,_|\\___|_|   \\_____|\\__|_|");
        log.info("");

        final Options options = new Options();
        options.addOption(new Option("help", "print this message"));
        options.addOption(new Option("folder", true, "folder to scan"));
        options.addOption(new Option("update", "update folder structure"));
        options.addOption(new Option("validate", "validate folder structure"));
        options.addOption(new Option("exclude", true, "exclude files to check"));

        FolderCtl folderCtl = new FolderCtl();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            for (Option opt : cmd.getOptions()) {
                switch (opt.getOpt()) {
                    case "folder":
                        folderCtl.folder = opt.getValue();
                        break;
                    case "update":
                        folderCtl.update = true;
                        break;
                    case "validate":
                        folderCtl.validate = true;
                        break;
                    case "exclude":
                        folderCtl.exclude.add(opt.getValue());
                        break;
                    default:
                        break;
                }
            }

            if (cmd.hasOption("help") || (!folderCtl.update && !folderCtl.validate)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("FolderCtl", options);
                System.exit(0);
            }

            folderCtl.run();
        } catch (ParseException | FolderCtlException exp) {
            // oops, something went wrong
            log.error("Parsing failed.  Reason: [{}]", exp.getMessage());
        }
    }

    private void run() throws FolderCtlException {
        log.info("Start scan");

        long runSeed = System.currentTimeMillis();
        H2Storage h2Storage = new H2Storage(runSeed);

        log.info("Folder [{}]", folder);

        if (!new File(folder).exists()) {
            throw new FolderCtlException("Folder non found");
        }

        AtomicLong fileInError = new AtomicLong(0);
        AtomicLong fileUpdated = new AtomicLong(0);
        AtomicLong fileValidated = new AtomicLong(0);
        try (Stream<Path> walk = Files.walk(Paths.get(folder))) {

            // File List
            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString())
                    .collect(Collectors.toList());

            // Store data
            result.forEach(file -> {
                File file2check = new File(file);
                if (!excludeFile(file)) {
                    if (update) {
                        h2Storage.updateFile(file, file2check.length(), file2check.lastModified());
                        fileUpdated.incrementAndGet();
                    } else if (validate) {
                        fileValidated.incrementAndGet();
                        if (!h2Storage.checkFile(file, file2check.length(), file2check.lastModified())) {
                            fileInError.incrementAndGet();
                        }
                    }
                }
            });

        } catch (IOException e) {
            throw new FolderCtlException(e.getMessage());
        }
        if (update) {
            h2Storage.cleanOldScan(folder);
        }

        log.info("End of scan");

        if (update) {
            log.info("[{}] files updated", fileUpdated.intValue());
        } else if (validate) {
            if (fileInError.intValue() > 0) {
                log.error("Some errors found [{}]", fileInError.intValue());
                System.exit(1);
            } else {
                log.info("[{}] files validated without errors", fileValidated.intValue());
            }
        }

    }

    private boolean excludeFile(final String file) {
        AtomicBoolean ret = new AtomicBoolean(false);
        exclude.forEach(pattern -> {
            if (!ret.get()) {
                if (Wildcard.match(file, pattern)) {
                    log.info("Exclude file [{}]", file);
                    ret.set(true);
                }
            }
        });
        return ret.get();
    }

}
