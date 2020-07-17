# nubank-authorizer

Nubank's Authorizer Code Challenge.

## Table of Contents

- [Installation](#installation)
	- [Docker](#docker)
	- [Leiningen](#leiningen)
- [Usage](#usage)
	- [Docker](#docker-1)
	- [Leiningen](#leiningen)
- [Tests](#tests)
	- [Docker](#docker-2)
	- [Leiningen](#leiningen)
- [Lint](#lint)
	- [Docker](#docker-3)
	- [clj-kondo](#clj-kondo)
- [Operations](#operations)
	- [Create account](#create-account)
	- [Authorize transaction](#authorize-transaction)
- [Architecture](#architecture)
	- [core](#core)
	- [ports](#ports)
	- [controller](#controller)
	- [adapters](#adapters)
	- [business-logic](#business-logic)
	- [storage](#storage)
	- [in-memory-storage](#in-memory-storage)
	- [database](#database)

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

To run you just have to pass a file via stdin that contains JSON operations in each line. There are some examples on [`input_examples`](https://github.com/otaviopace/nubank-authorizer/tree/master/input_examples) folder. If you wish to understand more about what kind of operations are supported, check the [`Operations`](#operations) section.

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

## Lint

The project uses [`clj-kondo`](https://github.com/borkdude/clj-kondo) for linting. You can either just run it directly or use via `Docker`.

### Docker

Just run:

```shell
make lint
```

### clj-kondo

To install it go [here](https://github.com/borkdude/clj-kondo/blob/master/doc/install.md).

Then run the linter with:

```shell
clj-kondo --lint src
```

## Operations

### Create account

Creates the account with availableLimit and activeCard set.

Rules to violations:

- Once created, the account should not be updated or recreated: account-already-initialized

#### Example

Input:
```
{"account":{"activeCard":true,"availableLimit":100}}
{"account":{"activeCard":true,"availableLimit":350}}
```

Output:
```
{"account":{"activeCard":true,"availableLimit":100},"violations":[]}
{"account":{"activeCard":true,"availableLimit":350},"violations":["account-already-initialized"]}
```

### Authorize transaction

Tries to authorize a transaction for a particular merchant, amount and time given the account's state and last authorized
transactions.

Rules to violations:

- The transaction amount should not exceed available limit: insufficient-limit;
- No transaction should be accepted when the card is not active: card-not-active;
- There should not be more than 3 transactions on a 2 minute interval: high-frequency-small-interval;
- There should not be more than 2 similar transactions (same amount and merchant) in a 2 minutes interval:
doubled-transaction;

#### Examples

1. Given there is an account with `activeCard: true` and `availableLimit: 100`.

Input:
```
{"transaction":{"merchant":"Burger King","amount":20,"time":"2019-02-13T10:00:00.000Z"}}
```

Output:
```
{"account":{"activeCard":true,"availableLimit":80},"violations":[]}
```

2. Given there is an account with `activeCard: true` and `availableLimit: 80`.

Input:
```
{"transaction":{"merchant":"Habbib's","amount":90,"time":"2019-02-13T10:00:00.000Z"}}
```

Output:
```
{"account":{"activeCard":true,"availableLimit":80},"violations":["insufficient-limit"]}
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
