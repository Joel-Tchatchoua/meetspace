package com.accenture.meetspace.room.units;

import com.accenture.meetspace.room.domain.exceptions.InvalidCapacityException;
import com.accenture.meetspace.room.domain.vo.Capacity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Capacity")
class CapacityTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 8, 200})
    @DisplayName("accepts a value between 1 and 200")
    void acceptsAValueBetween1And200(int value) {
        assertThat(new Capacity(value).value()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 201})
    @DisplayName("rejects a value outside 1 to 200")
    void rejectsAValueOutside1To200(int value) {
        assertThatThrownBy(() -> new Capacity(value))
                .isInstanceOf(InvalidCapacityException.class)
                .hasFieldOrPropertyWithValue("code", "INVALID_CAPACITY");
    }
}
