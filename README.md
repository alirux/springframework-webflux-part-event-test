# springframework-webflux-part-event-test

Test the WebFlux [multipart support in a streaming fashion](https://docs.spring.io/spring-framework/reference/web/webflux/controller/ann-methods/multipart-forms.html).

See [this issue in spring framework](https://github.com/spring-projects/spring-framework/issues/35245)

And/or see [this question on stack overflow](https://stackoverflow.com/questions/79700691/uploading-files-using-spring-partevent-and-webflux-gives-decodingexception-coul)

## My problem

It appears that the bytes received as PartEvent.content are duplicated:

```
142200
142201
142202
142203
142204
1422POST /stream HTTP/1.1
accept-encoding: gzip
user-agent: ReactorNetty/1.2.8
host: localhost:8080
accept: */*
WebTestClient-Request-Id: 1
Content-Type: multipart/form-data;boundary=LiITzs9M2zSJ_tpZo_PV2EcdVMb_OMM
Content-Length: 1812347
1
2
3
4
5
6
7
8
9
```

The bytes start again from the beginning in the middle of the upload, duplicating the part headers and the boundary. In this way, `pe.isLast()` is wrongly true multiple times in the same http part.

And this happens at random points of the file, sometimes at line 142204 like the example here, sometimes at a different line.

With curl or with the JUnit test is the same.

```
curl -F file1=@src/test/resources/numbers_1.log --trace-ascii upload.log http://localhost:8080/stream
```

I've been debugging Spring class `org.springframework.http.codec.multipart.MultipartParser` but I don't think it contains any bugs.

A problem with Netty? It could be, but it would be very strange.

In the past, I encountered antivirus software (installed on a server virtual machine) that was duplicating client requests.

It could be something similar in this case? Indeed, my computer is full of security/privacy software.

Some other environment problems?

I need to test the application with different machines, JVM versions, etc.