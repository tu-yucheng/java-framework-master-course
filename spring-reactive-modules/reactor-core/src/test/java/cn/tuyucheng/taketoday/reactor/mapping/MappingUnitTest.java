package cn.tuyucheng.taketoday.reactor.mapping;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class MappingUnitTest {
    @Test
    void givenInputStream_whenCallingTheMapOperator_thenItemsAreTransformed() {
        Function<String, String> mapper = String::toUpperCase;
        Flux<String> inFlux = Flux.just("tuyucheng", ".", "com");
        Flux<String> outFlux = inFlux.map(mapper);

        StepVerifier.create(outFlux)
                .expectNext("TUYUCHENG", ".", "COM")
                .expectComplete()
                .verify();
    }

    @Test
    void givenInputStream_whenCallingTheFlatMapOperator_thenItemsAreFlatten() {
        Function<String, Publisher<String>> mapper = s -> Flux.just(s.toUpperCase().split(""));
        Flux<String> inFlux = Flux.just("tuyucheng", ".", "com");
        Flux<String> outFlux = inFlux.flatMap(mapper);

        List<String> output = new ArrayList<>();
        outFlux.subscribe(output::add);
        assertThat(output).containsExactlyInAnyOrder("T", "U", "Y", "U", "C", "H", "E", "N", "G", ".", "C", "O", "M");
    }
}