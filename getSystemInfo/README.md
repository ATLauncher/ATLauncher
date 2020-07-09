# getSystemInfo

This small go program grabs information about the host system for use within ATLauncher.

## Building

To build, first make sure you have Docker installed on your machine.

Then you can simply run the below command:

```sh
./generateBinary
```

This will spit out the built files into the `dist` directory.

### Note for Windows

Windows cannot build arm binaries, so arm binaries will fail when using the `generateBinary` script on Windows.
