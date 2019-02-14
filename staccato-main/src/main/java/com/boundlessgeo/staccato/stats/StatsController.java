package com.boundlessgeo.staccato.stats;

import com.boundlessgeo.staccato.dto.ItemStatisticsResponse;
import com.boundlessgeo.staccato.es.IndexAliasLookup;
import com.boundlessgeo.staccato.service.AggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controller implementation for returning aggregations on data.
 *
 * @author joshfix
 * Created on 11/29/17
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController implements StatsApi {

    private final AggregationService service;
    private final IndexAliasLookup indexAliasLookup;

    @Override
    public Flux<ItemStatisticsResponse> getStats() {
        return service.getStats(indexAliasLookup.getWriteAliases()).name("getStats");
    }

    @Override
    public Mono<ItemStatisticsResponse> getStats(@PathVariable("type") String type) {
        return service.getStats(type).name("getStats");
    }

}
