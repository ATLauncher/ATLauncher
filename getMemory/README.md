# getMemory

![getMemory](https://github.com/ATLauncher/ATLauncher/workflows/getMemory/badge.svg)

This small go program simply prints out the detected system ram in the system.

## Building

To build, first make sure you have Docker installed on your machine.

Then you can simply run the below command:

```sh
./generateBinary
```

This will spit out the built files into the `dist` directory.

### Note for Windows

Windows cannot build arm binaries, so arm binaries will fail when using the `generateBinary` script on Windows.
