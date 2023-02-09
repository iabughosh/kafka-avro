package org.ibra.movie;

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import org.acme.kafka.quarkus.Movie;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.*;
import org.jboss.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;

@Path("/movies")
public class MovieResource {
    private static final Logger LOGGER = Logger.getLogger(MovieResource.class);

    @Channel("movies")
    @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 500)
    Emitter<Movie> emitter;

    @POST
    public Response enqueueMovie(Movie movie) {
        LOGGER.infof("Sending movie %s to Kafka", movie.getTitle());
        emitter.send(Message.of(movie)
                .withMetadata(Metadata.of(
                        OutgoingKafkaRecordMetadata.builder()
                                .withHeaders(new RecordHeaders()
                                  .add("JWT", "My JWT Token".getBytes(StandardCharsets.UTF_8)))
                                .build()
                )));
        return Response.accepted().build();
    }

}