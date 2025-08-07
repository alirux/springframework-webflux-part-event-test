package partevent.controller;

import static org.springframework.http.ResponseEntity.ok;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

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
		File outFile = new File("uploaded_file.bin");
		outFile.delete();
		
		var result = allPartsEvents.map(pe -> {
			if (pe instanceof FilePartEvent fileEvent) {
				DataBuffer content = fileEvent.content();
				acc.byteCount += content.readableByteCount();
				acc.partCount++;
				acc.lastIsTrueCount = pe.isLast() ? acc.lastIsTrueCount + 1 : acc.lastIsTrueCount;
				log.info("Part event name:{} last:{} length:{} byteCount:{} diff:{} acc:{} content:{} partEvent:{}", pe.name(), pe.isLast(), length, acc.byteCount, length - acc.byteCount, acc, content, pe);
				if (pe.isLast()) {
					log.info("Last buffer:\n{}", content.toString(Charset.defaultCharset()));
				}
				appendToFile(outFile, content);
				return content;
			}
			throw new RuntimeException("Unexpected event: " + pe);
		})
		.map(t -> ""+t.readableByteCount() + " - ")
		;
		
		return ok().body(result);
	}

	private void appendToFile(File outFile, DataBuffer content) {
		try (FileWriter fw = new FileWriter(outFile, true)) {
			fw.write(content.toString(Charset.defaultCharset()));
		} catch (IOException e) {
			log.error("Saving file:{}", outFile, e);
		}
	}
	
	@PostMapping("/stream2")
	public ResponseEntity<Mono<Accumulator>> handlePartsEvents2(@RequestBody Flux<PartEvent> allPartsEvents, @RequestHeader HttpHeaders headers) {
		
		long length = headers.getContentLength();
		
		var result = allPartsEvents
		.reduce(new Accumulator(), (acc, pe) -> {
			if (pe instanceof FilePartEvent fileEvent) {
				DataBuffer content = fileEvent.content();
				acc.byteCount += content.readableByteCount();
				acc.partCount++;
				log.info("Part event name:{} last:{} diff:{} acc:{} content:{} partEvent:{}", pe.name(), pe.isLast(), length - acc.byteCount, acc, content, pe);
				return acc;
			}
			throw new RuntimeException("Unexpected event: " + pe);
		})
		;
		
		return ok().body(result);
	}
	
}