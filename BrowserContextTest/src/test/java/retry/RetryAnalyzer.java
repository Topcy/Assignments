package retry;

import config.PlaywrightConfig;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        //retry the failed test until the configured retry count is reached
        if (retryCount < PlaywrightConfig.RETRIES){
            retryCount++;
            System.out.println("Retrying test: " + result.getName() + " | Attempt: " + (retryCount + 1)
            );
            return true;
        }
        return false;
    }
}
