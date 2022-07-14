package software.amazon.redshift.endpointaccess;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.atLeastOnce;

public abstract class AbstractStaticTest<T> {
    MockedStatic<T> mockedClass;

    abstract Class<T> getMockedClass();
    abstract MockedStatic.Verification getVerificationFunction();

    @BeforeEach
    void initializeMock() {
        // Initialize the mock
        mockedClass = Mockito.mockStatic(getMockedClass());
        // Ensure we are calling the real, not stubbed, method
        mockedClass.when(getVerificationFunction()).thenCallRealMethod();
    }

    @AfterEach
    void closeMock() {
        // Verify the static method we are testing was actually called
        try {
            mockedClass.verify(atLeastOnce(), getVerificationFunction());
        } finally {
            mockedClass.close();
        }
    }
}
