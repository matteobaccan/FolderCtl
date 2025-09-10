<pre>
  ______    _     _            _____ _   _
 |  ____|  | |   | |          / ____| | | |
 | |__ ___ | | __| | ___ _ __| |    | |_| |
 |  __/ _ \| |/ _` |/ _ \ '__| |    | __| |
 | | | (_) | | (_| |  __/ |  | |____| |_| |
 |_|  \___/|_|\__,_|\___|_|   \_____|\__|_|
</pre>
 
![Meterian vulnerability scan workflow](https://github.com/matteobaccan/FolderCtl/workflows/Meterian%20vulnerability%20scan%20workflow/badge.svg)
![Java CI with Maven](https://github.com/matteobaccan/FolderCtl/workflows/Java%20CI%20with%20Maven/badge.svg)
![CodeQL](https://github.com/matteobaccan/FolderCtl/workflows/CodeQL/badge.svg)
[![StyleCI](https://github.styleci.io/repos/297964112/shield?branch=master)](https://github.styleci.io/repos/297964112?branch=master)
[![GraalVM Build](https://github.com/matteobaccan/FolderCtl/actions/workflows/graalvm.yml/badge.svg)](https://github.com/matteobaccan/FolderCtl/actions/workflows/graalvm.yml)

# FolderCtl
Folder modification analyzer

### Building from Source

To build the native executable, you need to have GraalVM and Maven installed. You can then run the following command from the root of the project:

```bash
mvn package -DskipNativeVersion=false
```

The executable will be created in the `target/` directory.

The project can be built on Windows, Linux, and macOS.

### Help / Commands List

| Command  | Description | Arguments |
| ------------- | ------------- | ------------- |
| `help`  | Print the help message  | |
| `folder`  | The folder to scan  | `folderctl --folder /path/to/folder` |
| `update`  | Update the folder structure snapshot  | `folderctl --folder /path/to/folder --update` |
| `validate`  | Validate the folder against the snapshot  | `folderctl --folder /path/to/folder --validate` |
| `exclude`  | Exclude files from the scan (can be used multiple times)  | `folderctl --folder /path/to/folder --validate --exclude *.log` |
