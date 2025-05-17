package wh;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DoublesVectorTest {

    @Test
    void testBasicArrayOperations() {
        final double[] values = {1.0, 2.0, 3.0};
        
        assertThat(values[0]).isEqualTo(1.0);
        assertThat(values[1]).isEqualTo(2.0);
        assertThat(values[2]).isEqualTo(3.0);
        assertThat(values.length).isEqualTo(3);
    }
}
