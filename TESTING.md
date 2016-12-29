# Testing
While we haven't worked too much on testing in the past, we'd like to change that. Below is a simple set of rules we try
to follow when making tests.

## Directory structure
When creating tests for a class it should be placed in the '/src/test/java/' folder within it's correct package name
structure.

For instance a test for 'com.atlauncher.SomeClass' would go in '/src/test/java/com/atlauncher/'.

## Filenames of tests
Tests should be named after their class name with 'Test' appended to it in the correct directory structure explained
above.

For instance a test for 'SomeClass' would be 'SomeClassTest'.

## Writing test classes
When writing a test class you're free to do as you please.

When testing a classes methods we tend to stick to a method name of 'testMethodName' for testing a method in the class
of 'methodName'.

When testing something that isn't a classes method (such as an overall test of the class) we use helpful method names
which explain what the test does. For example 'testsThatADoesBWhenCDoesD'.

The best example for writing up test classes is to take a look at something existing and going from there.

## Testing with files
If you need to test with files, then you can add the below to the top of your test class to get access to a temporary
folder to work in.

```java
@Rule
public TemporaryFolder temporaryFolder = new TemporaryFolder();

private Path testStorage;

@Before
public void setUp() throws Exception {
    testStorage = temporaryFolder.newFolder("ATLauncherTests").toPath();
}
```

## Is this necessary?
While this isn't necessary, and we won't deny pull requests simply because it's not tested, we generally appreciate any
large changes or any addition of new classes to have tests in order to help us know that everything is running fine.

If you're not good at writing tests or don't know how (hey we're like that too) then feel free to just not do it,
someone might do it for you at some point.