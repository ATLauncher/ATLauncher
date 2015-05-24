ATLauncher Coding Standards & Styling Guidelines
====================================

So if you wish to submit any Pull Requests, please follow these coding standards. Styling guidelines are just how I like to have things styled, mainly relating to doc blocks and comments.

### Coding Standards

+ Please keep all line lengths to 120 characters and use 4 spaces rather than tab characters.
+ Please keep all variables at the top of the class.
+ Please keep all inner classes at the bottom.
+ Please don't use star imports.
+ Please mark all classes that are to be de/serialized with Gson with the @Json annotation for other developers.
+ Please use the IntelliJ-Coding-Style.jar for the project (if using IntelliJ) in order to keep all formatting consistent.
+ Please update the CHANGELOG.md file when fixing/adding something so it's easier to keep track of than git commits. Feel free to add in a 'by MyUsername' to the end of the changes you've made.
+ Please don't do large commits. My preference is a single commit for a single fix/addition rather than bundled up commits.
+ Please use final where possible.
+ Please make sure all files contain the GNU GPLv3 license at the top of the file.

### Styling Guidelines

+ Make sure all doc block information has a full stop at the end.
+ Make sure all doc block @ elements don't have a full stop at the end.
+ Make sure all comments not in doc blocks end in a full stop.
+ Make sure there is a blank line between any main doc block information and any @elements.

#### Example
    // Some comment. Which ends in a full stop.

    /**
     * Where the magic happens. Notice I end in a full stop.
     *
     * @param arguments all the arguments passed in from the command line, notice I don't end in a full stop
     */
