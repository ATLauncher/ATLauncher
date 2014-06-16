import com.atlauncher.reporter.GithubIssue;
import org.junit.Test;

public final class TestGithubIssue{
    @Test
    public void test(){
        GithubIssue issue = new GithubIssue("Test", "Test");
        System.out.println(issue);
    }
}