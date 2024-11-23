# Test of redis client 

This repository demonstrates how to reproduce problem with threads utilization with redisson

## Getting Started

### Prerequisites

Start single node redis with docker:

```bash
docker run --name redis-single-node -p 6379:6379 -d redis:latest
```

Build jar:

```bash
gradle shadowJar

```

### Testing

Use `taskset` to assign process to single cpu-core and pass `-XX:ActiveProcessorCount=1` to set jvm active cores to 1.

1. Redisson with 2 io threads and 200000 requests:

```bash
taskset --cpu-list 0 java -XX:+UseG1GC -Xms4096m -Xmx4096m -verbose:gc -XX:ActiveProcessorCount=1 -jar ./build/libs/redis-client-performance-1.0-SNAPSHOT-all.jar --provider redisson --threads 2 --request-pairs 100000
```

Output:
```
[0.004s][info][gc] Using G1
Arguments: redis = redis://localhost:6379, threads = 2, provider = redisson, request-pairs = 100000, delay-ms = 0
SLF4J(W): No SLF4J providers were found.
SLF4J(W): Defaulting to no-operation (NOP) logger implementation
SLF4J(W): See https://www.slf4j.org/codes.html#noProviders for further details.
Sleep start
Start test
[7.036s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 209M->76M(4096M) 296.854ms
[10.173s][info][gc] GC(1) Pause Young (Normal) (G1 Evacuation Pause) 254M->192M(4096M) 519.639ms
[13.149s][info][gc] GC(2) Pause Young (Normal) (G1 Evacuation Pause) 370M->297M(4096M) 405.142ms
[16.035s][info][gc] GC(3) Pause Young (Normal) (G1 Evacuation Pause) 475M->395M(4096M) 354.083ms
[18.789s][info][gc] GC(4) Pause Young (Normal) (G1 Evacuation Pause) 573M->494M(4096M) 398.823ms
[21.474s][info][gc] GC(5) Pause Young (Normal) (G1 Evacuation Pause) 672M->592M(4096M) 356.056ms
[24.364s][info][gc] GC(6) Pause Young (Normal) (G1 Evacuation Pause) 770M->691M(4096M) 513.800ms
[27.356s][info][gc] GC(7) Pause Young (Normal) (G1 Evacuation Pause) 869M->790M(4096M) 396.242ms
[30.009s][info][gc] GC(8) Pause Young (Normal) (G1 Evacuation Pause) 968M->888M(4096M) 435.366ms
[32.549s][info][gc] GC(9) Pause Young (Normal) (G1 Evacuation Pause) 1066M->986M(4096M) 405.459ms
[34.882s][info][gc] GC(10) Pause Young (Normal) (G1 Evacuation Pause) 1164M->1085M(4096M) 415.205ms
[37.356s][info][gc] GC(11) Pause Young (Normal) (G1 Evacuation Pause) 1263M->1183M(4096M) 572.855ms
[39.753s][info][gc] GC(12) Pause Young (Normal) (G1 Evacuation Pause) 1361M->1280M(4096M) 356.236ms
[41.972s][info][gc] GC(13) Pause Young (Normal) (G1 Evacuation Pause) 1458M->1368M(4096M) 303.213ms
[44.469s][info][gc] GC(14) Pause Young (Normal) (G1 Evacuation Pause) 1546M->1421M(4096M) 352.093ms
success = 77149 , errors = 22851
```

2. Redisson with 16 io threads and 200000 requests:

```bash
taskset --cpu-list 0 java -XX:+UseG1GC -Xms4096m -Xmx4096m -verbose:gc -XX:ActiveProcessorCount=1 -jar ./build/libs/redis-client-performance-1.0-SNAPSHOT-all.jar --provider redisson --threads 16 --request-pairs 100000
```

Output:
```
[0.004s][info][gc] Using G1
Arguments: redis = redis://localhost:6379, threads = 16, provider = redisson, request-pairs = 100000, delay-ms = 0
SLF4J(W): No SLF4J providers were found.
SLF4J(W): Defaulting to no-operation (NOP) logger implementation
SLF4J(W): See https://www.slf4j.org/codes.html#noProviders for further details.
Sleep start
Start test
[18.907s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 209M->9M(4096M) 28.048ms
[32.362s][info][gc] GC(1) Pause Young (Normal) (G1 Evacuation Pause) 205M->11M(4096M) 27.948ms
success = 100000 , errors = 0
```

3. Lettuce with 2 io threads and 200000 requests:

```bash
taskset --cpu-list 0 java -XX:+UseG1GC -Xms4096m -Xmx4096m -verbose:gc -XX:ActiveProcessorCount=1 -jar ./build/libs/redis-client-performance-1.0-SNAPSHOT-all.jar --provider lettuce --threads 2 --request-pairs 100000
```

Output:
```
[0.004s][info][gc] Using G1
Arguments: redis = redis://localhost:6379, threads = 2, provider = lettuce, request-pairs = 100000, delay-ms = 0
SLF4J(W): No SLF4J providers were found.
SLF4J(W): Defaulting to no-operation (NOP) logger implementation
SLF4J(W): See https://www.slf4j.org/codes.html#noProviders for further details.
Sleep start
Start test
success = 100000 , errors = 0
[12.650s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 209M->7M(4096M) 28.312ms
```

4. Lettuce with 16 io threads and 200000 requests:

```bash
taskset --cpu-list 0 java -XX:+UseG1GC -Xms4096m -Xmx4096m -verbose:gc -XX:ActiveProcessorCount=1 -jar ./build/libs/redis-client-performance-1.0-SNAPSHOT-all.jar --provider lettuce --threads 16 --request-pairs 100000
```

Output:
```
[0.003s][info][gc] Using G1
Arguments: redis = redis://localhost:6379, threads = 16, provider = lettuce, request-pairs = 100000, delay-ms = 0
SLF4J(W): No SLF4J providers were found.
SLF4J(W): Defaulting to no-operation (NOP) logger implementation
SLF4J(W): See https://www.slf4j.org/codes.html#noProviders for further details.
Sleep start
Start test
success = 100000 , errors = 0
[12.785s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) (Evacuation Failure: Pinned) 209M->13M(4096M) 46.482ms
```