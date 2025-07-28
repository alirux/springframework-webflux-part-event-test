package partevent.controller;

import static org.springframework.http.ResponseEntity.ok;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePartEvent;
import org.springframework.http.codec.multipart.PartEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class UploadStream {

	@PostMapping("/stream")
	public ResponseEntity<Flux<String>> handlePartsEvents(@RequestBody Flux<PartEvent> allPartsEvents, @RequestHeader HttpHeaders headers) {
		
		Accumulator acc = new Accumulator();
		long length = headers.getContentLength();
		
		var result = allPartsEvents.map(pe -> {
			if (pe instanceof FilePartEvent fileEvent) {
				DataBuffer content = fileEvent.content();
				acc.byteCount += content.readableByteCount();
				acc.count++;
				acc.trueCount = pe.isLast() ? acc.trueCount + 1 : acc.trueCount;
				log.info("Part event name:{} last:{} diff:{} acc:{} content:{} partEvent:{}", pe.name(), pe.isLast(), length - acc.byteCount, acc, content, pe);
				return content;
			}
			throw new RuntimeException("Unexpected event: " + pe);
		})
		.map(t -> ""+t.readableByteCount() + " - ")
		;
		
		return ok().body(result);
	}
	
	@PostMapping("/stream2")
	public ResponseEntity<Mono<Accumulator>> handlePartsEvents2(@RequestBody Flux<PartEvent> allPartsEvents, @RequestHeader HttpHeaders headers) {
		
		long length = headers.getContentLength();
		
		var result = allPartsEvents
		.reduce(new Accumulator(), (acc, pe) -> {
			if (pe instanceof FilePartEvent fileEvent) {
				DataBuffer content = fileEvent.content();
				acc.byteCount += content.readableByteCount();
				acc.count++;
				log.info("Part event name:{} last:{} diff:{} acc:{} content:{} partEvent:{}", pe.name(), pe.isLast(), length - acc.byteCount, acc, content, pe);
				return acc;
			}
			throw new RuntimeException("Unexpected event: " + pe);
		})
		;
		
		return ok().body(result);
	}
	
}