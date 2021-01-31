# ATLauncher

![Application](https://github.com/ATLauncher/ATLauncher/workflows/Application/badge.svg?branch=master)
[![Discord](https://discordapp.com/api/guilds/117047818136322057/embed.png?style=shield)](https://atl.pw/discord)

## What is it

ATLauncher is a launcher for Minecraft which integrates multiple different modpacks to allow you to download and install
modpacks easily and quickly.

## Links

- [ATLauncher Website](https://atlauncher.com)
- [ATLauncher Discord](https://atl.pw/discord)
- [ATLauncher Facebook](https://www.facebook.com/ATLauncher)
- [ATLauncher Reddit](https://www.reddit.com/r/ATLauncher)
- [ATLauncher Twitter](https://twitter.com/ATLauncher)

## Contributing to ATLauncher

Take a look at [CONTRIBUTING.md](CONTRIBUTING.md)

## Testing

Please see the [TESTING.md](TESTING.md) file for information on how we write tests.

## Prerequisites

In order to build ATLauncher, you will need Java 8, 9, 10, 11 or 12. Java 8 is recommended since we compile to Java 8
compatability regardless.

Everything else that's needed for the project is provided by Gradle, and accessed using the Gradle wrapper which can be
invoked by using `./gradlew`.

## Building

To build this project, simply run:

```sh
./gradlew build
```

This will build the application and output the resulting files for Windows, Linux and OSX in the `dist` directory.

## Running in test

If you want to run the launcher while developing with it, you can use your IDE (if you have one) to do that for you.

Alternatively you can run:

```sh
./gradlew run --args="--debug --debug-level 3 --working-dir=testLauncher"
```

## Using VSCode

This project is setup to use [VSCode](https://code.visualstudio.com/) for development. You're free to use any other IDE
that you're accustomed to (if any), but by using VSCode, you get the benefit of predefined tasks and launch commands as
well as a list of extensions recommended for the project.

## Checking for dependency updates

To check for dependency updates with gradle, simply run:

```sh
./gradlew dependencyUpdates
```

This will print a report to the console about any dependencies which have updates.

## Updating license headers in all files

If you add new files, or update the `LICENSEHEADER` file, you can add that to all source files by running:

```sh
./gradlew updateLicenses
```

To check that they're all correct, you can run the below command:

```sh
./gradlew checkLicenses
```

This is run during the CI process, and will fail if the license is missing or not up to date, so make sure that you add
this to all new files you create.

## Create Custom Themes

ATLauncher supports custom themes. The process is fairly straight forward, but may require a lot of trial and error.

First you must create a `MyThemeName.java` in the `src/main/java/com/atlauncher/themes/` directory. Your theme should
extend one of the base ATLauncher themes depending on what you need:

- `Dark` is the default theme and is a dark theme. It's a good place to start with some defaults for new dark themes.
- `Light` is a light theme. It's a good place to start with some defaults for new light themes.
- `ATLauncherLaf` is a base class which every theme MUST at some point extend. It provides some defaults including our
  brand colours and some defaults. This shouldn't be extended from unless you need absolute power.

Once you've created your class (look at other themes in the directory for an idea on what you can do), you'll need to
create a properties file in the `src/main/resources/com/atlauncher/themes/` directory. This properties file is how you
setup and change UI elements. You should use the existing examples in that directory as examples.

Last step is to register the theme in the file `src/main/java/com/atlauncher/gui/tabs/settings/GeneralSettingsTab.java`.

Now you can open the launcher and then switch to your theme.

We use a library called [FlatLaf](https://github.com/JFormDesigner/FlatLaf) to provide theme support. There are some
good references listed below to see the default values for the themes and see what you can overwrite:

- <https://github.com/JFormDesigner/FlatLaf/blob/master/flatlaf-core/src/main/resources/com/formdev/flatlaf/FlatLaf.properties>
  - This file contains all the base properties for all themes
- <https://github.com/JFormDesigner/FlatLaf/blob/master/flatlaf-core/src/main/resources/com/formdev/flatlaf/FlatLightLaf.properties>
  - This file contains all the base properties for light themes
- <https://github.com/JFormDesigner/FlatLaf/blob/master/flatlaf-core/src/main/resources/com/formdev/flatlaf/FlatDarkLaf.properties>
  - This file contains all the base properties for dark themes

### IntelliJ theme.json Support

You can also take IntelliJ `theme.json` files and convert them to themes for ATLauncher. From within the `theme.json`
file, take the `UI` object and plug that into [this site](https://tools.fromdev.com/json-to-property-converter.html) to
convert it from JSON to properties format.

There are also special rules you need to apply as we currently do not support these `theme.json` files out of the box,
so you need to manually apply the [transformations](https://github.com/JFormDesigner/FlatLaf/blob/master/flatlaf-core/src/main/java/com/formdev/flatlaf/IntelliJTheme.java)
in order for the theme to work exactly right.

For an example, see the `DraculaContrast` theme which uses this method.

### Tools To Help Theme Development

To help with theme development, with the launcher running (not in the release version, only in development), you can
press `Ctrl + Shift + Alt + X` to bring up a tool to highlight UI components to see their properties. You can also press
`Ctrl + Shift + Alt + Y` to bring up a list of all the default properties in the UIManager. These values can be modified
in your `.properties` file.

## Plugging In Your Data

To get started with the code and plug in your own data, you need to edit the
`/src/main/java/com/atlauncher/constants/Constants.java` file.

By using this source code you don't get permissions to use our CDN/files/assets/modpacks. See the License section at the
bottom for more.

See below for explanations as to what each constant means.

### VERSION

This is a LauncherVersion object passed in the reserved, major, minor, revision ints for this
version of the launcher. See the 'Versioning System' section below.

### API_BASE_URL

This is a link to your server side API for processing of stats. This is optional and can be removed.
We do not give implementation code, this is your own doing.

### PASTE_CHECK_URL

This is a link to the url where an instance of [stikked](https://github.com/claudehohl/Stikked) is running
(For instance <https://www.mypaste.com>) this is how the launcher knows if the paste was successful by checking the
response from the API for the url of the software.

_Please note that the domain given above IS NOT REAL. You must install [stikked](https://github.com/claudehohl/Stikked)
on your own domain and reference it, the domain is only there as an example of what a valid value is._

### PASTE_API_URL

Same as above

### SERVERS

This is an array of
[Server](https://github.com/ATLauncher/ATLauncher/blob/master/src/main/java/com/atlauncher/data/Server.java)
type elements the launcher uses as a base to download files.

### LAUNCHER_NAME

This is the name of the launcher.

### How to make your data

To make the data the Launcher needs you will need to figure out your own server side way of doing that. You can create a
system to do it automatically or you can manually do it by just popping the files on the server. The best way to get the
file structure and contents is to examine the source code and the ATLauncher files it downloads.

## Versioning System

Starting with version 3.2.1.0 a new versioning system was put into place. It works off the following:

Reserved.Major.Minor.Revision.Stream

So for 3.2.1.0 the major number is 2 and minor number is 1 and revision number is 0. Reserved is used as a base, only
incremented on complete rewrites. The stream is optional.

Major should be incremented when large changes/features are made.

Minor should be incremented when small changes/features are made.

Revision should be incremented when there are no new features and only contains bug fixes for the previous minor.

Build is used for beta releases allowing you to have higher version numbers but force users to update when the real
release comes.

Stream is used to define if it's a "Release" or a "Beta". When not provided, it defaults to "Release".

### Updating The Version

The version can be updated in a single place in the `/src/main/resources/version` file.

The stream value doesn't need to be provided, but should only ever be "Beta". When a release is ready to go out, the
stream should be removed from the version so that everything will automatically release.

## Translating

ATLauncher is written for English speakers. All our translations are community run by those who take their time and
submit updates to the text in a different language.

If you wish to help translate ATLauncher, please visit our page on [Crowdin](https://crowdin.com/project/atlauncher) and
start translating.

### Updating the template file

If new strings are added to the launcher, the template file will need to be updated in order to take into account the
new strings.

In order to do this, run `./gradlew generatePots` which will scan the source files and create a
`build/gettext/translations.pot` file.

Note that out of the box, this will not generate in the correct format. The file should be opened with
[POEdit](https://poedit.net/), which will automatically fix the file, which then you can save to `translations.pot` in
the root directory.

This file can then be uploaded to Crowdin by ATLauncher staff to give access to the translators.

### Adding new languages from Crowdin

Once a language has been translated enough to add support to the launcher (or update support) there's a few steps we
need to take.

First grab the built project from Crowdin, and then grab out the translation to add/update. For this example, let's take
German.

Pop this file in the `scripts/processTranslations/in` directory and then run the `scripts/processTranslations.bat` or
`scripts/processTranslations.sh` file to fix them up and output them into the `scripts/processTranslations/out`
directory.

Now take the converted files from the `scripts/processTranslations/out` directory and put them in the
`src\main\resources\assets\lang` directory.

Now open `src\main\java\com\atlauncher\data\Language.java` and in the static block at the top, add in the language:

```java
// add in the languages we have support for
static {
    locales.add(Locale.ENGLISH);
    locales.add(new Locale("de", "DE"));
}
```

Now the launcher should have the option to change to the language/s.

## License

This work is licensed under the GNU General Public License v3.0. To view a copy of this license, visit
<http://www.gnu.org/licenses/gpl-3.0.txt>.

A simple way to keep in terms of the license is by forking this repository and leaving it open source under the same
license. We love free software, seeing people use our code and then not share the code, breaking the license, is
saddening. So please take a look at the license and respect what we're doing.

Also, while we cannot enforce this under the license, you cannot use our CDN/files/assets/modpacks on your own launcher.
Again we cannot enforce this under the license, but needless to say, we'd be very unhappy if you did that and really
would like to leave cease and desist letters as a last resort.
