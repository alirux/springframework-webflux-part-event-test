package partevent.controller;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Adds the content-length header to the part
 */
@Slf4j
public class MultipartExchangeFilterFunction implements ExchangeFilterFunction {

    @Override
    @Nonnull
    public Mono<ClientResponse> filter(@Nonnull ClientRequest request, @Nonnull ExchangeFunction next) {
        if (MediaType.MULTIPART_FORM_DATA.includes(request.headers().getContentType())
                && (request.method() == HttpMethod.PUT || request.method() == HttpMethod.POST)) {
            return next.exchange(
                    ClientRequest.from(request)
                            .body((outputMessage, context) -> request.body().insert(new BufferingDecorator(outputMessage), context))
                            .build()
            );
        } else {
            return next.exchange(request);
        }
    }

    private static final class BufferingDecorator extends ClientHttpRequestDecorator {

        private BufferingDecorator(ClientHttpRequest delegate) {
            super(delegate);
        }

        @Override
        @Nonnull
        public Mono<Void> writeWith(@Nonnull Publisher<? extends DataBuffer> body) {
            return DataBufferUtils.join(body).flatMap(buffer -> {
                int readableByteCount = buffer.readableByteCount();
                log.error("Buffer length for content-type:{}", readableByteCount);
				getHeaders().setContentLength(readableByteCount);
                return super.writeWith(Mono.just(buffer));
            });
        }

    }

}

