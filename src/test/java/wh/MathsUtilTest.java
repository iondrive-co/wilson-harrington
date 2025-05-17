package wh;

import jaid.collection.DoublesVector;
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
        final double result = MathsUtil.solveKepler(Math.PI / 2, 0.5);
        assertTrue(result > 0);
    }
    
    @Test
    void calculateTrueAnomaly_shouldReturnCorrectValues() {
        // For circular orbit (e=0), true anomaly equals eccentric anomaly
        final double trueAnomaly1 = MathsUtil.calculateTrueAnomaly(Math.PI/2, 0.0);
        assertThat(trueAnomaly1).isCloseTo(Math.PI/2, within(1e-10));
        
        // For elliptical orbit, true anomaly should be greater at quadrature
        final double trueAnomaly2 = MathsUtil.calculateTrueAnomaly(Math.PI/2, 0.5);
        assertThat(trueAnomaly2).isGreaterThan(Math.PI/2);
    }
    
    @Test
    void calculateOrbitalState_shouldReturnNonNullVector() {
        final DoublesVector position = MathsUtil.calculateOrbitalState(
            1.0,    // semi-major axis
            0.0,    // eccentricity (circular)
            0.0,    // inclination
            0.0,    // argument of perihelion
            0.0,    // ascending node
            0.0     // mean anomaly
        );
        
        assertThat(position).isNotNull();
        assertThat(position.magnitude()).isCloseTo(1.0, within(1e-10));
    }
    
    @Test
    void calculateOrbitalState_withEccentricity_shouldVaryDistance() {
        final double semiMajorAxis = 1.0;
        final double eccentricity = 0.5;
        
        // At perihelion (mean anomaly = 0)
        final DoublesVector positionAtPerihelion = MathsUtil.calculateOrbitalState(
            semiMajorAxis, eccentricity, 0.0, 0.0, 0.0, 0.0
        );
        
        // At aphelion (mean anomaly = π)
        final DoublesVector positionAtAphelion = MathsUtil.calculateOrbitalState(
            semiMajorAxis, eccentricity, 0.0, 0.0, 0.0, Math.PI
        );
        
        assertThat(positionAtPerihelion.magnitude()).isLessThan(semiMajorAxis);
        assertThat(positionAtAphelion.magnitude()).isGreaterThan(semiMajorAxis);
    }
}
