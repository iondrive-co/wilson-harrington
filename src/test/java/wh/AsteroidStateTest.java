package wh;

import jaid.collection.DoublesVector;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class AsteroidStateTest {

    @Test
    void wilsonHarrington_shouldCreateCorrectAsteroid() {
        final AsteroidState asteroid = AsteroidState.wilsonHarrington();
        
        assertThat(asteroid).isNotNull();
        assertThat(asteroid.getDistanceFromSun()).isGreaterThan(0);
        assertThat(asteroid.getPosition()).isNotNull();
    }
    
    @Test
    void updateDistanceFromSun_shouldChangeDistance() {
        final AsteroidState asteroid = AsteroidState.wilsonHarrington();
        final double initialDistance = asteroid.getDistanceFromSun();
        final DoublesVector initialPosition = asteroid.getPosition();
        
        asteroid.updateDistanceFromSun(30, 365);
        
        assertThat(asteroid.getDistanceFromSun()).isNotEqualTo(initialDistance);
        assertThat(asteroid.getPosition()).isNotEqualTo(initialPosition);
    }
    
    @Test
    void updateDistanceFromSun_shouldFollowOrbitalPattern() {
        final AsteroidState asteroid = AsteroidState.wilsonHarrington();
        final double day0Distance = asteroid.getDistanceFromSun();
        
        asteroid.updateDistanceFromSun(183, 365); // Half orbit
        final double halfOrbitDistance = asteroid.getDistanceFromSun();
        
        asteroid.updateDistanceFromSun(365, 365); // Full orbit
        final double fullOrbitDistance = asteroid.getDistanceFromSun();
        
        assertThat(fullOrbitDistance).isCloseTo(day0Distance, within(0.01));
        assertThat(halfOrbitDistance).isNotCloseTo(day0Distance, within(0.5));
    }
    
    @Test
    void storedWaterKgs_shouldBeZeroInitially() {
        final AsteroidState asteroid = AsteroidState.wilsonHarrington();
        
        assertThat(asteroid.storedWaterKgs).isEqualTo(0);
    }
}
