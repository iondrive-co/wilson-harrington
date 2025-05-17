package wh;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AsteroidStateTest {

    @Test
    void wilsonHarrington_shouldCreateCorrectAsteroid() {
        final AsteroidState asteroid = AsteroidState.wilsonHarrington();
        
        assertThat(asteroid).isNotNull();
        assertThat(asteroid.getDistanceFromSun()).isGreaterThan(0);
    }
    
    @Test
    void updateDistanceFromSun_shouldChangeDistance() {
        final AsteroidState asteroid = AsteroidState.wilsonHarrington();
        final double initialDistance = asteroid.getDistanceFromSun();
        
        asteroid.updateDistanceFromSun(30, 365);
        
        assertThat(asteroid.getDistanceFromSun()).isNotEqualTo(initialDistance);
    }
}
