# ATLauncher coding standards & styling guidelines
If you wish to submit a pull request to ATLauncher then please take a look at the below sections about our coding and
styling standards before make said pull request.

## Coding standards
Coding standards are absolutely essential to having your pull request approved. While we may not close your pull
request if it doesn't follow these coding standards, we most likely will delay merging it until compliant.

- Please keep all line lengths to 120 characters.
- Please don't use tabs, use 4 spaced tabs.
- Please keep all variables at the top of the class.
- Please keep all inner classes at the bottom.
- Please don't use star imports.
- Please mark all classes that are to be de/serialized with Gson with the @Json annotation for other developers.
- Please update the CHANGELOG.md file when fixing/adding something so it's easier to keep track of than git commits.
Feel free to add in a 'by MyUsername' to the end of the changes you've made.
+ Please don't do large commits. My preference is a single commit for a single fix/addition rather than bundled up
commits.
+ Please use final where possible.
+ Please make sure all files contain the GNU GPLv3 license at the top of the file.

## Styling guidelines
Styling guidelines are just how we prefer to have things styled, mainly relating to doc blocks and comments. Not
following these guidelines will not get your pull requests denied as they're less important than our actual coding
standards listed above.

- Make sure all doc block information has a full stop at the end.
- Make sure all doc block @ elements don't have a full stop at the end.
- Make sure all comments not in doc blocks don't have a full stop at the end.
- Make sure there is a blank line between any main doc block information and any @elements.

### Example
    // Some comment. Which doesn't end in a full stop

    /**
     * Where the magic happens. Notice I end in a full stop.
     *
     * @param arguments all the arguments passed in from the command line, notice I don't end in a full stop
     */
