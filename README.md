# nubank-authorizer

Nubank's Authorizer Code Challenge.

## Installation

To install the project you will need either `Docker` or `leiningen`. Also, all the `Docker` comands that will be shown below will use `make`, if you don't want to install it, you can see what each one of them does on the [`Makefile`](https://github.com/otaviopace/nubank-authorizer/blob/master/Makefile) to run them directly.

### Docker

To build the image just run:

```shell
make build-app
```

### Leiningen

Since just `lein run` will do the trick to run the project, if you want prior dependency installation, just like the `Dockerfile` does, just do:

```shell
lein deps
```

## Usage

To run you just have to pass a file via stdin that contains JSON operations in each line. There are some examples on [`input_examples`](https://github.com/otaviopace/nubank-authorizer/tree/master/input_examples) folder.

### Docker

```shell
make run < input_examples/0_sample
```

The output should be:

```
{"account":{"activeCard":true,"availableLimit":100},"violations":[]}
{"account":{"activeCard":true,"availableLimit":80},"violations":[]}
{"account":{"activeCard":true,"availableLimit":80},"violations":["insufficient-limit"]}
```

### Leiningen

```shell
lein run < input_examples/0_sample
```

The output should be:

```
{"account":{"activeCard":true,"availableLimit":100},"violations":[]}
{"account":{"activeCard":true,"availableLimit":80},"violations":[]}
{"account":{"activeCard":true,"availableLimit":80},"violations":["insufficient-limit"]}
```

## Tests

To run the tests you also have two options, with `Docker` or `leiningen`.

### Docker

To create the image run:
```shell
make build-test
```

Then to run the tests do:
```shell
make test
```

### Leiningen

To run the tests just do:
```shell
lein test
```

## Architecture

The code is inspired by the Hexagonal Architecture. The main layers of this project are:

- ports
- controller
- adapters
- business-logic

The way it works is that the `ports` layer will interact with the extern world. In our case, it will handle the user input from stdin.

Then the `controller` will process each line using the `adapters` layer to both: pass data to the `business-logic`, and to return back the result to the `port`.

Here is a visual representation simplified:

<p align="center">
  <img src="https://user-images.githubusercontent.com/15306309/87804794-c2c39280-c82a-11ea-9390-0d7a5a1806db.png" alt="hexagonal-architecture" />
</p>

The reasioning behind this is that the `business-logic` is isolated so it has no knowledge of the outside world. The advantage of this is that we can create different `ports` and `adapters` to use the same logic with different circumstances, like to process a HTTP request, a Kafka message, CLI stdin, etc.

Below there is a simple explanation of each layer/namespace:

### core

> Entrypoint for the application.

It has the `-main` function, and since we are doing a CLI program, it only calls the `cli-stdin` port.

### ports

> Handles the outside world.

Right now it only has implementation for processing CLI stdin. This function processes the stdin by keeping only one line at once in memory, using a buffer to do this.

### controller

> Handles a line of user input.

It has the `controller!` function which adapts both at entry and exit of it to use the pure `business-logic`.

Also since the application is stateful (until the program ends), it stores the operations made in the `storage` layer.

### adapters

> Glues the ports entrance and exit for internal components.

In our case it converts JSON to EDN and vice versa.

### business-logic

> Pure business logic.

It doesn't have any side effects, it just purely converts some data structures to other ones by applying the business rules.

### storage

> Contains the Storage protocol for storing and retrieving data.

### in-memory-storage

> Implements the Storage protocol for in memory operations.

It uses an `atom` to avoid concurrency problems.

### database

> Provides helper functions over the Storage protocol to deal with application entities with ease.
