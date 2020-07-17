# nubank-authorizer

Challenge for Nubank's Authorizer Code Challenge.

## Installation

To install the project you will need either `Docker` or `leiningen`. Also, all the comands that will be shown below will use `make`, if you don't want to install it, you can see what each one of them does on the [`Makefile`](https://github.com/otaviopace/nubank-authorizer/blob/master/Makefile) to run them directly.

### Docker

To build the image just run:

```shell
make build
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

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2020 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
