package wh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathsUtilTest {

    @ParameterizedTest
    @CsvSource({
        "0.0, 0.0, 0.0",
        "0.0, 0.5, 0.0",
        "3.14159, 0.0, 3.14159",
        "3.14159, 0.5, 3.14159"
    })
    void solveKepler_shouldReturnCorrectEccentricAnomaly(double meanAnomaly, double eccentricity, double expected) {
        assertEquals(expected, MathsUtil.solveKepler(meanAnomaly, eccentricity), 1e-5);
    }

    @Test
    void solveKepler_withNonZeroEccentricity_shouldConverge() {
        double result = MathsUtil.solveKepler(Math.PI / 2, 0.5);
        assertTrue(result > 0);
    }
    
    @Test
    void calculateTrueAnomaly_shouldReturnCorrectValues() {
        // For circular orbit (e=0), true anomaly equals eccentric anomaly
        assertThat(MathsUtil.calculateTrueAnomaly(Math.PI/2, 0.0))
            .isCloseTo(Math.PI/2, within(1e-10));
            
        // For elliptical orbit, true anomaly should be greater at quadrature
        assertThat(MathsUtil.calculateTrueAnomaly(Math.PI/2, 0.5))
            .isGreaterThan(Math.PI/2);
    }
}
