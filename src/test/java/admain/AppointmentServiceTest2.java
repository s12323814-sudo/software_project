package admain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppointmentServiceTest2 {

    @Test
    void testDefaultConstructor() {
        AppointmentService svc = new AppointmentService();
        assertNotNull(svc);
    }
}
